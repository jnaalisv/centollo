package centollo.model.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.LocalDateTime;

@Entity // required by Hibernate
@Table(name = "product") // required by SansOrm
public class Product {

    @Id // required by Hibernate
    private long id;

    @Version
    @Column(name = "version")
    private long version = 0l;

    @Column(name = "lastModified")
    private LocalDateTime lastModified;

    @Column(name = "productCode") // required by SansOrm
    private String productCode;

    @Column(name = "name") // required by SansOrm
    private String name;

    @Column(name = "productType") // required by SansOrm
    @Enumerated(EnumType.STRING) // required by Hibernate, SansOrm
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
}
