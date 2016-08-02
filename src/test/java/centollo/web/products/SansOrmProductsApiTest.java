package centollo.web.products;

import centollo.infrastructure.config.SansOrmConfig;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {SansOrmConfig.class})
public class SansOrmProductsApiTest extends AbstractProductsApiTest {

}
