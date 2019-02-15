package com.interpark.smframework;

import android.content.Context;
import android.graphics.RectF;
import android.support.v4.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.Popup;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.types.ActionManager;
import com.interpark.smframework.base.types.Scheduler;
import com.interpark.smframework.shader.ShaderManager.ProgramType;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.sprite.SpriteSet;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.texture.TextureManager;
import com.interpark.smframework.util.Size;

public interface IDirector {

    enum SIDE_MENU_STATE {
        CLOSE,
        OPEN,
        MOVING
    }

    public enum SharedLayer {
        BACKGROUND,
        LEFT_MENU,
        RIGHT_MENU,
        BETWEEN_MENU_AND_SCENE,
        // scene
        BETWEEN_SCENE_AND_UI,
        UI,
        BETWEEN_UI_AND_POPUP,
        DIM,
        POPUP,
    }


    public FragmentActivity getActivity();
    public Context getContext();

    public void setDisplayRawWidth(int displayRawWidth, int displayRawHeight);
    public int getDisplayRawWidth();
    public int getDisplayRawHeight();
    public int getScreenOrientation();

    // for camera
//    public PreviewSurfaceView getPreviewSurfaceView();
////    public void setProfileEditTextView(SnapshotEditText view);
////    public SnapshotEditText getProfileEditText();

    public void enableScissorTest(boolean enable);
    public boolean isScissorTestEnabled();

    public Size getWinSize();
    public int getWidth();
    public int getHeight();
    public int getDeviceWidth();
    public int getDeviceHeight();
    public float getDisplayAdjust();
    public void runOnUiThread(final Runnable action);
    public void runOnDraw(final Runnable action);
    public void runOnDrawDelayed(final Runnable targetAction, final long delayTimeMillis);
    public void removeOnDraw(final Runnable targetAction);
    public void removeOnDraw(final Class<?> targetClass);
    public boolean hasOnDraw(final Runnable targetAction);
    public boolean hasOnDraw(final Class<?> targetClass);
    public boolean isGLThread();
    public RequestQueue getRequestQueue();

    public ActionManager getActionManager();
    public Scheduler getScheduler();

    public float getGlobalTime();

    public int getFrameBufferId();
    public void setFrameBufferId(int frameBufferId);
    public Sprite getFrameBufferSprite();


//    public SMActionBar getActionBar();
////    public SideMenuDrawer getSideMenu();


    public void setTouchEventDispatcherEnable(boolean enable);
    public boolean getTouchEventDispatcherEnable();

    public float[] getColor();
    public void setColor(float r, float g, float b, float a);

    public boolean bindTexture(Texture texture);
    public ShaderProgram useProgram(ProgramType type);

    public float[] getProjectionMatrix();
    public float[] getFrameBufferMatrix();
    public void setProjectionMatrix(float[] matrix);
    public void pushProjectionMatrix();
    public void popProjectionMatrix();
    public void updateProjectionMatrix();

    public long getTickCount();

    public void drawFillRect(float x1, float y1, float width, float height);
    public void drawRect(float x1, float y1, float width, float height, float lineWidth);
    public void drawLine(float x1, float y1, float x2, float y2, float lineWidth);
    public void drawCircle(float x, float y, float radius);
    public void drawCircle(float x, float y, float radius, float border);
    public void drawRing(float x, float y, float radius, float thickness);
    public void drawRing(float x, float y, float radius, float thickness, float border);
    public void drawAARect(float x, float y, float width, float height, float round);
    public void drawAARect(float x, float y, float width, float height, float round, float border);


    public SpriteSet getSpriteSet();

    public TextureManager getTextureManager();

    public boolean sceneFinish(SMScene scene, SceneParams result);

    public void showProgress(boolean show, RectF bounds);
    public void showUploadProgress(boolean show, int status, RectF bounds);

    public SMScene getTopScene();

    public void setSharedLayer(final SharedLayer layerId, SMView layer);
    public SMView getSharedLayer(final SharedLayer layerId);

    public void closePopupView(Popup view);

    public void setSideMenuOpenPosition(float position);

    public SIDE_MENU_STATE getSideMenuState();

    public SMScene getRunningScene();

    public boolean isSendCleanupToScene();

    public void replaceScene(SMScene scene);
    public void pushScene(SMScene scene);
    public void popScene();
    public void popToRootScene();
    public void popToSceneStackLevel(int level);
    public void setNextScene();
    public void popSceneWithTransition(SMScene scene);
    public void runWithScene(SMScene scene);
    public void startSceneAnimation();
    public void stopSceneAnimation();
    public SMScene getPreviousScene();
    public int getSceneStackCount();
}
