package centollo.model.domain;

import java.util.List;

public interface ProductRepository {

    List<Product> searchProducts(String query);
}
