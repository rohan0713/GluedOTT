package com.ott.gluedin.presentation.common

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * An [AppCompatImageView] that scales images to fill the view (aspect-fill)
 * while anchoring the image to the top edge (dy = 0) and centering horizontally.
 *
 * This prevents movie posters and hero banners from cutting off character faces,
 * show titles, and header artwork from the top edge.
 */
class TopCropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        recomputeMatrix()
        return changed
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        recomputeMatrix()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        recomputeMatrix()
    }

    private fun recomputeMatrix() {
        val d = drawable ?: return
        val dWidth = d.intrinsicWidth.toFloat()
        val dHeight = d.intrinsicHeight.toFloat()
        if (dWidth <= 0f || dHeight <= 0f) return

        val vWidth = (width - paddingLeft - paddingRight).toFloat()
        val vHeight = (height - paddingTop - paddingBottom).toFloat()
        if (vWidth <= 0f || vHeight <= 0f) return

        // Scale to fill the entire view (aspect-fill)
        val scale = maxOf(vWidth / dWidth, vHeight / dHeight)

        // Center horizontally
        val dx = (vWidth - dWidth * scale) * 0.5f + paddingLeft

        // Anchor to the top edge so that actors' heads and poster titles are never cut off
        val dy = paddingTop.toFloat()

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        imageMatrix = matrix
    }
}
