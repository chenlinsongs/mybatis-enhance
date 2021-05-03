package cus.mybatis.enhance.core.dynamic;


import cus.mybatis.enhance.core.mapper.CommonMapper;
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

import javax.persistence.Table;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
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
                    if (reflections != null){
                        xmlMapperResource[resourceWrap.getResourceIndex()] = null;
                    }
                    Resource resource = getMapper4Class(clazz,resourceWrap.getNodeList());
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
    private String nodeListToString(NodeList nodeList) throws TransformerException {

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
