package com.jpliot.slipactivitytest;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jpliot.slipview.SlipBackActivity;
import com.jpliot.slipview.SlipBackDialog;

public class MainActivity extends SlipBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFitSystemWindow(true);
    }


    public void toSecondActivity(View view) {
        slipToActivity(SecondActivity.class);
    }

    public void toDialog(View view) {
        final SlipBackDialog mDialog = new SlipBackDialog(this);
        final View content = getLayoutInflater().inflate(R.layout.dialog_layout,
                null);
        mDialog.setContentView(content);
        mDialog.show();
    }
}
