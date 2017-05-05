package centollo.infrastructure.hibernate;

import centollo.infrastructure.config.HibernateConfig;
import centollo.model.config.ModelConfiguration;
import centollo.model.domain.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Sql({"classpath:products.sql"})
@ContextConfiguration(classes = {ModelConfiguration.class, HibernateConfig.class})
public class HibernateProductRepositoryTest {

    @Inject
    private HibernateProductRepository hibernateProductRepository;


    @Test
    @Transactional
    public void searchProducts() {

        List<Product> products = hibernateProductRepository.searchProducts("a");
        assertThat(products.size()).isEqualTo(8);
    }

    @Test
    @Transactional
    public void nativeSearch() {

        List<Product> products = hibernateProductRepository.nativeSearch("a");
        assertThat(products.size()).isEqualTo(8);
    }

}
