package centollo;

import centollo.config.TestConfig;
import centollo.model.application.ProductService;
import centollo.model.config.ModelConfiguration;
import centollo.model.domain.Product;
import centollo.model.domain.ProductType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class, ModelConfiguration.class})
public class CrudTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void create() {

        Product product = new Product("XFV Super Fast Grinder", ProductType.GRINDERS);

        productService.save(product);

        assertThat(product.getId()).isGreaterThan(0);
        assertThat(product.getVersion()).isEqualTo(0);

        int rowCount = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "Product",
                "id=" +product.getId() +" and version=" + product.getVersion());

        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    public void readById() {
        Product product = new Product("XFV Super Fast Grinder", ProductType.GRINDERS);

        productService.save(product);


        product = productService.findById(product.getId());

        assertThat(product).isNotNull();

        int rowCount = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "Product",
                "id=" +product.getId() +" and version=" + product.getVersion());

        assertThat(rowCount).isEqualTo(1);
    }

    @Sql("classpath:products.sql")
    @Test
    public void readByCustomQuery() {

        List<Product> foundProducts = productService.searchProducts("notfound");

        assertThat(foundProducts).isEmpty();

        foundProducts = productService.searchProducts("Jav");
        assertThat(foundProducts).isNotEmpty();
    }

    @Sql("classpath:products.sql")
    @Test
    public void update() {

        List<Product> foundProducts = productService.searchProducts("Java");
        assertThat(foundProducts).isNotEmpty();

        Product product = foundProducts.get(0);
        product.setName("Kopi Luwak");

        productService.update(product);

        int rowCount = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "Product",
                "id=" +product.getId() +" and name='Kopi Luwak'");

        assertThat(rowCount).isEqualTo(1);
    }

    @Sql("classpath:products.sql")
    @Test
    public void delete() {

        List<Product> foundProducts = productService.searchProducts("Java");
        assertThat(foundProducts).isNotEmpty();

        productService.delete(foundProducts.get(0));

        assertThat(productService.searchProducts("Java")).isEmpty();
    }
}
