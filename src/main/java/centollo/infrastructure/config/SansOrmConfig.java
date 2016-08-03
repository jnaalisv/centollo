package centollo.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("centollo.infrastructure.sansorm")
@Import(SqlConfig.class)
public class SansOrmConfig {

}
