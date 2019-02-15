package com.interpark.app;

import android.support.v4.app.FragmentActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.interpark.app.scene.HellowInterparkScene;
import com.interpark.smframework.SMSurfaceView;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.SceneParams;

public class MainActivity extends FragmentActivity {

    private SMSurfaceView mSurfaceView;
    private int mDisplayRawWidth;
    private int mDisplayRawHeight;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

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

        // main scene 만들어서 붙인다.
        SceneParams sceneParam = new SceneParams();

        HellowInterparkScene scene = HellowInterparkScene.create(mSurfaceView.getDirector(), sceneParam, SMScene.SwipeType.MENU);
        mSurfaceView.startSMFrameWorkScene(scene);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
