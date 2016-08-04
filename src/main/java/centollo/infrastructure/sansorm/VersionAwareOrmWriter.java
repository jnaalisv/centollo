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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Expects version information to live in a column/field named "version" of type bigint/long
 */
public class VersionAwareOrmWriter {

    private static final int CACHE_SIZE = Integer.getInteger("centollo.infrastructure.sansorm.statementCacheSize", 500);
    public static final String VERSION_FIELD_NAME = "version";

    private static Map<Introspected, String> updateStatementCache;

    protected static final Object mapSqlType(Object object, int sqlType) {
        switch (sqlType) {
            case Types.TIMESTAMP:
                if (object instanceof java.util.Date) {
                    return new Timestamp(((java.util.Date) object).getTime());
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
        updateStatementCache = Collections.synchronizedMap(new LinkedHashMap<Introspected, String>(CACHE_SIZE) {
            private static final long serialVersionUID = -5324251353646078607L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<Introspected, String> eldest) {
                return this.size() > CACHE_SIZE;
            }
        });
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
