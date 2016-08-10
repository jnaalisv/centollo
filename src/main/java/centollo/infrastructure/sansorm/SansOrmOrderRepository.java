package centollo.infrastructure.sansorm;

import centollo.model.domain.OrderItem;
import centollo.model.domain.OrderRepository;
import centollo.model.domain.PurchaseOrder;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Optional;

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
        order.getOrderItems().forEach(oe -> oe.setOrder(order));

        order.getOrderItems().forEach(sqlExecutor::insertObject);
    }

    @Override
    public Optional<PurchaseOrder> findBy(Long orderId) {

        Optional<PurchaseOrder> maybePurchaseOrder = sqlExecutor.getObjectById(PurchaseOrder.class, orderId);

        if (maybePurchaseOrder.isPresent()) {
            PurchaseOrder purchaseOrder = maybePurchaseOrder.get();
            purchaseOrder.getOrderItems().addAll(sqlExecutor.listFromClause(OrderItem.class, "order_id =?", purchaseOrder.getId()));
        }

        return maybePurchaseOrder;
    }

}
