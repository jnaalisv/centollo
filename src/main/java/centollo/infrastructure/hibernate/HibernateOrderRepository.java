package centollo.infrastructure.hibernate;

import centollo.model.domain.OrderRepository;
import centollo.model.domain.PurchaseOrder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

@Repository
public class HibernateOrderRepository implements OrderRepository {

    private final SessionFactory sessionFactory;

    @Inject
    public HibernateOrderRepository(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public void add(PurchaseOrder purchaseOrder) {
        getCurrentSession().save(purchaseOrder);
    }

}
