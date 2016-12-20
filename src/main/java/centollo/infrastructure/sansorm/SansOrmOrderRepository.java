package centollo.infrastructure.sansorm;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;

import centollo.model.domain.OrderItem;
import centollo.model.domain.OrderRepository;
import centollo.model.domain.PurchaseOrder;

@Repository
public class SansOrmOrderRepository implements OrderRepository {

    private final SqlExecutor sqlExecutor;

    @Inject
    public SansOrmOrderRepository(final SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }


    @Override
    public void add(PurchaseOrder order) {
        sqlExecutor.insertObject(order);

        // 1. set ManyToOne references
        order.getOrderItems().forEach(orderItem -> orderItem.setOrder(order));

        // 2. insert each object separately
        order.getOrderItems().forEach(sqlExecutor::insertObject);
    }

    @Override
    public Optional<PurchaseOrder> findBy(Long orderId) {

        Optional<PurchaseOrder> maybePurchaseOrder = sqlExecutor.getObjectById(PurchaseOrder.class, orderId);

        if (maybePurchaseOrder.isPresent()) {
            PurchaseOrder purchaseOrder = maybePurchaseOrder.get();

            // populate OneToMany-collection
            purchaseOrder.getOrderItems().addAll(sqlExecutor.listFromClause(OrderItem.class, "order_id =?", purchaseOrder.getId()));
        }

        return maybePurchaseOrder;
    }

}
