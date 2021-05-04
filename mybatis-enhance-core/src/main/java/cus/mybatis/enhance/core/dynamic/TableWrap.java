package cus.mybatis.enhance.core.dynamic;

public class TableWrap {

    private String className;

    private String tableName;

    public TableWrap(String className, String tableName) {
        this.className = className;
        this.tableName = tableName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
