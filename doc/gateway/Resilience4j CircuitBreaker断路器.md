#### 一、CircuitBreaker断路器介绍

CircuitBreaker断路器通过具有三种正常状态的有限状态机实现：CLOSED、OPEN、HALF_OPEN和两种特殊的状态DISABLED和FORCED_OPEN；

![请添加图片描述](https://img-blog.csdnimg.cn/69099d0a794f41d7b74b8d58c879eaf6.png)

CircuitBreaker断路器使用滑动窗口存储和汇总调用结果，你可以在基于时间（time-based）的滑动窗口和基于计数（count-based）的滑动窗口之间做选择。基于计数的滑动窗口会汇总最后N次调用的结果，基于时间的滑动窗口会汇总最后N秒的调用结果。

#### 二、基于数量（count-based）的滑动窗口

基于计数（count-based）的滑动窗口由N个状态组成的圆形数组组成，如果窗口计数值是10，说明圆形数组计数为10次。滑动窗口以增量的方式更新汇总计数，汇总计数会在新的调用结果返回后更新。当旧的计数被逐出时，会从总的计数中减去计数值，并重置存储桶。

#### 三、基于时间（time-based）的滑动窗口

​		基于时间（time-based）的滑动窗口由N个状态组成的圆形数组组成，如果窗口时间是10秒，说明圆形数组统计时间为10秒。每个bucket汇总在某一秒内发生的所有调用的结果。循环数组的头bucket存储第二个轮回的返回结果，

滑动窗口不单独存储调用结果，而是增量更新部分聚合（bucket）和总聚合（bucket）。

当记录新的调用结果时，总聚合将以增量的方式更新。逐出最旧的存储桶时，将从总聚合中减去该存储桶的部分总聚合，并重置该存储桶。

#### 四、故障率和慢调用阀值

​		当故障率大于等于配置的阀值时CircuitBreaker断路器的状态会由CLOSED转为OPEN。例如：记录的故障率大于50%时，默认所有的异常都视为故障，你可以定义一个可以被视为故障的异常列表。其它所有的异常都会计为成功，除非被忽略。异常也可以被忽略，因此其即可以不计入异常也不计入成功。

​		当慢调用的百分比大于等于配置的阀值时CircuitBreaker断路器会由CLOSED转为OPEN。例如：当调用记录的50%耗时超过5秒，这有助于在外部系统实际无响应之前减少其负载。

​		如果记录了最少的调用次数，故障率和慢调用只可以通过计算判定。例如：如果配置最小调用次数是10，那么最少要记录10次，在故障率计算出来之前，如果评估了9次调用，即使所有9次调用均失败，断路器状态也不会转为OPEN。

​		当CircuitBreaker断路器状态为OPEN时将会拒绝调用并抛出CallNotPermittedException异常，等待一段时间后，CircuitBreaker断路器状态由OPEN转为HALF_OPEN并允许进行可配置的调用次数，以查看后端是否仍然不可用或已恢复可用。在所有的允许调用完成后，更多的调用将会被拒绝并抛出CallPermittedException异常。

​		如果故障率或慢调用率大于等于配置的阀值，断路器的状态变为OPEN。如果故障率和慢调用率低于配置的阀值，断路器的状态变为CLOSED。

​		CircuitBreaker断路器支持另外两种特殊的状态，DISABLED（一直允许访问）和FORCED_OPEN（一直拒绝访问）。在这两种状态下，不会产生断路器事件（状态转换除外），也不会记录任何指标。退出这些状态的唯一方法是触发状态转换或重置断路器。

CircuitBreaker断路器是线程安全的，如下所示：

- CircuitBreaker断路器的状态存储在AtomicReference；
- CircuitBreaker断路器使用原子操作更新状态；
- 通过滑动窗口同步记录调用次数和读取快照；

这意味着原子性应该得到保证，一个时间点只能有一个线程更新状态或滑动窗口；

然而CircuitBreaker断路器不是同步调用函数，这也就意味着函数调用不是关键部分；否则，断路器将会遇到巨大的性能损失和瓶颈，慢的函数调用会对整体性能/吞吐量造成巨大的负面影响；

如果20个并发线程请求执行一个函数的权限，并且CircuitBreaker断路器的状态为CLOSED，则允许所有的线程调用该函数。即使滑动窗口的大小是15，滑动窗口并不意味着只允许同时运行15个调用，如果要限制并发线程的数量，请使用Bulkhead，你可以把Bulkhead和CircuitBreaker断路器结合起来。

一个线程示例：

![请添加图片描述](https://img-blog.csdnimg.cn/12d8af29f248488f84a2c6d9224462ce.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Im-57Gz6I6JRW1pbHk=,size_17,color_FFFFFF,t_70,g_se,x_16)

三个线程示例：

![请添加图片描述](https://img-blog.csdnimg.cn/c472f571f6ec42c1bb55455b40d8d6b3.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6Im-57Gz6I6JRW1pbHk=,size_17,color_FFFFFF,t_70,g_se,x_16)

#### 五、创建一个CircuitBreakerRegistry实例

Resilience4j附带了一个基于ConcurrentHashMap内存的CircuitBreakerRegistry，它提供了线程安全和原子性保证，你可以使用CircuitBreakerRegistry来管理（创建和检索）断路器实例。你可以为所有断路器实例创建全局默认CircuitBreakerConfig的CircuitBreakerRegistry实例；

```java
CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
```

六、创建并配置CircuitBreaker断路器

你可以提供自定义的全局配置CircuitBreakerConfig，要创建自定义全局CircuitBreakerConfig，可以使用CircuitBreakerConfig builder生成器，可以使用builder配置如下属性：

| 配置属性                                     | 默认值                                         | 描述                                                         |
| -------------------------------------------- | ---------------------------------------------- | ------------------------------------------------------------ |
| failureRateThreshold                         | 50                                             | 配置故障率阀值百分比。当故障率大于等于CircuitBreaker断路器阀值状态转为OPEN，并且开启短路调用状态； |
| slowCallRateThreshold                        | 100                                            | 配置慢查询阀值百分比，CircuitBreaker断路器将调用持续时间大于等于slowCallDurationThreshold阀值的调用视为慢查询，当慢查询百分比大于等于阀值，CircuitBreaker断路器转为OPEN状态,并且开启短路调用状态。 |
| slowCallDurationThreshold                    | 60000[ms]                                      | 配置调用持续时间阀值，超过阀值的调用将被视为慢调用，并增加慢查询百分比 |
| permittedNumberOfCallsInHalfOpenState        | 10                                             | 当CircuitBreaker断路器处于Half-Open（半开）状态时允许的正常调用次数。 |
| maxWaitDurationInHalfOpenState               | 0[ms]                                          | 配置最大等待持续时间，以控制断路器在切换至OPEN状态之前保持HALF OPEN状态的最长时间。值为0标识断路器将在半开状态下无限等待，直到所有允许的调用完成。 |
| slidingWindowType                            | COUNT_BASED                                    | 配置滑动窗口的类型，当断路器closed时用于记录调用结果；滑动窗口可以是count-based或time-based; 如果滑动窗口是COUNT_BASED，最后的slidingWindowSize将会以次数为单位计算和聚合次数。如果滑动窗口是TIME_BASED的，最后的slidingWindowSize将以秒为单位记录和聚合； |
| slidingWindowSize                            | 100                                            | 配置用于记录CircuitBreaker关闭时的调用次数（时间）           |
| minimumNumberOfCalls                         | 100                                            | 配置CircuitBreaker断路器计算故障率或慢查询率之前的最小调用次数（单个滑动窗口期）；例如：如果minimumNumberOfCalls值为10，如果CircuitBreaker断路器记录了9次调用将不会转为OPEN状态，即使9次都失败 |
| waitDurationInOpenState                      | 60000[ms]                                      | 断路器由打开状态转为关闭状态需要的时间                       |
| automaticTransitionFromOpenToHalfOpenEnabled | false                                          | 如果设置为true，则意味着断路器将自动从OPEN状态转换为HALF_OPEN状态，无需调用即可触发转换。创建一个线程来监视断路器的所有实例，以便在waitDurationInOpenState通过后将其转换为HALF_OPEN状态。然而如果设置为false，则只有在发出调用时才会转换为HALF_OPEN,即使waitDurationInOpenState被设置之后也是如此，优点是没有线程监视所有断路器的状态。 |
| recordExceptions                             | empty                                          | 记录为错误的异常列表用于增加故障率，任何匹配到的异常或者其子类都会被当做失败。除非通过ignoreExceptions忽略掉的异常，如果你指定了一个异常列表，所有其它的异常都会被计算为成功，除非他们被ignoreExceptions忽略。 |
| ignoreExceptions                             | empty                                          | 一个指定被忽略的异常列表，既不会被计入成功也不会计入失败，任何匹配的异常或者异常的子类都不会计入成功或者失败，即使异常时recordExceptions |
| recordFailurePredicate                       | throwable->true,默认所有的异常都被记录为失败。 | 一个自定义断言，用于计算异常是否应该记录为失败。如果异常要记录为失败，则必须返回true；如果异常要记录为成功，则必须返回false；除非ignoreExceptions明确忽略该异常。 |
| ignoreExceptionPredicate                     | throwable->false，默认没有异常会被忽略         | 一个自定义断言，用于判定是否被忽略，即不被视为失败也不被视为成功。如果异常要被忽略，则必须返回true；如果异常要被视为失败，则必须返回false。 |

```java
// Create a custom configuration for a CircuitBreaker
CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
  .failureRateThreshold(50)
  .slowCallRateThreshold(50)
  .waitDurationInOpenState(Duration.ofMillis(1000))
  .slowCallDurationThreshold(Duration.ofSeconds(2))
  .permittedNumberOfCallsInHalfOpenState(3)
  .minimumNumberOfCalls(10)
  .slidingWindowType(SlidingWindowType.TIME_BASED)
  .slidingWindowSize(5)
  .recordException(e -> INTERNAL_SERVER_ERROR
                 .equals(getResponse().getStatus()))
  .recordExceptions(IOException.class, TimeoutException.class)
  .ignoreExceptions(BusinessException.class, OtherBusinessException.class)
  .build();

// Create a CircuitBreakerRegistry with a custom global configuration
CircuitBreakerRegistry circuitBreakerRegistry = 
  CircuitBreakerRegistry.of(circuitBreakerConfig);

// Get or create a CircuitBreaker from the CircuitBreakerRegistry 
// with the global default configuration
CircuitBreaker circuitBreakerWithDefaultConfig = 
  circuitBreakerRegistry.circuitBreaker("name1");

// Get or create a CircuitBreaker from the CircuitBreakerRegistry 
// with a custom configuration
CircuitBreaker circuitBreakerWithCustomConfig = circuitBreakerRegistry
  .circuitBreaker("name2", circuitBreakerConfig);
```

你可以添加由多个断路器共享的实例配置：

```java
CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
  .failureRateThreshold(70)
  .build();

circuitBreakerRegistry.addConfiguration("someSharedConfig", config);

CircuitBreaker circuitBreaker = circuitBreakerRegistry
  .circuitBreaker("name", "someSharedConfig");
```

可以覆盖配置：

```java
CircuitBreakerConfig defaultConfig = circuitBreakerRegistry
   .getDefaultConfig();

CircuitBreakerConfig overwrittenConfig = CircuitBreakerConfig
  .from(defaultConfig)
  .waitDurationInOpenState(Duration.ofSeconds(20))
  .build();
```

如果你不想使用CircuitBreakerRegistry管理CircuitBreaker实例，你可以直接创建实例对象：

```java
// Create a custom configuration for a CircuitBreaker
CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
  .recordExceptions(IOException.class, TimeoutException.class)
  .ignoreExceptions(BusinessException.class, OtherBusinessException.class)
  .build();

CircuitBreaker customCircuitBreaker = CircuitBreaker
  .of("testName", circuitBreakerConfig);
```

另外，你可以使用CircuitBreakerRegistry的建造方法创建：

```java
Map <String, String> circuitBreakerTags = Map.of("key1", "value1", "key2", "value2");

CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.custom()
    .withCircuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
    .addRegistryEventConsumer(new RegistryEventConsumer() {
        @Override
        public void onEntryAddedEvent(EntryAddedEvent entryAddedEvent) {
            // implementation
        }
        @Override
        public void onEntryRemovedEvent(EntryRemovedEvent entryRemoveEvent) {
            // implementation
        }
        @Override
        public void onEntryReplacedEvent(EntryReplacedEvent entryReplacedEvent) {
            // implementation
        }
    })
    .withTags(circuitBreakerTags)
    .build();

CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
```

如果你想插入自己的Registry实现，可以使用builder方法提供的接口RegistryStore和插件的自定义实现。

```java
CircuitBreakerRegistry registry = CircuitBreakerRegistry.custom()
    .withRegistryStore(new YourRegistryStoreImplementation())
    .withCircuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
    .build();
```

#### 六、修饰并执行函数式接口

你可以使用CircuitBreaker断路器修饰Callable、Supplier、Runnable、Consumer、CheckedRunnable、CheckedSupplier、CheckedConsumer或CompletionStage中的任何一个函数式接口。可以调用通过Try.of(...)或Try.run(...)修饰的函数，可以级联更多的函数，像map、flatMap、filter、recover或andThen，关联的函数仅仅是被调用。如果CircuitBreaker断路器的状态是CLOSED或HALF_OPEN，示例如下：

```java
// Given
CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("testName");

// When I decorate my function
CheckedFunction0<String> decoratedSupplier = CircuitBreaker
        .decorateCheckedSupplier(circuitBreaker, () -> "This can be any method which returns: 'Hello");

// and chain an other function with map
Try<String> result = Try.of(decoratedSupplier)
                .map(value -> value + " world'");

// Then the Try Monad returns a Success<String>, if all functions ran successfully.
assertThat(result.isSuccess()).isTrue();
assertThat(result.get()).isEqualTo("This can be any method which returns: 'Hello world'");
```

#### 七、消费发布的RegistryEvents时间

可以在CircuitBreakerRegistry上注册事件消费者，无论什么时候发生创建、替换或者删除都会触发消费；

```java
CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
circuitBreakerRegistry.getEventPublisher()
  .onEntryAdded(entryAddedEvent -> {
    CircuitBreaker addedCircuitBreaker = entryAddedEvent.getAddedEntry();
    LOG.info("CircuitBreaker {} added", addedCircuitBreaker.getName());
  })
  .onEntryRemoved(entryRemovedEvent -> {
    CircuitBreaker removedCircuitBreaker = entryRemovedEvent.getRemovedEntry();
    LOG.info("CircuitBreaker {} removed", removedCircuitBreaker.getName());
  });
```

#### 八、消费发布的CircuitBreakerEvents事件

CircuitBreakerEvent可以是状态转换、断路器重置、成功调用、记录的错误或忽略的错误。所有的时间都包含一些其它信息，如时间创建时间和调用处理持续时间，如果要消费事件，必须注册事件消费者。

```java
circuitBreaker.getEventPublisher()
    .onSuccess(event -> logger.info(...))
    .onError(event -> logger.info(...))
    .onIgnoredError(event -> logger.info(...))
    .onReset(event -> logger.info(...))
    .onStateTransition(event -> logger.info(...));
// Or if you want to register a consumer listening
// to all events, you can do:
circuitBreaker.getEventPublisher()
    .onEvent(event -> logger.info(...));
```

可以使用CircularEventConsumer将时间存储在具有固定容量的循环缓冲区中。

```java
CircularEventConsumer<CircuitBreakerEvent> ringBuffer = 
  new CircularEventConsumer<>(10);
circuitBreaker.getEventPublisher().onEvent(ringBuffer);
List<CircuitBreakerEvent> bufferedEvents = ringBuffer.getBufferedEvents()
```

九、覆盖RegistryStore

你可以通过自定义实现RegistryStore接口来覆盖基于内存实现的RegistryStore，例如：如果你想使用缓存，在一段时间后删除未使用的实例。

```java
CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.custom()
  .withRegistryStore(new CacheCircuitBreakerRegistryStore())
  .build();
```

