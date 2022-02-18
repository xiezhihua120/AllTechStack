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


   相关类：
   DeferredEncodeManager            利用Encoder和Resource进行编码，写入缓存中
   ReleaseManager                   释放、编码完成、失败都属于已完成
   DecodeHelper                     解码器相关属性、策略、解码器Registry映射表
   LoadData                         Key与DataFetcher组合
   DataSource                       本地、远程、磁盘数据、磁盘资源、内存
   DataFetcher                      加载、回调、取消
   DataFetcherGenerator             启动下一个，取消、回调



   状态机：
   ———————————————————————————————————————————————————————————————————————————————————————
   runReason.INITIALIZE

   INITIALIZE

   RESOURCE_CACHE                       ResourceCacheGenerator

   DATA_CACHE                           DataCacheGenerator

   SOURCE

   ———————————————————————————————————————————————————————————————————————————————————————
   runReason.SWITCH_TO_SOURCE_SERVICE

   SOURCE                               SourceGenerator

   FINISHED

   ———————————————————————————————————————————————————————————————————————————————————————
   runReason.DECODE_DATA

   解码                                  DecodeJob.decodeFromRetrievedData

   ———————————————————————————————————————————————————————————————————————————————————————


----------------------------------------------------------------------------------------------------
DecodeHelper：属性组合类；注册表获取加载器；

    this.glideContext = glideContext;                                       // 上下文

    this.model = model;                                                     // 数据
    this.signature = signature;
    this.width = width;
    this.height = height;
    this.resourceClass = resourceClass;
    this.transcodeClass = (Class<Transcode>) transcodeClass;

    this.diskCacheStrategy = diskCacheStrategy;                             // 存储
    this.diskCacheProvider = diskCacheProvider;

    this.priority = priority;                                               // 选项
    this.options = options;
    this.transformations = transformations;
    this.isTransformationRequired = isTransformationRequired;
    this.isScaleOnlyOrNoTransform = isScaleOnlyOrNoTransform;

    List<LoadData<?>> getLoadData() {
        if (!isLoadDataSet) {
          isLoadDataSet = true;
          loadData.clear();
          List<ModelLoader<Object, ?>> modelLoaders = glideContext.getRegistry().getModelLoaders(model);
          //noinspection ForLoopReplaceableByForEach to improve perf
          for (int i = 0, size = modelLoaders.size(); i < size; i++) {
            ModelLoader<Object, ?> modelLoader = modelLoaders.get(i);
            LoadData<?> current =
                modelLoader.buildLoadData(model, width, height, options);
            if (current != null) {
              loadData.add(current);
            }
          }
        }
        return loadData;
    }
    分析：LoadData实际上是sourceKey和DataFetcher的组合，之所以能得到他们，也是根据model来匹配生成的。
         更详细的，其实是model到Registry表中匹配ModelLoader，每个modelLoader.buildLoadData(model, width, height, options);
         时，更具model生成了sourceKey。
         因此，LoadData = sourceKey + DataFetcher

    List<Key> getCacheKeys() {
        if (!isCacheKeysSet) {
          isCacheKeysSet = true;
          cacheKeys.clear();
          List<LoadData<?>> loadData = getLoadData();
          //noinspection ForLoopReplaceableByForEach to improve perf
          for (int i = 0, size = loadData.size(); i < size; i++) {
            LoadData<?> data = loadData.get(i);
            if (!cacheKeys.contains(data.sourceKey)) {
              cacheKeys.add(data.sourceKey);
            }
            for (int j = 0; j < data.alternateKeys.size(); j++) {
              if (!cacheKeys.contains(data.alternateKeys.get(j))) {
                cacheKeys.add(data.alternateKeys.get(j));
              }
            }
          }
        }
        return cacheKeys;
    }
    分析：利用LoadData提取了所有的sourceKey

----------------------------------------------------------------------------------------------------
Registry
model加载成data			ModelLoader
data解码成resource		Decoder

[model]					[data]					[resource]                  [transcodedClass]

url						InputStream				Drawable                    Drawable
Uri						ByteBuffer				Bitmap                      Bitmap
String					File					GifDrawable                 File
File					ParcelFileDescriptor	BitmapDrawable

private final ModelLoaderRegistry modelLoaderRegistry;                                              // model到data使用的ModelLoaders
private final EncoderRegistry encoderRegistry;                                                      // data使用的Encoders
private final ResourceDecoderRegistry decoderRegistry;                                              // data到resource使用的Encoders
private final ResourceEncoderRegistry resourceEncoderRegistry;                                      // ？
private final DataRewinderRegistry dataRewinderRegistry;                                            // 读取data的头部
private final TranscoderRegistry transcoderRegistry;                                                // resourceClass到transcodeClass的transcoder
private final ImageHeaderParserRegistry imageHeaderParserRegistry;                                  // 图片头部解析器

private final ModelToResourceClassCache modelToResourceClassCache =new ModelToResourceClassCache(); // 保存的是[model、resource、transcode]所用得到的transcoders
private final LoadPathCache loadPathCache = new LoadPathCache();                                    // 保护一个DecodePath，其中包含一组decoders
private final Pool<List<Throwable>> throwableListPool = FactoryPools.threadSafeList();              // ？

----------------------------------------------------------------------------------------------------
解码过程分析：

Registry.loadPathCache
    核心功能：从Registry中查找能够满足<data,resource,transcode>的LoadPath
            其中，LoadPath本质是包含了一个DecodePath，后者中存放的是一组decoders

Registry.dataRewinderRegistry
    核心功能：针对data进行的一个封包和解包类，里面还是data

----------------------------------------------------------------------------------------------------
GifDrawable：
GifDrawable  ->  GifState  ->  GifFrameLoader
    [上游]								[自己]								[下游]
                                        GifDrawable
    getSize
    getFirstFrame
    setFrameTransformation
    getFrameTransformation
    getBuffer
    getFrameCount
    getFrameIndex
    resetLoopCount

    start							    startRunning
    stop								stopRunning

    setVisible
    getIntrinsicWidth
    getIntrinsicHeight

    isRunning
    setIsRunning
                                        onBoundsChange
                                        draw

    setAlpha
    setColorFilter
                                        getDestRect
                                        getPaint

    getOpacity
                                                                          findCallback
                                                                          onFrameReady
                                                                          notifyAnimationEndToListeners
    getConstantState
    recycle
    isRecycled
    setLoopCount

    registerAnimationCallback
    unregisterAnimationCallback
    clearAnimationCallbacks


GifFrameLoader:
[上游]								[自己]								[下游]
							         GifFrameLoader
setFrameTransformation
getFrameTransformation
getFirstFrame

subscribe
unsubscribe

getWidth
getHeight
getSize
getCurrentIndex

getFrameSize
getBuffer
getFrameCount
getLoopCount
								start
								stop
								clear
getCurrentFrame
								loadNextFrame
								recycleFirstFrame
setNextStartFromFirstFrame
																		onFrameReady

----------------------------------------------------------------------------------------------------
资源的回收过程：

public class GifDrawableResource extends DrawableResource<GifDrawable> implements Initializable {

  public GifDrawableResource(GifDrawable drawable) {
    super(drawable);
  }

  @Override
  public Class<GifDrawable> getResourceClass() {
    return GifDrawable.class;
  }

  @Override
  public int getSize() {
    return drawable.getSize();
  }

  @Override
  public void recycle() {
    drawable.stop();
    drawable.recycle();
  }

  @Override
  public void initialize() {
    drawable.getFirstFrame().prepareToDraw();
  }

}
