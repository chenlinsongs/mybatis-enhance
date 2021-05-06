package cus.mybatis.enhance.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = EnhanceMybatisProperties.MYBATIS_PREFIX)
public class EnhanceMybatisProperties {

    public static final String MYBATIS_PREFIX = "mybatis";

    /**
     * Location of Mapper class.
     */
    private String mapperPackages;

    public String getMapperPackages() {
        return mapperPackages;
    }

    public void setMapperPackages(String mapperPackages) {
        this.mapperPackages = mapperPackages;
    }
}
