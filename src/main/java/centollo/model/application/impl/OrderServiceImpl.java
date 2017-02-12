package centollo.model.application.impl;

import centollo.model.application.NotFoundException;
import centollo.model.application.OrderService;
import centollo.model.domain.OrderRepository;
import centollo.model.domain.PurchaseOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void save(PurchaseOrder order) {
        order.setLastModified(LocalDateTime.now());
        order.getOrderItems().forEach(orderItem -> orderItem.setOrder(order));

        orderRepository.add(order);
    }

    @Override
    @Transactional
    public PurchaseOrder findBy(Long orderId) {
        return orderRepository
                .findBy(orderId)
                .orElseThrow(() -> new NotFoundException(PurchaseOrder.class, orderId));
    }
}
