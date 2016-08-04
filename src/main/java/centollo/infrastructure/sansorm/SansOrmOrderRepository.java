package centollo.infrastructure.sansorm;

import centollo.model.domain.OrderRepository;
import centollo.model.domain.PurchaseOrder;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

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
        order.getOrderItems().forEach(sqlExecutor::insertObject);
    }

}
