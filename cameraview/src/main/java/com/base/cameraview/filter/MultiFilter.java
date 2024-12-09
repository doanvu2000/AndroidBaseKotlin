package com.base.cameraview.filter;

import android.opengl.GLES20;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.base.cameraview.size.Size;
import com.otaliastudios.opengl.core.Egloo;
import com.otaliastudios.opengl.program.GlProgram;
import com.otaliastudios.opengl.program.GlTextureProgram;
import com.otaliastudios.opengl.texture.GlFramebuffer;
import com.otaliastudios.opengl.texture.GlTexture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class MultiFilter implements Filter, OneParameterFilter, TwoParameterFilter {

    @VisibleForTesting
    final List<Filter> filters = new ArrayList<>();
    @VisibleForTesting
    final Map<Filter, State> states = new HashMap<>();
    private final Object lock = new Object();
    private Size size = null;
    private float parameter1 = 0F;
    private float parameter2 = 0F;

    /**
     * Creates a new group with the given filters.
     *
     * @param filters children
     */
    public MultiFilter(@NonNull Filter... filters) {
        this(Arrays.asList(filters));
    }

    /**
     * Creates a new group with the given filters.
     *
     * @param filters children
     */
    @SuppressWarnings("WeakerAccess")
    public MultiFilter(@NonNull Collection<Filter> filters) {
        for (Filter filter : filters) {
            addFilter(filter);
        }
    }

    /**
     * Adds a new filter. It will be used in the next frame.
     * If the filter is a {@link MultiFilter}, we'll use its children instead.
     *
     * @param filter a new filter
     */
    @SuppressWarnings("WeakerAccess")
    public void addFilter(@NonNull Filter filter) {
        if (filter instanceof MultiFilter) {
            MultiFilter multiFilter = (MultiFilter) filter;
            for (Filter multiChild : multiFilter.filters) {
                addFilter(multiChild);
            }
            return;
        }
        synchronized (lock) {
            if (!filters.contains(filter)) {
                filters.add(filter);
                states.put(filter, new State());
            }
        }
    }

    private void maybeCreateProgram(@NonNull Filter filter, boolean isFirst, boolean isLast) {
        State state = states.get(filter);
        //noinspection ConstantConditions
        if (state.isProgramCreated) return;
        state.isProgramCreated = true;

        // The first shader actually reads from a OES texture, but the others
        // will read from the 2d framebuffer texture. This is a dirty hack.
        String fragmentShader = isFirst
                ? filter.getFragmentShader()
                : filter.getFragmentShader().replace("samplerExternalOES ", "sampler2D ");
        String vertexShader = filter.getVertexShader();
        state.programHandle = GlProgram.create(vertexShader, fragmentShader);
        filter.onCreate(state.programHandle);
    }

    // We don't offer a removeFilter method since that would cause issues
    // with cleanup. Cleanup must happen on the GL thread so we'd have to wait
    // for new rendering call (which might not even happen).

    private void maybeDestroyProgram(@NonNull Filter filter) {
        State state = states.get(filter);
        //noinspection ConstantConditions
        if (!state.isProgramCreated) return;
        state.isProgramCreated = false;
        filter.onDestroy();
        GLES20.glDeleteProgram(state.programHandle);
        state.programHandle = -1;
    }

    private void maybeCreateFramebuffer(@NonNull Filter filter, boolean isFirst, boolean isLast) {
        State state = states.get(filter);
        if (isLast) {
            //noinspection ConstantConditions
            state.sizeChanged = false;
            return;
        }
        //noinspection ConstantConditions
        if (state.sizeChanged) {
            maybeDestroyFramebuffer(filter);
            state.sizeChanged = false;
        }
        if (!state.isFramebufferCreated) {
            state.isFramebufferCreated = true;
            state.outputTexture = new GlTexture(GLES20.GL_TEXTURE0,
                    GLES20.GL_TEXTURE_2D,
                    state.size.getWidth(),
                    state.size.getHeight());
            state.outputFramebuffer = new GlFramebuffer();
            state.outputFramebuffer.attach(state.outputTexture);
        }
    }

    private void maybeDestroyFramebuffer(@NonNull Filter filter) {
        State state = states.get(filter);
        //noinspection ConstantConditions
        if (!state.isFramebufferCreated) return;
        state.isFramebufferCreated = false;
        state.outputFramebuffer.release();
        state.outputFramebuffer = null;
        state.outputTexture.release();
        state.outputTexture = null;
    }

    // Any thread...
    private void maybeSetSize(@NonNull Filter filter) {
        State state = states.get(filter);
        //noinspection ConstantConditions
        if (size != null && !size.equals(state.size)) {
            state.size = size;
            state.sizeChanged = true;
            filter.setSize(size.getWidth(), size.getHeight());
        }
    }

    @Override
    public void onCreate(int programHandle) {
        // We'll create children during the draw() op, since some of them
        // might have been added after this onCreate() is called.
    }

    @NonNull
    @Override
    public String getVertexShader() {
        // Whatever, we won't be using this.
        return GlTextureProgram.SIMPLE_VERTEX_SHADER;
    }

    @NonNull
    @Override
    public String getFragmentShader() {
        // Whatever, we won't be using this.
        return GlTextureProgram.SIMPLE_FRAGMENT_SHADER;
    }

    @Override
    public void onDestroy() {
        synchronized (lock) {
            for (Filter filter : filters) {
                maybeDestroyFramebuffer(filter);
                maybeDestroyProgram(filter);
            }
        }
    }

    @Override
    public void setSize(int width, int height) {
        size = new Size(width, height);
        synchronized (lock) {
            for (Filter filter : filters) {
                maybeSetSize(filter);
            }
        }
    }

    @Override
    public void draw(long timestampUs, @NonNull float[] transformMatrix) {
        synchronized (lock) {
            for (int i = 0; i < filters.size(); i++) {
                boolean isFirst = i == 0;
                boolean isLast = i == filters.size() - 1;
                Filter filter = filters.get(i);
                State state = states.get(filter);

                maybeSetSize(filter);
                maybeCreateProgram(filter, isFirst, isLast);
                maybeCreateFramebuffer(filter, isFirst, isLast);

                //noinspection ConstantConditions
                GLES20.glUseProgram(state.programHandle);

                // Define the output framebuffer.
                // Each filter outputs into its own framebuffer object, except the
                // last filter, which outputs into the default framebuffer.
                if (!isLast) {
                    state.outputFramebuffer.bind();
                    GLES20.glClearColor(0, 0, 0, 0);
                } else {
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                }

                // Perform the actual drawing.
                // The first filter should apply all the transformations. Then,
                // since they are applied, we should use a no-op matrix.
                if (isFirst) {
                    filter.draw(timestampUs, transformMatrix);
                } else {
                    filter.draw(timestampUs, Egloo.IDENTITY_MATRIX);
                }

                // Set the input for the next cycle:
                // It is the framebuffer texture from this cycle. If this is the last
                // filter, reset this value just to cleanup.
                if (!isLast) {
                    state.outputTexture.bind();
                } else {
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                }

                GLES20.glUseProgram(0);
            }
        }
    }

    @NonNull
    @Override
    public Filter copy() {
        synchronized (lock) {
            MultiFilter copy = new MultiFilter();
            if (size != null) {
                copy.setSize(size.getWidth(), size.getHeight());
            }
            for (Filter filter : filters) {
                copy.addFilter(filter.copy());
            }
            return copy;
        }
    }

    @Override
    public float getParameter1() {
        return parameter1;
    }

    @Override
    public void setParameter1(float parameter1) {
        this.parameter1 = parameter1;
        synchronized (lock) {
            for (Filter filter : filters) {
                if (filter instanceof OneParameterFilter) {
                    ((OneParameterFilter) filter).setParameter1(parameter1);
                }
            }
        }
    }

    @Override
    public float getParameter2() {
        return parameter2;
    }

    @Override
    public void setParameter2(float parameter2) {
        this.parameter2 = parameter2;
        synchronized (lock) {
            for (Filter filter : filters) {
                if (filter instanceof TwoParameterFilter) {
                    ((TwoParameterFilter) filter).setParameter2(parameter2);
                }
            }
        }
    }

    @VisibleForTesting
    static class State {
        @VisibleForTesting
        boolean isProgramCreated = false;
        @VisibleForTesting
        boolean isFramebufferCreated = false;
        @VisibleForTesting
        Size size = null;
        private boolean sizeChanged = false;
        private int programHandle = -1;
        private GlFramebuffer outputFramebuffer = null;
        private GlTexture outputTexture = null;
    }
}
