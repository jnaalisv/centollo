
package centollo.infrastructure.sansorm.framework;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Java8OrmWriter extends Java8OrmBase {

    private static final String VERSION_FIELD_NAME = "version";

    private static final int CACHE_SIZE = Integer.getInteger("centollo.infrastructure.sansorm.framework.statementCacheSize", 500);

    private static Map<Introspected, String> createStatementCache;
    private static Map<Introspected, String> updateStatementCache;

    static {
        createStatementCache = Collections.synchronizedMap(new LinkedHashMap<Introspected, String>(CACHE_SIZE) {
            private static final long serialVersionUID = 4559270460685275064L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<Introspected, String> eldest) {
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

    public static <T> int[] insertListBatched(Connection connection, Iterable<T> iterable) throws SQLException {
        Iterator<T> iterableIterator = iterable.iterator();
        if (!iterableIterator.hasNext()) {
            return new int[]{};
        }

        Class<?> clazz = iterableIterator.next().getClass();
        Introspected introspected = Introspector.getIntrospected(clazz);
        final boolean hasSelfJoinColumn = introspected.hasSelfJoinColumn();
        if (hasSelfJoinColumn) {
            throw new RuntimeException("insertListBatched() is not supported for objects with self-referencing columns due to Derby limitations");
        }

        String[] columnNames = introspected.getInsertableColumns();

        PreparedStatement stmt = createStatementForInsert(connection, introspected, columnNames);
        ParameterMetaData metaData = stmt.getParameterMetaData();
        for (T item : iterable) {
            int parameterIndex = 1;
            for (String column : columnNames) {
                int parameterType = metaData.getParameterType(parameterIndex);
                Object object = mapSqlType(introspected.get(item, column), parameterType);
                if (object != null && !(hasSelfJoinColumn && introspected.isSelfJoinColumn(column))) {
                    stmt.setObject(parameterIndex, object, parameterType);
                }
                else {
                    stmt.setNull(parameterIndex, parameterType);
                }
                ++parameterIndex;
            }
            stmt.addBatch();
            stmt.clearParameters();
        }

        int[] rowCounts = stmt.executeBatch();
        stmt.close();

        return rowCounts;
    }

    public static <T> void insertListNotBatched(Connection connection, Iterable<T> iterable) throws SQLException {
        Iterator<T> iterableIterator = iterable.iterator();
        if (!iterableIterator.hasNext()) {
            return;
        }

        Class<?> clazz = iterableIterator.next().getClass();
        Introspected introspected = Introspector.getIntrospected(clazz);
        final boolean hasSelfJoinColumn = introspected.hasSelfJoinColumn();
        String[] idColumnNames = introspected.getIdColumnNames();
        String[] columnNames = introspected.getInsertableColumns();

        // Insert
        PreparedStatement stmt = createStatementForInsert(connection, introspected, columnNames);
        ParameterMetaData metaData = stmt.getParameterMetaData();
        for (T item : iterable) {
            int parameterIndex = 1;
            for (String column : columnNames) {
                int parameterType = metaData.getParameterType(parameterIndex);
                Object object = mapSqlType(introspected.get(item, column), parameterType);
                if (object != null && !(hasSelfJoinColumn && introspected.isSelfJoinColumn(column))) {
                    stmt.setObject(parameterIndex, object, parameterType);
                }
                else {
                    stmt.setNull(parameterIndex, parameterType);
                }
                ++parameterIndex;
            }

            stmt.executeUpdate();

            // Set auto-generated ID
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys != null) {
                final String idColumn = idColumnNames[0];
                while (generatedKeys.next()) {
                    introspected.set(item, idColumn, generatedKeys.getObject(1));
                }
                generatedKeys.close();
            }

            stmt.clearParameters();
        }
        stmt.close();

        // If there is a self-referencing column, update it with the generated IDs
        if (hasSelfJoinColumn) {
            final String selfJoinColumn = introspected.getSelfJoinColumn();
            final String idColumn = idColumnNames[0];
            StringBuilder sql = new StringBuilder("UPDATE ").append(introspected.getTableName()).append(" SET ");
            sql.append(selfJoinColumn).append("=? WHERE ").append(idColumn).append("=?");
            stmt = connection.prepareStatement(sql.toString());
            for (T item : iterable) {
                Object referencedItem = introspected.get(item, selfJoinColumn);
                if (referencedItem != null) {
                    stmt.setObject(1, introspected.getActualIds(referencedItem)[0]);
                    stmt.setObject(2, introspected.getActualIds(item)[0]);
                    stmt.addBatch();
                    stmt.clearParameters();
                }
            }
            stmt.executeBatch();
        }
    }

    public static <T> T updateObject(Connection connection, T target) throws SQLException {
        Class<?> clazz = target.getClass();
        Introspected introspected = Introspector.getIntrospected(clazz);
        String[] columnNames = introspected.getUpdatableColumns();

        PreparedStatement stmt = createStatementForUpdate(connection, introspected, columnNames);
        setParamsExecuteClose(target, introspected, columnNames, stmt);

        return target;
    }

    public static <T> int deleteObject(Connection connection, T target) throws SQLException {
        Class<?> clazz = target.getClass();
        Introspected introspected = Introspector.getIntrospected(clazz);

        return deleteObjectById(connection, clazz, introspected.getActualIds(target));
    }

    public static <T> int deleteObjectById(Connection connection, Class<T> clazz, Object... args) throws SQLException {
        Introspected introspected = Introspector.getIntrospected(clazz);

        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(introspected.getTableName()).append(" WHERE ");

        for (String idColumn : introspected.getIdColumnNames()) {
            sql.append(idColumn).append("=? AND ");
        }
        sql.setLength(sql.length() - 5);

        return executeUpdate(connection, sql.toString(), args);
    }

    public static int executeUpdate(Connection connection, String sql, Object... args) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            populateStatementParameters(stmt, args);
            return stmt.executeUpdate();
        }
    }

    private static PreparedStatement createStatementForUpdate(Connection connection, Introspected introspected, String[] columnNames) throws SQLException {
        String sql = updateStatementCache.get(introspected);
        if (sql == null) {
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
                sqlSB.setLength(sqlSB.length() - 5);
            }

            sql = sqlSB.toString();
            updateStatementCache.put(introspected, sql);
        }

        return connection.prepareStatement(sql);
    }

    public static <T> T insertObject(Connection connection, T target) throws SQLException {
        Class<?> clazz = target.getClass();
        Introspected introspected = Introspector.getIntrospected(clazz);
        String[] columnNames = introspected.getInsertableColumns();

        PreparedStatement stmt = createStatementForInsert(connection, introspected, columnNames);
        setParamsExecuteClose(target, introspected, columnNames, stmt);

        return target;
    }

    private static PreparedStatement createStatementForInsert(Connection connection, Introspected introspected, String[] columns) throws SQLException {
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

    private static String createSqlForInsert(String tableName, String[] columns) {
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

        return setParamsExecuteCloseVersioned(target, introspected, columnNames, stmt);
    }

    private static String createSqlForUpdate(Introspected introspected, String[] columnNames) {
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
        for (String column : columnNames) {
            int parameterType = metaData.getParameterType(parameterIndex);
            Object object = mapSqlType(introspected.get(target, column), parameterType);
            if (object != null) {
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

        int rowCount = stmt.executeUpdate();

        setGeneratedKeys(target, introspected, stmt);

        stmt.close();

        return rowCount;
    }

    private static void setGeneratedKeys(Object target, Introspected introspected, PreparedStatement stmt) throws SQLException {
        if (introspected.hasGeneratedId()) {
            // Set auto-generated ID
            final String idColumn = introspected.getIdColumnNames()[0];
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys != null && generatedKeys.next()) {
                introspected.set(target, idColumn, generatedKeys.getObject(1));
                generatedKeys.close();
            }
        }
    }

    private static <T> int setParamsExecuteCloseVersioned(T target, Introspected introspected, String[] columnNames, PreparedStatement stmt) throws SQLException {
        ParameterMetaData metaData = stmt.getParameterMetaData();
        int parameterIndex = 1;

        long previousVersion = 0;
        long newVersion = 0L;
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

        setGeneratedKeys(target, introspected, stmt);

        stmt.close();

        return rowCount;
    }
}
