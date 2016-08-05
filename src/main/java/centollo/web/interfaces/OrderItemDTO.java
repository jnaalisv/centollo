package centollo.web.interfaces;

import centollo.model.domain.OrderItem;

public class OrderItemDTO {

    public long id;
    public String productCode;
    public int itemCount;

    public OrderItemDTO() {}

    public OrderItemDTO(OrderItem orderItem) {
        this.productCode = orderItem.getProductCode();
        this.itemCount = orderItem.getItemCount();
        this.id = orderItem.getId();
    }

    public OrderItemDTO(String productCode, int itemCount) {
        this.productCode = productCode;
        this.itemCount = itemCount;
    }
}
