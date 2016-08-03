package centollo.model.application;

import centollo.model.domain.Product;

import java.util.List;

public interface ProductService {

    List<Product> searchProducts(String query);

    Product findBy(String productCode);

    void save(Product product);

    void update(Product product);
}
