package centollo.infrastructure.hibernate;

import centollo.model.domain.Product;
import centollo.model.domain.ProductRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HibernateProductRepository implements ProductRepository {

    private final SessionFactory sessionFactory;

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
    public void add(Product product) {
        getCurrentSession().save(product);
    }

    @Override
    public void update(Product product) {
        getCurrentSession().update(product);
    }

    @Override
    public Optional<Product> findById(long productId) {
        return getCurrentSession()
                .byId(Product.class)
                .loadOptional(productId);
    }

    @Override
    public void delete(Product product) {
        getCurrentSession().delete(product);
    }
}
