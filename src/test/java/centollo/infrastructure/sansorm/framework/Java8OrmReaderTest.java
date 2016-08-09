package centollo.infrastructure.sansorm.framework;

import centollo.infrastructure.config.SansOrmConfig;
import centollo.model.application.NotFoundException;
import centollo.model.domain.PurchaseOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;

@Sql({"classpath:products.sql"})
@ContextConfiguration(classes = {SansOrmConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class Java8OrmReaderTest {

    @Inject
    private TransactionAwareDataSourceProxy transactionAwareDataSource;

    @Test
    public void test() throws SQLException {
        try (Connection connection = transactionAwareDataSource.getConnection()) {
            long id = 1;

            PurchaseOrder purchaseOrder = Java8OrmReader
                    .objectById(connection, PurchaseOrder.class, id)
                    .orElseThrow(() -> new NotFoundException(PurchaseOrder.class, id));
        }
    }

}
