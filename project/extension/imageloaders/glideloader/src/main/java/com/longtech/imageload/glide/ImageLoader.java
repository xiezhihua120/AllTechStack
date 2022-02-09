package com.longtech.imageload.glide;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import jp.wasabeef.glide.transformations.CropSquareTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public final class ImageLoader {

    private static volatile ImageLoader sImageLoader;

    private int defaultPlaceholderImfId;
    private int defaultErrorImfId;
    private int defaultPlaceholderCircleImfId;
    private int defaultErrorCircleImfId;

    /**
     * 初始化默认图片,需在application中初始化
     *
     * @param placeholderImgId       加载中默认图
     * @param errorImgId             错误默认图
     * @param placeholderCircleImgId 加载中圆形默认图
     * @param errorCircleImgId       错误圆形默认图
     */
    public void initDefultImg(int placeholderImgId, int errorImgId, int placeholderCircleImgId, int errorCircleImgId) {
        this.defaultPlaceholderImfId = placeholderImgId;
        this.defaultErrorImfId = errorImgId;
        this.defaultPlaceholderCircleImfId = placeholderCircleImgId;
        this.defaultErrorCircleImfId = errorCircleImgId;
    }


    private ImageLoader() {
    }

    /**
     * ImageLoader单例
     *
     * @return ImageLoader对象
     */
    public static ImageLoader getInstance() {
        if (sImageLoader == null) {
            synchronized (ImageLoader.class) {
                if (sImageLoader == null) {
                    sImageLoader = new ImageLoader();
                }
            }
        }
        return sImageLoader;
    }

    /**
     * 检查Glide的预加载环境.
     * 在android version > 17以上，Glide在加载是会先判断Activity的生命状态,若已经销毁，此时继续加载会抛
     * IllegalArgumentException-You cannot start a load for a destroyed activity
     *
     * @param context 上下文
     * @return true 允许加载
     */
    private boolean checkGlideLoadEnvironment(Context context) {
        if (context == null) {
            return false;
        } else {
            if (context instanceof FragmentActivity) {
                return assertNotDestroyed((FragmentActivity) context);
            } else if (context instanceof Activity) {
                return assertNotDestroyed((Activity) context);
            } else if (context instanceof ContextWrapper) {
                return checkGlideLoadEnvironment(((ContextWrapper) context).getBaseContext());
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static boolean assertNotDestroyed(Activity activity) {
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed());
    }

    /**
     * 普通加载网络图片
     *
     * @param context 上下文
     * @param url     网络url
     * @param image   ImageView
     */
    public void loadImage(Context context, String url, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 普通加载网络图片
     *
     * @param activity 上下文
     * @param url      网络url
     * @param image    ImageView
     */
    public void loadImage(FragmentActivity activity, String url, ImageView image) {
        if (!checkGlideLoadEnvironment(activity)) {
            return;
        }
        Glide.with(activity)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 普通加载网络图片
     *
     * @param context 上下文
     * @param url     网络url
     * @param image   ImageView
     */
    public void loadImage(Fragment context, String url, ImageView image) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 普通加载网络图片
     *
     * @param view  上下文
     * @param url   网络url
     * @param image ImageView
     */
    public void loadImage(View view, String url, ImageView image) {
        Glide.with(view)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载网络图片,并设置大小
     *
     * @param context 上下文
     * @param url     网络url
     * @param image   ImageView
     * @param width   宽
     * @param height  高
     */
    public void loadImageOverride(Context context, String url, ImageView image, int width, int height) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }

        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(width, height)
                        .fitCenter())
                .into(image);
    }

    /**
     * 加载网络图片,并设置大小
     *
     * @param fragment 上下文
     * @param url      网络url
     * @param image    ImageView
     * @param width    宽
     * @param height   高
     */
    public void loadImageOverride(Fragment fragment, String url, ImageView image, int width, int height) {
        Glide.with(fragment)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(width, height)
                        .fitCenter())
                .into(image);
    }

    /**
     * 加载网络图片,并设置大小
     *
     * @param view   上下文
     * @param url    网络url
     * @param image  ImageView
     * @param width  宽
     * @param height 高
     */
    public void loadImageOverride(View view, String url, ImageView image, int width, int height) {
        Glide.with(view)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(width, height)
                        .fitCenter())
                .into(image);
    }

    /**
     * 加载网络图片,并自行设置默认图
     *
     * @param activity     上下文
     * @param url          网络url
     * @param image        ImageView
     * @param defaultResId 默认图片资源id
     */
    public void loadImage(FragmentActivity activity, String url, @DrawableRes int defaultResId, ImageView image) {
        if (!checkGlideLoadEnvironment(activity)) {
            return;
        }
        Glide.with(activity)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultResId)
                        .error(defaultResId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载网络图片,并自行设置默认图
     *
     * @param context      上下文
     * @param url          网络url
     * @param image        ImageView
     * @param defaultResId 默认图片资源id
     */
    public void loadImage(Context context, String url, @DrawableRes int defaultResId, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultResId)
                        .error(defaultResId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载网络图片,并自行设置默认图
     *
     * @param context      上下文
     * @param url          网络url
     * @param image        ImageView
     * @param defaultResId 默认图片资源id
     */
    public void loadImage(Fragment context, String url, @DrawableRes int defaultResId, ImageView image) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultResId)
                        .error(defaultResId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载网络图片,并自行设置默认图
     *
     * @param view         上下文
     * @param url          网络url
     * @param image        ImageView
     * @param defaultResId 默认图片资源id
     */
    public void loadImage(View view, String url, @DrawableRes int defaultResId, ImageView image) {
        Glide.with(view)
                .load(url)
                .apply(new RequestOptions().placeholder(defaultResId)
                        .error(defaultResId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载本地图片
     *
     * @param context 上下文
     * @param image   ImageView
     * @param uri     本地文件
     */
    public void loadImage(Context context, Uri uri, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载本地图片
     *
     * @param context 上下文
     * @param image   ImageView
     * @param uri     本地文件
     */
    public void loadImage(FragmentActivity context, Uri uri, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载本地图片
     *
     * @param context 上下文
     * @param image   ImageView
     * @param uri     本地文件
     */
    public void loadImage(Fragment context, Uri uri, ImageView image) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载本地图片
     *
     * @param context 上下文
     * @param image   ImageView
     * @param uri     本地文件
     */
    public void loadImage(View context, Uri uri, ImageView image) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载资源文件
     *
     * @param context    上下文
     * @param image      ImageView
     * @param resourceId 资源文件id
     */
    public void loadImage(Context context, @DrawableRes int resourceId, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(resourceId)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);

    }

    /**
     * 加载资源文件
     *
     * @param context    上下文
     * @param image      ImageView
     * @param resourceId 资源文件id
     */
    public void loadImage(FragmentActivity context, @DrawableRes int resourceId, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(resourceId)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);

    }

    /**
     * 加载资源文件
     *
     * @param context    上下文
     * @param image      ImageView
     * @param resourceId 资源文件id
     */
    public void loadImage(Fragment context, @DrawableRes int resourceId, ImageView image) {
        Glide.with(context)
                .load(resourceId)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);

    }

    /**
     * 加载资源文件
     *
     * @param context    上下文
     * @param image      ImageView
     * @param resourceId 资源文件id
     */
    public void loadImage(View context, @DrawableRes int resourceId, ImageView image) {
        Glide.with(context)
                .load(resourceId)
                .apply(new RequestOptions().placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);

    }

    /**
     * 加载网络图片 并设置圆形
     *
     * @param context 上下文
     * @param image   ImageView
     * @param url     网络图片rul
     */
    public void loadImageCircle(Context context, String url, ImageView image) {
        loadImageCircle(context, url, defaultErrorCircleImfId, defaultPlaceholderCircleImfId, image);
    }

    /**
     * 加载网络图片 并设置圆形
     *
     * @param context 上下文
     * @param image   ImageView
     * @param url     网络图片rul
     */
    public void loadImageCircle(FragmentActivity context, String url, ImageView image) {
        loadImageCircle(context, url, defaultErrorCircleImfId, defaultPlaceholderCircleImfId, image);
    }

    /**
     * 加载网络图片 并设置圆形
     *
     * @param fragment 上下文
     * @param image    ImageView
     * @param url      网络图片rul
     */
    public void loadImageCircle(Fragment fragment, String url, ImageView image) {
        loadImageCircle(fragment, url, defaultErrorCircleImfId, defaultPlaceholderCircleImfId, image);
    }

    /**
     * 加载网络图片 并设置圆形
     *
     * @param view  上下文
     * @param image ImageView
     * @param url   网络图片rul
     */
    public void loadImageCircle(View view, String url, ImageView image) {
        loadImageCircle(view, url, defaultErrorCircleImfId, defaultPlaceholderCircleImfId, image);
    }


    /**
     * 加载网络图片,并设置圆形,占位图和错误图用同一个
     *
     * @param context                上下文
     * @param url                    地址
     * @param placeHolderAndErrorRes 图片未加载和加载失败情况下的图片
     * @param image                  ImageView
     */
    public void loadImageCircle(Context context, String url, @DrawableRes int placeHolderAndErrorRes, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(placeHolderAndErrorRes)
                        .error(placeHolderAndErrorRes)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transforms(new CircleCrop()))
                .into(image);
    }

    /**
     * 加载网络图片,并设置圆形,占位图和错误图用同一个
     *
     * @param context                上下文
     * @param url                    地址
     * @param placeHolderAndErrorRes 图片未加载和加载失败情况下的图片
     * @param image                  ImageView
     */
    public void loadImageCircle(Fragment context, String url, @DrawableRes int placeHolderAndErrorRes, ImageView image) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(placeHolderAndErrorRes)
                        .error(placeHolderAndErrorRes)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transforms(new CircleCrop()))
                .into(image);
    }

    /**
     * 加载网络图片,并设置圆形,可以设置占位图,可以设置错误图
     *
     * @param context        上下文
     * @param url            地址
     * @param errorRes       图片加载失败情况下的图片
     * @param placeHolderRes 图片未加载时的图片
     * @param image          ImageView
     */
    public void loadImageCircle(Context context, String url, @DrawableRes int errorRes, @DrawableRes int placeHolderRes, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(placeHolderRes)
                        .error(errorRes)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop())
                .into(image);

    }

    /**
     * 加载网络图片,并设置圆形,可以设置占位图,可以设置错误图
     *
     * @param activity       上下文
     * @param url            地址
     * @param errorRes       图片加载失败情况下的图片
     * @param placeHolderRes 图片未加载时的图片
     * @param image          ImageView
     */
    public void loadImageCircle(FragmentActivity activity, String url, @DrawableRes int errorRes, @DrawableRes int placeHolderRes, ImageView image) {
        if (!checkGlideLoadEnvironment(activity)) {
            return;
        }
        Glide.with(activity)
                .load(url)
                .apply(new RequestOptions().placeholder(placeHolderRes)
                        .error(errorRes)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop())
                .into(image);
    }

    /**
     * 加载网络图片,并设置圆形,可以设置占位图,可以设置错误图
     *
     * @param context        上下文
     * @param url            地址
     * @param errorRes       图片加载失败情况下的图片
     * @param placeHolderRes 图片未加载时的图片
     * @param image          ImageView
     */
    public void loadImageCircle(Fragment context, String url, @DrawableRes int errorRes, @DrawableRes int placeHolderRes, ImageView image) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(placeHolderRes)
                        .error(errorRes)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop())
                .into(image);
    }

    /**
     * 加载网络图片,并设置圆形,可以设置占位图,可以设置错误图
     *
     * @param view           上下文
     * @param url            地址
     * @param errorRes       图片加载失败情况下的图片
     * @param placeHolderRes 图片未加载时的图片
     * @param image          ImageView
     */
    public void loadImageCircle(View view, String url, @DrawableRes int errorRes, @DrawableRes int placeHolderRes, ImageView image) {
        Glide.with(view)
                .load(url)
                .apply(new RequestOptions().placeholder(placeHolderRes)
                        .error(errorRes)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop())
                .into(image);
    }

    /**
     * 加载网络圆形图,并监听
     *
     * @param context 上下文
     * @param url     网络图片rul
     * @param handler Handler实例
     */
    public void loadImageListenter(Context context, String url, final Handler handler) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CircleCrop())
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        Message msg = new Message();
                        msg.obj = resource;
                        handler.sendMessage(msg);
                    }
                });

    }

    /**
     * 加载网络圆形图,并监听
     *
     * @param context 上下文
     * @param url     网络图片rul
     * @param handler Handler实例
     */
    public void loadImageListenter(Fragment context, String url, final Handler handler) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CircleCrop())
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        Message msg = new Message();
                        msg.obj = resource;
                        handler.sendMessage(msg);
                    }
                });

    }

    /**
     * 加载网络指定的填充模式,并监听
     *
     * @param context 上下文
     * @param url     网络图片rul
     * @param handler Handler实例
     */
    public void loadImageListenter(Context context, String url, final Handler handler, Transformation<Bitmap> transformation) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(transformation)
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        Message msg = new Message();
                        msg.obj = resource;
                        handler.sendMessage(msg);
                    }
                });

    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片url
     * @param radius  角度
     * @param image   要显示的imageView
     */
    public void loadRoundImage(Context context, String url, int radius, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, RoundedCornersTransformation.CornerType.ALL))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片url
     * @param radius  角度
     * @param image   要显示的imageView
     */
    public void loadRoundImage(FragmentActivity context, String url, int radius, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, RoundedCornersTransformation.CornerType.ALL))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片url
     * @param radius  角度
     * @param image   要显示的imageView
     */
    public void loadRoundImage(Fragment context, String url, int radius, ImageView image) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, RoundedCornersTransformation.CornerType.ALL))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param view   上下文
     * @param url    图片url
     * @param radius 角度
     * @param image  要显示的imageView
     */
    public void loadRoundImage(View view, String url, int radius, ImageView image) {
        Glide.with(view)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, RoundedCornersTransformation.CornerType.ALL))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片可设置默认图
     *
     * @param context        上下文
     * @param url            图片url
     * @param radius         角度
     * @param image          要显示的imageView
     * @param placeHolderRes 图片未加载时的图片
     * @param tErrorImfId    图片未加载时的图片
     */
    public void loadRoundImage(Context context, String url, int radius, ImageView image, int placeHolderRes, int tErrorImfId) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, RoundedCornersTransformation.CornerType.ALL))
                        .placeholder(placeHolderRes)
                        .error(tErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片可设置默认图
     *
     * @param context        上下文
     * @param url            图片url
     * @param radius         角度
     * @param image          要显示的imageView
     * @param placeHolderRes 图片未加载时的图片
     * @param tErrorImfId    图片未加载时的图片
     */
    public void loadRoundImage(FragmentActivity context, String url, int radius, ImageView image, int placeHolderRes, int tErrorImfId) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, RoundedCornersTransformation.CornerType.ALL))
                        .placeholder(placeHolderRes)
                        .error(tErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片可设置默认图
     *
     * @param context        上下文
     * @param url            图片url
     * @param radius         角度
     * @param image          要显示的imageView
     * @param placeHolderRes 图片未加载时的图片
     * @param tErrorImfId    图片未加载时的图片
     */
    public void loadRoundImage(Fragment context, String url, int radius, ImageView image, int placeHolderRes, int tErrorImfId) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, RoundedCornersTransformation.CornerType.ALL))
                        .placeholder(placeHolderRes)
                        .error(tErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片可设置默认图
     *
     * @param context        上下文
     * @param url            图片url
     * @param radius         角度
     * @param image          要显示的imageView
     * @param placeHolderRes 图片未加载时的图片
     * @param tErrorImfId    图片未加载时的图片
     */
    public void loadRoundImage(View context, String url, int radius, ImageView image, int placeHolderRes, int tErrorImfId) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, RoundedCornersTransformation.CornerType.ALL))
                        .placeholder(placeHolderRes)
                        .error(tErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context    上下文
     * @param url        图片url
     * @param radius     角度
     * @param cornerType 要圆角的未知
     * @param image      要显示的imageView
     */
    public void loadRoundImage(Context context, String url, int radius, RoundedCornersTransformation.CornerType cornerType, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, cornerType))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context    上下文
     * @param url        图片url
     * @param radius     角度
     * @param cornerType 要圆角的未知
     * @param image      要显示的imageView
     */
    public void loadRoundImage(FragmentActivity context, String url, int radius, RoundedCornersTransformation.CornerType cornerType, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, cornerType))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context    上下文
     * @param url        图片url
     * @param radius     角度
     * @param cornerType 要圆角的未知
     * @param image      要显示的imageView
     */
    public void loadRoundImage(Fragment context, String url, int radius, RoundedCornersTransformation.CornerType cornerType, ImageView image) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, cornerType))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }


    /**
     * 加载圆角图片
     *
     * @param context    上下文
     * @param url        图片url
     * @param radius     角度
     * @param cornerType 要圆角的未知
     * @param image      要显示的imageView
     */
    public void loadRoundImage(View context, String url, int radius, RoundedCornersTransformation.CornerType cornerType, ImageView image) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(radius, 0, cornerType))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片地址
     * @param radius  圆角角度
     * @param image   view
     */
    public void loadRoundImageNew(Context context, String url, int radius, ImageView image) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .placeholder(defaultPlaceholderImfId)
                .error(defaultErrorImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片地址
     * @param radius  圆角角度
     * @param image   view
     */
    public void loadRoundImageNew(FragmentActivity context, String url, int radius, ImageView image) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .placeholder(defaultPlaceholderImfId)
                .error(defaultErrorImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片地址
     * @param radius  圆角角度
     * @param image   view
     */
    public void loadRoundImageNew(Fragment context, String url, int radius, ImageView image) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .placeholder(defaultPlaceholderImfId)
                .error(defaultErrorImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片地址
     * @param radius  圆角角度
     * @param image   view
     */
    public void loadRoundImageNew(View context, String url, int radius, ImageView image) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .placeholder(defaultPlaceholderImfId)
                .error(defaultErrorImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context                 上下文
     * @param url                     图片地址
     * @param radius                  圆角角度
     * @param image                   view
     * @param defaultPlaceholderImfId 默认图
     */
    public void loadRoundImageNew(Context context, String url, int radius, ImageView image, int defaultPlaceholderImfId) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .placeholder(defaultPlaceholderImfId)
                .error(defaultPlaceholderImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context                 上下文
     * @param url                     图片地址
     * @param radius                  圆角角度
     * @param image                   view
     * @param defaultPlaceholderImfId 默认图
     */
    public void loadRoundImageNew(FragmentActivity context, String url, int radius, ImageView image, int defaultPlaceholderImfId) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .placeholder(defaultPlaceholderImfId)
                .error(defaultPlaceholderImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context                 上下文
     * @param url                     图片地址
     * @param radius                  圆角角度
     * @param image                   view
     * @param defaultPlaceholderImfId 默认图
     */
    public void loadRoundImageNew(Fragment context, String url, int radius, ImageView image, int defaultPlaceholderImfId) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .placeholder(defaultPlaceholderImfId)
                .error(defaultPlaceholderImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context                 上下文
     * @param url                     图片地址
     * @param radius                  圆角角度
     * @param image                   view
     * @param defaultPlaceholderImfId 默认图
     */
    public void loadRoundImageNew(View context, String url, int radius, ImageView image, int defaultPlaceholderImfId) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .placeholder(defaultPlaceholderImfId)
                .error(defaultPlaceholderImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片地址
     * @param radius  圆角角度
     * @param image   view
     * @param width   宽
     * @param height  高
     */
    public void loadRoundImageNew(Context context, String url, int radius, ImageView image, int width, int height) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .override(width, height)
                .placeholder(defaultPlaceholderImfId)
                .error(defaultErrorImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片地址
     * @param radius  圆角角度
     * @param image   view
     * @param width   宽
     * @param height  高
     */
    public void loadRoundImageNew(FragmentActivity context, String url, int radius, ImageView image, int width, int height) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .override(width, height)
                .placeholder(defaultPlaceholderImfId)
                .error(defaultErrorImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片地址
     * @param radius  圆角角度
     * @param image   view
     * @param width   宽
     * @param height  高
     */
    public void loadRoundImageNew(Fragment context, String url, int radius, ImageView image, int width, int height) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .override(width, height)
                .placeholder(defaultPlaceholderImfId)
                .error(defaultErrorImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context 上下文
     * @param url     图片地址
     * @param radius  圆角角度
     * @param image   view
     * @param width   宽
     * @param height  高
     */
    public void loadRoundImageNew(View context, String url, int radius, ImageView image, int width, int height) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .override(width, height)
                .placeholder(defaultPlaceholderImfId)
                .error(defaultErrorImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context                 上下文
     * @param url                     图片地址
     * @param radius                  圆角角度
     * @param image                   view
     * @param width                   宽
     * @param height                  高
     * @param defaultPlaceholderImfId 默认图
     */
    public void loadRoundImageNew(Context context, String url, int radius, ImageView image, int width, int height, int defaultPlaceholderImfId) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .override(width, height)
                .placeholder(defaultPlaceholderImfId)
                .error(defaultPlaceholderImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context                 上下文
     * @param url                     图片地址
     * @param radius                  圆角角度
     * @param image                   view
     * @param width                   宽
     * @param height                  高
     * @param defaultPlaceholderImfId 默认图
     */
    public void loadRoundImageNew(FragmentActivity context, String url, int radius, ImageView image, int width, int height, int defaultPlaceholderImfId) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .override(width, height)
                .placeholder(defaultPlaceholderImfId)
                .error(defaultPlaceholderImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context                 上下文
     * @param url                     图片地址
     * @param radius                  圆角角度
     * @param image                   view
     * @param width                   宽
     * @param height                  高
     * @param defaultPlaceholderImfId 默认图
     */
    public void loadRoundImageNew(Fragment context, String url, int radius, ImageView image, int width, int height, int defaultPlaceholderImfId) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .override(width, height)
                .placeholder(defaultPlaceholderImfId)
                .error(defaultPlaceholderImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }

    /**
     * 加载圆角图片
     *
     * @param context                 上下文
     * @param url                     图片地址
     * @param radius                  圆角角度
     * @param image                   view
     * @param width                   宽
     * @param height                  高
     * @param defaultPlaceholderImfId 默认图
     */
    public void loadRoundImageNew(View context, String url, int radius, ImageView image, int width, int height, int defaultPlaceholderImfId) {
        RequestOptions options = new RequestOptions()
                .transforms(new CenterCrop(), new RoundedCorners(radius))
                .override(width, height)
                .placeholder(defaultPlaceholderImfId)
                .error(defaultPlaceholderImfId)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(options).into(image);
    }


    /**
     * 加载网络图片 并设置正方形
     *
     * @param context 上下文
     * @param image   ImageView
     * @param url     网络图片rul
     */
    public void loadImageSquare(Context context, String url, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CropSquareTransformation())
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);

    }

    /**
     * 加载网络图片 并设置正方形
     *
     * @param context 上下文
     * @param image   ImageView
     * @param url     网络图片rul
     */
    public void loadImageSquare(Fragment context, String url, ImageView image) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CropSquareTransformation())
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);

    }

    /**
     * 加载网络图片 并设置矩形 自定义高度+宽度
     *
     * @param context 上下文
     * @param image   ImageView
     * @param url     网络图片rul
     * @param height  高
     * @param width   宽
     */
    public void loadImage(Context context, String url, ImageView image, int width, int height) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CropTransformation(width, height, CropTransformation.CropType.CENTER))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);

    }

    /**
     * 加载网络图片 并设置矩形 自定义高度+宽度
     *
     * @param context 上下文
     * @param image   ImageView
     * @param url     网络图片rul
     * @param height  高
     * @param width   宽
     */
    public void loadImage(FragmentActivity context, String url, ImageView image, int width, int height) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CropTransformation(width, height, CropTransformation.CropType.CENTER))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);

    }

    /**
     * 加载网络图片 并设置矩形 自定义高度+宽度
     *
     * @param context 上下文
     * @param image   ImageView
     * @param url     网络图片rul
     * @param height  高
     * @param width   宽
     */
    public void loadImage(Fragment context, String url, ImageView image, int width, int height) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CropTransformation(width, height, CropTransformation.CropType.CENTER))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);

    }

    /**
     * 加载网络图片 并设置矩形 自定义高度+宽度
     *
     * @param context 上下文
     * @param image   ImageView
     * @param url     网络图片rul
     * @param height  高
     * @param width   宽
     */
    public void loadImage(View context, String url, ImageView image, int width, int height) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CropTransformation(width, height, CropTransformation.CropType.CENTER))
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(image);

    }

    /**
     * 加载bitmap,并回调
     *
     * @param context      上下文
     * @param url          路径
     * @param simpleTarget 要加载的内容
     */
    public void loadBitmap(Context context, String url, SimpleTarget<Bitmap> simpleTarget) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(simpleTarget);
    }

    /**
     * 加载bitmap,并回调
     *
     * @param context      上下文
     * @param url          路径
     * @param simpleTarget 要加载的内容
     */
    public void loadBitmap(FragmentActivity context, String url, SimpleTarget<Bitmap> simpleTarget) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(simpleTarget);
    }

    /**
     * 加载bitmap,并回调
     *
     * @param context      上下文
     * @param url          路径
     * @param simpleTarget 要加载的内容
     */
    public void loadBitmap(Fragment context, String url, SimpleTarget<Bitmap> simpleTarget) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(simpleTarget);
    }

    /**
     * 加载bitmap,并回调
     *
     * @param context      上下文
     * @param url          路径
     * @param simpleTarget 要加载的内容
     */
    public void loadBitmap(View context, String url, SimpleTarget<Bitmap> simpleTarget) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(simpleTarget);
    }

    /**
     * 自定义视图(多用于自定义view加载图片)
     *
     * @param context      上下文
     * @param url          路径
     * @param simpleTarget 要加载的内容
     */
    public void loadImageContent(Context context, String url, SimpleTarget<Drawable> simpleTarget) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().centerCrop())
                .into(simpleTarget);
    }

    /**
     * 自定义视图(多用于自定义view加载图片)
     *
     * @param context      上下文
     * @param url          路径
     * @param simpleTarget 要加载的内容
     */
    public void loadImageContent(Fragment context, String url, SimpleTarget<Drawable> simpleTarget) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().centerCrop())
                .into(simpleTarget);
    }

    /**
     * 自定义ImageViewTarget
     *
     * @param context         上下文
     * @param url             路径
     * @param imageViewTarget 要加载的内容
     */
    public void loadImage(Context context, String url, ImageViewTarget<Drawable> imageViewTarget) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .centerCrop())
                .into(imageViewTarget);
    }

    /**
     * 自定义ImageViewTarget
     *
     * @param context         上下文
     * @param url             路径
     * @param imageViewTarget 要加载的内容
     */
    public void loadImage(FragmentActivity context, String url, ImageViewTarget<Drawable> imageViewTarget) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .centerCrop())
                .into(imageViewTarget);
    }

    /**
     * 自定义ImageViewTarget
     *
     * @param context         上下文
     * @param url             路径
     * @param imageViewTarget 要加载的内容
     */
    public void loadImage(Fragment context, String url, ImageViewTarget<Drawable> imageViewTarget) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .centerCrop())
                .into(imageViewTarget);
    }

    /**
     * 自定义ImageViewTarget
     *
     * @param context         上下文
     * @param url             路径
     * @param imageViewTarget 要加载的内容
     */
    public void loadImage(View context, String url, ImageViewTarget<Drawable> imageViewTarget) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .centerCrop())
                .into(imageViewTarget);
    }

    /**
     * 自定义ImageViewTarget
     *
     * @param context         上下文
     * @param url             路径
     * @param imageViewTarget 要加载的内容
     */
    public void loadImage(Context context, String url, @DrawableRes int defaultResId, ImageViewTarget<Drawable> imageViewTarget) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(defaultResId)
                        .error(defaultResId)
                        .centerCrop())
                .into(imageViewTarget);
    }

    /**
     * 自定义ImageViewTarget
     *
     * @param context         上下文
     * @param url             路径
     * @param imageViewTarget 要加载的内容
     */
    public void loadImage(Fragment context, String url, @DrawableRes int defaultResId, ImageViewTarget<Drawable> imageViewTarget) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(defaultResId)
                        .error(defaultResId)
                        .centerCrop())
                .into(imageViewTarget);
    }

    /**
     * 加载本地目录图片
     *
     * @param context 上下文
     * @param url     本地资源路径
     * @param image   要显示的imageView
     */
    public void loadImageLocal(Context context, String url, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load("file:///" + url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId))
                //.diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(image);
    }

    /**
     * 加载本地目录图片
     *
     * @param context 上下文
     * @param url     本地资源路径
     * @param image   要显示的imageView
     */
    public void loadImageLocal(FragmentActivity context, String url, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load("file:///" + url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId))
                //.diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(image);
    }

    /**
     * 加载本地目录图片
     *
     * @param context 上下文
     * @param url     本地资源路径
     * @param image   要显示的imageView
     */
    public void loadImageLocal(Fragment context, String url, ImageView image) {
        Glide.with(context)
                .load("file:///" + url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId))
                //.diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(image);
    }

    /**
     * 加载本地目录图片
     *
     * @param context 上下文
     * @param url     本地资源路径
     * @param image   要显示的imageView
     */
    public void loadImageLocal(View context, String url, ImageView image) {
        Glide.with(context)
                .load("file:///" + url)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId))
                //.diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(image);
    }

    /**
     * 设置动态GIF加载方式
     *
     * @param context 上下文
     * @param url     网络GIF资源路径
     * @param image   要显示的imageView
     */
    public void loadImageDynamicGif(Context context, String url, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .asGif()
                .load(url)
                .into(image);
    }


    /**
     * 设置动态GIF加载方式
     *
     * @param context 上下文
     * @param url     网络GIF资源路径
     * @param image   要显示的imageView
     */
    public void loadImageDynamicGif(Fragment context, String url, ImageView image) {
        Glide.with(context)
                .asGif()
                .load(url)
                .into(image);
    }

    /**
     * 设置动态GIF加载方式
     *
     * @param context 上下文
     * @param resId   资源文件id
     * @param image   要显示的imageView
     */
    public void loadImageDynamicGif(Context context, int resId, ImageView image) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .asGif()
                .load(resId)
                .into(image);
    }

    /**
     * 设置动态GIF加载方式
     *
     * @param context 上下文
     * @param resId   资源文件id
     * @param image   要显示的imageView
     */
    public void loadImageDynamicGif(Fragment context, int resId, ImageView image) {
        Glide.with(context)
                .asGif()
                .load(resId)
                .into(image);
    }

    /**
     * 设置监听请求接口
     *
     * @param context 上下文
     * @param url     图片路径
     * @param image   要显示的imageView
     * @param request 监听回调
     */
    public void loadImageListener(Context context, String url, ImageView image, RequestListener<Drawable> request) {
        if (!checkGlideLoadEnvironment(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().error(defaultErrorImfId).placeholder(defaultPlaceholderImfId))
                .listener(request)
                .into(image);
    }

    /**
     * 设置监听请求接口
     *
     * @param context 上下文
     * @param url     图片路径
     * @param image   要显示的imageView
     * @param request 监听回调
     */
    public void loadImageListener(Fragment context, String url, ImageView image, RequestListener<Drawable> request) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().error(defaultErrorImfId).placeholder(defaultPlaceholderImfId))
                .listener(request)
                .into(image);
    }

    /**
     * 加载一张图片并且根据 targetWidth/targetHeight的比值进行自动缩放
     * @param context 上下文
     * @param imageUrl 图片url
     * @param imageView 控件
     * @param targetWith 控件的宽度
     * @param targetHeight 控件的高度
     */
    public void loadImageWithAutoScale(
            @NonNull Context context,
            @Nullable String imageUrl,
            @Nullable ImageView imageView,
            int targetWith,
            int targetHeight) {
        if (!checkGlideLoadEnvironment(context) || imageView == null) {
            return;
        }
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions()
                        .placeholder(defaultPlaceholderImfId)
                        .error(defaultErrorImfId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(new ImageViewTarget<Bitmap>(imageView) {
                    @Override
                    protected void setResource(@Nullable Bitmap resource) {
                        if (view == null) {
                            return;
                        }

                        if (resource == null) {
                            view.setScaleType(ImageView.ScaleType.FIT_XY);
                        } else {
                            int width = resource.getWidth();
                            int height = resource.getHeight();
                            float sourceWHRatio = width * 1.0F / height;
                            float imageWHRatio = targetWith * 1.0F / targetHeight;
                            if (Math.round(sourceWHRatio) == Math.round(imageWHRatio)) {
                                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            } else {
                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            }
                            imageView.setImageBitmap(resource);
                        }
                    }
                });
    }
}
