package centollo.model.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import java.time.LocalDateTime;

@Entity
public class Product {

    @Id
    @GeneratedValue
    private long id;

    @Version
    private long version;

    @Column
    private LocalDateTime lastModified;

    @Column
    private String name;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    public Product() { /*hibernate*/}

    public Product(long id, String name, ProductType productType) {
        this.id = id;
        this.name = name;
        this.productType = productType;
    }

    public Product(String name, ProductType productType) {
        this.name = name;
        this.productType = productType;
    }

    public String getName() {
        return name;
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

    public ProductType getProductType() {
        return productType;
    }

    public void setName(String name) {
        this.name = name;
    }
}
