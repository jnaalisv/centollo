package centollo.infrastructure.sansorm;

import centollo.infrastructure.sansorm.framework.Java8OrmReader;
import centollo.infrastructure.sansorm.framework.Java8OrmWriter;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class SqlExecutor {

    private final TransactionAwareDataSourceProxy transactionAwareDataSource;

    @Inject
    public SqlExecutor(final TransactionAwareDataSourceProxy transactionAwareDataSource) {
        this.transactionAwareDataSource = transactionAwareDataSource;
    }

    public <T> Optional<T> getObjectById(Class<T> type, Object... ids) {
        return execute(connection -> Java8OrmReader.objectById(connection, type, ids));
    }

    public <T> Optional<T> objectFromClause(Class<T> type, String clause, Object... args) {
        return execute(connection -> Java8OrmReader.objectFromClause(connection, type, clause, args));
    }

    public <T> T insertObject(T object) {
        return execute(connection -> Java8OrmWriter.insertObject(connection, object));
    }

    public <T> int[] insertObjectBatched(Iterable<T> iterable) {
        return execute(connection -> Java8OrmWriter.insertListBatched(connection, iterable));
    }

    public <T> T updateObject(T object) {
        return execute(connection -> Java8OrmWriter.updateObject(connection, object));
    }

    public <T> T updateVersionedObject(T object) {
        int rowCount =  execute(connection -> Java8OrmWriter.updateVersionedObject(connection, object));
        if (rowCount == 0) {
            throw new ObjectOptimisticLockingFailureException(object.getClass(), "id not available");
        }
        return object;
    }

    public <T> int deleteObject(T object) {
        return execute(connection -> Java8OrmWriter.deleteObject(connection, object));
    }

    public <T> int deleteObjectById(Class<T> clazz, Object... args) {
        return execute(connection -> Java8OrmWriter.deleteObjectById(connection, clazz, args));
    }

    public <T> List<T> listFromClause(Class<T> clazz, String clause, Object... args) {
        return execute(connection -> Java8OrmReader.listFromClause(connection, clazz, clause, args));
    }

    public <T> int countObjectsFromClause(Class<T> clazz, String clause, Object... args) {
        return execute(connection -> Java8OrmReader.countObjectsFromClause(connection, clazz, clause, args));
    }

    public int executeUpdate(final String sql, final Object... args) {
        return execute(connection -> Java8OrmWriter.executeUpdate(connection, sql, args));
    }

    private <T> T execute(SqlClosure<T> sqlClosure) {
        Connection connection = null;
        try {
            connection = transactionAwareDataSource.getConnection();
            return sqlClosure.execute(connection);
        }
        catch (SQLException e) {
            if (e.getNextException() != null) {
                e = e.getNextException();
            }
            throw new RuntimeException(e);
        }
        finally {
            quietClose(connection);
        }
    }

    public static void quietClose(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            return;
        }
    }

}
