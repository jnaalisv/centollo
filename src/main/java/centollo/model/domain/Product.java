package centollo.model.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "product")
public class Product {

    @Id
    private long id;

    @Column(name = "productCode")
    private String productCode;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    public Product() { /*hibernate*/}

    public Product(long id, String productCode, String name, ProductType productType) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.productType = productType;
    }

    public String getName() {
        return name;
    }

    public String getProductCode() {
        return productCode;
    }

    public ProductType getProductType() {
        return productType;
    }

    public long getId() {
        return id;
    }
}
