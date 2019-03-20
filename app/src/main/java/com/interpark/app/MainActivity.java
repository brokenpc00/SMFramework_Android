package com.interpark.app;

import android.os.Message;
import android.support.v4.app.FragmentActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.interpark.app.scene.HellowInterparkScene;
import com.interpark.smframework.ClassHelper;
import com.interpark.smframework.SMSurfaceView;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.SceneParams;

public class MainActivity extends FragmentActivity implements ClassHelper.HelperListener {

    private SMSurfaceView mSurfaceView;
    private int mDisplayRawWidth;
    private int mDisplayRawHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
//
//        String str = stringFromJNI();

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        mDisplayRawWidth = outMetrics.widthPixels;
        mDisplayRawHeight = outMetrics.heightPixels;

        mSurfaceView = new SMSurfaceView(this);
        addContentView(mSurfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        addContentView(mSurfaceView, new FrameLayout.LayoutParams(mDisplayRawWidth/2, mDisplayRawHeight/2));

        mSurfaceView.getDirector().setDisplayRawWidth(mDisplayRawWidth, mDisplayRawHeight);


        ClassHelper.init(this);

        // main scene 만들어서 붙인다.
        SceneParams sceneParam = new SceneParams();

        HellowInterparkScene scene = HellowInterparkScene.create(mSurfaceView.getDirector(), sceneParam, SMScene.SwipeType.MENU);
        mSurfaceView.startSMFrameWorkScene(scene);
    }

    @Override
    public void runOnGLThread(final Runnable pRunnable) {
        this.mSurfaceView.queueEvent(pRunnable);
    }

    @Override
    public void showDialog(final String pTitle, final String pMessage) {

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSurfaceView != null) {
            mSurfaceView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onBackPressed() {
        if (mSurfaceView == null || !mSurfaceView.onBackPressed()) {
            mSurfaceView = null;
            super.onBackPressed();
        }
    }

}
