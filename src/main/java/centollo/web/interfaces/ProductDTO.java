package centollo.web.interfaces;

import centollo.model.domain.Product;
import centollo.model.domain.ProductType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductDTO {

    public String name;
    public String productCode;
    public ProductType productType;

    @JsonCreator
    public ProductDTO(@JsonProperty("name") String name,
                      @JsonProperty("productCode") String productCode,
                      @JsonProperty("productType") ProductType productType) {
        this.name = name;
        this.productCode = productCode;
        this.productType = productType;
    }

    public ProductDTO(Product product) {
        this.name = product.getName();
        this.productCode = product.getProductCode();
        this.productType = product.getProductType();
    }
}
