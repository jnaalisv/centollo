package centollo.infrastructure.sansorm.framework.sql;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class SqlGenerator {
    public static final String VERSION_FIELD_NAME = "version";

    static String getColumnsCsv(String[] columnNames, String[] columnTableNames, String tablePrefix) {

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

        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public static String selectFromClauseSql(String tableName, String[] columnNames, String[] columnTableNames, String clause) {
        StringBuilder sqlSB = new StringBuilder();
        sqlSB.append("SELECT ").append(getColumnsCsv(columnNames, columnTableNames, tableName)).append(" FROM ").append(tableName).append(' ').append(tableName);
        if (clause != null && !clause.isEmpty()) {
            if (!clause.toUpperCase().contains("WHERE") && !clause.toUpperCase().contains("JOIN")) {
                sqlSB.append(" WHERE ");
            }
            sqlSB.append(' ').append(clause);
        }

        return sqlSB.toString();
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

    static String getColumnsCsvExclude(String[] columnNames, String[] columnTableNames, String... excludeColumns) {
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

        return sqlSB.toString();
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
