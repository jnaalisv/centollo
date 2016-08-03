package centollo.infrastructure.sansorm;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
interface SqlClosure<T> {
    T execute(Connection connection) throws SQLException;
}