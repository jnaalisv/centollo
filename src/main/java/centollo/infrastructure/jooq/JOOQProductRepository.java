package centollo.infrastructure.jooq;

import centollo.model.domain.Product;
import centollo.model.domain.ProductRepository;
import centollo.model.domain.ProductType;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.RecordMapper;
import org.jooq.ResultQuery;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
public class JOOQProductRepository implements ProductRepository {

    private final DSLContext jooq;

    public JOOQProductRepository(final DSLContext jooq) {
        this.jooq = jooq;
    }

    private static final RecordMapper<Record3<Long, String, String>, Product> productRecordMapper
            = record -> new Product(record.value1(), record.value2(), ProductType.valueOf(record.value3()));

    @Override
    public List<Product> searchProducts(String query) {
        ResultQuery<Record3<Long, String, String>> sqlQuery = jooq
                .select(
                        field("id", Long.class),
                        field("name", String.class),
                        field("productType", String.class)
                )
                .from(table("product"))
                .where(field("name").like("%"+query+"%"));

        return sqlQuery.fetch(productRecordMapper);
    }

    @Override
    public void add(Product product) {

        jooq
            .insertInto(table("product"),
                    field("id", Long.class),
                    field("name", String.class),
                    field("productType", String.class))
            .values(product.getId(), product.getName(), product.getProductType().toString())
            .execute();
    }

    private static final Field<Long> VERSION = field("version", Long.class);

    @Override
    public void update(Product product) {

        long currentVersion = product.getVersion();
        long nextVersion = currentVersion + 1;

        int rowCount = jooq
                .update(table("product"))
                .set(field("name", String.class), product.getName())
                .set(field("productType", String.class), product.getProductType().toString())
                .set(VERSION, nextVersion)
                .where(field("id", Long.class).eq(product.getId()).and(VERSION.eq(currentVersion)))
                .execute();

        if (rowCount == 0) {
            throw new ObjectOptimisticLockingFailureException(Product.class, product.getId());
        }

        product.setVersion(nextVersion);
    }

    @Override
    public Optional<Product> findById(long productId) {
        return null;
    }

    @Override
    public void delete(Product product) {

    }
}
