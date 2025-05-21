package com.base.cameraview.size

/**
 * A simple class representing a size, with width and height values.
 */
class Size(val width: Int, val height: Int) : Comparable<Size> {
    /**
     * Returns a flipped size, with height equal to this size's width
     * and width equal to this size's height.
     *
     * @return a flipped size
     */
    fun flip(): Size {
        return Size(this.height, this.width)
    }

    override fun equals(o: Any?): Boolean {
        if (o == null) {
            return false
        }
        if (this === o) {
            return true
        }
        if (o is Size) {
            val size = o
            return this.width == size.width && this.height == size.height
        }
        return false
    }

    override fun toString(): String {
        return width.toString() + "x" + this.height
    }

    override fun hashCode(): Int {
        return this.height xor ((this.width shl (Integer.SIZE / 2)) or (this.width ushr (Integer.SIZE / 2)))
    }

    override fun compareTo(other: Size): Int {
        return this.width * this.height - other.width * other.height
    }

    companion object {
        const val DEFAULT_WIDTH = 480
        const val DEFAULT_HEIGHT = 640

        fun defaultSize() = Size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
    }
}
