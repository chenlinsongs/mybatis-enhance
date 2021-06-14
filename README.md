1.5.2
1.删除template.xml文件，删除不必要的文件
2.删除分页接口，又具体服务实现，和mybatis-enhance没有关系
3.CommonService抽象为接口，不在是具体类，提供抽象实现AbstractService，具体接口可以继承CommonService
接口，具体实现可以继承AbstractService
