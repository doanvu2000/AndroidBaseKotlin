package com.base.cameraview.filter;

import androidx.annotation.NonNull;

public interface Filter {

    /**
     * Returns a String containing the vertex shader.
     * Together with {@link #getFragmentShader()}, this will be used to
     * create the OpenGL program.
     *
     * @return vertex shader
     */
    @NonNull
    String getVertexShader();

    /**
     * Returns a String containing the fragment shader.
     * Together with {@link #getVertexShader()}, this will be used to
     * create the OpenGL program.
     *
     * @return fragment shader
     */
    @NonNull
    String getFragmentShader();

    /**
     * The filter program was just created. We pass in a handle to the OpenGL
     * program that was created, so you can fetch pointers.
     *
     * @param programHandle handle
     */
    void onCreate(int programHandle);

    /**
     * The filter program is about to be destroyed.
     */
    void onDestroy();

    /**
     * Called to render the actual texture. The given transformation matrix
     * should be applied.
     *
     * @param timestampUs     timestamp in microseconds
     * @param transformMatrix matrix
     */
    void draw(long timestampUs, @NonNull float[] transformMatrix);

    /**
     * Called anytime the output size changes.
     *
     * @param width  width
     * @param height height
     */
    void setSize(int width, int height);

    /**
     * Clones this filter creating a new instance of it.
     * If it has any important parameters, these should be passed
     * to the new instance.
     *
     * @return a clone
     */
    @NonNull
    Filter copy();
}
