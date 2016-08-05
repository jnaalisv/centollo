package centollo.infrastructure.jooq;

import centollo.model.domain.OrderRepository;
import centollo.model.domain.PurchaseOrder;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Optional;

@Repository
public class JOOQOrderRepository implements OrderRepository {

    private final DSLContext jooq;

    @Inject
    public JOOQOrderRepository(final DSLContext jooq) {
        this.jooq = jooq;
    }

    @Override
    public void add(PurchaseOrder order) {

    }

    @Override
    public Optional<PurchaseOrder> findBy(Long orderId) {
        return null;
    }
}