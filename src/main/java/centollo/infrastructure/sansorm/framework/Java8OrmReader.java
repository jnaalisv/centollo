package centollo.infrastructure.sansorm.framework;

import com.zaxxer.sansorm.internal.Introspected;
import com.zaxxer.sansorm.internal.Introspector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

public class Java8OrmReader extends Java8OrmBase {
    private static final int CACHE_SIZE = Integer.getInteger("centollo.infrastructure.sansorm.framework.statementCacheSize", 500);

    private static final Map<String, String> fromClauseStmtCache;

    static {
        fromClauseStmtCache = Collections.synchronizedMap(new LinkedHashMap<String, String>(CACHE_SIZE) {
            private static final long serialVersionUID = 6259942586093454872L;

            @Override
            protected boolean removeEldestEntry(Entry<String, String> eldest)
            {
                return this.size() > CACHE_SIZE;
            }
        });
    }

    private static <T> List<T> statementToList(PreparedStatement stmt, Class<T> clazz, Object... args) throws SQLException {
        try {
            populateStatementParameters(stmt, args);

            return resultSetToList(stmt.executeQuery(), clazz);
        }
        finally {
            stmt.close();
        }
    }

    private static <T> List<T> resultSetToList(ResultSet resultSet, Class<T> targetClass) throws SQLException {
        List<T> list = new ArrayList<>();
        if (!resultSet.next()) {
            resultSet.close();
            return list;
        }

        Introspected introspected = Introspector.getIntrospected(targetClass);
        final boolean hasJoinColumns = introspected.hasSelfJoinColumn();
        Map<T, Object> deferredSelfJoinFkMap = (hasJoinColumns ? new HashMap<>() : null);
        Map<Object, T> idToTargetMap = (hasJoinColumns ? new HashMap<>() : null);

        ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();
        final String[] columnNames = new String[columnCount];
        for (int column = columnCount; column > 0; column--) {
            columnNames[column - 1] = metaData.getColumnName(column).toLowerCase();
        }

        try {
            do {
                T target = targetClass.newInstance();
                list.add(target);
                for (int column = columnCount; column > 0; column--) {
                    Object columnValue = resultSet.getObject(column);
                    if (columnValue == null) {
                        continue;
                    }

                    String columnName = columnNames[column - 1];

                    if (hasJoinColumns && introspected.isSelfJoinColumn(columnName)) {
                        deferredSelfJoinFkMap.put(target, columnValue);
                    }
                    else {
                        introspected.set(target, columnName, columnValue);
                    }
                }

                if (hasJoinColumns) {
                    idToTargetMap.put(introspected.getActualIds(target)[0], target);
                }
            }
            while (resultSet.next());

            resultSet.close();

            if (hasJoinColumns) {
                // set the self join object instances based on the foreign key ids...
                String idColumn = introspected.getSelfJoinColumn();
                for (Entry<T, Object> entry : deferredSelfJoinFkMap.entrySet()) {
                    T value = idToTargetMap.get(entry.getValue());
                    if (value != null) {
                        introspected.set(entry.getKey(), idColumn, value);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }


    private static <T> Optional<T> statementToObject(PreparedStatement stmt, Class<T> clazz, Object... args) throws SQLException {
        populateStatementParameters(stmt, args);

        try (ResultSet resultSet = stmt.executeQuery()) {
            if (resultSet.next()) {
                T target = clazz.newInstance();
                return Optional.ofNullable(resultSetToObject(resultSet, target));
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stmt.close();
        }
    }

    private static <T> T resultSetToObject(ResultSet resultSet, T target) throws SQLException {
        Set<String> ignoreNone = Collections.emptySet();
        return resultSetToObject(resultSet, target, ignoreNone);
    }

    private static <T> T resultSetToObject(ResultSet resultSet, T target, Set<String> ignoredColumns) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();

        Introspected introspected = Introspector.getIntrospected(target.getClass());
        for (int column = metaData.getColumnCount(); column > 0; column--) {
            String columnName = metaData.getColumnName(column).toLowerCase();
            if (ignoredColumns.contains(columnName)) {
                continue;
            }

            Object mappedColumnValue = mapColumnValueToFieldValue(resultSet.getObject(column));

            if (mappedColumnValue == null) {
                continue;
            }

            introspected.set(target, columnName, mappedColumnValue);
        }
        return target;
    }

    private static Object mapColumnValueToFieldValue(Object object) {
        if (object instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) object;
            return timestamp.toLocalDateTime();
        }
        return object;
    }

    public static <T> Optional<T> objectById(Connection connection, Class<T> clazz, Object... args) throws SQLException {
        Introspected introspected = Introspector.getIntrospected(clazz);

        StringBuilder where = new StringBuilder();
        for (String column : introspected.getIdColumnNames()) {
            where.append(column).append("=? AND ");
        }

        // the where clause can be length of zero if we are loading an object that is presumed to 
        // be the only row in the table and therefore has no id.
        if (where.length() > 0) {
            where.setLength(where.length() - 5);
        }

        return objectFromClause(connection, clazz, where.toString(), args);
    }

    public static <T> List<T> listFromClause(Connection connection, Class<T> clazz, String clause, Object... args) throws SQLException {
        String sql = generateSelectFromClause(clazz, clause);

        PreparedStatement stmt = connection.prepareStatement(sql);
        List<T> list = statementToList(stmt, clazz, args);
        stmt.close();

        return list;
    }

    public static <T> Optional<T> objectFromClause(Connection connection, Class<T> clazz, String clause, Object... args) throws SQLException {
        String sql = generateSelectFromClause(clazz, clause);

        PreparedStatement stmt = connection.prepareStatement(sql);

        return statementToObject(stmt, clazz, args);
    }

    public static <T> int countObjectsFromClause(Connection connection, Class<T> clazz, String clause, Object... args) throws SQLException {
        Introspected introspected = Introspector.getIntrospected(clazz);

        String tableName = introspected.getTableName();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(");
        String countColumn = tableName + ".";
        String[] idColumnNames = introspected.getIdColumnNames();
        if (idColumnNames.length > 0) {
            countColumn += idColumnNames[0];
        }
        else {
            countColumn += introspected.getColumnNames()[0];
        }
        sql.append(countColumn).append(") FROM ").append(tableName).append(' ').append(tableName);
        if (clause != null && !clause.isEmpty()) {
            if (!clause.toUpperCase().contains("WHERE") && !clause.toUpperCase().contains("JOIN")) {
                sql.append(" WHERE ");
            }
            sql.append(' ').append(clause);
        }

        Optional<Number> maybeNumber = numberFromSql(connection, sql.toString(), args);

        return maybeNumber.orElse(0).intValue();
    }

    private static Optional<Number> numberFromSql(Connection connection, String sql, Object... args) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            populateStatementParameters(stmt, args);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.ofNullable((Number) resultSet.getObject(1));
                }

                return Optional.empty();
            }
        }
    }

    private static <T> String generateSelectFromClause(Class<T> clazz, String clause) {
        String cacheKey = clazz.getName() + clause;

        String sql = fromClauseStmtCache.get(cacheKey);
        if (sql == null) {
            Introspected introspected = Introspector.getIntrospected(clazz);

            String tableName = introspected.getTableName();

            StringBuilder sqlSB = new StringBuilder();
            sqlSB.append("SELECT ").append(getColumnsCsv(clazz, tableName)).append(" FROM ").append(tableName).append(' ').append(tableName);
            if (clause != null && !clause.isEmpty()) {
                if (!clause.toUpperCase().contains("WHERE") && !clause.toUpperCase().contains("JOIN")) {
                    sqlSB.append(" WHERE ");
                }
                sqlSB.append(' ').append(clause);
            }

            sql = sqlSB.toString();
            fromClauseStmtCache.put(cacheKey, sql);
        }

        return sql;
    }
}
