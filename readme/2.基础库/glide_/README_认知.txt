1、Glide的主体逻辑

----------------------------------------------------------------------------------------------------

   A、请求主体放在RequestManager中
   B、Glide使用Fragment来管理请求的生命周期，比单纯使用Activity的生命周期来管理请求，更加精确
      在实现上，Glide创新性的使用了Activity中添加一个不可见的Fragment来实现的
   C、RequestManagerFragment是生命周期事件的发送者，RequestManager是生命周期的接受者
      两者在关联上是通过ActivityFragmentLifeCycle来实现的，这样的实现更加规范合理，因为RequestManagerFragment本质上是Fragment，RequestManager本质上是任务请求
   D、RequestManagerFragment在逻辑上组成了一个Fragment的两层树形解构，每一个Fragment知道它的父节点和所有子节点

   在Activity中分配的是RootFragment，Activity中内嵌的是ChildFragment
   其中RootFragment知道所有的ChildFragment，ChildFragment知道Acitivty对应的RootFragment，这种关联是在Fragment生命周期中完成的

----------------------------------------------------------------------------------------------------
Q:生命周期是如何管理的？
   RequestManagerFragment：主要利用Fragment的生命周期，来管理RequestManager
   RequestManagerRetriever：获取RequestManagerFragment，创建RequestManager，让两者建立关系，返回RequestManager
                            1、生产Application的RequestManager，进程内全局唯一：                                                    直接构造RequestManager
                            2、生产FragmentActivity的RequestManager：                                                            输入activity、fm、isActivityDestoryed
                            3、生产Fragment的RequestManager：                                                                    输入activity、fm、isFragmentVisible
                            4、生产Activity的RequestManager：                                                                    输入activity、fm、isActivityDestoryed
                            5、生产View的RequestManager：先根据View找宿主FragmentActivity、Activity、Fragment、Application          复用以上的四中方式，方式根据View的宿主进行选择
   RequestManagerFactory：RequestManager构造器，仅仅是一个简单的RequestManager生成器
   RequestManager：构造器接受了参数Glide、LifeCycle、TreeNode、Context

   [-------------------------------------------]
    @NonNull
    private RequestManager supportFragmentGet(@NonNull Context context, @NonNull androidx.fragment.app.FragmentManager fm, @Nullable Fragment parentHint, boolean isParentVisible) {
        SupportRequestManagerFragment current = this.getSupportRequestManagerFragment(fm, parentHint, isParentVisible);
        RequestManager requestManager = current.getRequestManager();
        if (requestManager == null) {
            Glide glide = Glide.get(context);
            requestManager = this.factory.build(glide, current.getGlideLifecycle(), current.getRequestManagerTreeNode(), context);
            current.setRequestManager(requestManager);
        }

        return requestManager;
    }
    分析：首先查找可用的RequestManagerFragment，然后构建RequestManager，让两者建立关联，最终返回RequestManager

   [-------------------------------------------]
   @NonNull
   private SupportRequestManagerFragment getSupportRequestManagerFragment(@NonNull androidx.fragment.app.FragmentManager fm, @Nullable Fragment parentHint, boolean isParentVisible) {
       SupportRequestManagerFragment current = (SupportRequestManagerFragment)fm.findFragmentByTag("com.bumptech.glide.manager");
       if (current == null) {
           current = (SupportRequestManagerFragment)this.pendingSupportRequestManagerFragments.get(fm);
           if (current == null) {
               current = new SupportRequestManagerFragment();
               current.setParentFragmentHint(parentHint);
               if (isParentVisible) {
                   current.getGlideLifecycle().onStart();
               }

               this.pendingSupportRequestManagerFragments.put(fm, current);
               fm.beginTransaction().add(current, "com.bumptech.glide.manager").commitAllowingStateLoss();
               this.handler.obtainMessage(2, fm).sendToTarget();
           }
       }

       return current;
   }
   分析：RequestManagerFragment的缓存顺序：fm.findFragmentByTag  <-  pendingSupportRequestManagerFragments.get(fm)  <-  new RequestManagerFragment
        最后通过消息机制，发送到主线程中，来清理pendingSupportRequestManagerFragments，这样做的好处是防止fm方式创建了多个RequestManagerFragment

   [-------------------------------------------]
   @Nullable
   private Fragment findSupportFragment(@NonNull View target, @NonNull FragmentActivity activity) {
      tempViewToSupportFragment.clear();
      findAllSupportFragmentsWithViews(activity.getSupportFragmentManager().getFragments(), tempViewToSupportFragment);
      Fragment result = null;
      View activityRoot = activity.findViewById(android.R.id.content);
      View current = target;
      while (!current.equals(activityRoot)) {
         result = tempViewToSupportFragment.get(current);
         if (result != null) {break;}
         if (current.getParent() instanceof View) {
            current = (View) current.getParent();
         } else {
            break;
         }
      }
      tempViewToSupportFragment.clear();
      return result;
   }
    分析：在fm中找到所有的<Fragment,View>，对于目标target而言，需要向上找parent，看parent在哪个Fragment中
         即fm -> fm.getFragments() -> <View, Fragment>  ->  target.findRootContent
  [-------------------------------------------]
  总结：RequestManagerFragment不管怎么创建管理，最终都是放到fm上了；

----------------------------------------------------------------------------------------------------
Q:请求是如何管理的？
RequestManager：     核心元素：目标响应管理器(TargetTracker)、请求管理器(RequestTracker)、请求选项(RequestOptions)、请求构造器(RequestBuilder)
RequestTracker：
TargetTracker：
RequestListener：
RequestOptions：     请求选项，会包括内存、展示模式、变换、动画等配置
BaseRequestOptions： 请求参数封装类，包括了各种请求参数，比如请求优先级、缩略图、错误图等等； class BaseRequestOptions<T extends BaseRequestOptions<T>>， 所以T是范型类型，又是BaseRequestOption的子类
RequestBuilder：     请求(request)、目标(target)、目标响应管理器(TargetTracker)、请求管理器(RequestTracker)； RequestBuilder.as(XXX_ImageType.class).apply(XXX_RequestOptions).load(XXX_Object).into(XXX_Target)

as得到 RequestBuilder：
load得到 RequestBuilder：
downloadOnly得到 RequestBuilder：

----------------------------------------------------------------------------------------------------

DefaultConnectivityMonitor
1、Activity生命周期onStart开始注册广播
1、接受系统通知
2、通知外部监听者

RequestManager.lifecycle.register(defaultConnectivityMonitor)

Lifecycle    ->    DefaultConnectivityMonitor
                            (注册广播)
                      ConnectivieListener        <-    BroadcastReceiver
                            (通知变化)
----------------------------------------------------------------------------------------------------

Q:请求是如何执行的？
RequestTracker：批量管理Request，主要包括运行、暂停、恢复、清除、重启，内部实现包括一个requests和pengdingRequests
TargetTracker：批量管理Target，主要把Fragment等生命周期传递给Target，包括onStart、onStop、onDestory
RequestBuilder：

----------------------------------------------------------------------------------------------------

Q:请求内部的核心工作？
SingleRequest.begin()

如果进行中：那么不允许重复允许
如果已完成：回调通知onResourceReady

1、target.onLoadStared(drawable)
2、target.getSize()
3、request.onSizeReady
      engine.load()

      核心引擎加载过程: 特别注意它是加锁的
      A、从一级内存缓存中加载：ActiveResources
      B、从二级内存缓存中加载：MemoryCache
      C、获取一个EnginJob开始
	    创建一个DecodeJob任务
	    启动解码工作（从网络获取资源 - 从磁盘缓存中获取资源）

4、EnginJob核心逻辑
   需求：在理想的情况中，客户希望存在一个任务，可以做到完全实时的控制。
   现状：A、现实情况中，任务是在子线程中运行的，并且是放到线程池中运行的；
        B、线程的启动、停止、状态获取、回调都是异步的，无法做到实时控制
   解决方案：虚构一个逻辑上的任务，该任务处于理想状态，可随时被启动、被暂停、被终止，可以即时获取状态
        子线程任务通过回调，与逻辑任务建立关联，因此只要加锁控制好回调即可（断开回调，即关闭了子线程任务）

5、decodeJob.fetchGlideExecutor
      executor.execute(decodeJob)
   核心逻辑： 子线程任务包括启动、回调通知、暂停等；安装阶段划分又包括开始、网络获取、缓存获取、编码、结束等阶段
            1、状态机逻辑解决阶段问题
            2、操作、状态与回调解决客户控制问题

            [上游]                     [自己]                        [下游]
                                      DecodeJob
                                      init
            willDecodeFromCache
            release
                                      onEncodeComplete
                                      onLoadFailed
                                      releaseInternal

                                      compareTo
                                      getPriority
            cancel
            run
                                      runWrapped
                                      getNextGenerator
                                      runGenerators
                                      notifyFailed
                                      notifyComplete
                                      setNotifiedOrThrow
                                      getNextStage

                                      reschedule
                                                                  onDataFetcherReady
                                                                  onDataFetcherFailed
                                     decodeFromRetrievedData
                                     notifyEncodeAndRelease
                                     decodeFromData
                                     decodeFromFetcher
                                     runLoadPath
                                     getOptionsWithConfig
                                     logWithTimeAndKey
                                     logWithTimeAndKey

            getVerifier


----------------------------------------------------------------------------------------------------





----------------------------------------------------------------------------------------------------