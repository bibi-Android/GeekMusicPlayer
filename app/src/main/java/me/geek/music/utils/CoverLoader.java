package me.geek.music.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.LruCache;

import me.geek.music.R;
import me.geek.music.application.AppCache;

/**
 * 专辑封面图片加载器
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class CoverLoader {
    private static final String KEY_NULL = "null";

    /**
     * 缩略图缓存,用于音乐列表
     */
    private LruCache<String, Bitmap> mThumbnailCacahe;

    /**
     * 高斯模糊图缓存,用于播放页背景
     */
    private LruCache<String, Bitmap> mBlurCache;

    /**
     * 圆形图缓存,用于播放页CD
     */
    private LruCache<String, Bitmap> mRoundCache;

    private CoverLoader(){
        //获取当前进程的可用内存
        int maxMeory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //缓存大小为当前进程可用内存的1/8
        int cacheSize = maxMeory / 8;

        mThumbnailCacahe = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                //转换为KB, 与cacheSize单位一致,必须重写此方法，来测量Bitmap的大小
                return bitmap.getByteCount() / 1024;
            }
        };

        mBlurCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        mRoundCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

    }

    /**
     * 用单例设计来控制CoverLoader对象创建的次数
     */
    private static class SingletonHolder{
        private static CoverLoader instance = new CoverLoader();
    }

    public static CoverLoader getInstance(){
        return SingletonHolder.instance;
    }


    /**
     * 获取指定大小的bitmap
     */
    private Bitmap loadBitmap(String uri, int length){
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;//仅获取大小,不返回bitmap，但是能获取bitmap的宽和高（op.outWidth和op.outWidth)
        BitmapFactory.decodeFile(uri, options);//获取尺寸信息
        int maxLength = options.outWidth > options.outHeight? options.outWidth : options.outHeight;//返回最长的一边

        //压缩尺寸,避免卡顿
        int inSampleSize = maxLength / length;//length所需要的宽度
        if(inSampleSize < 1){//如果图片最长的部分小于所需长度则不需要进行压缩
            inSampleSize = 1;
        }
        options.inSampleSize = inSampleSize;//图片的压缩
        //获取bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(uri, options);


    }

    /**
     * 加载缩略图
     */
    public Bitmap loadThumbnail(String uri){
        Bitmap bitmap;
        if(TextUtils.isEmpty(uri)){
            bitmap = mThumbnailCacahe.get(KEY_NULL);//从缓存中获取
            if(bitmap == null){
                bitmap = BitmapFactory.decodeResource(AppCache.getContext().getResources(), R.drawable.default_cover);//用于根据给定的资源ID从指定的资源文件中解析、创建Bitmap对象。
                mThumbnailCacahe.put(KEY_NULL, bitmap);
            }
        }else{//如果给定的uri不是空
            bitmap = mThumbnailCacahe.get(uri);
            if(bitmap == null){//uri存在,但是还没放入缓存中
                bitmap = loadBitmap(uri, ScreenUtils.getScreenWidth() / 10);
                if(bitmap == null){
                    bitmap = loadThumbnail(null);//uri参数无效,用默认图
                }
            }
            mThumbnailCacahe.put(uri, bitmap);//放入缓存
        }
        return bitmap;
    }

    /**
     * 加载高斯模糊图
     */
    public Bitmap loadBlur(String uri){
        Bitmap bitmap;
        if(TextUtils.isEmpty(uri)){//没有资源链接,看看缓存有没有
            bitmap = mBlurCache.get(KEY_NULL);
            if(bitmap == null){//缓存中没有,使用默认图
                bitmap = BitmapFactory.decodeResource(AppCache.getContext().getResources(), R.drawable.play_page_default_bg);
                mThumbnailCacahe.put(KEY_NULL, bitmap);
            }
        }else{//有资源uri,直接调用
            bitmap = mBlurCache.get(uri);
            if(bitmap == null){
                bitmap = loadBitmap(uri, ScreenUtils.getScreenWidth() / 2);///缓存中没有,尝试加载uri的bitmap
                if(bitmap == null){//如果bitmap还是空
                    bitmap = loadBlur(null);
                }else{
                    bitmap = ImageUtils.blur(bitmap, ImageUtils.BLUR_RADIUS);
                }
                mBlurCache.put(uri, bitmap);//放入缓存

            }
        }
        return bitmap;
    }

    /**
     * 加载圆形专辑图
     */
    public Bitmap loadRound(String uri){
        Bitmap bitmap;
        if(TextUtils.isEmpty(uri)){
            bitmap = mRoundCache.get(KEY_NULL);
            if(bitmap == null){
                bitmap = BitmapFactory.decodeResource(AppCache.getContext().getResources(), R.drawable.play_page_default_cover);
                bitmap = ImageUtils.resizeImage(bitmap, ScreenUtils.getScreenWidth() / 2, ScreenUtils.getScreenWidth() / 2);
                mRoundCache.put(KEY_NULL, bitmap);
            }
        }else{
            bitmap = mRoundCache.get(uri);
            if(bitmap == null){
                bitmap = loadBitmap(uri, ScreenUtils.getScreenWidth() / 2);
                if(bitmap == null){
                    bitmap = loadRound(null);
                }else{
                    bitmap = ImageUtils.resizeImage(bitmap, ScreenUtils.getScreenWidth() / 2, ScreenUtils.getScreenWidth() / 2 );
                    bitmap = ImageUtils.createCircleImage(bitmap);
                }
                mRoundCache.put(uri, bitmap);
            }
        }
        return bitmap;
    }



}
