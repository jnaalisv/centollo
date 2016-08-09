package centollo.infrastructure.sansorm.framework.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SqlGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlGenerator.class);

    public static final String VERSION_FIELD_NAME = "version";

    private static final Map<String, String> csvCache = new ConcurrentHashMap<>();
    private static final int CACHE_SIZE = Integer.getInteger("centollo.infrastructure.sansorm.framework.statementCacheSize", 500);

    private static final Map<String, String> fromClauseStmtCache = Collections.synchronizedMap(new LinkedHashMap<String, String>(CACHE_SIZE) {
        private static final long serialVersionUID = 6259942586093454872L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return this.size() > CACHE_SIZE;
        }
    });

    private static Map<String, String> createStatementCache = Collections.synchronizedMap(new LinkedHashMap<String, String>(CACHE_SIZE) {
        private static final long serialVersionUID = 4559270460685275064L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return this.size() > CACHE_SIZE;
        }
    });

    private static Map<String, String> updateStatementCache = Collections.synchronizedMap(new LinkedHashMap<String, String>(CACHE_SIZE) {
        private static final long serialVersionUID = -5324251353646078607L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return this.size() > CACHE_SIZE;
        }
    });

    public static <T> String getColumnsCsv(String tableName, String[] columnNames, String[] columnTableNames, String tablePrefix) {
        String cacheKey = (tablePrefix == null ? tableName : tablePrefix + tableName);

        String columnCsv = csvCache.get(cacheKey);
        if (columnCsv == null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < columnNames.length; i++) {
                String column = columnNames[i];
                String columnTableName = columnTableNames[i];

                if (columnTableName != null) {
                    sb.append(columnTableName).append('.');
                }
                else if (tablePrefix != null) {
                    sb.append(tablePrefix).append('.');
                }

                sb.append(column).append(',');
            }

            columnCsv = sb.deleteCharAt(sb.length() - 1).toString();
            csvCache.put(cacheKey, columnCsv);
        }

        return columnCsv;
    }

    public static <T> String selectFromClauseSql(String tableName, String[] columnNames, String[] columnTableNames, String clause) {
        String cacheKey = tableName + clause;

        String sql = fromClauseStmtCache.get(cacheKey);
        if (sql == null) {

            StringBuilder sqlSB = new StringBuilder();
            sqlSB.append("SELECT ").append(getColumnsCsv(tableName, columnNames, columnTableNames, tableName)).append(" FROM ").append(tableName).append(' ').append(tableName);
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

    public static String countObjectsFromClauseSql(String tableName, String[] idColumnNames, String[] columnNames, String clause) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(");
        String countColumn = tableName + ".";

        if (idColumnNames.length > 0) {
            countColumn += idColumnNames[0];
        }
        else {
            countColumn += columnNames[0];
        }
        sql.append(countColumn).append(") FROM ").append(tableName).append(' ').append(tableName);
        if (clause != null && !clause.isEmpty()) {
            if (!clause.toUpperCase().contains("WHERE") && !clause.toUpperCase().contains("JOIN")) {
                sql.append(" WHERE ");
            }
            sql.append(' ').append(clause);
        }
        return sql.toString();
    }

    public static String objectByIdSql(String[] idColumnNames) {
        StringBuilder where = new StringBuilder();
        for (String column : idColumnNames) {
            where.append(column).append("=? AND ");
        }

        // the where clause can be length of zero if we are loading an object that is presumed to
        // be the only row in the table and therefore has no id.
        if (where.length() > 0) {
            where.setLength(where.length() - 5);
        }
        return where.toString();
    }

    public static <T> String getColumnsCsvExclude(String[] columnNames, String[] columnTableNames, String...excludeColumns) {
        Set<String> excludes = new HashSet<>(Arrays.asList(excludeColumns));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnNames.length; i++) {
            String column = columnNames[i];
            if (excludes.contains(column)) {
                continue;
            }

            String columnTableName = columnTableNames[i];

            if (columnTableName != null) {
                sb.append(columnTableName).append('.');
            }

            sb.append(column).append(',');
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public static String createSqlForUpdate(String tableName, String[] idColumnNames, String[] columnNames) {
        String sql = updateStatementCache.get(tableName);
        if (sql == null) {
            StringBuilder sqlSB = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
            for (String column : columnNames) {
                sqlSB.append(column).append("=?,");
            }
            sqlSB.deleteCharAt(sqlSB.length() - 1);

            if (idColumnNames.length > 0) {
                sqlSB.append(" WHERE ");
                for (String column : idColumnNames) {
                    sqlSB.append(column).append("=? AND ");
                }

                sqlSB.append(VERSION_FIELD_NAME);
                sqlSB.append(" =?");
            }

            sql =  sqlSB.toString();

            updateStatementCache.put(tableName, sql);
        }
        return sql;
    }

    public static String createStatementForUpdateSql(String tableName, String[] idColumnNames, String[] columnNames) {
        String sql = updateStatementCache.get(tableName);
        if (sql == null) {
            StringBuilder sqlSB = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
            for (String column : columnNames) {
                sqlSB.append(column).append("=?,");
            }
            sqlSB.deleteCharAt(sqlSB.length() - 1);

            if (idColumnNames.length > 0) {
                sqlSB.append(" WHERE ");
                for (String column : idColumnNames) {
                    sqlSB.append(column).append("=? AND ");
                }
                sqlSB.setLength(sqlSB.length() - 5);
            }

            sql = sqlSB.toString();
            updateStatementCache.put(tableName, sql);
        }

        return sql;
    }

    public static String createSqlForInsert(String tableName, String[] columns) {
        String sql = createStatementCache.get(tableName);
        if (sql == null) {

            StringBuilder sqlSB = new StringBuilder("INSERT INTO ").append(tableName).append('(');
            StringBuilder sqlValues = new StringBuilder(") VALUES (");
            for (String column : columns) {
                sqlSB.append(column).append(',');
                sqlValues.append("?,");
            }
            sqlValues.deleteCharAt(sqlValues.length() - 1);
            sqlSB.deleteCharAt(sqlSB.length() - 1).append(sqlValues).append(')');

            sql = sqlSB.toString();
            createStatementCache.put(tableName, sql);
        }
        return sql;
    }

    public static String deleteObjectByIdSql(String tableName, String[] idColumnNames) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(tableName).append(" WHERE ");

        for (String idColumn : idColumnNames) {
            sql.append(idColumn).append("=? AND ");
        }
        sql.setLength(sql.length() - 5);
        return sql.toString();
    }

    public static String selfJoinSql(String selfJoinColumn, String tableName, String idColumn) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        sql.append(selfJoinColumn).append("=? WHERE ").append(idColumn).append("=?");
        return sql.toString();
    }
}
