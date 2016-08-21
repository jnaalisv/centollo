package centollo.infrastructure.sansorm;

import centollo.model.domain.OrderRepository;
import centollo.model.domain.PurchaseOrder;
import org.jnaalisv.sqlmapper.SqlQueries;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Optional;

@Repository
public class SansOrmOrderRepository implements OrderRepository {

    private final SqlQueries sqlQueries;

    @Inject
    public SansOrmOrderRepository(final SqlQueries sqlQueries) {
        this.sqlQueries = sqlQueries;
    }


    @Override
    public void add(PurchaseOrder order) {
        sqlQueries.insertObject(order);
        order.getOrderItems().forEach(sqlQueries::insertObject);
    }

    @Override
    public Optional<PurchaseOrder> findBy(Long orderId) {
        return sqlQueries.queryForOneById(PurchaseOrder.class, orderId);
    }

}
