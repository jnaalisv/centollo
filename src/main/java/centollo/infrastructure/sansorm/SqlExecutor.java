package centollo.infrastructure.sansorm;

import com.zaxxer.sansorm.OrmElf;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.zaxxer.sansorm.SqlClosureElf.quietClose;

@Service
public class SqlExecutor {

    private final TransactionAwareDataSourceProxy transactionAwareDataSource;

    @Inject
    public SqlExecutor(final TransactionAwareDataSourceProxy transactionAwareDataSource) {
        this.transactionAwareDataSource = transactionAwareDataSource;
    }

    public <T> Optional<T> getObjectById(Class<T> type, Object... ids) {
        T result = execute(connection -> OrmElf.objectById(connection, type, ids));
        return Optional.ofNullable(result);
    }

    public <T> Optional<T> objectFromClause(Class<T> type, String clause, Object... args) {
        T result = execute(connection -> OrmElf.objectFromClause(connection, type, clause, args));
        return Optional.ofNullable(result);
    }

    public <T> T insertObject(T object) {
        return execute(connection -> OrmElf.insertObject(connection, object));
    }

    public <T> T updateObject(T object) {
        return execute(connection -> OrmElf.updateObject(connection, object));
    }

    public <T> T updateVersionedObject(T object) {
        int rowCount =  execute(connection -> VersionAwareOrmWriter.updateVersionedObject(connection, object));
        if (rowCount == 0) {
            throw new ObjectOptimisticLockingFailureException(object.getClass(), "id not available");
        }
        return object;
    }

    public <T> int deleteObject(T object) {
        return execute(connection -> OrmElf.deleteObject(connection, object));
    }

    public <T> int deleteObjectById(Class<T> clazz, Object... args) {
        return execute(connection -> OrmElf.deleteObjectById(connection, clazz, args));
    }

    public <T> List<T> listFromClause(Class<T> clazz, String clause, Object... args) {
        return execute(connection -> OrmElf.listFromClause(connection, clazz, clause, args));
    }

    public <T> int countObjectsFromClause(Class<T> clazz, String clause, Object... args) {
        return execute(connection -> OrmElf.countObjectsFromClause(connection, clazz, clause, args));
    }

    public int executeUpdate(final String sql, final Object... args) {
        return execute(connection -> OrmElf.executeUpdate(connection, sql, args));
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
}
