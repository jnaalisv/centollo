package centollo.infrastructure.sansorm.framework.introspection;

import centollo.model.domain.OrderItem;
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
        assertThat(columnNames).containsExactly("id", "version", "lastmodified");
    }

    @Test
    public void getInsertableColumns() {
        String[] columnNames = introspected.getInsertableColumns();
        assertThat(columnNames).containsExactly("version", "lastmodified");
    }

    @Test
    public void getUpdatableColumns() {
        String[] columnNames = introspected.getUpdatableColumns();
        assertThat(columnNames).containsExactly("version", "lastmodified");
    }

    @Test
    public void getColumnTableNames() {
        String[] columnNames = introspected.getColumnTableNames();
        assertThat(columnNames).containsExactly(null, null, null);
    }

    @Test
    public void joinColumnTest() {
        Introspected withJoinColumn = Introspector.getIntrospected(OrderItem.class);

        String[] columnNames = withJoinColumn.getColumnNames();
        assertThat(columnNames).containsExactly("id", "productcode", "order_id", "itemcount");

        String[] insertableColumns = withJoinColumn.getInsertableColumns();
        assertThat(insertableColumns).containsExactly("productcode", "order_id", "itemcount");

        String[] updatableColumns = withJoinColumn.getUpdatableColumns();
        assertThat(updatableColumns).containsExactly("productcode", "itemcount");
    }
}
