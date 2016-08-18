package centollo.model.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "productCode")
    private String productCode;

    @ManyToOne
    @JoinColumn(name="order_id")
    private PurchaseOrder order;

    @Column(name = "itemCount")
    private int itemCount;

    public OrderItem() {

    }

    public OrderItem(String productCode, PurchaseOrder order, int itemCount) {
        this.productCode = productCode;
        this.order = order;
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

    public PurchaseOrder getOrder() {
        return order;
    }

    public void setOrder(PurchaseOrder order) {
        this.order = order;
    }

    public long getId() {
        return id;
    }
}
