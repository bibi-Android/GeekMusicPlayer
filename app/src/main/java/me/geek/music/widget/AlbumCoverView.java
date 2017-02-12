package me.geek.music.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import me.geek.music.R;
import me.geek.music.utils.CoverLoader;
import me.geek.music.utils.ImageUtils;
import me.geek.music.utils.ScreenUtils;

/**
 * 专辑封面,自定义旋转专辑View
 * 这里有5个地方需要绘制
 * 1.最上面的一条虚线
 * 2.黑胶外侧的半透明边框，这个不需要旋转也比较简单。
 * 3.黑胶
 * 4.专辑封面
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class AlbumCoverView extends View implements ValueAnimator.AnimatorUpdateListener {

    private static final long TIME_UPDATE = 50L;
    private static final float DISC_ROTATION_INCREASE = 0.5f;
    private static final float NEEDLE_ROTATION_PLAY = 0.0f;
    private static final float NEEDLE_ROTATION_PAUSE = -25.0f;
    private Handler mHandler = new Handler();
    private Bitmap mDiscBitmap;//外环黑色胶带
    private Bitmap mCoverBitmap;//专辑图
    private Bitmap mNeedleBitmap;//磁针
    private Drawable mTopLine;//可绘制的对象抽象为Drawable,顶部的虚线
    private Drawable mCoverBorder;//在黑胶外环的虚线
    private int mTopLineHeight;
    private int mCoverBorderWidth;
    private Matrix mDiscMatrix = new Matrix();
    private Matrix mCoverMatrix = new Matrix();
    private Matrix mNeedleMatrix = new Matrix();
    private ValueAnimator mPlayAnimator;
    private ValueAnimator mPauseAnimator;
    private float mDiscRotation = 0.0f;
    private float mNeedleRotation = NEEDLE_ROTATION_PLAY;
    private boolean isPlaying = false;

    //图片起始坐标
    private Point mDiscPoint = new Point();//黑胶的起始坐标
    private Point mCoverPoint = new Point();//专辑封面的起始坐标
    private Point mNeedlePoint = new Point();//指针的起始坐标

    //旋转中心坐标
    private Point mDiscCenterPoint = new Point();
    private Point mCoverCenterPoint = new Point();
    private Point mNeedleCenterPoint = new Point();


    public AlbumCoverView(Context context) {
        this(context, null);
    }

    public AlbumCoverView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumCoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */

    private void init() {
        //使用drawable资源但不为其设置theme主题
        mTopLine = ResourcesCompat.getDrawable(getResources(), R.drawable.play_page_cover_top_line_shape, null);
        mCoverBorder = ResourcesCompat.getDrawable(getResources(), R.drawable.play_page_cover_border_shape, null);

        mDiscBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.play_page_disc);
        //黑胶直径为屏幕的3/4
        mDiscBitmap = ImageUtils.resizeImage(mDiscBitmap, (int)((ScreenUtils.getScreenWidth()*0.75)), (int) (ScreenUtils.getScreenWidth() * 0.75));

        mCoverBitmap = CoverLoader.getInstance().loadRound(null);

        mNeedleBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.play_page_needle);
        mNeedleBitmap = ImageUtils.resizeImage(mNeedleBitmap, (int) (ScreenUtils.getScreenWidth() * 0.25),
                (int) (ScreenUtils.getScreenWidth() * 0.375));

        mTopLineHeight = ScreenUtils.dp2px(1);
        mCoverBorderWidth = ScreenUtils.dp2px(1);

        //ValueAnimator.ofFloat(-25.0f, 0.0f)构造了一个比较复杂的动画渐变，值是-25f,然后变化到0；
        mPlayAnimator = ValueAnimator.ofFloat(NEEDLE_ROTATION_PAUSE, NEEDLE_ROTATION_PLAY);
        //设置动画时长，单位是毫秒,0.3s
        mPlayAnimator.setDuration(300);
        mPlayAnimator.addUpdateListener(this);

        mPauseAnimator = ValueAnimator.ofFloat(NEEDLE_ROTATION_PLAY, NEEDLE_ROTATION_PAUSE);
        mPauseAnimator.setDuration(300);
        mPauseAnimator.addUpdateListener(this);

    }

    @Override
    /**
     * 在view给其孩子设置尺寸和位置时被调用。子view，包括孩子在内，必须重写onLayout(boolean, int, int, int, int)方法，并且调用各自的layout(int, int, int, int)方法。
     */
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initSize();
    }


    /**
     * 确定图片起始坐标与旋转中心坐标
     */
    private void initSize() {
        int discOffsetY = mNeedleBitmap.getHeight() / 2;//刚好到黑胶顶部的高度

        //图片起始图标
        mDiscPoint.x = (getWidth()-mDiscBitmap.getWidth()) / 2;//黑胶最左边的x方向的位置,设定为(View宽度-黑胶直径)的一半
        mDiscPoint.y = discOffsetY;//指针高度的一半

        mCoverPoint.x = (getWidth()-mCoverBitmap.getWidth()) / 2;
        mCoverPoint.y = discOffsetY + (mDiscBitmap.getHeight() - mCoverBitmap.getHeight()) / 2;

        mNeedlePoint.x = getWidth() / 2  - mNeedleBitmap.getWidth() / 6;
        mNeedlePoint.y = -mNeedleBitmap.getWidth() / 6;

        //旋转中心坐标
        mDiscCenterPoint.x = getWidth() / 2;
        mDiscCenterPoint.y = mDiscBitmap.getHeight() / 2  + discOffsetY;

        mCoverCenterPoint.x = mDiscCenterPoint.x;
        mCoverCenterPoint.y = mDiscCenterPoint.y;

        mNeedleCenterPoint.x = mCoverCenterPoint.x;
        mNeedleCenterPoint.y = 0;
    }

    /**
     *  Paint     就是画笔
     *  Bitmap    就是画布
     *  Canvas   就是画家
     */
    @Override
    protected void onDraw(Canvas canvas) {//继承View,在onDraw函数中实现绘图
        super.onDraw(canvas);

        //1. 绘制顶部的虚线
        //设置一个绘图的矩形区域,setBounds(int left, int top, int right, int bottom);
        mTopLine.setBounds(0, 0, getWidth(), mTopLineHeight);
        mTopLine.draw(canvas);

        //2. 绘制黑胶唱片外侧般透明框
        mCoverBorder.setBounds(mDiscPoint.x - mCoverBorderWidth, mDiscPoint.y-mCoverBorderWidth, mDiscPoint.x + mDiscBitmap.getWidth() + mCoverBorderWidth, mDiscPoint.y +
                mDiscBitmap.getHeight() + mCoverBorderWidth);
        mCoverBorder.draw(canvas);


        //3.绘制黑胶
        //设置旋转中心,setRotate,选择图像的中心点作为旋转点旋转15度，如：
        // matrix.setRotate(15,bmp.getWidth()/2,bmp.getHeight()/2)
        mDiscMatrix.setRotate(mDiscRotation, mDiscCenterPoint.x, mDiscCenterPoint.y);
        //设置图片起始坐标
        mDiscMatrix.preTranslate(mDiscPoint.x, mDiscPoint.y);
        canvas.drawBitmap(mDiscBitmap,mDiscMatrix, null);

        // 4.绘制封面
        mCoverMatrix.setRotate(mDiscRotation, mCoverCenterPoint.x, mCoverCenterPoint.y);
        mCoverMatrix.preTranslate(mCoverPoint.x, mCoverPoint.y);
        canvas.drawBitmap(mCoverBitmap, mCoverMatrix, null);

        // 5.绘制指针
        mNeedleMatrix.setRotate(mNeedleRotation, mNeedleCenterPoint.x, mNeedleCenterPoint.y);
        mNeedleMatrix.preTranslate(mNeedlePoint.x, mNeedlePoint.y);
        canvas.drawBitmap(mNeedleBitmap, mNeedleMatrix, null);
    }

    /**
     * 控制指针是否旋转角度
     */

    public void initNeedle(boolean isPlaying){
        //在播放的话就设置旋转角度为起始位置,停止播放的时候逆时针旋转25度
        mNeedleRotation = isPlaying? NEEDLE_ROTATION_PLAY : NEEDLE_ROTATION_PAUSE;
        //invalidate()是用来刷新View的.比如在修改某个view的显示时，调用invalidate()才能看到重新绘制的界面
        invalidate();
    }

    /**
     * 设置专辑图片
     */
    public void setCoverBitmap(Bitmap bitmap){
        mCoverBitmap = bitmap;
        mDiscRotation = 0.0f;
        invalidate();
    }

    /**
     * 播放控制
     */
    public void start(){
        if(isPlaying){
            return;
        }
        isPlaying = true;
        mHandler.post(mRotationRunnable);
        mPlayAnimator.start();
    }

    public void pause(){
        if(!isPlaying){//如果没有正在播放
            return;
        }
        isPlaying = false;
        mHandler.removeCallbacks(mRotationRunnable);//删除Runnable
        mPauseAnimator.start();

    }

    private Runnable mRotationRunnable = new Runnable() {
        @Override
        public void run() {
            if(isPlaying){
                mDiscRotation += DISC_ROTATION_INCREASE;
                if(mDiscRotation >= 360){
                    mDiscRotation = 0;
                }
            }
            invalidate();
            mHandler.postDelayed(this, TIME_UPDATE);//又延迟启动mHandle实现循环
        }
    };

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        //animation.getAnimatedValue()来动态获取前面设置的两个角度
        mNeedleRotation = (float)animation.getAnimatedValue();
        invalidate();
    }
}