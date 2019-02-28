package com.bornfight.roundedtabbar.utils

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by tomislav on 07/03/2017.
 */

internal class HorizontalOffsetDecoration(context: Context, offset: Float) : RecyclerView.ItemDecoration() {

    private val offset: Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, offset, context.resources.displayMetrics).toInt()

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {

        outRect.left = offset / 2
        outRect.right = offset / 2

    }
}