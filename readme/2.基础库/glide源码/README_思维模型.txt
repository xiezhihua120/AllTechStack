学习知识是没有用的，从知识中反思和实践，才能提升；学习的本质是构建知识网络（以备学以致用），构建的手段主要是建立新旧知识的关联

Why：
Glide是一个图片加载框架，核心关键词有两个API、性能
API是说Glide库的采用流式语法、以及丰富的功能实现了图片加载库
性能是说Glide充分考虑了图片解码速度、资源管理的压力，为了实现高性能：
1、智能采样和缓存，减少解码次数
2、资源缓存重用，资源管理效率提高
3、深度的生命周期管理，即使的释放资源

What：
核心逻辑：
   要点一：加载。Glide最本质的逻辑是从model获取到data，进而转化成resouce、transcodeClass，最终选择性的缓存到ResourceCache和DataCache中
   要点二：缓存。运行时从ActiveResouce、MemCache中获取，如果获取不到就进行数据加载过程
   要点三：回收。为了保证数据即使收回，采用Fragment来管理资源的生命周期

How：
在第一层客户端中，提供了友好的API、丰富的API
在第二层请求器中，提供了可复用、可组合的请求器逻辑
在第三层任务引擎中，主要是任务Engine来调度EngineJob和DecodeJob，前者是逻辑任务，后者是实际的子线程任务
    A、状态机中实现了资源缓存、数据缓存、源数据、解码、编码状态转移和控制
    B、状态机中使用的ModelLoader、Decoder主要存放在Registy工具注册表中
    C、不同的状态下使用不同的缓存Key，例如在ResourceDataGenerator中使用的主键是ResourceDataKey
第四层加载与解码
    原则：找到第一个可以处理数据的ModelLoader
         找到第一个可以解码的Decoder

场景一：加载png图片
   解码的资源存放到了ResourceCache中，杀死APP后，glide会进入Engine加载流程，先从ResourceCache中加载

场景二：加载gif图片
   为什么能加载gif动画
   在Engine的解码流出中，有一个ImageType解析过程，发现文件的头部有gif魔幻数标记，从而确定使用ByteBufferGifDecoder