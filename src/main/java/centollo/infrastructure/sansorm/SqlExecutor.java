package centollo.infrastructure.sansorm;

import com.zaxxer.sansorm.OrmElf;
import com.zaxxer.sansorm.SqlClosure;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class SqlExecutor {

    @Inject
    public SqlExecutor(TransactionAwareDataSourceProxy transactionAwareDataSource) {
        AbstractSqlClosure.setDefaultDataSource(transactionAwareDataSource);
    }

    public <T> T getObjectById(Class<T> type, Object... ids) {
        return new ObjectByIdClosure<T>(type, ids).execute();
    }

    public <T> T objectFromClause(Class<T> type, String clause, Object... args) {
        return new ObjectFromClause<T>(type, clause, args).execute();
    }

    public <T> T insertObject(T object) {
        return new InsertClosure<T>(object).execute();
    }

    public <T> T updateObject(T object) {
        return new UpdateClosure<T>(object).execute();
    }

    public <T> int deleteObject(T object) {
        return new DeleteClosure<T>(object).execute();
    }

    public <T> int deleteObjectById(Class<T> clazz, Object... args) {
        return new DeleteByIdClosure<T>(clazz, args).execute();
    }

    public <T> List<T> listFromClause(Class<T> clazz, String clause, Object... args) {
        return new ListFromClauseClosure<T>(clazz, clause, args).execute();
    }

    public <T> int countObjectsFromClause(Class<T> clazz, String clause, Object... args) {
        return new CountObjectsFromClause<T>(clazz, clause, args).execute();
    }

    public int executeUpdate(final String sql, final Object... args) {
        return new SqlClosure<Integer>() {
            @Override
            protected Integer execute(Connection connection) throws SQLException {
                return OrmElf.executeUpdate(connection, sql, args);
            }
        }.execute();
    }

    private static class ListFromClauseClosure<T> extends AbstractSqlClosure<List<T>> {
        private Class<T> clazz;
        private String clause;
        private Object[] args;

        public ListFromClauseClosure(Class<T> clazz, String clause, Object[] args) {
            this.clazz = clazz;
            this.clause = clause;
            this.args = args;
        }

        @Override
        protected List<T> execute(Connection connection) throws SQLException {
            return OrmElf.listFromClause(connection, clazz, clause, args);
        }
    }

    private static class UpdateClosure<T> extends AbstractSqlClosure<T> {
        private T object;

        public UpdateClosure(T object) {
            this.object = object;
        }

        @Override
        protected T execute(Connection connection) throws SQLException {
            return OrmElf.updateObject(connection, object);
        }
    }

    private static class InsertClosure<T> extends AbstractSqlClosure<T> {
        private T object;

        public InsertClosure(T object) {
            this.object = object;
        }

        @Override
        protected T execute(Connection connection) throws SQLException {
            return OrmElf.insertObject(connection, object);
        }
    }
    
    private static class DeleteClosure<T> extends AbstractSqlClosure<Integer> {
        private T object;

        public DeleteClosure(T object) {
            this.object = object;
        }

        @Override
        protected Integer execute(Connection connection) throws SQLException {
            return OrmElf.deleteObject(connection, object);
        }
    }

    private static class DeleteByIdClosure<T> extends AbstractSqlClosure<Integer> {
        private Class<T> clazz;
        private Object[] args;

        public DeleteByIdClosure(Class<T> clazz, Object... args) {
            this.clazz = clazz;
            this.args = args;
        }

        @Override
        protected Integer execute(Connection connection) throws SQLException {
            return OrmElf.deleteObjectById(connection, clazz, args);
        }
    }

    private static class ObjectFromClause<T> extends AbstractSqlClosure<T> {
        private Class<T> clazz;
        private String clause;
        private Object[] args;

        public ObjectFromClause(Class<T> clazz, String clause, Object[] args) {
            this.clause = clause;
            this.args = args;
            this.clazz = clazz;
        }

        @Override
        protected T execute(Connection connection) throws SQLException {
            return OrmElf.objectFromClause(connection, clazz, clause, args);
        }
    }

    private static class ObjectByIdClosure<T> extends AbstractSqlClosure<T> {
        private Class<T> type;
        private Object[] ids;

        public ObjectByIdClosure(Class<T> type, Object[] ids) {
            this.type = type;
            this.ids = ids;
        }

        @Override
        protected T execute(Connection connection) throws SQLException {
            return OrmElf.objectById(connection, type, ids);
        }
    }

    private static class CountObjectsFromClause<T> extends AbstractSqlClosure<Integer> {
        private Class<T> clazz;
        private String clause;
        private Object[] args;

        public CountObjectsFromClause(Class<T> clazz, String clause, Object[] args) {
            this.clazz = clazz;
            this.clause = clause;
            this.args = args;
        }

        @Override
        protected Integer execute(Connection connection) throws SQLException {
            return OrmElf.countObjectsFromClause(connection, clazz, clause, args);
        }
    }
}
