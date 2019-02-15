package com.interpark.smframework.shader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.SparseArray;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.IOUtils;

import java.io.InputStream;
import java.io.InputStreamReader;


public class ShaderManager {
    public static int SOURCE_FILE = 0;
    public static int SOURCE_STRING = 1;

    final private SparseArray<Shader> mShaderSet = new SparseArray<Shader>();
    final private SparseArray<ShaderProgram> mProgramSet = new SparseArray<ShaderProgram>();
    private ShaderProgram mActiveProgram = null;


    // 계속 추가 할 거임..
    public static enum ProgramType {
        /** 기본 스프라이트 */
        Sprite(1, "glsl/sprite.vsh", "glsl/sprite.fsh", new ProgSprite()),
        /** Circle sprite */
        SpriteCircle(2, "glsl/sprite.vsh", "glsl/sprite_circle.fsh", new ProgSpriteCircle()),
        /** 기본 도형 **/
        Primitive(3, "glsl/primitive.vsh", "glsl/primitive.fsh", new ProgPrimitive()),
        /** Circle **/
        PrimitiveCircle(4, "glsl/sprite.vsh", "glsl/primitive_circle.fsh", new ProgPrimitiveCircle()),
        /** Ring **/
        PrimitiveRing(5, "glsl/sprite.vsh", "glsl/primitive_ring.fsh", new ProgPrimitiveRing()),
        /** AA Rounded Rectangle */
        PrimitiveAARect(6, "glsl/sprite.vsh", "glsl/primitive_aarect.fsh", new ProgPrimitiveAARect()),
        /** 3D 스프라이트 */
        Sprite3D(7, "glsl/sprite3d.vsh", "glsl/sprite3d.fsh", new ProgSprite3D()),
        /** yuv2rgb */
        // 속도 때문에 yuv로 받아야 한다. argb로 받으면 겁나 느림..
        CameraPreview(8, "glsl/sprite.vsh", "glsl/yuv2rgb.fsh", new ProgCameraPreview());


        private int key;
        private String vertexShader;
        private String fragmentShader;
        private ShaderProgram program;
        private int vertexShaderKey;
        private int fragmentShaderKey;

        ProgramType(int key, String vertexShader, String fragmentShader, ShaderProgram program) {
            this.key = key;
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
            this.program = program;
            this.vertexShaderKey = vertexShader.hashCode();
            this.fragmentShaderKey = fragmentShader.hashCode();
        }

        /** Shader Key */
        public int getKey() {
            return key;
        }

        public int getVertexShaderKey() {
            return vertexShaderKey;
        }

        public int getFragmentShaderKey() {
            return fragmentShaderKey;
        }

        public String getVertexShaderSource() {
            return vertexShader;
        }

        public String getFragmentShaderSource() {
            return fragmentShader;
        }

        public ShaderProgram getShaderProgram() {
            return program;
        }
    };

    public ShaderProgram getActiveProgram() {
        return mActiveProgram;
    }


    public ShaderProgram useProgram(final IDirector director, final ProgramType type) {
        if (mActiveProgram != null && mActiveProgram.getType() == type) {
            return mActiveProgram;
        }

        ShaderProgram program = mProgramSet.get(type.getKey());
        if (program == null) {
            program = loadProgram(director, type);
            if (program != null) {
//				Log.d("ShaderManager", "Shader Loaded : " + type.getVertexShaderSource() + " " + type.getFragmentShaderSource());
                mProgramSet.put(type.getKey(), program);
                program.complete();
            } else {
                program = null;
            }
        }
        mActiveProgram = program;

        return program;
    }

    public void setMatrix(float[] matrix) {
        if (mActiveProgram != null && matrix != null) {
            mActiveProgram.setMatrix(matrix);
        }
    }

    public void release(IDirector director) {
        mActiveProgram = null;

        int key = 0;

        // clear all program
        if (director.isGLThread()) {
            for (int i = 0; i < mProgramSet.size(); i++) {
                key = mProgramSet.keyAt(i);
                ShaderProgram program = mProgramSet.get(key);
                if (program != null) {
                    program.delete();
                }
            }
        }
        mProgramSet.clear();

        // clear all shader
        if (director.isGLThread()) {
            for(int i = 0; i < mShaderSet.size(); i++) {
                key = mShaderSet.keyAt(i);
                Shader shader = mShaderSet.get(key);
                if (shader != null) {
                    GLES20.glDeleteShader(shader.getId());
                }
            }
        }
        mShaderSet.clear();
    }


    private ShaderProgram loadProgram(IDirector director, ProgramType type) {
        int vertexShaderId;
        int fragmentShaderId;

        // load vertex shader
        Shader vertexShader = mShaderSet.get(type.getVertexShaderKey());
        if (vertexShader != null) {
            vertexShaderId = vertexShader.getId();
        } else {
            String source = loadShaderSource(director.getContext(), type.getVertexShaderSource());
            vertexShaderId = loadShader(source, GLES20.GL_VERTEX_SHADER);
            if (vertexShaderId == 0) {
//				Log.d("Load Program", "Vertex Shader Failed");
                return null;
            }
            vertexShader = new Shader(vertexShaderId);
        }

        // load fragment shader
        Shader fragmentShader = mShaderSet.get(type.getFragmentShaderKey());
        if (fragmentShader != null) {
            fragmentShaderId = fragmentShader.getId();
        } else {
            String source = loadShaderSource(director.getContext(), type.getFragmentShaderSource());
            fragmentShaderId = loadShader(source, GLES20.GL_FRAGMENT_SHADER);
            if (fragmentShaderId == 0) {
//				Log.d("Load Program", "Fragment Shader Failed");

                // vertex shader가 생성된 상태이므로 삭제한다.
                if (vertexShader != null && vertexShader.getRefCount() <= 0) {
                    GLES20.glDeleteShader(vertexShader.getId());
                }
                return null;
            }
            fragmentShader = new Shader(fragmentShaderId);
        }

        // create program
        int programId = GLES20.glCreateProgram();

        if (programId != 0) {
            ShaderProgram program = type.getShaderProgram();//new (type.getShaderProgramClass())(render, type, programId);
            program.linkInterface(director, type, programId);

            GLES20.glAttachShader(programId, vertexShader.getId());
            GLES20.glAttachShader(programId, fragmentShader.getId());
            GLES20.glLinkProgram(programId);

            int[] link = new int[1];
            GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, link, 0);
            if (link[0] <= 0) {
//				Log.d("Load Program", "Linking Failed");

                if (vertexShader != null && vertexShader.getRefCount() <= 0) {
                    GLES20.glDeleteShader(vertexShader.getId());
                }
                if (fragmentShader != null && fragmentShader.getRefCount() <= 0) {
                    GLES20.glDeleteShader(vertexShader.getId());
                }

                GLES20.glDeleteProgram(programId);

                return null;
            }

            // put into shader cache
            vertexShader.incRef();
            fragmentShader.incRef();
            mShaderSet.put(type.getVertexShaderKey(), vertexShader);
            mShaderSet.put(type.getFragmentShaderKey(), fragmentShader);

            return program;
        }



        return null;
    }

    public static int loadShader(final String shaderSource, final int shaderType) {
        int[] compiled = new int[1];
        int shaderId = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shaderId, shaderSource);
        GLES20.glCompileShader(shaderId);
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
//			Log.d("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(shaderId));
            return 0;
        }

        return shaderId;
    }

    public static String loadShaderSource(final Context context, final String fileName) {
        StringBuffer source = null;
        InputStream is = null;
        InputStreamReader reader = null;

        try {
            final int BUFFER_SIZE = 1024;
            char[] buffer = new char[BUFFER_SIZE];
            is = context.getAssets().open(fileName);
            reader = new InputStreamReader(is);
            source = new StringBuffer();

            int readCount = 0;
            while ((readCount = reader.read(buffer, 0, BUFFER_SIZE)) != -1) {
                source.append(buffer, 0, readCount);
            }

        } catch (Exception e) {
//			Log.d("Read Shader Source Failed : ", fileName);
            return null;
        } finally {
            IOUtils.closeSilently(is);
            IOUtils.closeSilently(reader);
        }

        return source.toString();
    }
}
