package centollo.web.products;

import centollo.infrastructure.config.JOOQConfig;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {JOOQConfig.class})
public class JOOQProductsApiTest extends AbstractProductsApiTest {

}
