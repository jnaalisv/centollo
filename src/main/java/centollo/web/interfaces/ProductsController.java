package centollo.web.interfaces;

import centollo.model.application.ProductService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductService productService;

    @Inject
    public ProductsController(final ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ProductDTO> productsQuery(String query) {
        return productService
                .searchProducts(query)
                .stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }
}
