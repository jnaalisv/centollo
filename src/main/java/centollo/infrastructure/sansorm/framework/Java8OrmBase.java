package centollo.infrastructure.sansorm.framework;

import com.zaxxer.sansorm.internal.Introspected;
import com.zaxxer.sansorm.internal.Introspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class Java8OrmBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(Java8OrmBase.class);

    private static Map<String, String> csvCache;

    static{
        csvCache = new ConcurrentHashMap<String, String>();
    }

    protected Java8OrmBase(){
        // protected constructor
    }

    protected static final void populateStatementParameters(PreparedStatement stmt, Object... args) throws SQLException{
        ParameterMetaData parameterMetaData = stmt.getParameterMetaData();
        final int paramCount = parameterMetaData.getParameterCount();
        if (paramCount > 0 && args.length < paramCount){
            throw new RuntimeException("Too few parameters supplied for query");
        }

        for (int column = paramCount; column > 0; column--){
            int parameterType = parameterMetaData.getParameterType(column);
            Object object = mapSqlType(args[column - 1], parameterType);
            stmt.setObject(column, object, parameterType);
        }
    }

    public static final <T> String getColumnsCsv(Class<T> clazz, String... tablePrefix) {
        String cacheKey = (tablePrefix == null || tablePrefix.length == 0 ? clazz.getName() : tablePrefix[0] + clazz.getName());

        String columnCsv = csvCache.get(cacheKey);
        if (columnCsv == null) {
            Introspected introspected = Introspector.getIntrospected(clazz);
            StringBuilder sb = new StringBuilder();
            String[] columnNames = introspected.getColumnNames();
            String[] columnTableNames = introspected.getColumnTableNames();
            for (int i = 0; i < columnNames.length; i++) {
                String column = columnNames[i];
                String columnTableName = columnTableNames[i];

                if (columnTableName != null) {
                    sb.append(columnTableName).append('.');
                }
                else if (tablePrefix.length > 0) {
                    sb.append(tablePrefix[0]).append('.');
                }

                sb.append(column).append(',');
            }

            columnCsv = sb.deleteCharAt(sb.length() - 1).toString();
            csvCache.put(cacheKey, columnCsv);
        }

        return columnCsv;
    }

    public static final <T> String getColumnsCsvExclude(Class<T> clazz, String...excludeColumns) {
        Set<String> excludes = new HashSet<String>(Arrays.asList(excludeColumns));

        Introspected introspected = Introspector.getIntrospected(clazz);
        StringBuilder sb = new StringBuilder();
        String[] columnNames = introspected.getColumnNames();
        String[] columnTableNames = introspected.getColumnTableNames();
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

    protected static final Object mapSqlType(Object object, int sqlType) {

        LOGGER.debug("mapSqlType " +object.getClass().getSimpleName() + " to sqlType:" + sqlType);

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
}
