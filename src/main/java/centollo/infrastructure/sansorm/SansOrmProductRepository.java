package centollo.infrastructure.sansorm;

import centollo.model.domain.Product;
import centollo.model.domain.ProductRepository;
import com.zaxxer.sansorm.OrmElf;
import com.zaxxer.sansorm.SqlClosure;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SansOrmProductRepository implements ProductRepository {

    private final TransactionAwareDataSourceProxy transactionAwareDataSourceProxy;

    @Inject
    public SansOrmProductRepository(TransactionAwareDataSourceProxy transactionAwareDataSourceProxy) {
        SqlClosure.setDefaultDataSource(transactionAwareDataSourceProxy);
        this.transactionAwareDataSourceProxy = transactionAwareDataSourceProxy;
    }

    @Override
    public List<Product> searchProducts(String query) {
        try {
            return OrmElf.listFromClause(transactionAwareDataSourceProxy.getConnection(), Product.class, "name LIKE ?", "%" + query+"%");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
