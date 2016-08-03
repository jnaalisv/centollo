package centollo.infrastructure.sansorm;

import centollo.model.domain.Product;
import centollo.model.domain.ProductRepository;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Repository
public class SansOrmProductRepository implements ProductRepository {

    private final SqlExecutor sqlExecutor;

    @Inject
    public SansOrmProductRepository(final SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    @Override
    public List<Product> searchProducts(String query) {
        return sqlExecutor.listFromClause(Product.class, "name LIKE ?", "%" + query+"%");
    }

    @Override
    public Optional<Product> findBy(String productCode) {
        return sqlExecutor.objectFromClause(Product.class, "productCode = ?", productCode);
    }
}
