package centollo.web.interfaces;

import centollo.model.domain.PurchaseOrder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDTO {

    public long id;

    public List<OrderItemDTO> orderItems;

    public OrderDTO() {}

    public OrderDTO(PurchaseOrder purchaseOrder) {
        this.id = purchaseOrder.getId();
        this.orderItems = purchaseOrder
                .getOrderItems()
                .stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());
    }

    public OrderDTO(OrderItemDTO...newOrderItem) {
        orderItems = Arrays.asList(newOrderItem);
    }
}
