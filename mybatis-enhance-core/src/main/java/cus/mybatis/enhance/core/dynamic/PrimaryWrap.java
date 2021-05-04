package cus.mybatis.enhance.core.dynamic;


public class PrimaryWrap {

    private String property;

    private String column;

    public PrimaryWrap(String property, String column) {
        this.property = property;
        this.column = column;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}
