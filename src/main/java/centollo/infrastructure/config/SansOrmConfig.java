package centollo.infrastructure.config;

import org.jnaalisv.sqlmapper.SqlQueries;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

@Configuration
@ComponentScan("centollo.infrastructure.sansorm")
@Import(SqlConfig.class)
public class SansOrmConfig {

    @Bean
    public SqlQueries sqlQueries(TransactionAwareDataSourceProxy transactionAwareDataSourceProxy) {
        return new SqlQueries(transactionAwareDataSourceProxy);
    }

}
