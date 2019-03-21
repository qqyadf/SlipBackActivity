package com.jpliot.slipactivitytest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.jpliot.slipview.SlipBackActivity;

public class SecondActivity extends SlipBackActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    public void toThirdActivity(View view) {
        slipToActivity(ThirdActivity.class);
    }
}
