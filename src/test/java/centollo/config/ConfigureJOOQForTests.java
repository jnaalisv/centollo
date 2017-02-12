package centollo.config;

import centollo.infrastructure.jooq.config.JOOQConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("jooq")
@Configuration
@Import(JOOQConfig.class)
public class ConfigureJOOQForTests {

}
