package centollo.infrastructure.sansorm;

import centollo.model.domain.Product;
import centollo.model.domain.ProductRepository;
import org.jnaalisv.sqlmapper.SqlQueries;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Repository
public class SansOrmProductRepository implements ProductRepository {

    private final SqlQueries sqlQueries;

    @Inject
    public SansOrmProductRepository(final SqlQueries sqlQueries) {
        this.sqlQueries = sqlQueries;
    }

    @Override
    public List<Product> searchProducts(String query) {
        return sqlQueries.queryByClause(Product.class, "name LIKE ?", "%" + query+"%");
    }

    @Override
    public Optional<Product> findBy(String productCode) {
        return sqlQueries.queryForOneByClause(Product.class, "productCode = ?", productCode);
    }

    @Override
    public void add(Product product) {
        sqlQueries.insertObject(product);
    }

    @Override
    public void update(Product product) {
        sqlQueries.updateObject(product);

        //sqlExecutor.updateVersionedObject(product);
    }
}
