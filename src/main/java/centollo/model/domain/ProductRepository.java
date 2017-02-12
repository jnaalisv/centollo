package centollo.model.domain;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> searchProducts(String query);

    void add(Product product);

    void update(Product product);

    Optional<Product> findById(long productId);

    void delete(Product product);
}
