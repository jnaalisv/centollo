package centollo.infrastructure.sansorm.framework.sql;

import centollo.infrastructure.sansorm.framework.introspection.Introspected;
import centollo.infrastructure.sansorm.framework.introspection.Introspector;
import centollo.model.domain.PurchaseOrder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlGeneratorTest {

    @Test
    public void objectByIdSql(){
        String sql = SqlGenerator.objectByIdSql(Introspector.getIntrospected(PurchaseOrder.class).getIdColumnNames());
        assertThat(sql).isEqualTo("id=?");
    }

    @Test
    public void selectFromClauseSql() {
        Introspected introspected = Introspector.getIntrospected(PurchaseOrder.class);

        String tableName = introspected.getTableName();
        String[] columnNames = introspected.getColumnNames();
        String[] columnTableNames = introspected.getColumnTableNames();

        String sql = SqlGenerator.selectFromClauseSql(tableName, columnNames, columnTableNames, "id = ?");

        assertThat(sql).isEqualTo("SELECT purchase_order.id,purchase_order.version,purchase_order.lastmodified,purchase_order.orderitems FROM purchase_order purchase_order WHERE  id = ?");
    }

    @Test
    public void countObjectsFromClauseSql(){
        Introspected introspected = Introspector.getIntrospected(PurchaseOrder.class);

        String sql = SqlGenerator.countObjectsFromClauseSql(introspected.getTableName(), introspected.getIdColumnNames(), introspected.getColumnNames(), "id = ?");
        assertThat(sql).isEqualTo("SELECT COUNT(purchase_order.id) FROM purchase_order purchase_order WHERE  id = ?");

    }

    @Test
    public void getColumnsCsv(){
        Introspected introspected = Introspector.getIntrospected(PurchaseOrder.class);

        String tableName = introspected.getTableName();
        String[] columnNames = introspected.getColumnNames();
        String[] columnTableNames = introspected.getColumnTableNames();

        String sql = SqlGenerator.getColumnsCsv(columnNames, columnTableNames, "order");
        assertThat(sql).isEqualTo("order.id,order.version,order.lastmodified,order.orderitems");

    }

    @Test
    public void getColumnsCsvExclude(){
        Introspected introspected = Introspector.getIntrospected(PurchaseOrder.class);
        String[] columnNames = introspected.getColumnNames();
        String[] columnTableNames = introspected.getColumnTableNames();

        String sql = SqlGenerator.getColumnsCsvExclude(columnNames, columnTableNames, "order");
        assertThat(sql).isEqualTo("id,version,lastmodified,orderitems");

    }
}
