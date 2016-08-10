package centollo.infrastructure.sansorm.framework;

import centollo.model.domain.PurchaseOrder;
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

abstract class Java8OrmBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(Java8OrmBase.class);

    static void populateStatementParameters(PreparedStatement stmt, Object... args) throws SQLException{
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

    static Object mapSqlType(Object object, int sqlType) {

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

            case Types.INTEGER:
                if (object instanceof PurchaseOrder) {
                    return ((PurchaseOrder) object).getId();
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
