package centollo.model.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    private long id;

    @Column(name = "productCode")
    private String productCode;

    @ManyToOne
    private PurchaseOrder purchaseOrder;

    @Column(name = "itemCount")
    private int itemCount;

    public OrderItem() {

    }

    public OrderItem(String productCode, PurchaseOrder purchaseOrder, int itemCount) {
        this.productCode = productCode;
        this.purchaseOrder = purchaseOrder;
        this.itemCount = itemCount;
    }

    public OrderItem(String productCode, int itemCount) {
        this.productCode = productCode;
        this.itemCount = itemCount;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getItemCount() {
        return itemCount;
    }
}
