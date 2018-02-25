package net.sharewire.googlemapsclustering

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import net.sharewire.googlemapsclustering.IconStyle.Builder
import net.sharewire.googlemapsclustering.Preconditions.checkNotNull
import ru.coffeeplanter.maptest.R

/**
 * Represents the visual style of map marker icons. Supports customization
 * of individual attributes by setting their values with [Builder].
 */
class IconStyle private constructor(builder: Builder) {

    /**
     * Returns the background color of cluster icons.
     *
     * @return the background color of cluster icons
     */
    @get:ColorInt
    val clusterBackgroundColor: Int
    /**
     * Returns the text color of cluster icons.
     *
     * @return the text color of cluster icons
     */
    @get:ColorInt
    val clusterTextColor: Int
    /**
     * Returns the text color of cluster icons.
     *
     * @return the text color of cluster icons
     */
    @get:ColorInt
    val clusterStrokeColor: Int
    /**
     * Returns the width of the stroke of cluster icons.
     *
     * @return the width of the stroke of cluster icons
     */
    val clusterStrokeWidth: Int
    /**
     * Returns the size of the text of cluster icons.
     *
     * @return the size of the text of cluster icons
     */
    val clusterTextSize: Int
    /**
     * Returns the icon resource of cluster items.
     *
     * @return the icon resource of cluster items
     */
    @get:DrawableRes
    val clusterIconResId: Int

    init {
        clusterBackgroundColor = builder.clusterBackgroundColor
        clusterTextColor = builder.clusterTextColor
        clusterStrokeColor = builder.clusterStrokeColor
        clusterStrokeWidth = builder.clusterStrokeWidth
        clusterTextSize = builder.clusterTextSize
        clusterIconResId = builder.clusterIconResId
    }

    @Suppress("unused")
    /**
     * The builder for [IconStyle]. Allows to customize different style attributes.
     * If a style attribute is not set explicitly, the default value will be used.
     */
    class Builder
    /**
     * Creates a new builder with the default style.
     */
    (context: Context) {

        internal var clusterBackgroundColor: Int = 0
        internal var clusterTextColor: Int = 0
        internal var clusterStrokeColor: Int = 0
        internal var clusterStrokeWidth: Int = 0
        internal var clusterTextSize: Int = 0
        internal var clusterIconResId: Int = 0

        init {
            checkNotNull(context)
            //            clusterBackgroundColor = ContextCompat.getColor(
            //                    context, R.color.cluster_background);
            clusterBackgroundColor = ContextCompat.getColor(
                    context, R.color.colorPrimary)
            clusterTextColor = ContextCompat.getColor(
                    context, R.color.cluster_text)
            clusterStrokeColor = ContextCompat.getColor(
                    context, R.color.cluster_stroke)
            clusterStrokeWidth = context.resources
                    .getDimensionPixelSize(R.dimen.cluster_stroke_width)
            clusterTextSize = context.resources
                    .getDimensionPixelSize(R.dimen.cluster_text_size)
            clusterIconResId = R.drawable.ic_map_marker
        }

        /**
         * Sets the background color of cluster icons.
         *
         * @param color the background color of cluster icons
         * @return the builder instance
         */
        fun setClusterBackgroundColor(@ColorInt color: Int): Builder {
            clusterBackgroundColor = color
            return this
        }

        /**
         * Sets the text color of cluster icons.
         *
         * @param color the text color of cluster icons
         * @return the builder instance
         */
        fun setClusterTextColor(@ColorInt color: Int): Builder {
            clusterTextColor = color
            return this
        }

        /**
         * Sets the color of the stroke of cluster icons.
         *
         * @param color the color of the stroke of cluster icons
         * @return the builder instance
         */
        fun setClusterStrokeColor(@ColorInt color: Int): Builder {
            clusterStrokeColor = color
            return this
        }

        /**
         * Sets the width of the stroke of cluster icons.
         *
         * @param width the width of the stroke of cluster icons
         * @return the builder instance
         */
        fun setClusterStrokeWidth(width: Int): Builder {
            clusterStrokeWidth = width
            return this
        }

        /**
         * Sets the size of the text of cluster icons.
         *
         * @param size the size of the text of cluster icons
         * @return the builder instance
         */
        fun setClusterTextSize(size: Int): Builder {
            clusterTextSize = size
            return this
        }

        /**
         * Sets the icon resource of cluster items.
         *
         * @param resId the icon resource of cluster items
         * @return the builder instance
         */
        fun setClusterIconResId(@DrawableRes resId: Int): Builder {
            clusterIconResId = resId
            return this
        }

        /**
         * Creates a new instance of [IconStyle] with provided style attributes.
         *
         * @return new instance of [IconStyle] with provided style attributes
         */
        fun build(): IconStyle {
            return IconStyle(this)
        }
    }

}
