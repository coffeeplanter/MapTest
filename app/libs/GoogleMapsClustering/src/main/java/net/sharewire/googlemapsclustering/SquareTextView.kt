package net.sharewire.googlemapsclustering

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

internal class SquareTextView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val measuredWidth = measuredWidth

        setMeasuredDimension(measuredWidth, measuredWidth)
    }

}
