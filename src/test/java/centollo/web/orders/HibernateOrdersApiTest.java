package centollo.web.orders;

import centollo.infrastructure.config.HibernateConfig;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {HibernateConfig.class})
public class HibernateOrdersApiTest extends AbstractOrdersApiTest {

}
