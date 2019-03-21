package com.jpliot.slipview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class SlipBackLayout extends FrameLayout {

    private static final float FINALSCALE = 0.97f;
    private static final float MIN_PECENT = 0.5f;//拖拽的最小距离比例，释放后继续移动
    private static final float MIN_SPEED = 500;//拖拽释放后，继续滑动的最小速度
    private static final int MAX_ALPHA = 180;//透明度最大255

    private ViewDragHelper mViewDragHelper;
    private Activity mCurActivity;//当前显示的Activiy
    private Dialog mCurDialog;
    private View mCurContentView;//Activity里的根显示View
    private View mPreContentView;//前一个Activity中的根显示View


    private boolean isSlipBackEnable = true;//是否使能手势拖拽返回上一层
    public boolean isSliping = false;//当前界面是否正处在滑动过程
    //Activity是否透明，滑动过程需要透明处理，用当前界面覆盖
    public boolean isActivityTranslucent = false;//当前Activity的显示是否透明
    private int leftOffset;//离左侧距离
    private float mSlipPercent;//移动距离
    private float mScale;//当前缩放比例

    private int width, height;
    private float firstX, firstY;
    private View mScrollView;
    private Drawable mShadowDrawable;


    public SlipBackLayout(@NonNull Context context) {
        this(context, null);
    }

    public SlipBackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlipBackLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);//如果重写了onDraw(),需要这样设置
        mViewDragHelper = ViewDragHelper.create(this, 1f,
                new ViewDragHelperListener());
        mViewDragHelper.setEdgeTrackingEnabled(1);//0x1：左侧边缘，0x2：右侧边缘，0x4:顶上边缘，0x8:底部边缘
        mShadowDrawable = new ColorDrawable(Color.argb(0xaa, 0xaa, 0xaa, 0xaa));

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if (!isSlipBackEnable) {
            super.onLayout(changed, left, top, right, bottom);

            return;
        }

        int l = getPaddingLeft()+leftOffset;
        int t = getPaddingTop();
        int r = l + mCurContentView.getMeasuredWidth();
        int b = t + mCurContentView.getMeasuredHeight();
        mCurContentView.layout(l, t, r, b);

        if (changed) {
            width = getWidth();
            height = getHeight();
        }
        mScrollView = findScrollView(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isSlipBackEnable) {
            canvas.drawARGB((int) (MAX_ALPHA-MAX_ALPHA*mSlipPercent), 0, 0, 0);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (isSlipBackEnable) {
            if (mViewDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE
                    && child == mCurContentView) {
                Rect rect = new Rect();
                child.getHitRect(rect);
                mShadowDrawable.setBounds(rect.left - mShadowDrawable.getIntrinsicWidth(),
                        rect.top, rect.left, rect.bottom);
                mShadowDrawable.setAlpha((int) ((1-mSlipPercent) * 255));
                mShadowDrawable.draw(canvas);
            }
        }

        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isSlipBackEnable) {
            return super.onInterceptTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstX = ev.getRawX();
                firstY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mScrollView != null && pointInview(mScrollView, firstX, firstY)) {
                    float disX = ev.getRawX() - firstX;
                    float disY = ev.getRawY() - firstY;
                    if (disY > disX && disY > mViewDragHelper.getTouchSlop()) {
                        return super.onInterceptTouchEvent(ev);
                    }
                }
                break;
        }

        if (mViewDragHelper.shouldInterceptTouchEvent(ev)) {
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSlipBackEnable) {
            mViewDragHelper.processTouchEvent(event);
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 使能拖拽返回
     *
     * @param enable
     */
    public void setSlipBackEnable(boolean enable) {
        isSlipBackEnable = enable;
    }

    /**
     * 是否支持拖拽返回
     *
     * @return
     */
    public boolean isSlipBackEnable() {
        return isSlipBackEnable;
    }

    public void finishSlip() {
        if (mCurActivity != null) {
            mCurActivity.finish();
            mCurActivity.overridePendingTransition(0, 0);
        } else if (mCurDialog != null) {
            mCurDialog.dismiss();
        }
    }

    private boolean pointInview(View view, float x, float y) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect.contains((int) x, (int) y);
    }

    private View findScrollView(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view.getVisibility() != VISIBLE)
                continue;

            if (view instanceof ViewGroup) {
                view = findScrollView((ViewGroup) view);
                if (view != null) return view;

            } else if (view instanceof ScrollView || view instanceof HorizontalScrollView
                    || view instanceof NestedScrollView || view instanceof WebView
                    || view instanceof ViewPager) {
                return view;
            }
        }

        return null;
    }

    /**
     * 使指定的Activity界面透明
     *
     * @param activity
     */
    private void translucentActivity(Activity activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Method method = Activity.class.getDeclaredMethod("getActivityOptions");
                method.setAccessible(true);
                Object options = method.invoke(activity);

                Class<?> translucentClazz = null;
                Class<?>[] classes = Activity.class.getDeclaredClasses();
                for (Class clazz : classes) {
                    if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                        translucentClazz = clazz;
                        break;
                    }
                }

                method = Activity.class.getDeclaredMethod("convertToTranslucent",
                        translucentClazz, ActivityOptions.class);
                method.setAccessible(true);
                method.invoke(activity, null, options);
            } else {
                Class<?> translucentClazz = null;
                Class<?>[] classes = Activity.class.getDeclaredClasses();
                for (Class clazz : classes) {
                    if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                        translucentClazz = clazz;
                        break;
                    }
                }

                Method method = Activity.class.getDeclaredMethod("convertToTranslucent",
                        translucentClazz);
                method.setAccessible(true);
                method.invoke(activity, new Object[] {null});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void recoverActivityFromTranslucent(Activity activity) {
        try {
            Method method = Activity.class.getDeclaredMethod("convertFromTranslucent");
            method.setAccessible(true);
            method.invoke(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void forwardScaleAnimation() {
        if (mPreContentView != null) {
            ObjectAnimator animatorX = ObjectAnimator.ofFloat(mPreContentView,
                    "scaleX", 1f, FINALSCALE);
            ObjectAnimator animatorY = ObjectAnimator.ofFloat(mPreContentView,
                    "scaleY", 1f, FINALSCALE);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animatorX, animatorY);
            animatorSet.setDuration(200).start();
        }
    }

    public void backwardScaleAnimation() {
        if (mPreContentView != null) {
            ObjectAnimator animatorX = ObjectAnimator.ofFloat(mPreContentView,
                    "scaleX", FINALSCALE, 1f);
            ObjectAnimator animatorY = ObjectAnimator.ofFloat(mPreContentView,
                    "scaleY", FINALSCALE, 1f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animatorX, animatorY);
            animatorSet.setDuration(200).start();
        }
    }

    /**
     * 把当前界面添加到Activity的DecorView下, 并把DecorView下的LinearLayout
     * 移到当前界面下，方便做拖拽显示
     *
     * @param activity
     */
    public void attachToActivity(Activity activity) {

        TypedArray typedArray = activity.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.windowBackground});//activity加载前显示的背景
        int backgound = typedArray.getResourceId(0, 0);//获取背景值
        typedArray.recycle();

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        mCurContentView = decorView.getChildAt(0);
        mCurContentView.setBackgroundResource(backgound);
        decorView.removeView(mCurContentView);
        decorView.addView(this);
        this.addView(mCurContentView);

        mCurActivity = activity;
        Activity prevActivity = SlipActivityManager.getInstance().getPrevActivity();
        if (prevActivity != null && prevActivity instanceof SlipBackActivity) {
            ViewGroup preDecordView = (ViewGroup) prevActivity.getWindow().getDecorView();
            View layout = preDecordView.getChildAt(0);
            if (layout != null && layout instanceof SlipBackLayout) {
                mPreContentView = ((SlipBackLayout) layout).getChildAt(0);
            }
        }
    }

    public void attachToDialog(Dialog dialog) {

        ViewGroup decorView = (ViewGroup) dialog.getWindow().getDecorView();
        mCurContentView = decorView.getChildAt(0);
        decorView.removeView(mCurContentView);
        decorView.addView(this);
        this.addView(mCurContentView);

        mCurDialog = dialog;
        Activity curActivity = SlipActivityManager.getInstance().getCurActivity();
        if (curActivity != null && curActivity instanceof SlipBackActivity) {
            ViewGroup preDecordView = (ViewGroup) curActivity.getWindow().getDecorView();
            View layout = preDecordView.getChildAt(0);
            if (layout != null && layout instanceof SlipBackLayout) {
                mPreContentView = ((SlipBackLayout) layout).getChildAt(0);
            }
        }
    }
    /**************ViewDragHelper.Callback******************/
    class ViewDragHelperListener extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(@NonNull View view, int i) {
            if (isSlipBackEnable) {
                isSliping = true;
                if (mCurActivity != null && !isActivityTranslucent) {
                    translucentActivity(mCurActivity);
                    isActivityTranslucent = true;
                }

                return view == mCurContentView;
            }
            return false;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            leftOffset = getPaddingLeft();
            if (isSlipBackEnable) {
                leftOffset = Math.min(Math.max(left, getPaddingLeft()),
                        width);
            }

            return leftOffset;
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

            mSlipPercent = 1.0f * Math.abs(left) / width;

            invalidate();
            //滑动中改变缩放度
            mScale = FINALSCALE + (1 - FINALSCALE) * mSlipPercent;
            if (mPreContentView != null) {
                mPreContentView.setScaleX(mScale);
                mPreContentView.setScaleY(mScale);
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (state == ViewDragHelper.STATE_IDLE) {
                if (mSlipPercent == 1) {//移动完成
                    finishSlip();
                } else if (mSlipPercent == 0){//回到原始位置
                    isSliping = false;
                    if (mCurActivity != null && isActivityTranslucent) {
                        isActivityTranslucent = false;
                        recoverActivityFromTranslucent(mCurActivity);
                    }
                }
            }
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            leftOffset = 0;
            if (!isSlipBackEnable) return;

            boolean continueMove = xvel >= MIN_SPEED || mSlipPercent >= MIN_PECENT;
            if (continueMove) {
                //继续滑完，到前一页
                if (mViewDragHelper.settleCapturedViewAt(width, getPaddingTop())) {
                    ViewCompat.postInvalidateOnAnimation(SlipBackLayout.this);
                }
            } else {
                //恢复当前页的移动距离到0
                if (mViewDragHelper.settleCapturedViewAt(getPaddingLeft(), getPaddingTop())) {
                    ViewCompat.postInvalidateOnAnimation(SlipBackLayout.this);
                }
            }
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return width;
        }
    }
}
