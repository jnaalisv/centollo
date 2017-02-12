package centollo.config;

import centollo.infrastructure.hibernate.config.HibernateConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

//@Profile("hibernate")
@Configuration
@Import(HibernateConfig.class)
public class ConfigureHibernateForTests {

}

