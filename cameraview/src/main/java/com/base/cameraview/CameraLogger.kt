package com.base.cameraview

import android.util.Log
import androidx.annotation.VisibleForTesting
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Utility class that can log traces and info.
 */
class CameraLogger private constructor(private val mTag: String) {
    private fun should(messageLevel: Int): Boolean {
        return sLevel <= messageLevel && sLoggers.isNotEmpty()
    }

    /**
     * Log to the verbose channel.
     *
     * @param data log contents
     * @return the log message, if logged
     */
    fun v(vararg data: Any): String? {
        return log(LEVEL_VERBOSE, *data)
    }

    /**
     * Log to the info channel.
     *
     * @param data log contents
     * @return the log message, if logged
     */
    fun i(vararg data: Any): String? {
        return log(LEVEL_INFO, *data)
    }

    /**
     * Log to the warning channel.
     *
     * @param data log contents
     * @return the log message, if logged
     */
    fun w(vararg data: Any): String? {
        return log(LEVEL_WARNING, *data)
    }

    /**
     * Log to the error channel.
     *
     * @param data log contents
     * @return the log message, if logged
     */
    fun e(vararg data: Any): String? {
        return log(LEVEL_ERROR, *data)
    }

    private fun log(@LogLevel level: Int, vararg data: Any): String? {
        if (!should(level)) return null

        val message = StringBuilder()
        var throwable: Throwable? = null
        for (`object` in data) {
            if (`object` is Throwable) {
                throwable = `object`
            }
            message.append(`object`)
            message.append(" ")
        }
        val string = message.toString().trim { it <= ' ' }
        for (logger in sLoggers) {
            logger.log(level, mTag, string, throwable)
        }
        lastMessage = string
        lastTag = mTag
        return string
    }

    /**
     * Interface of integers representing log levels.
     *
     * @see .LEVEL_VERBOSE
     *
     * @see .LEVEL_INFO
     *
     * @see .LEVEL_WARNING
     *
     * @see .LEVEL_ERROR
     */
//    @IntDef([LEVEL_VERBOSE, LEVEL_INFO, LEVEL_WARNING, LEVEL_ERROR])
    @Retention(AnnotationRetention.SOURCE)
    annotation class LogLevel

    /**
     * A Logger can listen to internal log events
     * and log them to different providers.
     * The default logger will simply post to logcat.
     */
    fun interface Logger {
        /**
         * Notifies that an internal log event was just triggered.
         *
         * @param level     the log level
         * @param tag       the log tag
         * @param message   the log message
         * @param throwable an optional throwable
         */
        fun log(
            @LogLevel level: Int,
            tag: String,
            message: String,
            throwable: Throwable?
        )
    }

    companion object {
        const val LEVEL_VERBOSE: Int = 0
        const val LEVEL_INFO: Int = 1
        const val LEVEL_WARNING: Int = 2
        const val LEVEL_ERROR: Int = 3

        @VisibleForTesting
        var lastMessage: String? = null

        @VisibleForTesting
        var lastTag: String? = null

        @VisibleForTesting
        var sAndroidLogger: Logger =
            Logger { level: Int, tag: String, message: String, throwable: Throwable? ->
                when (level) {
                    LEVEL_VERBOSE -> Log.v(tag, message, throwable)
                    LEVEL_INFO -> Log.i(tag, message, throwable)
                    LEVEL_WARNING -> Log.w(tag, message, throwable)
                    LEVEL_ERROR -> Log.e(tag, message, throwable)
                }
            }
        private var sLevel = 0
        private val sLoggers: MutableSet<Logger> = CopyOnWriteArraySet<Logger>()

        init {
            setLogLevel(LEVEL_ERROR)
            sLoggers.add(sAndroidLogger)
        }

        /**
         * Creates a CameraLogger that will stream logs into the
         * internal logs and dispatch them to [Logger]s.
         *
         * @param tag the logger tag
         * @return a new CameraLogger
         */
        @JvmStatic
        fun create(tag: String): CameraLogger {
            return CameraLogger(tag)
        }

        /**
         * Sets the log sLevel for logcat events.
         *
         * @param logLevel the desired log sLevel
         * @see .LEVEL_VERBOSE
         *
         * @see .LEVEL_INFO
         *
         * @see .LEVEL_WARNING
         *
         * @see .LEVEL_ERROR
         */
        fun setLogLevel(@LogLevel logLevel: Int) {
            sLevel = logLevel
        }

        /**
         * Registers an external [Logger] for log events.
         * Make sure to unregister using [.unregisterLogger].
         *
         * @param logger logger to add
         */
        fun registerLogger(logger: Logger) {
            sLoggers.add(logger)
        }

        /**
         * Unregisters a previously registered [Logger] for log events.
         * This is needed in order to avoid leaks.
         *
         * @param logger logger to remove
         */
        fun unregisterLogger(logger: Logger) {
            sLoggers.remove(logger)
        }
    }
}

