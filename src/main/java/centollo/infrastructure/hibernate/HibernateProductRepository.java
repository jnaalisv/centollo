package centollo.infrastructure.hibernate;

import centollo.model.domain.Product;
import centollo.model.domain.ProductRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class HibernateProductRepository implements ProductRepository {

    private final SessionFactory sessionFactory;

    @Inject
    public HibernateProductRepository(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<Product> searchProducts(String query) {
        return getCurrentSession()
                .createQuery("SELECT p FROM Product p where p.name like :query", Product.class)
                .setParameter("query", "%"+query + "%")
                .list();
    }

    @Override
    public Optional<Product> findBy(String productCode) {
        return getCurrentSession()
                .createQuery("SELECT p FROM Product p where p.productCode = :productCode", Product.class)
                .setParameter("productCode", productCode)
                .uniqueResultOptional();
    }

    @Override
    public void add(Product product) {
        getCurrentSession().save(product);
    }

    @Override
    public void update(Product product) {
        getCurrentSession().update(product);
    }
}
