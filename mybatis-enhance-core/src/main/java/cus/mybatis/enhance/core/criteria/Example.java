package cus.mybatis.enhance.core.criteria;


import cus.mybatis.enhance.core.annotaion.Primary;
import cus.mybatis.enhance.core.lambda.IGetter;
import cus.mybatis.enhance.core.page.Page;
import cus.mybatis.enhance.core.utils.BeanUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cus.mybatis.enhance.core.page.Pageable;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class Example<T> implements Pageable<T> {
    Logger logger = LoggerFactory.getLogger(Example.class);

    Page<T> page;
    Object clazz;

    public Example(Class c)  {
        this();
        String name = c.getName();
        try {
            this.clazz = Class.forName(name).newInstance();
        } catch (InstantiationException e) {
            logger.error("",e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("",e);
        } catch (ClassNotFoundException e) {
            logger.error("",e);
        }
    }

    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> orderCriteria;

    private Example() {
        orderCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOrderCriteria() {
        return orderCriteria;
    }

    public void or(Criteria criteria) {
        orderCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        orderCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (orderCriteria.size() == 0) {
            orderCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        orderCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    public Page<T> getPage() {
        return page;
    }

    public Page<T> setPage(Page<T> page) {
        this.page = page;
        return page;
    }

    public class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    protected abstract class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIsNull(IGetter<T> fn) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " is null");
            return (Criteria) this;
        }

        public Criteria andIsNotNull(IGetter<T> fn) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " is not null");
            return (Criteria) this;
        }

        public Criteria andEqualTo(IGetter<T> fn, String value) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " =", value, property);
            return (Criteria) this;
        }

        public Criteria andNotEqualTo(IGetter<T> fn, String value) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " <>", value, property);
            return (Criteria) this;
        }

        public Criteria andGreaterThan(IGetter<T> fn, String value) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " >", value, property);
            return (Criteria) this;
        }

        public Criteria andGreaterThanOrEqualTo(IGetter<T> fn, String value) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " >=", value, property);
            return (Criteria) this;
        }

        public Criteria andLessThan(IGetter<T> fn, String value) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " <", value, property);
            return (Criteria) this;
        }

        public Criteria andLessThanOrEqualTo(IGetter<T> fn, String value) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " <=", value, property);
            return (Criteria) this;
        }

        /**
         * value = "%"+condition+"%"
         * */
        public Criteria andLike(IGetter<T> fn, String value) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " like", value, property);
            return (Criteria) this;
        }

        public Criteria andNotLike(IGetter<T> fn, String value) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " not like", value, property);
            return (Criteria) this;
        }

        public Criteria andIn(IGetter<T> fn, List<String> values) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " in", values, property);
            return (Criteria) this;
        }

        public Criteria andNotIn(IGetter<T> fn, List<String> values) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " not in", values, property);
            return (Criteria) this;
        }

        public Criteria andBetween(IGetter<T> fn, String value1, String value2) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " between", value1, value2, property);
            return (Criteria) this;
        }

        public Criteria andNotBetween(IGetter<T> fn, String value1, String value2) {
            String property = BeanUtils.convertToFieldName(fn);
            String name = getColumnName(property);
            addCriterion(name + " not between", value1, value2, property);
            return (Criteria) this;
        }

    }


    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }


    public String getColumnName(String property){
        Field field;
        field = FieldUtils.getField(clazz.getClass(),property,true);
        Column column = field.getAnnotation(Column.class);
        if (column != null){
            return column.name();
        }
        else {
            Primary primary = field.getAnnotation(Primary.class);
            return primary.name();
        }
    }

    public Object getClazz() {
        return  clazz;
    }

    public void setClazz(Object clazz) {
        this.clazz = clazz;
    }
}
