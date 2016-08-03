package centollo.web.interfaces;

import centollo.model.domain.Product;
import centollo.model.domain.ProductType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductDTO {

    public String name;
    public String productCode;
    public ProductType productType;
    public long id;

    @JsonCreator
    public ProductDTO(@JsonProperty("id") long id,
                      @JsonProperty("name") String name,
                      @JsonProperty("productCode") String productCode,
                      @JsonProperty("productType") ProductType productType) {
        this.id = id;
        this.name = name;
        this.productCode = productCode;
        this.productType = productType;
    }

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.productCode = product.getProductCode();
        this.productType = product.getProductType();
    }
}
