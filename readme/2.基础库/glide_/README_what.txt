官网：https://github.com/bumptech/glide
     http://bumptech.github.io/glide/doc/download-setup.html


1、Google推荐——Glide使用详解
https://www.jianshu.com/p/7ce7b02988a4

2、Glide v4 快速高效的Android图片加载库
https://muyangmin.github.io/glide-docs-cn/int/recyclerview.html

3、Glide踩坑全集
https://www.jianshu.com/p/4b96635653d6?utm_campaign=maleskine&utm_content=note&utm_medium=seo_notes&utm_source=recommendation

4、Android Glide 源码分析和学习
https://www.jianshu.com/p/64fab1a85487

5、变换库
https://github.com/wasabeef/glide-transformations

----------------------------------------------------------------------------------------------------

概念：
请求
请求者
占位符
选项
配置
变换
过渡
目标
缓存
资源重用

----------------------------------------------------------------------------------------------------

基础使用：
1、基本方法
Glide的一个完整的请求至少需要三个参数，代码如下：
    String url = "http://img1.dzwww.com:8080/tupian_pl/20150813/16/7858995348613407436.jpg";
    ImageView imageView = (ImageView) findViewById(R.id.imageView);
    Glide.with(context)
        .load(url)
        .into(imageView);
with(Context context) - 需要上下文，这里还可以使用 Activity、FragmentActivity、android.support.v4.app.Fragment、android.app.Fragment 的对象。将 Activity/Fragment 对象作为参数的好处是，图片的加载会和 Activity/Fragment 的生命周期保持一致，例如：onPaused 时暂停加载，onResume 时又会自动重新加载。所以在传参的时候建议使用 Activity/Fragment 对象，而不是 Context。
load(String url) - 这里我们所使用的一个字符串形式的网络图片的 URL，后面会讲解 load() 的更多使用方式
into(ImageView imageView) - 你需要显示图片的目标 ImageView

2、占位图设置
偶尔出现图片加载慢或者加载不出来的情况是难以避免的，所以为了 UI 能好看一些，我们会使用占位图。Glide 也为我们提供这种方法 placeHolder() 和 error()
String url = "http://img1.dzwww.com:8080/tupian_pl/20150813/16/7858995348613407436.jpg";
ImageView imageView = (ImageView) findViewById(R.id.imageView);
Glide.with(context)
    .load(url)
    .placeholder(R.drawable.place_image)//图片加载出来前，显示的图片
    .error(R.drawable.error_image)//图片加载失败后，显示的图片
    .into(imageView);

3、缩略图
Glide 的缩略图功能在这里不得不说，和占位图略有不同，占位图必须使用资源文件才行，而缩略图是动态的占位图可以从网络中加载。缩略图会在世纪请求加载完成或者处理完之后才显示。在原始图片到达之后，缩略图不会取代原始图片，只会被抹除。

Glide.with( context )
    .load( url )
    .thumbnail( 0.2f )
    .into( imageView );

DrawableRequestBuilder<String> thumbnailRequest = Glide.with( context ).load( url );
Glide.with( context )
        .load( url )
        .thumbnail( thumbnailRequest )
        .into( imageView );

4、动画开关
动画效果可以让图片加载变得更加的平滑，crossFade() 方法强制开启 Glide 默认的图片淡出淡入动画，当前版本3.7.0是默认开启的。crossFade() 还有一个重载方法 crossFade(int duration)。可以控制动画的持续时间，单位ms。动画默认的持续时间是300ms。既然可以添加动画，那肯定就可以设置没有任何淡出淡入效果，调用 dontAnimate()
Glide.with(context)
    .load(url)
    .crossFade()//或者使用 dontAnimate() 关闭动画
    .placeholder(R.drawable.place_image)
    .error(R.drawable.error_image)
    .into(imageView);

5、图片大小与裁剪
在项目开发过程中，指定图片显示大小长长可能用到，毕竟从服务器获取的图片不一定都是符合设计图的标准的。我们在这里就可以使用 override(width,height) 方法，在图片显示到 ImageView 之前，重新改变图片大小。
Glide.with(context)
    .load(url)
    .override(width,height)//这里的单位是px
    .into(imageView);

6、图片的缓存处理
为了更快的加载图片，我们肯定希望可以直接拿到图片，而不是进行网络请求，所以我们需要缓存。Glide 通过使用默认的内存和磁盘缓存来避免不必要的网络请求，之后我们再详细的去看它的实现。
   6.1 内存缓存
       内存缓存是 Glide 默认帮我们做了的，除非你不需要，可以调用 skipMemoryCache(true) 告诉 Glide 跳过内存缓存。这样 Glide 就不会把这张图片放到内存缓存中，该方法只影响内存缓存。（不要问调用skipMemoryCache(false)的问题，Glide 是默认将图片放入内存缓存中的）
   6.2 磁盘缓存
       磁盘缓存也是默认开启的，当然也是可以关闭的，不过关闭的方式略微有点不一样。
       Glide.with(context)
           .load(url)
           .skipMemoryCache(true)
           .diskCacheStrategy( DiskCacheStrategy.NONE )
           .into(imageView);
   6.3 自定义磁盘缓存行为
       DiskCacheStrategy 的枚举意义：
       DiskCacheStrategy.NONE 什么都不缓存
       DiskCacheStrategy.SOURCE 只缓存全尺寸图
       DiskCacheStrategy.RESULT 只缓存最终的加载图
       DiskCacheStrategy.ALL 缓存所有版本图（默认行为）

       Glide.with(context)
           .load(url)
           .diskCacheStrategy( DiskCacheStrategy.SOURCE )
           .into(imageView);

7、图片请求的优先级
同一时间加载多个图片，App 将难以避免这种情况。如果这个时候我们希望用户的体验更好，往往会选择先加载对于用户更加重要的图片。Glide 可以调用 .priority() 方法配合 Priority 枚举来设置图片加载的优先级。

//设置 HIGH 优先级
Glide.with( context )
    .load( highPriorityImageUrl )
    .priority (Priority.HIGH )
    .into( imageView );

//设置 LOW 优先级
Glide.with( context )
    .load( lowPriorityImageUrl )
    .priority( Priority.LOW )
    .into( imageView );

Priority.LOW
Priority.NORMAL
Priority.HIGH
Priority.IMMEDIAT
这里有一点需要注意，优先级并不是完全严格遵守的。Glide 将会用他们作为一个准则，尽可能的处理这些请求，但是不能保证所有的图片都会按照所有要求的顺序加载

8、显示 Gif 和 Video
显示 GIf 对于 Glide 来说一个比较特别的功能（至少 Picasso 暂时还不行）而且使用起来非常简单
String gifUrl = "http://i2.mhimg.com/M00/0E/AE/CgAAilTPWJ2Aa_EIACcMxiZi5xE299.gif";
Glide.with( context )
    .load( gifUrl )
    .placeholder（ R.drawable.default ）
    .error( R.drawable.error )
    .into( imageView );

这段代码还有点问题，如果加载的不是一张 gif 图的话，是没有办法显示的。
Glide.with( context )
    .load( gifUrl )
    .asGif()
    .error( R.drawable.error )
    .into( imageView );

做以上修改，如果图片类型不是 Gif 图的话就会当作 load 失败来处理，因此 error() 会被回调。即使这个url的图片是好的，也是不会显示的。当然，如果你想显示 Gif 但只是向现实静态的图片你就可以这么做
Glide.with( context )
    .load( gifUrl )
    .asBitmap()
    .error( R.drawable.error )
    .into( imageView );

仅仅是显示 Gif 的第一帧图像，这样就可以保证图片的正常显示了。
还有一个神奇的功能，Glide 还能显示视频！But...只能够显示手机本地的视频，要是向现实网络上的视频的话，还是另寻他法吧！
String filePath = "/storrage/emulated/0/Pictures/video.mp4";
Glide.with( context )
    .load( Uri.fromFile( new File( filePath ) ) )
    .into( imageView );

----------------------------------------------------------------------------------------------------
进阶使用

1、Target篇
到现在为止，我们所涉及到的代码都是直接加载图片到 ImageView 中。Glide 隐藏做了所有的网络请求和后台的线程处理，图片准备好之后切回到 UI 线程刷新 ImageView。也就是说 ImageView 在我们代码的链式结构中成为了最后一步，但是如果我们需要获取到 Bitmap 本身
的话我们就需要用到 Target 了。Target 其实就是整个图片的加载的生命周期，所以我们就可以通过它在图片加载完成之后获取到 Bitmap。

[SimpleTarget]
private SimpleTarget<Bitmap> mSimpleTarget = new SimpleTarget<Bitmap>() {
    @Override
    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> animation) {
        mImageView.setImageBitmap(resource);
    }
};
private void loadImageSimpleTarget() {
    Glide.with( thi s)
        .load( mUrl )
        .asBitmap()
        .into( mSimpleTarget );
}

[ViewTarget]
当我们使用 Custom View 时，Glide 并不支持加载图片到自定义 view 中的，使用 ViewTarget 更容易实现。
public class CustomView extends FrameLayout {
    private ImageView mImageView;

    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = new ImageView(getContext());
        addView(mImageView , LayoutParams.MATCH_PARENT , LayoutParams.MATCH_PARENT);
    }

    public void setImage(Drawable drawable){
        mImageView.setImageDrawable(drawable);
    }
}
上面这个例子就没有办法直接使用 .into() ，如果我们使用 ViewTarget 实现呢！
public void loadImageTarget(Context context){
    CustomView mCustomView = (CustomView) findViewById(R.id.custom_view);

    ViewTarget viewTarget = new ViewTarget<CustomView,GlideDrawable>( mCustomView ) {
        @Override
        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
            this.view.setImage(resource);
        }
    };

    Glide.with(context)
            .load(mUrl)
            .into(viewTarget);
}

2、Transformations篇
图片显示之前我们可能还需要对图片进行处理操作，比如：图片切圆角，灰阶处理等等；这些需求我们通过 Transformations 操作 bitmap 来实现，我们可以修改图片的任意属性：尺寸，范围，颜色，像素位置等等。其实我们之前已经提到过两个 Transformation 了，即 fitCenter 和 centerCrop ，这两个是 Glide 已经实现的。
接下来就要讲讲怎么样来实现自己的 Transformation ，我们需要创建一个类去实现 Transformation 接口，但是要实现这个方法还是比较复杂的，接口中 transform 方法提供的参数 Resource<T> resource 不是那么好处理的。如果你只是想要对图片（不是 Gif 和 video）做常规的 bitmap 转换，我们推荐你使用抽象类 BitmapTransformation。它简化了很多的实现，这应该能覆盖 95% 的应用场景啦。
下面的代码实现了对图片切圆角的操作，其中 getId() 方法描述了这个 Transformation 的唯一标识，为避免意外我们需要确保它是唯一的。
public class RoundTransformation extends BitmapTransformation {
    private float radius = 0f;

    public RoundTransformation(Context context) {
        this(context, 4);
    }

    public RoundTransformation(Context context, int px) {
        super(context);
        this.radius = px;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return roundCrop(pool, toTransform);
    }

    private Bitmap roundCrop(BitmapPool pool, Bitmap source) {
        if (source == null)
            return null;

        Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        RectF rectF = new RectF(0f, 0f, source.getWidth(), source.getHeight());
        canvas.drawRoundRect(rectF, radius, radius, paint);
        return result;
    }

    @Override
    public String getId() {
        return getClass().getName() + Math.round(radius);
    }

}
现在我们有了自己的 Transformation 就可以来看看怎么使用了。
调用 .transform() 方法，将自定义的 Transformation 的对象作为参数传递进去就可以使用你的 Transformation 了，这里也可以使用 .bitmaoTransform() 但是它只能用于 bitmap 的转换
Glide.with(context)
    .load(mUrl)
    .transform(new RoundTransformation(context , 20))
    .into(mImageView);

如果我们需要同时执行多个 Transformation 的话，我们不能使用链式的形式多次调用 .transform() 或 .bitmapTransform() 方法，即使你调用了，之前的配置就会被覆盖掉！我们可以直接传递多个转换对象给 .transform() 或 .bitmapTransform() 。
Glide.with(context)
    .load(mUrl)
    .transform(new RoundTransformation(context , 20) ，  new RotateTransformation(context , 90f))
    .into(mImageView);

注:这里需要注意一点 .centerCrop() 和 .fitCenter() 也都是 Transformation 所以也是遵循同时使用多个 Transformation 的规则的，即：当你使用了自定义转换后你就不能使用 .centerCrop() 或 .fitCenter() 了。

这里有一个 GLide Transformations 的库，它提供了很多 Transformation 的实现，非常值得去看，不必重复造轮子对吧！
glide-transformations

3、Animate篇
从图像到图像的平滑过渡是非常重要，Glide 中有一个标准动画去柔软的在你的 UI 中改变，但是我们现在希望设置自己的动画。
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:fillAfter="true">

    <scale
        android:duration="@android:integer/config_longAnimTime"
        android:fromXScale="0.1"
        android:fromYScale="0.1"
        android:pivotX="50%"
        android:pivotY="50%"
        android:toXScale="1"
        android:toYScale="1"/>
</set>
这是个 XML 动画缩放动画，图片刚开始小的，然后逐渐增大到原尺寸。我们现在要应用到 Glide 加载图片中去，调用 .animate() 方法传入 XML 动画的 id 即可。
Glide.with(context)
    .load(mUrl)
    .transform(new RoundTransformation(this , 20))
    .animate( R.anim.zoom_in )
    .into(mImageView);

这种加载方式用在常规的 ImageView 上是没有问题的，但如果使用的 Target 是一些自定义的时候就没法好好的实现了。这时候我们就可以通过传入实现了 ViewPropertyAnimation.Animator 接口的类对象来实现。
ViewPropertyAnimation.Animator animator = new ViewPropertyAnimation.Animator() {
    @Override
    public void animate(View view) {
        view.setAlpha( 0f );

        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat( view, "alpha", 0f, 1f );
        fadeAnim.setDuration( 2500 );
        fadeAnim.start();
    }
};

然后，我们只需要在 Glide 请求中设置这个动画对象就ok了
Glide.with(context)
    .load(mUrl)
    .animate( animator )
    .into(viewTarget);

4、Modules篇
Glide 的 Module 是一个可以全局改变 Glide 的行为的东西，为了定制 Glide 的行为我们要去实现 interface GlideModule 来写我们自己的代码。
public class ExampleModule implements GlideModule{
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
    }
}
可以看到 GlideModule 为我们提供了两个方法，这里我们主要使用的是 applyOptions(Context context, GlideBuilder builder) ， 我们自己的需要重新定义的代码写在该方法里就可以了。然后我们还需要去 AndroidManifest.xml 中使用 meta 声明我们上面实现的 Module
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.mrtrying.demoglide">
    <application>
        <meta-data
            android:name="com.mrtrying.demoglide.module.ExampleModule"
            android:value="GlideModule" />
        ...
    </application>
    ...
</manifest>
这个过程走通了，接下来我们来看看是怎么自定义的。applyOptions(Context context, GlideBuilder builder) 中有两个参数， 我们通过使用 GlideBuilder 来实现我们的需求。先看看 GlideBuilder 中可用的方法

.setMemoryCache(MemoryCache memoryCache)
.setBitmapPool(BitmapPool bitmapPool)
.setDiskCache(DiskCache.Factory diskCacheFactory)
.setDiskCacheService(ExecutorService service)
.setResizeService(ExecutorService service)
.setDecodeFormat(DecodeFormat decodeFormat)
可以看到，这个 GlideBuilder 对象给你访问了 Glide 重要的核心组件。接下来我们就要试着去使用这些方法


5、增加 Glide 的图片质量
在 Android 中有两个主要的方法对图片进行解码：ARGB_8888 和 RGB_565 。前者为每个像素使用4个字节，后者每个像素仅使用2个字节。ARGB_8888 的有时就是图像质量更高以及能储存一个 alpha 通道。 Picasso 使用的就是 ARGB_8888 ， Glide 默认使用低质量的 RGB_565 ，但是现在你就可以使用 Glide module 来改变图片解码规则。就象这样
public class QualityModule implements GlideModule{
    @Override
    public void applyOptions(Context context , GlideBuilder builder){
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
    }

    @Override
    public void registerComponents(Context context , Glide glide){
        // nothing to do here
    }
}
