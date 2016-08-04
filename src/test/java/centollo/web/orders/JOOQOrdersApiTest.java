package centollo.web.orders;

import centollo.infrastructure.config.JOOQConfig;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {JOOQConfig.class})
public class JOOQOrdersApiTest extends AbstractOrdersApiTest {

}
