package centollo.infrastructure.sansorm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSqlClosure<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSqlClosure.class);

    private static TransactionAwareDataSourceProxy defaultDataSource;

    private List<Statement> closeStatements = new ArrayList<>();
    private List<ResultSet> closeResultSets = new ArrayList<>();

    private Object[] args;

    private TransactionAwareDataSourceProxy transactionAwareDataSource;

    public AbstractSqlClosure() {
        transactionAwareDataSource = defaultDataSource;
        if (transactionAwareDataSource == null) {
            throw new RuntimeException("No default DataSource has been set");
        }
    }

    public static void setDefaultDataSource(TransactionAwareDataSourceProxy transactionAwareDataSource) {
        defaultDataSource = transactionAwareDataSource;
    }

    public final T execute() {
        Connection connection = null;
        try {
            connection = transactionAwareDataSource.getConnection();

            if (args != null) {
            	return execute(connection, args);
            } else {
            	return execute(connection);
            }
        }
        catch (SQLException e) {
            if (e.getNextException() != null) {
                e = e.getNextException();
            }
            throw new RuntimeException(e);
        }
        finally {
            closeResultSets.forEach(AbstractSqlClosure::quietClose);

            closeStatements.forEach(AbstractSqlClosure::quietClose);

            closeResultSets.clear();
            closeStatements.clear();

            quietClose(connection);
        }
    }

    /**
     * Execute the closure with the specified arguments.  Note using this method
     * does not create a true closure because the arguments are not encapsulated
     * within the closure itself.  Meaning you cannot create an instance of the
     * closure and pass it to another executor.
     *
     * @param args arguments to be passed to the <code>execute(Connection connection, Object...args)</code> method
     * @return
     */
    public final T executeWith(Object...args) {
    	this.args = args;
    	return execute();
    }

    /**
     * Used to automatically close a Statement when the closure completes.
     *
     * @param statement the Statement to automatically close
     * @return the Statement that will be closed (same as the input parameter)
     */
    protected final <S extends Statement> S autoClose(S statement) {
        if (statement != null) {
            closeStatements.add(statement);
        }
        return statement;
    }

    /**
     * Used to automatically code a ResultSet when the closure completes.
     *
     * @param resultSet the ResultSet to automatically close
     * @return the ResultSet that will be closed (same as the input parameter)
     */
    protected final ResultSet autoClose(ResultSet resultSet) {
        if (resultSet != null) {
            closeResultSets.add(resultSet);
        }
        return resultSet;
    }

    /**
     * Subclasses of <code>SqlClosure</code> must override this method or the alternative
     * <code>execute(Connection connection, Object...args)</code> method.
     * @param connection the Connection to be used, do not close this connection yourself
     * @return the templated return value from the closure
     * @throws SQLException thrown if a SQLException occurs
     */
    protected T execute(final Connection connection) throws SQLException {
    	return null;
    }

    /**
     * Subclasses of <code>SqlClosure</code> must override this method or the alternative
     * <code>execute(Connection connection)</code> method.
     * @param connection the Connection to be used, do not close this connection yourself
     * @param args the arguments passed into the <code>SqlClosure(Object...args)</code> constructor
     * @return the templated return value from the closure
     * @throws SQLException thrown if a SQLException occurs
     */
    protected T execute(final Connection connection, Object...args) throws SQLException {
    	return null;
    }

    private static void quietClose(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            }
            catch (SQLException e) {
                LOGGER.warn("connection.close failed", e);
                return;
            }
        }
    }

    private static void quietClose(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            }
            catch (SQLException e) {
                LOGGER.warn(" statement.close failed", e);
                return;
            }
        }
    }

    private static void quietClose(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            }
            catch (SQLException e) {
                LOGGER.warn("resultSet.close failed", e);
                return;
            }
        }
    }
}
