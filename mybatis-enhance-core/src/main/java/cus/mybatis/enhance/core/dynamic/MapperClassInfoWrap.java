package cus.mybatis.enhance.core.dynamic;

import java.io.Serializable;
import java.util.List;

public class MapperClassInfoWrap implements Serializable {

    private TableWrap tableWrap;

    private PrimaryWrap primaryWrap;

    private List<FieldWrap> fieldWrapList;

    private Class entity;

    private Class mapper;

    public MapperClassInfoWrap(TableWrap tableWrap, PrimaryWrap primaryWrap, List<FieldWrap> fieldWrapList,Class entity,Class mapper) {
        this.tableWrap = tableWrap;
        this.primaryWrap = primaryWrap;
        this.fieldWrapList = fieldWrapList;
        this.entity = entity;
        this.mapper = mapper;
    }

    public TableWrap getTableWrap() {
        return tableWrap;
    }

    public void setTableWrap(TableWrap tableWrap) {
        this.tableWrap = tableWrap;
    }

    public PrimaryWrap getPrimaryWrap() {
        return primaryWrap;
    }

    public void setPrimaryWrap(PrimaryWrap primaryWrap) {
        this.primaryWrap = primaryWrap;
    }

    public List<FieldWrap> getFieldWrapList() {
        return fieldWrapList;
    }

    public void setFieldWrapList(List<FieldWrap> fieldWrapList) {
        this.fieldWrapList = fieldWrapList;
    }

    public Class getEntity() {
        return entity;
    }

    public void setEntity(Class entity) {
        this.entity = entity;
    }

    public Class getMapper() {
        return mapper;
    }

    public void setMapper(Class mapper) {
        this.mapper = mapper;
    }
}
