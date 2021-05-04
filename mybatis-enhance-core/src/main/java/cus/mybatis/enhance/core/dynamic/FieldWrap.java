package cus.mybatis.enhance.core.dynamic;

import java.io.Serializable;
import java.lang.annotation.Annotation;

public class FieldWrap implements Serializable {

    private String property;

    private String column;

    private Class<? extends Annotation> annotationCls;

    public FieldWrap(String property, String column,Class<? extends Annotation> annotationCls) {
        this.property = property;
        this.column = column;
        this.annotationCls = annotationCls;
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

    public Class<? extends Annotation> getAnnotationCls() {
        return annotationCls;
    }

    public void setAnnotationCls(Class<? extends Annotation> annotationCls) {
        this.annotationCls = annotationCls;
    }
}
