package centollo.model.domain;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> searchProducts(String query);

    Optional<Product> findBy(String productCode);

    void add(Product product);

    void update(Product product);
}
