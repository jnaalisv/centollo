package centollo.web.products;

import centollo.model.config.ModelConfiguration;
import centollo.model.domain.ProductType;
import centollo.web.AbstractWebApiTest;
import centollo.web.config.WebConfiguration;
import centollo.web.interfaces.ProductDTO;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Sql({"classpath:products.sql"})
@ContextConfiguration(classes = {ModelConfiguration.class, WebConfiguration.class })
public abstract class AbstractProductsApiTest extends AbstractWebApiTest {

    @Test
    public void shouldFindProductsByExactName() {
        List<ProductDTO> products = httpGet("/products?query=Java")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAsListOf(ProductDTO.class);

        assertThat(products.size()).isEqualTo(1);

        ProductDTO java = products.get(0);

        assertThat(java.name).isEqualTo("Java");
        assertThat(java.productCode).isEqualTo("J1");
        assertThat(java.productType).isEqualTo(ProductType.BEANS);
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

        assertThat(products.size()).isEqualTo(8);

        products.forEach(
                p -> {
                    assertThat(p.name).isNotEmpty();
                    assertThat(p.productCode).isNotEmpty();
                    assertThat(p.productType).isNotNull();
                }
        );
    }

    @Test
    public void shouldFindByProductCode() {
        ProductDTO kona = httpGet("/products/K2")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAs(ProductDTO.class);

        assertThat(kona.productCode).isEqualTo("K2");
        assertThat(kona.productType).isEqualTo(ProductType.BEANS);
    }

    @Test
    public void shouldReturn404OnProductNotFound() {
        httpGet("/products/nope!")
                .acceptApplicationJson()
                .expect404();
    }

    @Test
    public void shouldAddANewProduct() {

        ProductDTO aNewProduct = new ProductDTO(0l, "E10", "Compak E10", ProductType.GRINDERS);

        ProductDTO postedProduct = httpPost("/products")
                .contentTypeApplicationJson()
                .content(aNewProduct)
                .acceptApplicationJson()
                .expect201()
                .responseBodyAs(ProductDTO.class);

        assertThat(aNewProduct.name).isEqualTo(postedProduct.name);
        assertThat(aNewProduct.productCode).isEqualTo(postedProduct.productCode);
        assertThat(aNewProduct.productType).isEqualTo(postedProduct.productType);
    }

    @Test
    public void shouldUpdateAProduct() {
        ProductDTO compakE8 = httpGet("/products/CE8")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAs(ProductDTO.class);

        assertThat(compakE8.productType).isEqualTo(ProductType.GRINDERS);

        compakE8.name = "Fancy New E8!";

        ProductDTO updatedCompakE8 = httpPut("/products/CE8")
                .contentTypeApplicationJson()
                .content(compakE8)
                .acceptApplicationJson()
                .expect200()
                .responseBodyAs(ProductDTO.class);

        assertThat(updatedCompakE8.name).isEqualTo("Fancy New E8!");
        assertThat(updatedCompakE8.productType).isEqualTo(ProductType.GRINDERS);

        updatedCompakE8 = httpGet("/products/CE8")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAs(ProductDTO.class);
        
        assertThat(updatedCompakE8.name).isEqualTo("Fancy New E8!");
        assertThat(updatedCompakE8.productType).isEqualTo(ProductType.GRINDERS);
    }

    @Test
    public void assertOptimisticLocking() {

        ProductDTO compakE8 = httpGet("/products/CE8")
                .acceptApplicationJson()
                .expect200()
                .responseBodyAs(ProductDTO.class);

        assertThat(compakE8.version).isEqualTo(0);

        ProductDTO updatedCompakE8 = httpPut("/products/CE8")
                .contentTypeApplicationJson()
                .content(compakE8)
                .acceptApplicationJson()
                .expect200()
                .responseBodyAs(ProductDTO.class);

        assertThat(updatedCompakE8.version).isEqualTo(1);

        httpPut("/products/CE8")
                .contentTypeApplicationJson()
                .content(compakE8)
                .acceptApplicationJson()
                .expect409();
    }
}
