package com.interpark.smframework.util.ImageProcess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;

import com.interpark.smframework.SMDirector;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.sprite.BitmapSprite;
import com.interpark.smframework.base.sprite.Sprite;
import com.interpark.smframework.base.texture.BitmapTexture;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.AppConst;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageProcessFunction {
    public ImageProcessFunction() {
        _inputData = null;
        _outputData = null;
        _param = null;
        _clearColor = Color4F.TRANSPARENT;
        _isCaptureOnly = false;
        _outputImage = null;
        initParam();
    }

    @Override
    public void finalize() throws Throwable {
        try {
            releaseInputData();
            releaseOutputData();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("All done!");
            super.finalize();
        }
    }


    public boolean onPreProcess(SMView view) {
        if (view!=null) {
            Size canvasSize = view.getContentSize();

            float x = canvasSize.width * 0.5f;
            float y = canvasSize.height * 0.5f;

            return startProcess(view, canvasSize, new Vec2(x, y), Vec2.MIDDLE, 1.0f, 1.0f);
        } else {
            return true;
        }
    }

    public boolean onProcessInBackground() {return false;}

    public Sprite onPostProcess() {
        Bitmap bitmap = null;

        if (getOutputImage()!=null) {
            bitmap = SMView.copyBitmap(getOutputImage());
            releaseOutputData();
        } else if (getOutputData()!=null) {
            int bpp = getOutputBpp();

            int pixelFormat = PixelFormat.UNKNOWN;

            if (bpp==3) {
                pixelFormat = PixelFormat.RGB_888;
            } else {
                pixelFormat = PixelFormat.RGBA_8888;
            }

            Size size = getOutputSize();
            byte[] data = getOutputData();
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            bitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
            releaseOutputData();
        }

        if (bitmap!=null) {
            return BitmapSprite.createFromBitmap(SMDirector.getDirector(), "IMGPROC", bitmap);
        }

        return null;
    }

    protected boolean startProcess(SMView view, final Size canvasSize, final Vec2 position, final Vec2 anchorPoint, final float scaleX, final float scaleY) {

        Bitmap bmp = view.captureView();

        _capturedTexture = new BitmapTexture(SMDirector.getDirector(), "IMGPROC", bmp);

        onCaptureComplete(bmp);

        if (bmp!=null) {
            if (isCaptureOnly()) {
                return true;
            }

            _inputSize = new Size(bmp.getWidth(), bmp.getHeight());
            _inputBpp = 4;

            ByteBuffer buffer = ByteBuffer.allocate(bmp.getRowBytes()*bmp.getHeight());
            bmp.copyPixelsToBuffer(buffer);
            buffer.get(_inputData);

            return true;
        }

        return false;
    }

    public Texture getCapturedTexture() {
        return _capturedTexture;
    }

    public SceneParams getParam() {return _param;}

    public void onCaptureComplete(Bitmap bitmap) {}

    protected void setTask(ImageProcessTask task) {_task = task;}

    protected void onProgress(float progress) {}

    protected void setCaptureOnly() {_isCaptureOnly=true;}

    protected boolean isCaptureOnly() {return _isCaptureOnly;}

    protected void initOutputBuffer(final int width, final int height, final int bpp) {
        _outputData = new byte[width*height*bpp];
        _outputSize = new Size(width, height);
        _outputBpp = bpp;
    }

    protected void setOutputImage(Bitmap image) {_outputImage = image;}

    protected Size getOutputSize() {return _outputSize;}

    protected Size getInputSize() {return _inputSize;}

    protected int getInputBpp() {return _inputBpp;}

    protected int getOutputBpp() {return _outputBpp;}

    protected Bitmap getOutputImage() {return _outputImage;}

    protected byte[] getInputData() {return _inputData;}

    protected byte[] getOutputData() {return _outputData;}

    protected int getInputDataLength() {return (int)(_inputSize.width*_inputSize.height*_inputBpp);}

    protected void releaseInputData() {
        _inputData = null;
    }

    protected void releaseOutputData() {
        _outputData = null;
    }

    protected SceneParams initParam() {
        if (_param==null) {
            _param = SceneParams.create();
        }

        return _param;
    }

    protected void setClearColor(final Color4F clearColor) {_clearColor = clearColor;}

    protected void interrupt() {_interrupt = true;}

    protected boolean isInterrupt() {return _interrupt;}

    protected void onReadPixelsCommand() {}

    protected Size _inputSize = new Size();
    protected byte[] _inputData = null;
    protected int _inputBpp;


    private BitmapTexture _capturedTexture = null;
    private SceneParams _param = null;
    private Size _outputSize = new Size();
    private byte[] _outputData = null;
    private Bitmap _outputImage = null;
    private int _outputBpp;
    private Color4F _clearColor = new Color4F(0, 0, 0, 0);
    private boolean _interrupt;
    private boolean _isCaptureOnly;

    private ImageProcessTask _task = null;
}
