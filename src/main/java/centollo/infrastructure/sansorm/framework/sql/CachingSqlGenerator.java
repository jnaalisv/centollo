package centollo.infrastructure.sansorm.framework.sql;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CachingSqlGenerator {
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

    public static String getColumnsCsv(String tableName, String[] columnNames, String[] columnTableNames, String tablePrefix) {
        String cacheKey = (tablePrefix == null ? tableName : tablePrefix + tableName);

        String columnCsv = csvCache.get(cacheKey);
        if (columnCsv == null) {
            columnCsv = SqlGenerator.getColumnsCsv(columnNames, columnTableNames, tablePrefix);
            csvCache.put(cacheKey, columnCsv);
        }

        return columnCsv;
    }

    public static String selectFromClauseSql(String tableName, String[] columnNames, String[] columnTableNames, String clause) {
        String cacheKey = tableName + clause;
        String sql = fromClauseStmtCache.get(cacheKey);
        if (sql == null) {
            sql = SqlGenerator.selectFromClauseSql(tableName, columnNames, columnTableNames, clause);
            fromClauseStmtCache.put(cacheKey, sql);
        }

        return sql;
    }

    public static String createSqlForUpdate(String tableName, String[] idColumnNames, String[] columnNames) {
        String sql = updateStatementCache.get(tableName);
        if (sql == null) {
            sql =  SqlGenerator.createSqlForUpdate(tableName, idColumnNames, columnNames);
            updateStatementCache.put(tableName, sql);
        }
        return sql;
    }

    public static String createSqlForInsert(String tableName, String[] columns) {
        String sql = createStatementCache.get(tableName);
        if (sql == null) {
            sql = SqlGenerator.createSqlForInsert(tableName, columns);
            createStatementCache.put(tableName, sql);
        }
        return sql;
    }
}
