package cus.mybatis.enhance.core.dynamic;


import cus.mybatis.enhance.core.annotaion.Primary;
import cus.mybatis.enhance.core.mapper.CommonMapper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.builder.BuilderException;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import cus.mybatis.enhance.core.annotaion.MyBatisDao;
import cus.mybatis.enhance.core.utils.OperateXMLByDOM;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


public class EnhanceSqlSessionFactoryBean extends SqlSessionFactoryBean {

    Logger logger = LoggerFactory.getLogger(EnhanceSqlSessionFactoryBean.class);

    private String searchLocation;

    private String commonMapperXmlPath;

    public EnhanceSqlSessionFactoryBean(String searchLocation, String commonMapperXmlPath) {
        this.searchLocation = searchLocation;
        this.commonMapperXmlPath = commonMapperXmlPath;
    }

    @Override
    public void setMapperLocations(Resource[] xmlMapperResource) {
        List<Resource> resource = getEntityMapperResource(xmlMapperResource);
        super.setMapperLocations(resource.toArray(new Resource[resource.size()]));
    }

    private List<Resource> getEntityMapperResource(Resource[] xmlMapperResource){
        Reflections reflections = new Reflections(searchLocation);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(MyBatisDao.class);
        List<Resource> resources = new ArrayList<>();
        if (annotated !=null && annotated.size() > 0){
            Iterator iterator = annotated.iterator();
            if (logger.isInfoEnabled())
                logger.info("Dao total:"+annotated.size());
            int i = 0;
            while (iterator.hasNext()){
                Class clazz = (Class) iterator.next();
                if (CommonMapper.class.isAssignableFrom(clazz)){
                    if (logger.isInfoEnabled())
                        logger.info("init dao:"+clazz.getName()+" "+(i++));
                    ResourceWrap resourceWrap = getNodeList(clazz.getName(),xmlMapperResource);
                    NodeList nodeList = null;
                    if (resourceWrap != null){
                        nodeList = resourceWrap.getNodeList();
                        xmlMapperResource[resourceWrap.getResourceIndex()] = null;
                    }
                    Resource resource = getMapperFromClass(clazz,nodeList);
                    resources.add(resource);
                }
            }
            for (int k = 0; k < xmlMapperResource.length;k++){
                if (xmlMapperResource[k] != null){
                    resources.add(xmlMapperResource[k]);
                }
            }
        }else {
            resources = Arrays.asList(xmlMapperResource);
        }
        return resources;
    }

    private Resource getMapper4Class(Class dao,NodeList nodeList){
        Resource resource = new ClassPathResource(commonMapperXmlPath);
        try {
            Type[] daoTypes = dao.getGenericInterfaces();
            Class entity = null;
            if (daoTypes != null && daoTypes.length > 0){
                for (Type type:daoTypes) {
                    if (type instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        //返回表示此类型实际类型参数的 Type 对象的数组
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments !=null && actualTypeArguments.length == 2){
                            entity = (Class) actualTypeArguments[1];
                        }else {
                            throw new RuntimeException("设置的泛型不对");
                        }
                    }
                }
            }else {
                throw new RuntimeException("没有找到抽象接口");
            }
            Document document = createDocument(new InputSource(resource.getInputStream()));
            updateNamespace(document,dao);
            updateSelect(document,entity);
            if (nodeList != null){
                insertNodeList(document,nodeList);
            }
            Table table = (Table) entity.getAnnotation(Table.class);
            String tableName = table.name();
            String xmlDoc = OperateXMLByDOM.doc2FormatString(document);

            String tableReplace = xmlDoc.replaceAll("\\$\\{table}",tableName);
            resource = new InputStreamResource(new ByteArrayInputStream(tableReplace.getBytes("UTF-8")),dao.getName());
        } catch (IOException e) {
            logger.error("",e);
        }
        return resource;
    }

    private Resource getMapperFromClass(Class mapper,NodeList nodeList){

        try {
            Type[] daoTypes = mapper.getGenericInterfaces();
            Class entity = null;
            if (daoTypes != null && daoTypes.length > 0){
                for (Type type:daoTypes) {
                    if (type instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        //返回表示此类型实际类型参数的 Type 对象的数组
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments !=null && actualTypeArguments.length == 2){
                            entity = (Class) actualTypeArguments[1];
                        }else {
                            throw new RuntimeException("设置的泛型不对");
                        }
                    }
                }
            }else {
                throw new RuntimeException("没有找到抽象接口");
            }
            MapperClassInfoWrap mapperClassInfoWrap = getMapperClassInfoWrap(mapper,entity);
            String xmlFileContent = createMapperXmlFile(mapperClassInfoWrap,nodeList);

           return new InputStreamResource(new ByteArrayInputStream(xmlFileContent.getBytes("UTF-8")),mapper.getName());
        } catch (IOException e) {
            logger.error("生成mapper xml文件失败",e);
            throw new RuntimeException(e);
        }
    }

    private MapperClassInfoWrap getMapperClassInfoWrap(Class mapper,Class entity){
        List<Field> fields =  FieldUtils.getAllFieldsList(entity);

        PrimaryWrap primaryWrap = null;
        List<FieldWrap> fieldWrapList = new ArrayList();
        for (Field field:fields){
            String columnName = null;
            Class<? extends Annotation> annotationCls = null;
            String property = null;
            if (field.getAnnotation(Column.class) != null){
                Column column = field.getAnnotation(Column.class);
                columnName = column.name();
                property = field.getName();
                annotationCls = Column.class;
            }if (field.getAnnotation(Primary.class) != null){
                Primary column = field.getAnnotation(Primary.class);
                columnName = column.name();
                property = field.getName();
                primaryWrap = new PrimaryWrap(property,columnName);
                annotationCls = Primary.class;
            }

            FieldWrap wrap = new FieldWrap(property,columnName,annotationCls);
            fieldWrapList.add(wrap);
        }

        Table table = (Table) entity.getAnnotation(Table.class);
        String classSimpleName = table.getClass().getSimpleName();
        TableWrap tableWrap = new TableWrap(classSimpleName,table.name());

        MapperClassInfoWrap classInfoWrap = new MapperClassInfoWrap(tableWrap,primaryWrap,fieldWrapList,entity,mapper);
        return classInfoWrap;
    }


    private String createMapperXmlFile(MapperClassInfoWrap mapperClassInfoWrap,NodeList nodeList){
        Class mapper = mapperClassInfoWrap.getMapper();
        String space = "  ";
        String twoNewLine = "\r\n\n";
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        builder.append("\r\n");
        builder.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
        builder.append("\r\n");
        builder.append("<mapper namespace=\""+mapper.getName()+"\">");
        builder.append("\r\n");
        builder.append(twoNewLine);
        builder.append(updateByExampleWhereClause());

        builder.append(twoNewLine);
        builder.append(exampleWhereClause());

        //1.insert
        builder.append(twoNewLine);
        builder.append(createInsertSql(mapperClassInfoWrap,space));

        //2.insertSelective
        builder.append(twoNewLine);
        builder.append(createInsertSelectiveSql(mapperClassInfoWrap,space));

        //3.updateByExample
        builder.append(twoNewLine);
        builder.append(getUpdateByExampleSql(mapperClassInfoWrap));

        //4.updateByExampleSelective
        builder.append(twoNewLine);
        builder.append(getUpdateByExampleSelectiveSql(mapperClassInfoWrap));

        //5.updateByPrimaryKey
        builder.append(twoNewLine);
        builder.append(getUpdateByPrimaryKeySql(mapperClassInfoWrap));

        //6.updateByPrimarySelective
        builder.append(twoNewLine);
        builder.append(getUpdateByPrimarySelectiveSql(mapperClassInfoWrap));

        //7.countByExample
        builder.append(twoNewLine);
        builder.append(getCountByExampleSql(mapperClassInfoWrap));

        //8.deleteByExample
        builder.append(twoNewLine);
        builder.append(getDeleteByExampleSql(mapperClassInfoWrap));

        //9.deleteByPrimaryKey
        builder.append(twoNewLine);
        builder.append(getDeleteByPrimaryKeySql(mapperClassInfoWrap));

        //10.selectByExample
        builder.append(twoNewLine);
        builder.append(getSelectByExampleSql(mapperClassInfoWrap));

        //11.selectByPrimaryKey
        builder.append(twoNewLine);
        builder.append(getSelectByPrimaryKeySql(mapperClassInfoWrap));

        builder.append(twoNewLine);
        if (nodeList != null){
            String customSql = nodeListToString(nodeList);
            if (logger.isDebugEnabled()){
                logger.debug("mapper:{},customSql:{}",mapperClassInfoWrap.getMapper().getName(),customSql);
            }
            builder.append(customSql);
            builder.append(twoNewLine);
        }

        builder.append("</mapper>");
        logger.info("{}",builder.toString());
        return builder.toString();
    }

    private String exampleWhereClause(){
        return  "    <sql id=\"Example_Where_Clause\">\n" +
                "        <where>\n" +
                "            <foreach collection=\"orderCriteria\" item=\"criteria\" separator=\"or\">\n" +
                "                <if test=\"criteria.valid\">\n" +
                "                    <trim prefix=\"(\" prefixOverrides=\"and\" suffix=\")\">\n" +
                "                        <foreach collection=\"criteria.criteria\" item=\"criterion\">\n" +
                "                            <choose>\n" +
                "                                <when test=\"criterion.noValue\">\n" +
                "                                    and ${criterion.condition}\n" +
                "                                </when>\n" +
                "                                <when test=\"criterion.singleValue\">\n" +
                "                                    and ${criterion.condition} #{criterion.value}\n" +
                "                                </when>\n" +
                "                                <when test=\"criterion.betweenValue\">\n" +
                "                                    and ${criterion.condition} #{criterion.value} and #{criterion.secondValue}\n" +
                "                                </when>\n" +
                "                                <when test=\"criterion.listValue\">\n" +
                "                                    and ${criterion.condition}\n" +
                "                                    <foreach close=\")\" collection=\"criterion.value\" item=\"listItem\" open=\"(\" separator=\",\">\n" +
                "                                        #{listItem}\n" +
                "                                    </foreach>\n" +
                "                                </when>\n" +
                "                            </choose>\n" +
                "                        </foreach>\n" +
                "                    </trim>\n" +
                "                </if>\n" +
                "            </foreach>\n" +
                "        </where>\n" +
                "    </sql>";
    }

    private String updateByExampleWhereClause(){
        return  "  <sql id=\"Update_By_Example_Where_Clause\">\n" +
                "      <where>\n" +
                "          <foreach collection=\"example.orderCriteria\" item=\"criteria\" separator=\"or\">\n" +
                "              <if test=\"criteria.valid\">\n" +
                "                  <trim prefix=\"(\" prefixOverrides=\"and\" suffix=\")\">\n" +
                "                      <foreach collection=\"criteria.criteria\" item=\"criterion\">\n" +
                "                          <choose>\n" +
                "                              <when test=\"criterion.noValue\">\n" +
                "                                  and ${criterion.condition}\n" +
                "                              </when>\n" +
                "                              <when test=\"criterion.singleValue\">\n" +
                "                                  and ${criterion.condition} #{criterion.value}\n" +
                "                              </when>\n" +
                "                              <when test=\"criterion.betweenValue\">\n" +
                "                                  and ${criterion.condition} #{criterion.value} and #{criterion.secondValue}\n" +
                "                              </when>\n" +
                "                              <when test=\"criterion.listValue\">\n" +
                "                                  and ${criterion.condition}\n" +
                "                                  <foreach close=\")\" collection=\"criterion.value\" item=\"listItem\" open=\"(\" separator=\",\">\n" +
                "                                      #{listItem}\n" +
                "                                  </foreach>\n" +
                "                              </when>\n" +
                "                          </choose>\n" +
                "                      </foreach>\n" +
                "                  </trim>\n" +
                "              </if>\n" +
                "          </foreach>\n" +
                "      </where>\n" +
                "  </sql>";
    }

    /**
     * createInsertSql
     * */
    private String createInsertSql(MapperClassInfoWrap mapperClassInfoWrap,String margin){
        PrimaryWrap primaryWrap = mapperClassInfoWrap.getPrimaryWrap();
        Class entity = mapperClassInfoWrap.getEntity();
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        List<FieldWrap> fieldWrapList = mapperClassInfoWrap.getFieldWrapList();

        String newLine = "\r\n";
        String oneSpace = " ";
        String towSpace = "  ";
        StringBuilder builder = new StringBuilder();
        builder.append(margin+"<insert");
        builder.append(oneSpace+"id=\"insert\"");
        builder.append(oneSpace+"keyColumn=\""+primaryWrap.getColumn()+"\"");
        builder.append(oneSpace+"keyProperty=\""+primaryWrap.getProperty()+"\"");
        builder.append(oneSpace+"parameterType=\""+entity.getName()+"\"");
        builder.append(oneSpace+"useGeneratedKeys=\"true\"");
        builder.append(">");

        builder.append(newLine);

        String childMargin = margin+towSpace;

        builder.append(childMargin+"insert into "+tableWrap.getTableName()+" (");
        builder.append(newLine);
        for (int i = 0;i < fieldWrapList.size();i++){
            FieldWrap fieldWrap = fieldWrapList.get(i);
            builder.append(childMargin);
            builder.append("`"+fieldWrap.getColumn()+"`");
            if (i < fieldWrapList.size()-1){
                builder.append(",");
            }
            builder.append(newLine);
        }
        builder.append(childMargin);
        builder.append(")");
        builder.append(newLine);

        builder.append(childMargin);
        builder.append("VALUES");
        builder.append(newLine);

        builder.append(childMargin);
        builder.append("(");
        builder.append(newLine);

        for (int i = 0;i < fieldWrapList.size();i++){
            FieldWrap fieldWrap = fieldWrapList.get(i);
            builder.append(childMargin);
            builder.append("#{"+fieldWrap.getProperty()+"}");
            if (i < fieldWrapList.size()-1){
                builder.append(",");
            }
            builder.append(newLine);
        }
        builder.append(childMargin);
        builder.append(")");
        builder.append(newLine);

        builder.append(margin);
        builder.append("</insert>");
        logger.info("{}",builder.toString());
        return builder.toString();
    }

    /**
     * createInsertSelectiveSql
     * */
    private String createInsertSelectiveSql(MapperClassInfoWrap mapperClassInfoWrap,String margin){
        PrimaryWrap primaryWrap = mapperClassInfoWrap.getPrimaryWrap();
        Class entity = mapperClassInfoWrap.getEntity();
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        List<FieldWrap> fieldWrapList = mapperClassInfoWrap.getFieldWrapList();

        String insertSelectiveSqlTemplate =
                "  <insert id=\"insertSelective\" keyColumn=\"%s\" keyProperty=\"%s\" parameterType=\"%s\" useGeneratedKeys=\"true\">\n" +
                "    insert into %s \n" +
                "    <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n"+
                "%s" +
                "    </trim>\n" +
                "    <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n" +
                "%s" +
                "    </trim>\n" +
                "  </insert>\n";

        String keyColumn = primaryWrap.getColumn();
        String keyProperty = primaryWrap.getProperty();
        String parameterType = entity.getName();
        String tableName = tableWrap.getTableName();

        String insertSelectiveColumns = getSelectiveColumns(fieldWrapList,false);
        String insertSelectiveValues = getSelectiveColumns(fieldWrapList,true);

        String insertSelectiveSql =  String.format(insertSelectiveSqlTemplate,
                keyColumn,
                keyProperty,
                parameterType,
                tableName,
                insertSelectiveColumns,
                insertSelectiveValues
                );

        return insertSelectiveSql;
    }


    private String getSelectiveColumns(List<FieldWrap> fieldWrapList,boolean isValue){
        String newLine = "\r\n";
        String fourSpace = "    ";
        String sixSpace = "      ";
        StringBuilder insertSelectiveColumns = new StringBuilder();
        for (int i = 0;i < fieldWrapList.size();i++){
            FieldWrap fieldWrap = fieldWrapList.get(i);
            insertSelectiveColumns.append(fourSpace);
            insertSelectiveColumns.append("<if test=\""+fieldWrap.getProperty()+" != null\">");
            insertSelectiveColumns.append(newLine);
            insertSelectiveColumns.append(sixSpace);
            if (isValue){
                insertSelectiveColumns.append("#{"+fieldWrap.getProperty()+"}");
            }else {
                insertSelectiveColumns.append(fieldWrap.getColumn());
            }
            insertSelectiveColumns.append(",");
            insertSelectiveColumns.append(newLine);
            insertSelectiveColumns.append(fourSpace);
            insertSelectiveColumns.append("</if>");
            insertSelectiveColumns.append(newLine);
        }
        return insertSelectiveColumns.toString();
    }

    /**
     * updateByExampleSelective
     * */
    private String getUpdateByExampleSelectiveSql(MapperClassInfoWrap mapperClassInfoWrap){
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        List<FieldWrap> fieldWrapList = mapperClassInfoWrap.getFieldWrapList();

        String updateSelectiveSqlTemplate =
                "  <update id=\"updateByExampleSelective\" parameterType=\"map\">\n" +
                "    update %s\n" +
                "    <set>\n" +
                "%s"+
                "    </set>\n" +
                "    <if test=\"example != null\">\n" +
                "      <include refid=\"Update_By_Example_Where_Clause\" />\n" +
                "    </if>\n" +
                "  </update>";

        String tableName = tableWrap.getTableName();
        String updateSelectiveColumnAndValues = getUpdateSelectiveColumnAndValues(fieldWrapList);
        return  String.format(updateSelectiveSqlTemplate,tableName,updateSelectiveColumnAndValues);

    }

    private String getUpdateSelectiveColumnAndValues(List<FieldWrap> fieldWrapList){
        String newLine = "\r\n";
        String fourSpace = "    ";
        String sixSpace = "      ";
        StringBuilder sqlFragment = new StringBuilder();
        for (int i = 0;i < fieldWrapList.size();i++){
            FieldWrap fieldWrap = fieldWrapList.get(i);
            sqlFragment.append(fourSpace);
            sqlFragment.append("<if test=\"record."+fieldWrap.getProperty()+" != null\">");
            sqlFragment.append(newLine);
            sqlFragment.append(sixSpace);
            sqlFragment.append(fieldWrap.getColumn()+" = #{record."+fieldWrap.getProperty()+"}");
            sqlFragment.append(",");
            sqlFragment.append(newLine);
            sqlFragment.append(fourSpace);
            sqlFragment.append("</if>");
            sqlFragment.append(newLine);
        }
        return sqlFragment.toString();
    }

    /**
     * countByExample
     * */
    private String getCountByExampleSql(MapperClassInfoWrap mapperClassInfoWrap){
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        String countByExampleSqlTemplate =
                "    <select id=\"countByExample\" parameterType=\"map\" resultType=\"java.lang.Long\">\n" +
                "        select count(*) from %s\n" +
                "        <if test=\"_parameter != null\">\n" +
                "            <include refid=\"Example_Where_Clause\" />\n" +
                "        </if>\n" +
                "    </select>";

        return  String.format(countByExampleSqlTemplate, tableWrap.getTableName());
    }

    /**
     * deleteByPrimaryKey
     * */
    private String getDeleteByPrimaryKeySql(MapperClassInfoWrap mapperClassInfoWrap){
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        PrimaryWrap primaryWrap = mapperClassInfoWrap.getPrimaryWrap();
        String deleteByPrimaryKeySqlTemplate =
                "    <delete id=\"deleteByPrimaryKey\" >\n" +
                "        delete from %s\n" +
                "        where %s = #{primaryValue}\n" +
                "    </delete>";

        return  String.format(deleteByPrimaryKeySqlTemplate,tableWrap.getTableName(),primaryWrap.getColumn());
    }

    /**
     * deleteByExample
     * */
    private String getDeleteByExampleSql(MapperClassInfoWrap mapperClassInfoWrap){
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        String deleteByExampleSqlTemplate =
                "    <delete id=\"deleteByExample\" parameterType=\"map\">\n" +
                "        delete from  %s\n" +
                "        <if test=\"_parameter != null\">\n" +
                "            <include refid=\"Example_Where_Clause\" />\n" +
                "        </if>\n" +
                "    </delete>";

        return  String.format(deleteByExampleSqlTemplate,tableWrap.getTableName());
    }

    /**
     * selectByExample
     * */
    private String getSelectByExampleSql(MapperClassInfoWrap mapperClassInfoWrap){
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        Class entity = mapperClassInfoWrap.getEntity();
        String selectByExampleSql =
                "    <select id=\"selectByExample\" parameterType=\"map\" resultType=\"%s\">\n" +
                "        select * from %s\n" +
                "        <if test=\"_parameter != null\">\n" +
                "            <include refid=\"Example_Where_Clause\" />\n" +
                "        </if>\n" +
                "        <if test=\"orderByClause != null\">\n" +
                "            order by ${orderByClause}\n" +
                "        </if>\n" +
                "    </select>";

        return  String.format(selectByExampleSql,entity.getName(),tableWrap.getTableName());
    }

    /**
     * selectByPrimaryKey
     * */
    private String getSelectByPrimaryKeySql(MapperClassInfoWrap mapperClassInfoWrap){
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        Class entity = mapperClassInfoWrap.getEntity();
        PrimaryWrap primaryWrap = mapperClassInfoWrap.getPrimaryWrap();
        String sql =
                "    <select id=\"selectByPrimaryKey\"  resultType=\"%s\">\n" +
                "        select * from %s\n" +
                "        where %s = #{primaryValue}\n" +
                "    </select>";
        return  String.format(sql,entity.getName(),tableWrap.getTableName(),primaryWrap.getColumn());
    }

    /**
     * updateByExample
     * */
    private String getUpdateByExampleSql(MapperClassInfoWrap mapperClassInfoWrap){
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        List<FieldWrap> fieldWrapList = mapperClassInfoWrap.getFieldWrapList();
        String sql =
                "    <update id=\"updateByExample\" parameterType=\"map\">\n" +
                "        update %s\n" +
                "        <set>\n" +
                "%s"+
                "        </set>\n" +
                "        <if test=\"example != null\">\n" +
                "            <include refid=\"Update_By_Example_Where_Clause\" />\n" +
                "        </if>\n" +
                "    </update>";
        String sqlFragment = getUpdateColumns(fieldWrapList);
        return String.format(sql,tableWrap.getTableName(),sqlFragment);
    }

    private String getUpdateColumns(List<FieldWrap> fieldWrapList){
        String newLine = "\r\n";
        String elevenSpace = "           ";
        StringBuilder sqlFragment = new StringBuilder();
        for (int i = 0;i < fieldWrapList.size();i++){
            FieldWrap fieldWrap = fieldWrapList.get(i);
            sqlFragment.append(elevenSpace);
            sqlFragment.append(fieldWrap.getColumn()+" = #{record."+fieldWrap.getProperty()+"}");
            sqlFragment.append(",");
            sqlFragment.append(newLine);
        }
        return sqlFragment.toString();
    }

    /**
     * updateByPrimarySelectiveSql
     * */
    private String getUpdateByPrimarySelectiveSql(MapperClassInfoWrap mapperClassInfoWrap){
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        PrimaryWrap primaryWrap = mapperClassInfoWrap.getPrimaryWrap();
        List<FieldWrap> fieldWrapList = mapperClassInfoWrap.getFieldWrapList();
        String sql =
                "    <update id=\"updateByPrimaryKeySelective\" parameterType=\"map\">\n" +
                "        update %s\n" +
                "        <set>\n" +
                "%s"+
                "        </set>\n" +
                "        where %s = #{%s}\n" +
                "    </update>";

        String sqlFragment = getUpdateByPrimarySelectiveColumnAndValues(fieldWrapList);
        return String.format(sql,tableWrap.getTableName(),sqlFragment,primaryWrap.getColumn(),primaryWrap.getProperty());
    }


    private String getUpdateByPrimarySelectiveColumnAndValues(List<FieldWrap> fieldWrapList){
        String newLine = "\r\n";
        String fourSpace = "         ";
        String sixSpace = "           ";
        StringBuilder sqlFragment = new StringBuilder();
        for (int i = 0;i < fieldWrapList.size();i++){
            FieldWrap fieldWrap = fieldWrapList.get(i);
            //不包含主键
            if (Primary.class.isAssignableFrom(fieldWrap.getAnnotationCls())){
                continue;
            }
            sqlFragment.append(fourSpace);
            sqlFragment.append("<if test=\""+fieldWrap.getProperty()+" != null\">");
            sqlFragment.append(newLine);
            sqlFragment.append(sixSpace);
            sqlFragment.append(fieldWrap.getColumn()+" = #{"+fieldWrap.getProperty()+"}");
            sqlFragment.append(",");
            sqlFragment.append(newLine);
            sqlFragment.append(fourSpace);
            sqlFragment.append("</if>");
            sqlFragment.append(newLine);
        }
        return sqlFragment.toString();
    }

    /**
     * updateByPrimaryKey
     * */
    private String getUpdateByPrimaryKeySql(MapperClassInfoWrap mapperClassInfoWrap){
        TableWrap tableWrap = mapperClassInfoWrap.getTableWrap();
        PrimaryWrap primaryWrap = mapperClassInfoWrap.getPrimaryWrap();
        String sql =
                "    <update id=\"updateByPrimaryKey\" parameterType=\"map\">\n" +
                "        update %s\n" +
                "        <set>\n" +
                "%s"+
                "        </set>\n" +
                "        where %s = #{%s}\n" +
                "    </update>";
        String sqlFragment = getUpdateByPrimaryColumns(mapperClassInfoWrap.getFieldWrapList());
        return String.format(sql,tableWrap.getTableName(),sqlFragment,primaryWrap.getColumn(),primaryWrap.getProperty());
    }

    private String getUpdateByPrimaryColumns(List<FieldWrap> fieldWrapList){
        String newLine = "\r\n";
        String elevenSpace = "           ";
        StringBuilder sqlFragment = new StringBuilder();
        for (int i = 0;i < fieldWrapList.size();i++){
            FieldWrap fieldWrap = fieldWrapList.get(i);
            //不包含主键
            if (Primary.class.isAssignableFrom(fieldWrap.getAnnotationCls())){
                continue;
            }
            sqlFragment.append(elevenSpace);
            sqlFragment.append(fieldWrap.getColumn()+" = #{"+fieldWrap.getProperty()+"}");
            sqlFragment.append(",");
            sqlFragment.append(newLine);
        }
        return sqlFragment.toString();
    }

    private ResourceWrap getNodeList(String namespace, Resource[] xmlMapperResource){

        for (int i = 0;i < xmlMapperResource.length; i++){
            Resource resource = xmlMapperResource[i];
            if (resource != null){
                try {
                    Document document = createDocument(new InputSource(resource.getInputStream()));
                    NodeList mapperNode = document.getElementsByTagName("mapper");
                    NamedNodeMap arrMap = mapperNode.item(0).getAttributes();
                    if (namespace.equals(arrMap.getNamedItem("namespace").getNodeValue())){
                        if (mapperNode.item(0).hasChildNodes()){
                            return new ResourceWrap(mapperNode.item(0).getChildNodes(),resource,i);
                        }else {
                            //xml文件中并没有sql语句的情况
                            return new ResourceWrap(null,resource,i);
                        }
                    }
                } catch (IOException e) {
                    logger.error("",e);
                }
            }
        }
        return null;
    }

    private Document createDocument(InputSource inputSource) {
        // important: this must only be called AFTER common constructor
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(false);
            factory.setCoalescing(false);
            factory.setExpandEntityReferences(true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void warning(SAXParseException exception) throws SAXException {
                }
            });
            return builder.parse(inputSource);
        } catch (Exception e) {
            throw new BuilderException("Error creating document instance.  Cause: " + e, e);
        }
    }

    private void updateNamespace(Document doc,Class clazz){
        NodeList mapper = doc.getElementsByTagName("mapper");
        Element namespace = (Element) mapper.item(0);
        namespace.setAttribute("namespace",clazz.getName());
    }

    private void updateSelect(Document doc,Class clazz) {
        NodeList employees = doc.getElementsByTagName("select");
        Element emp;
        //loop for each employee
        for(int i=0; i<employees.getLength();i++){
            emp = (Element) employees.item(i);
            String resultType =emp.getAttribute("resultType");
            if ("entity".equals(resultType)){
                emp.setAttribute("resultType",clazz.getName());
            }
        }
    }

    private void insertNodeList(Document doc,NodeList nodeList) {
        NodeList mapper = doc.getElementsByTagName("mapper");
        Element sqlContent = (Element) mapper.item(0);
        adoptNode(doc,sqlContent,nodeList);
    }

    private void adoptNode(Document doc, Node parent,NodeList nodeList){
        if (nodeList == null){
            return;
        }
        for (int i = 0;i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            Node cloneNode = node.cloneNode(true);
            Node adoptNode =  doc.adoptNode(cloneNode);
            parent.appendChild(adoptNode);
        }
    }

    /**
     * 将node转为string
     * */
    private String nodeListToString(NodeList nodeList) {

        if (nodeList == null){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0;i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            NodeList childList = node.getChildNodes();
            String value = nodeToString(node,true);
            stringBuilder.append(value);
            nodeListToString(childList);
        }
        return stringBuilder.toString();
    }

    private String nodeToString(Node node,boolean omitXMLDeclaration){
        if (node == null){
            return "";
        }

        final StringWriter writer = new StringWriter();
        try {
            final Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXMLDeclaration ? "yes" : "no");
            t.setOutputProperty(OutputKeys.INDENT, "yes"); // indent to show results in a more human readable format
            t.transform(new DOMSource(node), new StreamResult(writer));
        } catch(final TransformerException e) {
            logger.error("",e);
        }
        return writer.toString();
    }
}
