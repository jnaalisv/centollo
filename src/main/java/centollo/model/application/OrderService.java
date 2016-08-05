package centollo.model.application;

import centollo.model.domain.PurchaseOrder;

public interface OrderService {

    void save(PurchaseOrder purchaseOrder);

    PurchaseOrder findBy(Long orderId);
}
