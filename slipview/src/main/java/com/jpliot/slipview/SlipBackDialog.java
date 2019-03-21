package com.jpliot.slipview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.Visibility;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class SlipBackDialog extends Dialog {
    protected SlipBackLayout mSlipBackLayout;

    public SlipBackDialog(@NonNull Context context) {
        this(context, 0);
    }

    public SlipBackDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        setFullScreen();
        setStatusbarTranslucent();

        //滑动布局初始化
        mSlipBackLayout = new SlipBackLayout(context);
        int num = SlipActivityManager.getInstance().getActivityNum();
        mSlipBackLayout.setSlipBackEnable(num >= 1 ? true : false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSlipBackLayout.attachToDialog(this);
        setFitSystemWindow(true);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }


    protected void setFullScreen() {
        Window window = getWindow();
        //背景透明

        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setDimAmount(0);//去掉背景模糊

        window.setWindowAnimations(R.style.DialogAnimation);

        //全屏
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }
    /**
     * 全透状态栏
     */
    protected void setStatusbarTransparent() {
        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }


    /**
     * 半透明状态栏
     */
    protected void setStatusbarTranslucent() {

        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * 如果需要内容紧贴着StatusBar
     * 应该在对应的xml布局文件中，设置根布局fitsSystemWindows=true。
     */
    protected void setFitSystemWindow(boolean fitSystemWindow) {
        View view = ((ViewGroup)findViewById(android.R.id.content))
                .getChildAt(0);
        if (view != null) {
            view.setFitsSystemWindows(fitSystemWindow);
        }
    }
}
