package centollo.web.products;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jnaalisv.test.MockMvcRequestBuilder;

@ActiveProfiles("hibernate")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class HibernateProductsApiTest {

    @Autowired
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
    public void shouldFindProductsByName() {
        httpGet("/products?query=java")
                .acceptApplicationJson()
                .expect200()
                .responseBody();
    }
}
