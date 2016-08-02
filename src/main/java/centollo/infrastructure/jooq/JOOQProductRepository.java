package centollo.infrastructure.jooq;

import centollo.model.domain.Product;
import centollo.model.domain.ProductRepository;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

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
        ResultQuery sqlQuery = jooq
                .select(field("p.name", String.class), field("p.id", Long.class))
                .from(table("product p"))
                .where(field("p.name").like("%"+query+"%"));

        Result<Record2<String, Long>> result = sqlQuery.fetch();

        return result
                .stream()
                .map(r -> new Product(r.value2(), r.value1()))
                .collect(Collectors.toList());
    }
}
