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

        order.getOrderItems().forEach(orderItem -> orderItem.setOrderId(order.getId()));

        //sqlExecutor.insertObjectBatched(order.getOrderItems()); // TODO didnt update id

        order.getOrderItems().forEach(sqlExecutor::insertObject);
    }

    @Override
    public Optional<PurchaseOrder> findBy(Long orderId) {

        Optional<PurchaseOrder> maybeOrder = sqlExecutor.objectFromClause(PurchaseOrder.class, "id = ?", orderId);

        if (maybeOrder.isPresent()) {
            PurchaseOrder purchaseOrder = maybeOrder.get();
            purchaseOrder.setOrderItems(sqlExecutor.listFromClause(OrderItem.class, "order_id = ?", purchaseOrder.getId()));
        }

        return maybeOrder;
    }

}
