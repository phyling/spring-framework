# 	AnnotationConfigApplicationContext#

# refresh()的14个方法 

​	1）按照顺序执行

​	2）第12个，第13个方法只有异常时才执行

​	3）初始化完毕，最后清空缓存的数据，最后一个方法一定会执行 

## 1.prepareRefresh()

```java
// Prepare this context for refreshing.
////准备工作包括设置启动时间，是否激活标识位，
// 初始化属性源(property source)配置
prepareRefresh();
```

## 2.prepareBeanFactory

```java
// Tell the subclass to refresh the internal bean factory.
//返回一个factory 为什么需要返回一个工厂
//因为要对工厂进行初始化
ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

// Prepare the bean factory for use in this context.
//准备工厂
prepareBeanFactory(beanFactory);
```

## 3.postProcessBeanFactory

```java
//这个方法在当前版本的spring是没用任何代码的
//可能spring期待在后面的版本中去扩展吧
//当然其子类是有实现的
postProcessBeanFactory(beanFactory);
```

## 4.**invokeBeanFactoryPostProcessors**(重要)

```java
// Invoke factory processors registered as beans in the context.
//在spring的环境中去执行已经被注册的 factory processors
//设置执行自定义的ProcessBeanFactory 和spring内部自己定义的
invokeBeanFactoryPostProcessors(beanFactory);
```

重要展开说：

​	这个方法是执行用户自定义的和spring内部定义的BeanDefinitionRegistryPostProcessor和BeanFactoryPostProcessor的实现类

### *AbstractApplicationContext#invokeBeanFactoryPostProcessors*

```java
/**
 * 实例化并调用所有已注册的BeanFactoryPostProcessor bean，实际上我们已经在实例化注册器
 * AnnotationConfigApplicationContext实例的时候已经注册到DefaultListableBeanFactory 当中的beanDefinitionMap中
 * 这里拿出来进行解析
 * 必须在单例实例化之前调用。
 */
protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
   //这个地方需要注意getBeanFactoryPostProcessors()是获取手动给spring的BeanFactoryPostProcessor
   //自定义并不仅仅是程序员自己写的
   //自己写的可以加component也可以不加
   //如果加了getBeanFactoryPostProcessors()这个是spring内部自己扫描的BeanDefinitionRegistryPostProcessor
   //为什么得不到getBeanFactoryPostProcessors（）这个方法是直接获取一个list，
   //这个list是在AnnotationConfigApplicationContext被定义
   //所谓的自定义的就是你手动调用AnnotationConfigApplicationContext.addBeanFactoryPostProcessor();
   PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

   // Detect a LoadTimeWeaver and prepare for weaving, if found in the meantime
   // (e.g. through an @Bean method registered by ConfigurationClassPostProcessor)
   if (beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
      beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
      beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
   }
}
```

#### *PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors*

​	1.会执行自定义的BeanFactoryPostProcessor，它包含2种分别是

​		1）BeanDefinitionRegistryPostProcessor

​		2 ) BeanFactoryPostProcessor

​	当然这里不会执行，只是将对应的后置处理器放到对应的List当中，等待执行

```java 
//自定义的beanFactoryPostProcessors
for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
   if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
      BeanDefinitionRegistryPostProcessor registryProcessor =
            (BeanDefinitionRegistryPostProcessor) postProcessor;
      registryProcessor.postProcessBeanDefinitionRegistry(registry);
      registryProcessors.add(registryProcessor);
   }
   else {//BeanDefinitionRegistryPostProcessor  BeanFactoryPostProcessor
      regularPostProcessors.add(postProcessor);
   }
}
```

​	2.这个currentRegistryProcessors 放的是spring内部自己实现了BeanDefinitionRegistryPostProcessor接口的对象

```java
//这个currentRegistryProcessors 放的是spring内部自己实现了BeanDefinitionRegistryPostProcessor接口的对象
List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();
```

​      3.获取所有实现BeanDefinitionRegistryPostProcessor接口的bean的名字

实际上在初始化最初只有一个ConfigurationClassPostProcessor

```java
//getBeanNamesForType  根据bean的类型获取bean的名字
String[] postProcessorNames =
      beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
```

```java
//这个地方可以得到一个BeanFactoryPostProcessor，因为是spring默认在最开始自己注册的
//为什么要在最开始注册这个呢？
//因为spring的工厂需要许解析去扫描等等功能
//而这些功能都是需要在spring工厂初始化完成之前执行
//要么在工厂最开始的时候、要么在工厂初始化之中，反正不能再之后
//因为如果在之后就没有意义，因为那个时候已经需要使用工厂了
//所以这里spring'在一开始就注册了一个BeanFactoryPostProcessor，用来插手springFactory的实例化过程
//在这个地方断点可以知道这个类叫做ConfigurationClassPostProcessor
//ConfigurationClassPostProcessor那么这个类能干嘛呢？可以参考源码
//下面我们对这个牛逼哄哄的类（他能插手spring工厂的实例化过程还不牛逼吗？）重点解释
for (String ppName : postProcessorNames) {
   if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
      currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
      processedBeans.add(ppName);
   }
}
```

​	4.在这个invokeBeanFactoryPostProcessors当中最先执行的后置处理器是

​     BeanDefinitionRegistryPostProcessor，然后在执行BeanFactoryPostProcessor

```java
//排序不重要，况且currentRegistryProcessors这里也只有一个数据
sortPostProcessors(currentRegistryProcessors, beanFactory);
//合并list，不重要(为什么要合并，因为还有自己的)
registryProcessors.addAll(currentRegistryProcessors);
//最重要。注意这里是方法调用
//执行所有BeanDefinitionRegistryPostProcessor

invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
//执行完成了所有BeanDefinitionRegistryPostProcessor
//这个list只是一个临时变量，故而要清除
currentRegistryProcessors.clear();
```

##### 		 invokeBeanDefinitionRegistryPostProcessors

```java
private static void invokeBeanDefinitionRegistryPostProcessors(
      Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

   //因为只有一条数据
   for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
      postProcessor.postProcessBeanDefinitionRegistry(registry);
   }
}
```

###### 	 *ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry*

这个方法当中做的一些事情

​	1.configCandidates 放一些配置类appconfig...

```java
//定义一个list存放app 提供的bd（项目当中提供了@Compent）
List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
//获取容器中注册的所有bd名字
//7个(加配置类)或者6个(加配置类) ，因为JPA的支持是由系统决定的
String[] candidateNames = registry.getBeanDefinitionNames();
```

​	2.判断是否是Configuration类

```java
for (String beanName : candidateNames) {
   BeanDefinition beanDef = registry.getBeanDefinition(beanName);
   if (ConfigurationClassUtils.isFullConfigurationClass(beanDef) ||
         ConfigurationClassUtils.isLiteConfigurationClass(beanDef)) {
      //如果BeanDefinition中的configurationClass属性为full或者lite,则意味着已经处理过了,直接跳过
      //这里需要结合下面的代码才能理解
      if (logger.isDebugEnabled()) {
         logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
      }
   }
   //判断是否是Configuration类，如果加了Configuration下面的这几个注解就不再判断了
   // 还有  add(Component.class.getName());
   //    candidateIndicators.add(ComponentScan.class.getName());
   //    candidateIndicators.add(Import.class.getName());
   //    candidateIndicators.add(ImportResource.class.getName());
   //beanDef == appconfig
   else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
      //BeanDefinitionHolder 也可以看成一个数据结构
      configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
   }
}
```

​		2.1 那他是怎么判断的？通过调用 ConfigurationClassUtils#checkConfigurationClassCandidate

```java 
AnnotationMetadata metadata;
if (beanDef instanceof AnnotatedBeanDefinition &&
      className.equals(((AnnotatedBeanDefinition) beanDef).getMetadata().getClassName())) {
   // Can reuse the pre-parsed metadata from the given BeanDefinition...
   //如果BeanDefinition 是 AnnotatedBeanDefinition的实例,并且className 和 BeanDefinition中 的元数据 的类名相同
   // 则直接从BeanDefinition 获得Metadata
   metadata = ((AnnotatedBeanDefinition) beanDef).getMetadata();
}
```

​		

```java 
else if (beanDef instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) beanDef).hasBeanClass()) {
   // Check already loaded Class if present...
   // since we possibly can't even load the class file for this Class.
   //如果BeanDefinition 是 AbstractBeanDefinition的实例,并且beanDef 有 beanClass 属性存在
   //则实例化StandardAnnotationMetadata
   Class<?> beanClass = ((AbstractBeanDefinition) beanDef).getBeanClass();
   metadata = new StandardAnnotationMetadata(beanClass, true);
}
```

​		

```java
else {
   try {
      MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(className);
      metadata = metadataReader.getAnnotationMetadata();
   }
   catch (IOException ex) {
      if (logger.isDebugEnabled()) {
         logger.debug("Could not find class file for introspecting configuration annotations: " + className, ex);
      }
      return false;
   }
}
```

会尝试从以上3中方式取得bd的注解元数据，如果取不到就直接返回false 不是配置类；如果取到了，会判断是full配置类还是lite配置类。

​        判断是否为full 配置类

```java
//判断当前这个bd中存在的类是不是加了@Configruation注解
//如果存在则spring认为他是一个全注解的类
if (isFullConfigurationCandidate(metadata)) {
   //如果存在Configuration 注解,则为BeanDefinition 设置configurationClass属性为full
   beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL);
}
```

​     判断是否是Lite配置类，否则返回非配置类

```java
//判断是否加了以下注解，摘录isLiteConfigurationCandidate的源码
//     candidateIndicators.add(Component.class.getName());
//    candidateIndicators.add(ComponentScan.class.getName());
//    candidateIndicators.add(Import.class.getName());
//    candidateIndicators.add(ImportResource.class.getName());
//如果不存在Configuration注解，spring则认为是一个部分注解类
else if (isLiteConfigurationCandidate(metadata)) {
   beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE);
}
else {
   return false;
}
```

​	2.2 是配置类则添加到configCandidates 当中

​    3.如果执行以上逻辑configCandidates 是空列表说明没有找到配置类，直接返回

```java
// Return immediately if no @Configuration classes were found
if (configCandidates.isEmpty()) {
   return;
}
// 排序，根据order,不重要
// Sort by previously determined @Order value, if applicable
configCandidates.sort((bd1, bd2) -> {
   int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
   int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
   return Integer.compare(i1, i2);
});
```

4.BeanNameGenerator相关，默认是null 使用spring的默认beanName生成策略

```java
SingletonBeanRegistry sbr = null;
//如果BeanDefinitionRegistry是SingletonBeanRegistry子类的话,
// 由于我们当前传入的是DefaultListableBeanFactory,是SingletonBeanRegistry 的子类
// 因此会将registry强转为SingletonBeanRegistry
if (registry instanceof SingletonBeanRegistry) {
   sbr = (SingletonBeanRegistry) registry;
   if (!this.localBeanNameGeneratorSet) {//是否有自定义的
      BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(CONFIGURATION_BEAN_NAME_GENERATOR);
      //SingletonBeanRegistry中有id为 org.springframework.context.annotation.internalConfigurationBeanNameGenerator
      //如果有则利用他的，否则则是spring默认的
      if (generator != null) {
         this.componentScanBeanNameGenerator = generator;
         this.importBeanNameGenerator = generator;
      }
   }
}
```

  5.实例化ConfigurationClassParser 为了解析各个配置类，同时定义2个Set用来去重前面定义的ConfigurationClass的已解析和未解析的配置类

```java
// Parse each @Configuration class
//实例化ConfigurationClassParser 为了解析各个配置类
ConfigurationClassParser parser = new ConfigurationClassParser(
      this.metadataReaderFactory, this.problemReporter, this.environment,
      this.resourceLoader, this.componentScanBeanNameGenerator, registry);

//实例化2个set,candidates用于将之前加入的configCandidates进行去重
//因为可能有多个配置类重复了
//alreadyParsed用于判断是否处理过
Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
```

  5.1.开始解析配置类列表

```java
do {
   parser.parse(candidates);
   parser.validate();
    //todo ...
}
while (!candidates.isEmpty());
```

- ​	5.1.1.ConfigurationClassParser#parse

  

```java
public void parse(Set<BeanDefinitionHolder> configCandidates) {
   this.deferredImportSelectors = new LinkedList<>();
   //根据BeanDefinition 的类型 做不同的处理,一般都会调用ConfigurationClassParser#parse 进行解析
   for (BeanDefinitionHolder holder : configCandidates) {
      BeanDefinition bd = holder.getBeanDefinition();
      //
      try {
         if (bd instanceof AnnotatedBeanDefinition) {
            //解析注解对象，并且把解析出来的bd放到map，但是这里的bd指的是普通的
            //何谓不普通的呢？比如@Bean 和各种beanFactoryPostProcessor得到的bean不在这里put
            //但是是这里解析，只是不put而已
            parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
         }
}
```

- ​        5.1.2 ConfigurationClassParser#parse(AnnotationMetadata metadata, String beanName) 重载方法

```java
protected final void parse(AnnotationMetadata metadata, String beanName) throws IOException {
   processConfigurationClass(new ConfigurationClass(metadata, beanName));
}
```

- ​	5.1.3 processConfigurationClass 真正执行的在这里

```java
protected void processConfigurationClass(ConfigurationClass configClass) throws IOException {
   if (this.conditionEvaluator.shouldSkip(configClass.getMetadata(), ConfigurationPhase.PARSE_CONFIGURATION)) {
      return;
   }

   // 处理Imported 的情况
   //就是当前这个注解类有没有被别的类import
   ConfigurationClass existingClass = this.configurationClasses.get(configClass);
   if (existingClass != null) {
      if (configClass.isImported()) {
         if (existingClass.isImported()) {
            existingClass.mergeImportedBy(configClass);
         }
         // Otherwise ignore new imported config class; existing non-imported class overrides it.
         return;
      }
      else {
         // Explicit bean definition found, probably replacing an imports.
         // Let's remove the old one and go with the new one.
         this.configurationClasses.remove(configClass);
         this.knownSuperclasses.values().removeIf(configClass::equals);
      }
   }
```

- ​	5.1.4 ConfigurationClassParser#doProcessConfigurationClass

  ```java
  @Nullable
  protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
        throws IOException {
  
     // Recursively process any member (nested) classes first
     //处理内部类
     processMemberClasses(configClass, sourceClass);
  
     // Process any @PropertySource annotations
     for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
           sourceClass.getMetadata(), PropertySources.class,
           org.springframework.context.annotation.PropertySource.class)) {
        if (this.environment instanceof ConfigurableEnvironment) {
           processPropertySource(propertySource);
        }
        else {
           logger.warn("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
                 "]. Reason: Environment must implement ConfigurableEnvironment");
        }
     }
  
     // Process any @ComponentScan annotations
     Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
           sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
     if (!componentScans.isEmpty() &&
           !this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
        for (AnnotationAttributes componentScan : componentScans) {
           // The config class is annotated with @ComponentScan -> perform the scan immediately
           //扫描普通类=componentScan=com.phyling
           //这里扫描出来所有@Component
           //并且把扫描的出来的普通bean放到map当中
           Set<BeanDefinitionHolder> scannedBeanDefinitions =
                 this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
           // Check the set of scanned definitions for any further config classes and parse recursively if needed
           //检查扫描出来的类当中是否还有configuration
           for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
              BeanDefinition bdCand = holder.getBeanDefinition().getOriginatingBeanDefinition();
              if (bdCand == null) {
                 bdCand = holder.getBeanDefinition();
              }
              //检查  todo
              if (ConfigurationClassUtils.checkConfigurationClassCandidate(bdCand, this.metadataReaderFactory)) {
                 parse(bdCand.getBeanClassName(), holder.getBeanName());
              }
           }
        }
     }
  
     /**
      * 上面的代码就是扫描普通类----@Component
      * 并且放到了map当中
      */
     // Process any @Import annotations
     //处理@Import  imports 3种情况
     //ImportSelector
     //普通类
     //ImportBeanDefinitionRegistrar
     //这里和内部地柜调用时候的情况不同
     /**
      * 这里处理的import是需要判断我们的类当中时候有@Import注解
      * 如果有这把@Import当中的值拿出来，是一个类
      * 比如@Import(xxxxx.class)，那么这里便把xxxxx传进去进行解析
      * 在解析的过程中如果发觉是一个importSelector那么就回调selector的方法
      * 返回一个字符串（类名），通过这个字符串得到一个类
      * 继而在递归调用本方法来处理这个类
      *
      * 判断一组类是不是imports（3种import）
      *
      *
      */
     processImports(configClass, sourceClass, getImports(sourceClass), true);
  
     // Process any @ImportResource annotations
     AnnotationAttributes importResource =
           AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
     if (importResource != null) {
        String[] resources = importResource.getStringArray("locations");
        Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
        for (String resource : resources) {
           String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
           configClass.addImportedResource(resolvedResource, readerClass);
        }
     }
  
     // Process individual @Bean methods
     Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
     for (MethodMetadata methodMetadata : beanMethods) {
        configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
     }
  
     // Process default methods on interfaces
  
     processInterfaces(configClass, sourceClass);
  
     // Process superclass, if any
     if (sourceClass.getMetadata().hasSuperClass()) {
        String superclass = sourceClass.getMetadata().getSuperClassName();
        if (superclass != null && !superclass.startsWith("java") &&
              !this.knownSuperclasses.containsKey(superclass)) {
           this.knownSuperclasses.put(superclass, configClass);
           // Superclass found, return its annotation metadata and recurse
           return sourceClass.getSuperClass();
        }
     }
  
     // No superclass -> processing is complete
     return null;
  }
  ```



5.最后执行BeanFactoryPostProcessor，包含程序自定义的和spring内部的

```java 
// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
//执行BeanFactoryPostProcessor的回调，前面不是吗？
//前面执行的BeanFactoryPostProcessor的子类BeanDefinitionRegistryPostProcessor的回调
//这是执行的是BeanFactoryPostProcessor    postProcessBeanFactory
//ConfuguratuonClassPpostProcssor
invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
//自定义BeanFactoryPostProcessor
invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
```



## 5.**registerBeanPostProcessors**(重要)

```java
// Register bean processors that intercept bean creation.
//注册beanPostProcessor
registerBeanPostProcessors(beanFactory);
```

重要展开说：

​	

## 6.initMessageSource

```java
// Initialize message source for this context.
//和国际化有关，对于整个spring 环境并不是很重要
initMessageSource();
```

## 7.initApplicationEventMulticaster

```java
// Initialize event multicaster for this context.
//初始化应用事件广播器
initApplicationEventMulticaster();
```

## 8.onRefresh

```java
// Initialize other special beans in specific context subclasses.
//在具体的子类当中是初始化其他特殊的一些beans 默认当前是没有任何操作
onRefresh();
```

## 9.registerListeners

```java
// Check for listener beans and register them.
//检查侦听器bean并注册它们
registerListeners();
```

## 10.**finishBeanFactoryInitialization**(重要)

```java
// Instantiate all remaining (non-lazy-init) singletons.
//实例化余下所有的非懒加载的单例beans
finishBeanFactoryInitialization(beanFactory);
```

## 11.finishRefresh

```java 
// Last step: publish corresponding event.
////调用LifecycleProcessor来完成此上下文的刷新
//方法并发布上下文刷新事件
finishRefresh();
```

## 12.destroyBeans

```java 
// Destroy already created singletons to avoid dangling resources.
//初始化异常，那么会销毁容器
destroyBeans();
```

## 13.cancelRefresh

```java
// Reset 'active' flag.
cancelRefresh(ex);
```

## 14.resetCommonCaches

```java
// Reset common introspection caches in Spring's core, since we
// might not ever need metadata for singleton beans anymore...
//重置Spring核心中的常见内的缓存，因为我们
//可能再也不需要单例bean的元数据了……
resetCommonCaches();
```
