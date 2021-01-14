### 死磕源码系列【consul配置中心监视器类ConfigWatch动态刷新配置】

> consul作为配置中心时可以开启动态刷新配置的功能，其实现类是通过ConfigWatch来实现；

##### 监视器类相关属性配置

```yaml
spring:
  cloud:
    consul:
      config:
        watch:
          # 是否开启配置中心配置变动，默认：true
          enabled: true
          # 监控的固定延迟值,即一次执行完成到下一次执行开始之间的间隔，默认：1000毫秒
          delay: 1000
          # 等待（或阻塞）监视查询的秒数，默认：55秒，需要小于60
          wait-time: 55
```

> 此处要说明一下wait-time属性，此属性是设置调用consul的接口方法时会阻塞指定的时间，如果在阻塞过程中有配置修改则立马返回，否则要等到阻塞时间结束，delay属性指的是在上次调用接口结束之后多久开始下一次调度；

##### 1.ConfigWatch定时任务配置方法

```java
	@Override
	public void start() {
		if (this.running.compareAndSet(false, true)) {
			this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(this::watchConfigKeyValues,
					this.properties.getWatch().getDelay());
		}
	}
```

> 指定一个固定的线程池，每次在上次任务执行完成后间隔指定的时间执行下次任务；

##### 2.watchConfigKeyValues监视器任务方法

```java
	@Timed("consul.watch-config-keys")
	public void watchConfigKeyValues() {
		if (!this.running.get()) {
			return;
		}
		for (String context : this.consulIndexes.keySet()) {
			  ...
        //通过API调度获取配置中心配置信息，接口阻塞指定的超时时间，默认是：55s
				Response<List<GetValue>> response = this.consul.getKVValues(context, aclToken,
						new QueryParams(this.properties.getWatch().getWaitTime(), currentIndex));

				// if response.value == null, response was a 404, otherwise it was a
				// 200, reducing churn if there wasn't anything
				if (response.getValue() != null && !response.getValue().isEmpty()) {
          // 获取配置中心的新版本
					Long newIndex = response.getConsulIndex();

					if (newIndex != null && !newIndex.equals(currentIndex)) {
						// 判定新版本和老版本是否一样，不同则发布事件刷新
						if (!this.consulIndexes.containsValue(newIndex) && !currentIndex.equals(-1L)) {
							if (log.isTraceEnabled()) {
								log.trace("Context " + context + " has new index " + newIndex);
							}
							RefreshEventData data = new RefreshEventData(context, currentIndex, newIndex);
              //发布刷新本地配置事件
							this.publisher.publishEvent(new RefreshEvent(this, data, data.toString()));
						}
						else if (log.isTraceEnabled()) {
							log.trace("Event for index already published for context " + context);
						}
						this.consulIndexes.put(context, newIndex);
					}
				...
	}
```

> 上述监视器方法监测到配置变化会发布RefreshEvent事件，该事件会被RefreshEventListener监听器监听

##### 3.RefreshEventListener监听器

```java
public class RefreshEventListener implements SmartApplicationListener {

	private static Log log = LogFactory.getLog(RefreshEventListener.class);

	private ContextRefresher refresh;

	private AtomicBoolean ready = new AtomicBoolean(false);

	public RefreshEventListener(ContextRefresher refresh) {
		this.refresh = refresh;
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return ApplicationReadyEvent.class.isAssignableFrom(eventType)
				|| RefreshEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationReadyEvent) {
			handle((ApplicationReadyEvent) event);
		}
		else if (event instanceof RefreshEvent) {
			handle((RefreshEvent) event);
		}
	}

	public void handle(ApplicationReadyEvent event) {
		this.ready.compareAndSet(false, true);
	}

	public void handle(RefreshEvent event) {
		if (this.ready.get()) { // don't handle events before app is ready
			log.debug("Event received " + event.getEventDesc());
      //调用ContextRefresher刷新配置方法
			Set<String> keys = this.refresh.refresh();
			log.info("Refresh keys changed: " + keys);
		}
	}

}
```

GitHub地址：[https://github.com/mingyang66/EmilyGateway](https://github.com/mingyang66/EmilyGateway)