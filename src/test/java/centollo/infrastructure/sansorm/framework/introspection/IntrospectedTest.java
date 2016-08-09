package centollo.infrastructure.sansorm.framework.introspection;

import centollo.model.domain.PurchaseOrder;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntrospectedTest {

    private Introspected introspected;

    @Before
    public void init() {
        introspected = Introspector.getIntrospected(PurchaseOrder.class);
    }

    @Test
    public void getTableName() {
        String tableName = introspected.getTableName();
        assertThat(tableName).isEqualTo("purchase_order");
    }

    @Test
    public void getColumnNames() {
        String[] columnNames = introspected.getColumnNames();
        assertThat(columnNames).contains("id", "version", "lastmodified", "orderitems");
    }
}
