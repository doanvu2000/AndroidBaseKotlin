package com.base.cameraview.filter

interface Filter {
    /**
     * Returns a String containing the vertex shader.
     * Together with [.getFragmentShader], this will be used to
     * create the OpenGL program.
     *
     * @return vertex shader
     */
    val vertexShader: String

    /**
     * Returns a String containing the fragment shader.
     * Together with [.getVertexShader], this will be used to
     * create the OpenGL program.
     *
     * @return fragment shader
     */
    val fragmentShader: String

    /**
     * The filter program was just created. We pass in a handle to the OpenGL
     * program that was created, so you can fetch pointers.
     *
     * @param programHandle handle
     */
    fun onCreate(programHandle: Int)

    /**
     * The filter program is about to be destroyed.
     */
    fun onDestroy()

    /**
     * Called to render the actual texture. The given transformation matrix
     * should be applied.
     *
     * @param timestampUs     timestamp in microseconds
     * @param transformMatrix matrix
     */
    fun draw(timestampUs: Long, transformMatrix: FloatArray)

    /**
     * Called anytime the output size changes.
     *
     * @param width  width
     * @param height height
     */
    fun setSize(width: Int, height: Int)

    /**
     * Clones this filter creating a new instance of it.
     * If it has any important parameters, these should be passed
     * to the new instance.
     *
     * @return a clone
     */
    fun copy(): Filter
}
