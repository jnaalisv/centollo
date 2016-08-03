package centollo.model.application.impl;

import centollo.model.application.NotFoundException;
import centollo.model.application.ProductService;
import centollo.model.domain.Product;
import centollo.model.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Inject
    public ProductServiceImpl(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public List<Product> searchProducts(String query) {
        return productRepository.searchProducts(query);
    }

    @Override
    @Transactional
    public Product findBy(String productCode) {
        return productRepository
                .findBy(productCode)
                .orElseThrow(() -> new NotFoundException(Product.class, productCode));
    }

    @Override
    @Transactional
    public void save(Product product) {
        productRepository.add(product);
    }
}
