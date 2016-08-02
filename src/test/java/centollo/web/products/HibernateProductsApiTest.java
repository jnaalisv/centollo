package centollo.web.products;

import centollo.infrastructure.config.HibernateConfig;
import centollo.web.interfaces.ProductDTO;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ContextConfiguration(classes = {HibernateConfig.class})
public class HibernateProductsApiTest extends AbstractWebApiTest{

    @Test
    public void shouldFindProductsByExactName() {
        List<ProductDTO> products = httpGet("/products?query=Java")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAsListOf(ProductDTO.class);

        assertThat(products.size()).isEqualTo(1);
        assertThat(products.get(0).name).isEqualTo("Java");

    }

    @Test
    public void shouldNotFindAnythingWithInvalidName() {
        List<ProductDTO> products = httpGet("/products?query=tea")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAsListOf(ProductDTO.class);

        assertThat(products.size()).isEqualTo(0);
    }

    @Test
    public void shouldFindWithPartialName() {
        List<ProductDTO> products = httpGet("/products?query=a")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAsListOf(ProductDTO.class);

        assertThat(products.size()).isEqualTo(7);
    }
}
