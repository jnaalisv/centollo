package centollo.infrastructure.sansorm.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class Introspected {
    private static final Logger LOGGER = LoggerFactory.getLogger(Introspected.class);

    private Class<?> clazz;
    private String tableName;

    private Map<String, FieldColumnInfo> columnToField;

    private FieldColumnInfo selfJoinFCInfo;

    private boolean isGeneratedId;

    // We use arrays because iteration is much faster
    private FieldColumnInfo[] idFieldColumnInfos;
    private String[] idColumnNames;
    private String[] columnNames;
    private String[] columnTableNames;
    private String[] columnsSansIds;

    private String[] insertableColumns;
    private String[] updatableColumns;
    
    // Instance initializer
    {
        columnToField = new LinkedHashMap<>();
    }

    Introspected(Class<?> clazz) {
        this.clazz = clazz;

        LOGGER.info("Introspect " + clazz.getSimpleName());

        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation != null) {
            tableName = tableAnnotation.name();
        }

        LOGGER.info("tableName " + tableName);

        try {
            ArrayList<FieldColumnInfo> idFcInfos = new ArrayList<>();

            for (Field field : clazz.getDeclaredFields()) {

                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers)) {
                    LOGGER.info("  -- field " + field.getName() + " skipped");
                    continue;
                }

                OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
                if (oneToManyAnnotation != null) {
                    LOGGER.info("  -- field " + field.getName() + " skipped because oneToManyAnnotation");
                    continue;
                }

                field.setAccessible(true);

                FieldColumnInfo fcInfo = new FieldColumnInfo(field);

                processColumnAnnotation(fcInfo);

                Id idAnnotation = field.getAnnotation(Id.class);
                if (idAnnotation != null) {
                    LOGGER.info("  -- field " + field.getName() + " has Id annotation");
                    // Is it a problem that Class.getDeclaredFields() claims the fields are returned unordered?  We count on order.
                    idFcInfos.add(fcInfo);
                    GeneratedValue generatedAnnotation = field.getAnnotation(GeneratedValue.class);
                    isGeneratedId = (generatedAnnotation != null);
                    if (isGeneratedId && idFcInfos.size() > 1) {
                        throw new IllegalStateException("Cannot have multiple @Id annotations and @GeneratedValue at the same time.");
                    }
                }

                Enumerated enumAnnotation = field.getAnnotation(Enumerated.class);
                if (enumAnnotation != null) {
                    LOGGER.info("  -- field " + field.getName() + " has Enumerated annotation");
                    fcInfo.setEnumConstants(enumAnnotation.value());
                }
            }

            readColumnInfo(idFcInfos);

            getInsertableColumns();
            getUpdatableColumns();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Object get(Object target, String columnName) {
        FieldColumnInfo fcInfo = columnToField.get(columnName);
        if (fcInfo == null) {
            throw new RuntimeException("Cannot find field mapped to column " + columnName + " on type " + target.getClass().getCanonicalName());
        }

        try {
            Object value = fcInfo.field.get(target);
            // Fix-up column value for enums, integer as boolean, etc.
            if (fcInfo.enumConstants != null) {
                value = (fcInfo.enumType == EnumType.ORDINAL ? ((Enum<?>) value).ordinal() : ((Enum<?>) value).name());
            }

            return value;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void set(Object target, String columnName, Object value) {
        FieldColumnInfo fcInfo = columnToField.get(columnName);
        if (fcInfo == null) {
            throw new RuntimeException("Cannot find field mapped to column " + columnName + " on type " + target.getClass().getCanonicalName());
        }

        try {
            final Class<?> fieldType = fcInfo.fieldType;
            Class<?> columnType = value.getClass();
            Object columnValue = value;

            if (fieldType != columnType) {
                // Fix-up column value for enums, integer as boolean, etc.
                if (fieldType == boolean.class && columnType == Integer.class) {
                    columnValue = (((Integer) columnValue) != 0);
                }
                else if (columnType == BigDecimal.class) {
                    if (fieldType == BigInteger.class) {
                        columnValue = ((BigDecimal) columnValue).toBigInteger();
                    }
                    else if (fieldType == Integer.class) {
                        columnValue = (int) ((BigDecimal) columnValue).longValue();
                    }
                    else if (fieldType == Long.class) {
                        columnValue = ((BigDecimal) columnValue).longValue();
                    }
                }
                else if (fcInfo.enumConstants != null) {
                    columnValue = fcInfo.enumConstants.get(columnValue);
                }
                else if (columnValue instanceof Clob) {
                    columnValue = readClob((Clob) columnValue);
                }
            }

            fcInfo.field.set(target, columnValue);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    boolean hasSelfJoinColumn()
    {
        return selfJoinFCInfo != null;
    }

    boolean isSelfJoinColumn(String columnName)
    {
        return selfJoinFCInfo.columnName.equals(columnName);
    }

    String getSelfJoinColumn()
    {
        return (selfJoinFCInfo != null ? selfJoinFCInfo.columnName : null);
    }

    String[] getColumnNames() {
        return columnNames;
    }


    String[] getColumnTableNames() {
        return columnTableNames;
    }


    String[] getIdColumnNames() {
        return idColumnNames;
    }

    public String[] getColumnsSansIds() {
        return columnsSansIds;
    }

    boolean hasGeneratedId() {
        return isGeneratedId;
    }

    String[] getInsertableColumns() {
        if (insertableColumns != null) {
            return insertableColumns;
        }

        LinkedList<String> columns = new LinkedList<>();
        if (hasGeneratedId()) {
            columns.addAll(Arrays.asList(columnsSansIds));
        }
        else {
            columns.addAll(Arrays.asList(columnNames));
        }

        Iterator<String> iterator = columns.iterator();
        while (iterator.hasNext()) {
            if (!isInsertableColumn(iterator.next())) {
                iterator.remove();
            }
        }

        insertableColumns = columns.toArray(new String[0]);
        return insertableColumns;
    }

    String[] getUpdatableColumns() {
        if (updatableColumns != null) {
            return updatableColumns;
        }

        LinkedList<String> columns = new LinkedList<>();
        if (hasGeneratedId()) {
            columns.addAll(Arrays.asList(columnsSansIds));
        }
        else {
            columns.addAll(Arrays.asList(columnNames));
        }

        Iterator<String> iterator = columns.iterator();
        while (iterator.hasNext()) {
            if (!isUpdatableColumn(iterator.next())) {
                iterator.remove();
            }
        }

        updatableColumns = columns.toArray(new String[0]);
        return updatableColumns;
    }

    private boolean isInsertableColumn(String columnName) {
        FieldColumnInfo fcInfo = columnToField.get(columnName);
        return (fcInfo != null && fcInfo.insertable);
    }

    private boolean isUpdatableColumn(String columnName) {
        FieldColumnInfo fcInfo = columnToField.get(columnName);
        return (fcInfo != null && fcInfo.updatable);
    }

    Object[] getActualIds(Object target) {
        if (idColumnNames.length == 0) {
            return null;
        }

        try {
            Object[] ids = new Object[idColumnNames.length];
            int i = 0;
            for (FieldColumnInfo fcInfo : idFieldColumnInfos) {
                ids[i++] = fcInfo.field.get(target);
            }
            return ids;
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    String getTableName()
    {
        return tableName;
    }

    public String getColumnNameForProperty(String propertyName) {
        for (FieldColumnInfo fcInfo : columnToField.values()) {
            if (fcInfo.field.getName().equalsIgnoreCase(propertyName)) {
                return fcInfo.columnName;
            }
        }

        return null;
    }

    private void readColumnInfo(ArrayList<FieldColumnInfo> idFcInfos) {
        idFieldColumnInfos = new FieldColumnInfo[idFcInfos.size()];
        idColumnNames = new String[idFcInfos.size()];
        int i = 0;
        int j = 0;
        for (FieldColumnInfo fcInfo : idFcInfos) {
            idColumnNames[i] = fcInfo.columnName;
            idFieldColumnInfos[i] = fcInfo;
            ++i;
        }

        columnNames = new String[columnToField.size()];
        columnTableNames = new String[columnNames.length];
        columnsSansIds = new String[columnNames.length - idColumnNames.length];
        i = 0;
        j = 0;
        for (Entry<String, FieldColumnInfo> entry : columnToField.entrySet()) {
            columnNames[i] = entry.getKey();
            columnTableNames[i] = entry.getValue().columnTableName;
            if (!idFcInfos.contains(entry.getValue())) {
                columnsSansIds[j] = entry.getKey();
                ++j;
            }
            ++i;
        }
    }

    private String readClob(Clob clob) throws IOException, SQLException {
        Reader reader = clob.getCharacterStream();
        try {
            StringBuilder sb = new StringBuilder();
            char[] cbuf = new char[1024];
            while (true) {
                int rc = reader.read(cbuf);
                if (rc == -1) {
                    break;
                }
                sb.append(cbuf, 0, rc);
            }
            return sb.toString();
        }
        finally {
            reader.close();
        }
    }

    private void processColumnAnnotation(FieldColumnInfo fcInfo) {
        Field field = fcInfo.field;

        Column columnAnnotation = field.getAnnotation(Column.class);

        if (columnAnnotation != null) {

            fcInfo.columnName = columnAnnotation.name().toLowerCase();
            String columnTableName = columnAnnotation.table();

            LOGGER.info("  -- field " + field.getName() + " has Column annotation: ["+columnTableName+ "."+ fcInfo.columnName+ "]");

            if (columnTableName != null && columnTableName.length() > 0) {
                fcInfo.columnTableName = columnTableName.toLowerCase();
            }

            fcInfo.insertable = columnAnnotation.insertable();
            fcInfo.updatable = columnAnnotation.updatable();
        }
        else {
            // If there is no Column annotation, is there a JoinColumn annotation?
            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
            if (joinColumnAnnotation != null) {
                // Is the JoinColumn a self-join?
                if (field.getType() == clazz) {
                    fcInfo.columnName = joinColumnAnnotation.name().toLowerCase();
                    selfJoinFCInfo = fcInfo;
                }
                else {
                    throw new RuntimeException("JoinColumn annotations can only be self-referencing: " + field.getType().getCanonicalName() + " != " + clazz.getCanonicalName());
                }
            }
            else {
                fcInfo.columnName = field.getName().toLowerCase();
            }
        }

        Transient transientAnnotation = field.getAnnotation(Transient.class);
        if (transientAnnotation == null) {
            columnToField.put(fcInfo.columnName, fcInfo);
        }
    }

    private static class FieldColumnInfo {
        private boolean updatable;
        private boolean insertable;
        private String columnName;
        private String columnTableName;
        private Field field;
        private Class<?> fieldType;
        private EnumType enumType;
        private Map<Object, Object> enumConstants;

        FieldColumnInfo(Field field) {
            this.field = field;
            this.fieldType = field.getType();

            // remap safe conversions
            if (fieldType == java.util.Date.class) {
                fieldType = Timestamp.class;
            }
            else if (fieldType == java.time.LocalDateTime.class) {
                fieldType = Timestamp.class;
            }

            else if (fieldType == int.class) {
                fieldType = Integer.class;
            }
            else if (fieldType == long.class) {
                fieldType = Long.class;
            }
        }

        <T extends Enum<?>> void setEnumConstants(EnumType type) {
            this.enumType = type;
            enumConstants = new HashMap<>();
            @SuppressWarnings("unchecked")
            T[] enums = (T[]) field.getType().getEnumConstants();
            for (T enumConst : enums) {
                Object key = (type == EnumType.ORDINAL ? enumConst.ordinal() : enumConst.name());
                enumConstants.put(key, enumConst);
            }
        }

        @Override
        public String toString() {
            return field.getName() + "->" + columnName;
        }
    }
}
