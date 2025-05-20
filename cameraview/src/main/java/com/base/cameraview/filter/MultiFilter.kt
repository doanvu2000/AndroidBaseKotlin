package com.base.cameraview.filter

import android.opengl.GLES20
import androidx.annotation.VisibleForTesting
import com.base.cameraview.size.Size
import com.otaliastudios.opengl.core.Egloo.IDENTITY_MATRIX
import com.otaliastudios.opengl.program.GlProgram
import com.otaliastudios.opengl.program.GlTextureProgram
import com.otaliastudios.opengl.texture.GlFramebuffer
import com.otaliastudios.opengl.texture.GlTexture

@Suppress("unused")
class MultiFilter(filters: MutableCollection<Filter>) : Filter, OneParameterFilter,
    TwoParameterFilter {
    @VisibleForTesting
    val filters: MutableList<Filter> = ArrayList()

    @VisibleForTesting
    val states: MutableMap<Filter?, State?> = HashMap<Filter?, State?>()
    private val lock = Any()
    private var size: Size? = null
    override var parameter1: Float = 0f
        set(parameter1) {
            field = parameter1
            synchronized(lock) {
                for (filter in filters) {
                    if (filter is OneParameterFilter) {
                        filter.parameter1 = parameter1
                    }
                }
            }
        }
    override var parameter2: Float = 0f
        set(parameter2) {
            field = parameter2
            synchronized(lock) {
                for (filter in filters) {
                    if (filter is TwoParameterFilter) {
                        filter.parameter2 = parameter2
                    }
                }
            }
        }

    /**
     * Creates a new group with the given filters.
     *
     * @param filters children
     */
    constructor(vararg filters: Filter) : this(mutableListOf(*filters))

    /**
     * Creates a new group with the given filters.
     *
     * @param filters children
     */
    init {
        for (filter in filters) {
            addFilter(filter)
        }
    }

    /**
     * Adds a new filter. It will be used in the next frame.
     * If the filter is a [MultiFilter], we'll use its children instead.
     *
     * @param filter a new filter
     */
    fun addFilter(filter: Filter) {
        if (filter is MultiFilter) {
            val multiFilter = filter
            for (multiChild in multiFilter.filters) {
                addFilter(multiChild)
            }
            return
        }
        synchronized(lock) {
            if (!filters.contains(filter)) {
                filters.add(filter)
                states.put(filter, State())
            }
        }
    }

    private fun maybeCreateProgram(filter: Filter, isFirst: Boolean, isLast: Boolean) {
        val state = states[filter]
        if (state!!.isProgramCreated) return
        state.isProgramCreated = true

        // The first shader actually reads from a OES texture, but the others
        // will read from the 2d framebuffer texture. This is a dirty hack.
        val fragmentShader = if (isFirst)
            filter.fragmentShader
        else
            filter.fragmentShader.replace("samplerExternalOES ", "sampler2D ")
        val vertexShader = filter.vertexShader
        state.programHandle = GlProgram.create(vertexShader, fragmentShader)
        filter.onCreate(state.programHandle)
    }

    // We don't offer a removeFilter method since that would cause issues
    // with cleanup. Cleanup must happen on the GL thread so we'd have to wait
    // for new rendering call (which might not even happen).
    private fun maybeDestroyProgram(filter: Filter) {
        val state = states[filter]
        if (!state!!.isProgramCreated) return
        state.isProgramCreated = false
        filter.onDestroy()
        GLES20.glDeleteProgram(state.programHandle)
        state.programHandle = -1
    }

    private fun maybeCreateFramebuffer(filter: Filter, isFirst: Boolean, isLast: Boolean) {
        val state = states[filter]
        if (isLast) {
            state?.sizeChanged = false
            return
        }
        if (state?.sizeChanged == true) {
            maybeDestroyFramebuffer(filter)
            state.sizeChanged = false
        }
        if (!state!!.isFramebufferCreated) {
            state.isFramebufferCreated = true
            state.outputTexture = GlTexture(
                GLES20.GL_TEXTURE0,
                GLES20.GL_TEXTURE_2D,
                state.size!!.width,
                state.size!!.height
            )
            state.outputFramebuffer = GlFramebuffer()
            state.outputFramebuffer!!.attach(state.outputTexture!!)
        }
    }

    private fun maybeDestroyFramebuffer(filter: Filter) {
        val state = states[filter]
        if (!state!!.isFramebufferCreated) return
        state.isFramebufferCreated = false
        state.outputFramebuffer!!.release()
        state.outputFramebuffer = null
        state.outputTexture!!.release()
        state.outputTexture = null
    }

    // Any thread...
    private fun maybeSetSize(filter: Filter) {
        val state = states[filter]
        if (size != null && size != state!!.size) {
            state.size = size
            state.sizeChanged = true
            filter.setSize(size!!.width, size!!.height)
        }
    }

    override fun onCreate(programHandle: Int) {
        // We'll create children during the draw() op, since some of them
        // might have been added after this onCreate() is called.
    }

    override val vertexShader: String
        get() =// Whatever, we won't be using this.
            GlTextureProgram.Companion.SIMPLE_VERTEX_SHADER

    override val fragmentShader: String
        get() =// Whatever, we won't be using this.
            GlTextureProgram.Companion.SIMPLE_FRAGMENT_SHADER

    override fun onDestroy() {
        synchronized(lock) {
            for (filter in filters) {
                maybeDestroyFramebuffer(filter)
                maybeDestroyProgram(filter)
            }
        }
    }

    override fun setSize(width: Int, height: Int) {
        size = Size(width, height)
        synchronized(lock) {
            for (filter in filters) {
                maybeSetSize(filter)
            }
        }
    }

    override fun draw(timestampUs: Long, transformMatrix: FloatArray) {
        synchronized(lock) {
            for (i in filters.indices) {
                val isFirst = i == 0
                val isLast = i == filters.size - 1
                val filter = filters[i]
                val state = states[filter]

                maybeSetSize(filter)
                maybeCreateProgram(filter, isFirst, isLast)
                maybeCreateFramebuffer(filter, isFirst, isLast)

                state?.let {
                    GLES20.glUseProgram(state.programHandle)
                }

                // Define the output framebuffer.
                // Each filter outputs into its own framebuffer object, except the
                // last filter, which outputs into the default framebuffer.
                if (!isLast) {
                    state?.outputFramebuffer!!.bind()
                    GLES20.glClearColor(0f, 0f, 0f, 0f)
                } else {
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
                }

                // Perform the actual drawing.
                // The first filter should apply all the transformations. Then,
                // since they are applied, we should use a no-op matrix.
                if (isFirst) {
                    filter.draw(timestampUs, transformMatrix)
                } else {
                    filter.draw(timestampUs, IDENTITY_MATRIX)
                }

                // Set the input for the next cycle:
                // It is the framebuffer texture from this cycle. If this is the last
                // filter, reset this value just to cleanup.
                if (!isLast) {
                    state?.outputTexture!!.bind()
                } else {
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                }

                GLES20.glUseProgram(0)
            }
        }
    }

    override fun copy(): Filter {
        synchronized(lock) {
            val copy = MultiFilter()
            if (size != null) {
                copy.setSize(size!!.width, size!!.height)
            }
            for (filter in filters) {
                copy.addFilter(filter.copy())
            }
            return copy
        }
    }

    @VisibleForTesting
    class State {
        @VisibleForTesting
        var isProgramCreated: Boolean = false

        @VisibleForTesting
        var isFramebufferCreated: Boolean = false

        @VisibleForTesting
        var size: Size? = null
        var sizeChanged = false
        var programHandle = -1
        var outputFramebuffer: GlFramebuffer? = null
        var outputTexture: GlTexture? = null
    }
}
