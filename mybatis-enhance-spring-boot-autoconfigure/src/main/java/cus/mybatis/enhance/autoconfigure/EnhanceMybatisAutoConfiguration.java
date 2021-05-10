package cus.mybatis.enhance.autoconfigure;

import cus.mybatis.enhance.core.dynamic.EnhanceSqlSessionFactoryBean;
import cus.mybatis.enhance.core.mapper.CommonMapper;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScannerRegistrar;
import org.mybatis.spring.boot.autoconfigure.*;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.springframework.context.annotation.Configuration
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties({MybatisProperties.class, EnhanceMybatisProperties.class})
@AutoConfigureAfter({ DataSourceAutoConfiguration.class, MybatisLanguageDriverAutoConfiguration.class, MapperScannerConfigurer.class })
@AutoConfigureBefore({MybatisAutoConfiguration.class})
public class EnhanceMybatisAutoConfiguration implements InitializingBean, ApplicationContextAware {

    Logger logger = LoggerFactory.getLogger(EnhanceMybatisAutoConfiguration.class);

    private final MybatisProperties properties;

    private final Interceptor[] interceptors;

    private final TypeHandler[] typeHandlers;

    private final LanguageDriver[] languageDrivers;

    private final ResourceLoader resourceLoader;

    private final DatabaseIdProvider databaseIdProvider;

    private final List<ConfigurationCustomizer> configurationCustomizers;

    ApplicationContext applicationContext;

    public EnhanceMybatisAutoConfiguration(MybatisProperties properties,
                                           ObjectProvider<Interceptor[]> interceptorsProvider, ObjectProvider<TypeHandler[]> typeHandlersProvider,
                                           ObjectProvider<LanguageDriver[]> languageDriversProvider, ResourceLoader resourceLoader,
                                           ObjectProvider<DatabaseIdProvider> databaseIdProvider, ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
        this.properties = properties;
        this.interceptors = interceptorsProvider.getIfAvailable();
        this.typeHandlers = typeHandlersProvider.getIfAvailable();
        this.languageDrivers = languageDriversProvider.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.databaseIdProvider = databaseIdProvider.getIfAvailable();
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
    }


    @Override
    public void afterPropertiesSet() {
        checkConfigFileExists();
    }

    private void checkConfigFileExists() {
        if (this.properties.isCheckConfigLocation() && org.springframework.util.StringUtils.hasText(this.properties.getConfigLocation())) {
            Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
            Assert.state(resource.exists(),
                    "Cannot find config location: " + resource + " (please add config file or check your Mybatis configuration)");
        }
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {

        SqlSessionFactoryBean factory = new EnhanceSqlSessionFactoryBean(getMapperClass());
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (org.springframework.util.StringUtils.hasText(this.properties.getConfigLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
        }
        applyConfiguration(factory);
        if (this.properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(this.properties.getConfigurationProperties());
        }
        if (!ObjectUtils.isEmpty(this.interceptors)) {
            factory.setPlugins(this.interceptors);
        }
        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }
        if (org.springframework.util.StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }
        if (this.properties.getTypeAliasesSuperType() != null) {
            factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
        }
        if (org.springframework.util.StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(this.typeHandlers)) {
            factory.setTypeHandlers(this.typeHandlers);
        }
        //mybatis-enhance可以完全不需要xml文件
        Resource[] resources = this.properties.resolveMapperLocations();
        factory.setMapperLocations(resources!=null?resources:new Resource[]{});

        Set<String> factoryPropertyNames = Stream
                .of(new BeanWrapperImpl(SqlSessionFactoryBean.class).getPropertyDescriptors()).map(PropertyDescriptor::getName)
                .collect(Collectors.toSet());
        Class<? extends LanguageDriver> defaultLanguageDriver = this.properties.getDefaultScriptingLanguageDriver();
        if (factoryPropertyNames.contains("scriptingLanguageDrivers") && !ObjectUtils.isEmpty(this.languageDrivers)) {
            // Need to mybatis-spring 2.0.2+
            factory.setScriptingLanguageDrivers(this.languageDrivers);
            if (defaultLanguageDriver == null && this.languageDrivers.length == 1) {
                defaultLanguageDriver = this.languageDrivers[0].getClass();
            }
        }
        if (factoryPropertyNames.contains("defaultScriptingLanguageDriver")) {
            // Need to mybatis-spring 2.0.2+
            factory.setDefaultScriptingLanguageDriver(defaultLanguageDriver);
        }

        return factory.getObject();
    }

    private void applyConfiguration(SqlSessionFactoryBean factory) {
        Configuration configuration = this.properties.getConfiguration();
        if (configuration == null && !org.springframework.util.StringUtils.hasText(this.properties.getConfigLocation())) {
            configuration = new Configuration();
        }
        if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
            for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                customizer.customize(configuration);
            }
        }
        factory.setConfiguration(configuration);
    }

    private Set<Class> getMapperClass() throws ClassNotFoundException {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        Set<Class> mapperClass = new HashSet<>();
        for(String beanName : allBeanNames) {
            GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
            BeanDefinition beanDefinition = genericApplicationContext.getBeanDefinition(beanName);
            if (beanDefinition instanceof GenericBeanDefinition){
                GenericBeanDefinition genericBeanDefinition = (GenericBeanDefinition) beanDefinition;
                if ("org.mybatis.spring.mapper.MapperFactoryBean".equals(genericBeanDefinition.getBeanClassName())){
                    String mapperClassName = (String) genericBeanDefinition.getConstructorArgumentValues().getGenericArgumentValues().get(0).getValue();
                    mapperClass.add(Class.forName(mapperClassName));
                }
            }
        }
        return mapperClass;
    }




    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
       this.applicationContext = applicationContext;
    }
}
