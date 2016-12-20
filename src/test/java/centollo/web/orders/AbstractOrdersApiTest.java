package centollo.web.orders;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import centollo.model.config.ModelConfiguration;
import centollo.web.AbstractWebApiTest;
import centollo.web.config.WebConfiguration;
import centollo.web.interfaces.OrderDTO;
import centollo.web.interfaces.OrderItemDTO;
import centollo.web.interfaces.ProductDTO;

@Sql({"classpath:products.sql"})
@ContextConfiguration(classes = {ModelConfiguration.class, WebConfiguration.class })
public abstract class AbstractOrdersApiTest extends AbstractWebApiTest {

    @Test
    public void shouldPostSimpleOrder() {
        ProductDTO kona = httpGet("/products/K2")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAs(ProductDTO.class);

        ProductDTO java = httpGet("/products/J1")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAs(ProductDTO.class);


        OrderItemDTO konaOrderItem = new OrderItemDTO(kona.productCode, 5);
        OrderItemDTO javaOrderItem = new OrderItemDTO(java.productCode, 1);

        OrderDTO aNewOrder = new OrderDTO(konaOrderItem, javaOrderItem);

        OrderDTO postedOrder = httpPost("/orders")
                .contentTypeApplicationJson()
                .content(aNewOrder)
                .acceptApplicationJson()
                .expect201()
                .responseBodyAs(OrderDTO.class);

        assertThat(postedOrder.id).isGreaterThan(0L);
        assertThat(postedOrder.orderItems.size()).isEqualTo(2);
        postedOrder.orderItems.forEach(orderItemDTO -> {
            assertThat(orderItemDTO.id).isGreaterThan(0);
            assertThat(orderItemDTO.productCode).isIn(java.productCode, kona.productCode);
        });

        OrderDTO theOrder = httpGet("/orders/"+postedOrder.id)
                .acceptApplicationJson()
                .expect200()
                .responseBodyAs(OrderDTO.class);

        assertThat(theOrder.id).isGreaterThan(0L);
        assertThat(theOrder.orderItems.size()).isEqualTo(2);
        theOrder.orderItems.forEach(orderItemDTO -> {
            assertThat(orderItemDTO.id).isGreaterThan(0);
            assertThat(orderItemDTO.productCode).isIn(java.productCode, kona.productCode);
        });
    }
}
