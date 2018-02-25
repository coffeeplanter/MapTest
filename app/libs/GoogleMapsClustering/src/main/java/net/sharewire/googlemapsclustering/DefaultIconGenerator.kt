package net.sharewire.googlemapsclustering

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.SparseArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import ru.coffeeplanter.maptest.R

/**
 * The implementation of [IconGenerator] that generates icons with the default style
 * and caches them for subsequent use. To customize the style of generated icons use
 * [DefaultIconGenerator.setIconStyle].
 */
internal class DefaultIconGenerator<in T : ClusterItem>
/**
 * Creates an icon generator with the default icon style.
 */
(context: Context) : IconGenerator<T> {

    private val mContext: Context = Preconditions.checkNotNull(context)

    private var mIconStyle: IconStyle? = null

    private var mClusterItemIcon: BitmapDescriptor? = null

    private val mClusterIcons = SparseArray<BitmapDescriptor>()

    init {
        setIconStyle(createDefaultIconStyle())
    }

    /**
     * Sets a custom icon style used to generate marker icons.
     *
     * @param iconStyle the custom icon style used to generate marker icons
     */
    private fun setIconStyle(iconStyle: IconStyle) {
        mIconStyle = Preconditions.checkNotNull(iconStyle)
    }

    override fun getClusterIcon(cluster: Cluster<T>): BitmapDescriptor {
        val clusterBucket = getClusterIconBucket(cluster)
        var clusterIcon: BitmapDescriptor? = mClusterIcons.get(clusterBucket)

        if (clusterIcon == null) {
            clusterIcon = createClusterIcon(clusterBucket)
            mClusterIcons.put(clusterBucket, clusterIcon)
        }

        return clusterIcon
    }

    override fun getClusterItemIcon(clusterItem: T): BitmapDescriptor {
        if (mClusterItemIcon == null) {
            mClusterItemIcon = createClusterItemIcon()
        }
        return mClusterItemIcon as BitmapDescriptor
    }

    private fun createDefaultIconStyle(): IconStyle {
        return IconStyle.Builder(mContext).build()
    }

    @SuppressLint("InflateParams")
    private fun createClusterIcon(clusterBucket: Int): BitmapDescriptor {
        @SuppressLint("InflateParams")
        val clusterIconView = LayoutInflater.from(mContext)
                .inflate(R.layout.map_cluster_icon, null) as TextView
        clusterIconView.background = createClusterBackground()
        clusterIconView.setTextColor(mIconStyle!!.clusterTextColor)
        clusterIconView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                mIconStyle!!.clusterTextSize.toFloat())

        clusterIconView.text = getClusterIconText(clusterBucket)

        clusterIconView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        clusterIconView.layout(0, 0, clusterIconView.measuredWidth,
                clusterIconView.measuredHeight)

        val iconBitmap = Bitmap.createBitmap(clusterIconView.measuredWidth,
                clusterIconView.measuredHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(iconBitmap)
        clusterIconView.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(iconBitmap)
    }

    private fun createClusterBackground(): Drawable {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable.setColor(mIconStyle!!.clusterBackgroundColor)
        gradientDrawable.setStroke(mIconStyle!!.clusterStrokeWidth,
                mIconStyle!!.clusterStrokeColor)
        return gradientDrawable
    }

    private fun createClusterItemIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromResource(mIconStyle!!.clusterIconResId)
    }

    private fun getClusterIconBucket(cluster: Cluster<T>): Int {
        val itemCount = cluster.items.size
        if (itemCount <= CLUSTER_ICON_BUCKETS[0]) {
            return itemCount
        }

        return (0 until CLUSTER_ICON_BUCKETS.size - 1)
                .firstOrNull { itemCount < CLUSTER_ICON_BUCKETS[it + 1] }
                ?.let { CLUSTER_ICON_BUCKETS[it] }
                ?: CLUSTER_ICON_BUCKETS[CLUSTER_ICON_BUCKETS.size - 1]
    }

    private fun getClusterIconText(clusterIconBucket: Int): String {
        return if (clusterIconBucket < CLUSTER_ICON_BUCKETS[0])
            clusterIconBucket.toString()
        else
            clusterIconBucket.toString() + "+"
    }

    companion object {

        private val CLUSTER_ICON_BUCKETS = intArrayOf(10, 20, 50, 100, 500, 1000, 5000, 10000, 20000)
    }

}
