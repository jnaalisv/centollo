package centollo.web.orders;

import centollo.model.config.ModelConfiguration;
import centollo.web.AbstractWebApiTest;
import centollo.web.config.WebConfiguration;
import centollo.web.interfaces.OrderDTO;
import centollo.web.interfaces.OrderItemDTO;
import centollo.web.interfaces.ProductDTO;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

@Sql({"classpath:products.sql"})
@ContextConfiguration(classes = {ModelConfiguration.class, WebConfiguration.class })
public abstract class AbstractOrdersApiTest extends AbstractWebApiTest {

    @Test
    public void shouldPostSimpleOrder() {
        ProductDTO kona = httpGet("/products/K2")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAs(ProductDTO.class);

        OrderItemDTO newOrderItem = new OrderItemDTO(kona.productCode, 5);

        OrderDTO aNewOrder = new OrderDTO(newOrderItem);

        OrderDTO postedOrder = httpPost("/orders")
                .contentTypeApplicationJson()
                .content(aNewOrder)
                .acceptApplicationJson()
                .expect201()
                .responseBodyAs(OrderDTO.class);

    }
}
