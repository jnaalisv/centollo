package centollo.infrastructure.jooq;

import centollo.model.domain.Product;
import centollo.model.domain.ProductRepository;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.ResultQuery;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
public class JOOQProductRepository implements ProductRepository {

    private final DSLContext jooq;

    @Inject
    public JOOQProductRepository(final DSLContext jooq) {
        this.jooq = jooq;
    }

    @Override
    public List<Product> searchProducts(String query) {
        ResultQuery<Record3<Long, String, String>> sqlQuery = jooq
                .select(
                        field("id", Long.class),
                        field("productCode", String.class),
                        field("name", String.class)
                )
                .from(table("product"))
                .where(field("name").like("%"+query+"%"));

        return sqlQuery.fetchInto(Product.class);
    }

    @Override
    public Optional<Product> findBy(String productCode) {
        ResultQuery<Record3<Long, String, String>> sqlQuery = jooq
                .select(
                        field("id", Long.class),
                        field("productCode", String.class),
                        field("name", String.class)
                )
                .from(table("product"))
                .where(field("productCode").eq(productCode));

        Product productOrNull = sqlQuery.fetchOneInto(Product.class);

        return Optional.ofNullable(productOrNull);
    }
}
