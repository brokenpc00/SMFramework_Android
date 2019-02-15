package com.interpark.smframework.shader;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;

public abstract class ShaderProgram {
    protected IDirector director;
    protected ShaderManager.ProgramType type;

    protected int programId;
    protected int vertexShaderId;
    protected int fragmentShaderId;

    public int getProgramId() {
        return programId;
    }

    public ShaderManager.ProgramType getType() {
        return type;
    }

    public void linkInterface(IDirector director, ShaderManager.ProgramType type, int programId) {
        this.director = director;
        this.type = type;
        this.programId = programId;
    }

    public void delete() {
        GLES20.glDeleteProgram(programId);
    }


    public void bind() {
        GLES20.glUseProgram(programId);
    }


    public abstract void complete();
    public abstract void unbind();
    public abstract void setMatrix(float[] matrix);
}
