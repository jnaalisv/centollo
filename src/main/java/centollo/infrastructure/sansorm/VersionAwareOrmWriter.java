package centollo.infrastructure.sansorm;

import com.zaxxer.sansorm.internal.Introspected;
import com.zaxxer.sansorm.internal.Introspector;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * - Added support for
 *  @Version-annotation, java.time.LocalDatetime, note: OrmReader doesnt support converting java.sql.Timestamp to java.time.LocalDateTime
 *
 */
public class VersionAwareOrmWriter {

    private static final int CACHE_SIZE = Integer.getInteger("centollo.infrastructure.sansorm.statementCacheSize", 500);
    public static final String VERSION_FIELD_NAME = "version";

    private static Map<Introspected, String> updateStatementCache;
    private static Map<Introspected, String> createStatementCache;

    protected static final Object mapSqlType(Object object, int sqlType) {
        switch (sqlType) {

            case Types.TIMESTAMP:
                if (object instanceof java.util.Date) {
                    return new Timestamp(((java.util.Date) object).getTime());
                }
                if (object instanceof java.time.LocalDateTime) {
                    LocalDateTime localDateTime = (LocalDateTime) object;
                    return Timestamp.valueOf(localDateTime);
                }
                break;

            case Types.DECIMAL:
                if (object instanceof BigInteger) {
                    return new BigDecimal(((BigInteger) object));
                }
                break;

            case Types.SMALLINT:
                if (object instanceof Boolean) {
                    return (((Boolean) object) ? (short) 1 : (short) 0);
                }
                break;

            default:
                break;
        }

        return object;
    }

    static {
        createStatementCache = Collections.synchronizedMap(new LinkedHashMap<Introspected, String>(CACHE_SIZE) {
            private static final long serialVersionUID = 4559270460685275064L;

            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<Introspected, String> eldest)
            {
                return this.size() > CACHE_SIZE;
            }
        });

        updateStatementCache = Collections.synchronizedMap(new LinkedHashMap<Introspected, String>(CACHE_SIZE) {
            private static final long serialVersionUID = -5324251353646078607L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<Introspected, String> eldest) {
                return this.size() > CACHE_SIZE;
            }
        });
    }

    public static <T> T insertObject(Connection connection, T target) throws SQLException
    {
        Class<?> clazz = target.getClass();
        Introspected introspected = Introspector.getIntrospected(clazz);
        String[] columnNames = introspected.getInsertableColumns();

        PreparedStatement stmt = createStatementForInsert(connection, introspected, columnNames);
        setParamsExecuteClose(target, introspected, columnNames, stmt);

        return target;
    }

    private static <T> PreparedStatement createStatementForInsert(Connection connection, Introspected introspected, String[] columns) throws SQLException {
        String sql = createStatementCache.get(introspected);
        if (sql == null) {
            sql = createSqlForInsert(introspected.getTableName(), columns);
            createStatementCache.put(introspected, sql);
        }

        if (introspected.hasGeneratedId()) {
            return connection.prepareStatement(sql, introspected.getIdColumnNames());
        } else {
            return connection.prepareStatement(sql);
        }
    }

    public static String createSqlForInsert(String tableName, String[] columns) {
        StringBuilder sqlSB = new StringBuilder("INSERT INTO ").append(tableName).append('(');
        StringBuilder sqlValues = new StringBuilder(") VALUES (");
        for (String column : columns) {
            sqlSB.append(column).append(',');
            sqlValues.append("?,");
        }
        sqlValues.deleteCharAt(sqlValues.length() - 1);
        sqlSB.deleteCharAt(sqlSB.length() - 1).append(sqlValues).append(')');

        return sqlSB.toString();
    }

    public static <T> int updateVersionedObject(Connection connection, T target) throws SQLException {
        Class<?> clazz = target.getClass();
        Introspected introspected = Introspector.getIntrospected(clazz);
        String[] columnNames = introspected.getUpdatableColumns();

        String sql = updateStatementCache.get(introspected);
        if (sql == null) {
            sql = createSqlForUpdate(introspected, columnNames);
            updateStatementCache.put(introspected, sql);
        }

        PreparedStatement stmt = connection.prepareStatement(sql);

        return setParamsExecuteClose(target, introspected, columnNames, stmt);
    }

    public static String createSqlForUpdate(Introspected introspected, String[] columnNames) {
        StringBuilder sqlSB = new StringBuilder("UPDATE ").append(introspected.getTableName()).append(" SET ");
        for (String column : columnNames) {
            sqlSB.append(column).append("=?,");
        }
        sqlSB.deleteCharAt(sqlSB.length() - 1);

        String[] idColumnNames = introspected.getIdColumnNames();
        if (idColumnNames.length > 0) {
            sqlSB.append(" WHERE ");
            for (String column : idColumnNames) {
                sqlSB.append(column).append("=? AND ");
            }

            sqlSB.append(VERSION_FIELD_NAME);
            sqlSB.append(" =?");
        }

        return sqlSB.toString();
    }

    private static <T> int setParamsExecuteClose(T target, Introspected introspected, String[] columnNames, PreparedStatement stmt) throws SQLException {
        ParameterMetaData metaData = stmt.getParameterMetaData();
        int parameterIndex = 1;

        long previousVersion = 0;
        long newVersion = 0l;
        int versionSqlType = Types.BIGINT;

        for (String column : columnNames) {
            int parameterType = metaData.getParameterType(parameterIndex);
            Object object = mapSqlType(introspected.get(target, column), parameterType);
            if (object != null) {


                if (VERSION_FIELD_NAME.equals(column)) {

                    previousVersion = (long) object;
                    versionSqlType = parameterType;

                    newVersion = previousVersion + 1; // increment version

                    object = newVersion;
                }

                stmt.setObject(parameterIndex, object, parameterType);
            }
            else {
                stmt.setNull(parameterIndex, parameterType);
            }
            ++parameterIndex;
        }

        // If there is still a parameter left to be set, it's the ID used for an update
        if (parameterIndex <= metaData.getParameterCount()) {
            for (Object id : introspected.getActualIds(target)) {
                stmt.setObject(parameterIndex, id, metaData.getParameterType(parameterIndex));
                ++parameterIndex;
            }
        }

        // TODO: broken on insert
        stmt.setObject(parameterIndex, previousVersion, versionSqlType);

        int rowCount = stmt.executeUpdate();

        introspected.set(target, VERSION_FIELD_NAME, newVersion);

        if (introspected.hasGeneratedId()) {
            // Set auto-generated ID
            final String idColumn = introspected.getIdColumnNames()[0];
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys != null && generatedKeys.next()) {
                introspected.set(target, idColumn, generatedKeys.getObject(1));
                generatedKeys.close();
            }
        }

        stmt.close();

        return rowCount;
    }
}
