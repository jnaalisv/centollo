package centollo.web.interfaces;

import centollo.model.application.ProductService;
import centollo.model.domain.Product;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping(path = "/{productCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ProductDTO get(@PathVariable String productCode) {
        return new ProductDTO(productService.findBy(productCode));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ProductDTO> post(@RequestBody ProductDTO newProduct) {

        Product product = new Product(0l, newProduct.productCode, newProduct.name, newProduct.productType);
        productService.save(product);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Location", "products/" + product.getProductCode());
        return new ResponseEntity<>(new ProductDTO(product), responseHeaders, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{productCode}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ProductDTO put(@RequestBody ProductDTO aProduct) {

        Product product = new Product(aProduct.id, aProduct.productCode, aProduct.name, aProduct.productType);
        productService.update(product);

        return new ProductDTO(product);
    }
}
