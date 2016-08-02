package centollo.web.products;

import centollo.infrastructure.config.HibernateConfig;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {HibernateConfig.class})
public class HibernateProductsApiTest extends AbstractProductsApiTest {

}
