package centollo.web.interfaces;

import centollo.model.domain.Product;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductDTO {

    public String name;
    public String productCode;

    @JsonCreator
    public ProductDTO(@JsonProperty("name") String name,
                      @JsonProperty("productCode") String productCode) {
        this.name = name;
        this.productCode = productCode;
    }

    public ProductDTO(Product product) {
        this.name = product.getName();
        this.productCode = product.getProductCode();
    }
}
