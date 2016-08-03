package centollo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jnaalisv.test.springframework.MockMvcRequestBuilder;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public abstract class AbstractWebApiTest {

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
}