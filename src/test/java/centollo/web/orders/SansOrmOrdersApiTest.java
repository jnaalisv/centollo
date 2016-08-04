package centollo.web.orders;

import centollo.infrastructure.config.SansOrmConfig;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {SansOrmConfig.class})
public class SansOrmOrdersApiTest extends AbstractOrdersApiTest {

}
