package centollo.model.application.impl;

import centollo.model.application.OrderService;
import centollo.model.domain.OrderRepository;
import centollo.model.domain.PurchaseOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Inject
    public OrderServiceImpl(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void save(PurchaseOrder order) {
        order.setLastModified(LocalDateTime.now());
        orderRepository.add(order);
    }
}
