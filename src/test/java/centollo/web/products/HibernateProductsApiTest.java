package centollo.web.products;

import centollo.infrastructure.config.HibernateConfig;
import centollo.model.config.ModelConfiguration;
import centollo.web.config.WebConfiguration;
import centollo.web.interfaces.ProductDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jnaalisv.test.springframework.MockMvcRequestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Sql({"classpath:products.sql"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {HibernateConfig.class, ModelConfiguration.class, WebConfiguration.class })
@WebAppConfiguration
public class HibernateProductsApiTest {

    @Inject
    private WebApplicationContext webApplicationContext;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected MockMvc mockMvc;

    @Before
    public void initMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    protected MockMvcRequestBuilder httpGet(String urlTemplate, Object... urlVars) {
        return new MockMvcRequestBuilder(mockMvc, objectMapper, get(urlTemplate, urlVars));
    }

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
