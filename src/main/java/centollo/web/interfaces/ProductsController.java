package centollo.web.interfaces;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsController {

    public ProductsController() {

    }

    @GetMapping
    public List<ProductDTO> productsQuery(String query) {
        return Arrays.asList(new ProductDTO(query));
    }
}
