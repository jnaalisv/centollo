package centollo.model.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "product")
public class Product {

    @Id
    public long id;

    @Column(name = "productCode")
    private String productCode;

    @Column(name = "name")
    private String name;

    public Product() { /*hibernate*/}

    public Product(long id, String productCode, String name) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getProductCode() {
        return productCode;
    }
}
