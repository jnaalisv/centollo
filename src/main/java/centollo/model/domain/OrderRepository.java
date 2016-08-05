package centollo.model.domain;

import java.util.Optional;

public interface OrderRepository {

    void add(PurchaseOrder order);

    Optional<PurchaseOrder> findBy(Long orderId);
}
