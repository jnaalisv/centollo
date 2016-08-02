package centollo.web.interfaces;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductDTO {

    public String name;

    @JsonCreator
    public ProductDTO(@JsonProperty("name") String name) {
        this.name = name;
    }
}
