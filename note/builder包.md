父类**BaseBuilder**
```java
public abstract class BaseBuilder {
  //封装配置信息
  protected final Configuration configuration;

  //类型别名注册器
  protected final TypeAliasRegistry typeAliasRegistry;

  //类型处理注册器
  protected final TypeHandlerRegistry typeHandlerRegistry;
}
```

#### **XMLConfigBuilder**
**XMLCOnfigBuilder**主要属性：
```java
public class XMLConfigBuilder extends BaseBuilder {

  //是否解析完成
  private boolean parsed;

  //解析器
  private final XPathParser parser;

  //环境参数
  private String environment;

  //反射工厂
  private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();
}
```
有多个构造，都是如下私有构造的封装：
```java
  /**
   * @param parser      解析器
   * @param environment 环境参数
   * @param props       属性配置
   */
  private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
    //设置默认的别名注册器(typeAliasRegistry)和语言注册器(languageRegistry)
    super(new Configuration());
    ErrorContext.instance().resource("SQL Mapper Configuration");

    //设置参数
    this.configuration.setVariables(props);
    this.parsed = false;
    this.environment = environment;
    this.parser = parser;
  }
```
核心方法为**parse()**方法，代码如下
```java
 /**
   * 解析配置文件中的配置
   *
   * @return
   */
  public Configuration parse() {
    if (parsed) {
      throw new BuilderException("Each XMLConfigBuilder can only be used once.");
    }
    parsed = true;
    //解析配置，/configuration为配置在xml文件中的节点路径
    parseConfiguration(parser.evalNode("/configuration"));
    return configuration;
  }
/**
   * 解析配置的具体代码
   *
   * @param root 配置节点
   */
  private void parseConfiguration(XNode root) {
    try {
      //issue #117 read properties first
      propertiesElement(root.evalNode("properties"));
      Properties settings = settingsAsProperties(root.evalNode("settings"));
      loadCustomVfs(settings);
      loadCustomLogImpl(settings);
      typeAliasesElement(root.evalNode("typeAliases"));
      pluginElement(root.evalNode("plugins"));
      objectFactoryElement(root.evalNode("objectFactory"));
      objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
      reflectorFactoryElement(root.evalNode("reflectorFactory"));
      settingsElement(settings);
      // read it after objectFactory and objectWrapperFactory issue #631
      environmentsElement(root.evalNode("environments"));
      databaseIdProviderElement(root.evalNode("databaseIdProvider"));
      typeHandlerElement(root.evalNode("typeHandlers"));

      //解析mappers节点
      mapperElement(root.evalNode("mappers"));
    } catch (Exception e) {
      throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
  }

```
**mapperElenent(root.evalNode("mappers"))** 调用解析**mappers**节点，进而加载mapper文件:
```java
/**
   * 解析配置文件里的mappers节点
   *
   * @param parent
   * @throws Exception
   */
  private void mapperElement(XNode parent) throws Exception {
    if (parent != null) {
      for (XNode child : parent.getChildren()) {
        if ("package".equals(child.getName())) {
          String mapperPackage = child.getStringAttribute("name");
          configuration.addMappers(mapperPackage);
        } else {
          String resource = child.getStringAttribute("resource");
          String url = child.getStringAttribute("url");
          String mapperClass = child.getStringAttribute("class");

          //resource不为空，从resource加载
          if (resource != null && url == null && mapperClass == null) {
            ErrorContext.instance().resource(resource);
            InputStream inputStream = Resources.getResourceAsStream(resource);
            //构建mapper解析builder
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
            //解析mapper
            mapperParser.parse();
          } else if (resource == null && url != null && mapperClass == null) {
            //url配置不为空，从url加载
            ErrorContext.instance().resource(url);
            InputStream inputStream = Resources.getUrlAsStream(url);
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
            mapperParser.parse();
          } else if (resource == null && url == null && mapperClass != null) {
            //配置的mapperClass不为空，反射获得类实例添加mapper
            Class<?> mapperInterface = Resources.classForName(mapperClass);
            configuration.addMapper(mapperInterface);
          } else {
            throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
          }
        }
      }
    }
  }
```
对应的配置方式分别为
```xml
<configuration>
	<mappers>
		<mapper url=""/>
		<mapper resource=""/>
		<mapper class=""/>
	</mappers>
</configuration>
```
```xml
<configuration>
	<mappers>
		<package name = "com.XXX"/>
	</mappers>
</configuration>
```

#### **XMLConfigBuilder**