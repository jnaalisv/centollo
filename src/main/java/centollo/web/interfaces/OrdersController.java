package centollo.web.interfaces;

import centollo.model.application.OrderService;
import centollo.model.domain.OrderItem;
import centollo.model.domain.PurchaseOrder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;

    @Inject
    public OrdersController(final OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<OrderDTO> post(@RequestBody OrderDTO aNewOrder) {

        List<OrderItem> orderItems = aNewOrder
                .orderItems
                .stream()
                .map(orderItemDTO -> new OrderItem(orderItemDTO.productCode, orderItemDTO.itemCount))
                .collect(Collectors.toList());


        PurchaseOrder order = new PurchaseOrder(0l, orderItems);
        orderService.save(order);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Location", "orders/" + order.getId());
        return new ResponseEntity<>(new OrderDTO(order), responseHeaders, HttpStatus.CREATED);
    }

}
