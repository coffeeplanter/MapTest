package net.sharewire.googlemapsclustering

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import java.util.*
import java.util.concurrent.Executors

/**
 * Groups multiple items on a map into clusters based on the current zoom level.
 * Clustering occurs when the map becomes idle, so an instance of this class
 * must be set as a camera idle listener using [GoogleMap.setOnCameraIdleListener].
 *
 * @param <T> the type of an item to be clustered
</T> */
@Suppress("unused")
internal class ClusterManager<T : ClusterItem>
/**
 * Creates a new cluster manager using the default icon generator.
 * To customize marker icons, set a custom icon generator using
 * [ClusterManager.setIconGenerator].
 *
 * @param googleMap the map instance where markers will be rendered
 */
(context: Context, googleMap: GoogleMap) : GoogleMap.OnCameraIdleListener {

    private val mGoogleMap: GoogleMap

    private val mQuadTree: QuadTree<T>

    private val mRenderer: ClusterRenderer<T>

    private val mExecutor = Executors.newSingleThreadExecutor()

    private var mQuadTreeTask: AsyncTask<*, *, *>? = null

    private var mClusterTask: AsyncTask<*, *, *>? = null

    private var mMinClusterSize = DEFAULT_MIN_CLUSTER_SIZE

    /**
     * Defines signatures for methods that are called when a cluster or a cluster item is clicked.
     *
     * @param <T> the type of an item managed by [ClusterManager].
    </T> */
    interface Callbacks<in T : ClusterItem> {
        /**
         * Called when a marker representing a cluster has been clicked.
         *
         * @param cluster the cluster that has been clicked
         * @return `true` if the listener has consumed the event (i.e., the default behavior should not occur);
         * `false` otherwise (i.e., the default behavior should occur). The default behavior is for the camera
         * to move to the marker and an info window to appear.
         */
        fun onClusterClick(cluster: Cluster<T>): Boolean

        /**
         * Called when a marker representing a cluster item has been clicked.
         *
         * @param clusterItem the cluster item that has been clicked
         * @return `true` if the listener has consumed the event (i.e., the default behavior should not occur);
         * `false` otherwise (i.e., the default behavior should occur). The default behavior is for the camera
         * to move to the marker and an info window to appear.
         */
        fun onClusterItemClick(clusterItem: T): Boolean
    }

    init {
        Preconditions.checkNotNull(context)
        mGoogleMap = Preconditions.checkNotNull(googleMap)
        mRenderer = ClusterRenderer(context, googleMap)
        mQuadTree = QuadTree(QUAD_TREE_BUCKET_CAPACITY)
    }

    /**
     * Sets a custom icon generator thus replacing the default one.
     *
     * @param iconGenerator the custom icon generator that's used for generating marker icons
     */
    fun setIconGenerator(iconGenerator: IconGenerator<T>) {
        Preconditions.checkNotNull(iconGenerator)
        mRenderer.setIconGenerator(iconGenerator)
    }

    /**
     * Sets a custom cluster icon generator thus replacing the default one.
     *
     * @param iconGenerator the custom icon generator that's used for generating cluster icons
     */
    fun setClusterIconGenerator(iconGenerator: IconGenerator<T>) {
        Preconditions.checkNotNull(iconGenerator)
        mRenderer.setClusterIconGenerator(iconGenerator)
    }

    /**
     * Sets a callback that's invoked when a cluster or a cluster item is clicked.
     *
     * @param callbacks the callback that's invoked when a cluster or an individual item is clicked.
     * To unset the callback, use `null`.
     */
    fun setCallbacks(callbacks: Callbacks<T>?) {
        mRenderer.setCallbacks(callbacks)
    }

    /**
     * Sets items to be clustered thus replacing the old ones.
     *
     * @param clusterItems the items to be clustered
     */
    fun setItems(clusterItems: List<T>) {
        Preconditions.checkNotNull(clusterItems)
        buildQuadTree(clusterItems)
    }

    /**
     * Sets the minimum size of a cluster. If the cluster size
     * is less than this value, display individual markers.
     */
    fun setMinClusterSize(minClusterSize: Int) {
        Preconditions.checkArgument(minClusterSize > 0)
        mMinClusterSize = minClusterSize
    }

    override fun onCameraIdle() {
        cluster()
    }

    private fun buildQuadTree(clusterItems: List<T>) {
        if (mQuadTreeTask != null) {
            mQuadTreeTask!!.cancel(true)
        }

        mQuadTreeTask = QuadTreeTask(clusterItems).executeOnExecutor(mExecutor)
    }

    private fun cluster() {
        if (mClusterTask != null) {
            mClusterTask!!.cancel(true)
        }

        mClusterTask = ClusterTask(mGoogleMap.projection.visibleRegion.latLngBounds,
                mGoogleMap.cameraPosition.zoom).executeOnExecutor(mExecutor)
    }

    private fun getClusters(latLngBounds: LatLngBounds, zoomLevel: Float): List<Cluster<T>> {
        val clusters = ArrayList<Cluster<T>>()

        val tileCount = (Math.pow(2.0, zoomLevel.toDouble()) * 2).toLong()

        val startLatitude = latLngBounds.northeast.latitude
        val endLatitude = latLngBounds.southwest.latitude

        val startLongitude = latLngBounds.southwest.longitude
        val endLongitude = latLngBounds.northeast.longitude

        val stepLatitude = 180.0 / tileCount
        val stepLongitude = 360.0 / tileCount

        val startX = ((startLongitude + 180.0) / stepLongitude).toLong()
        val startY = ((90.0 - startLatitude) / stepLatitude).toLong()

        var endX = ((endLongitude + 180.0) / stepLongitude).toLong() + 1
        val endY = ((90.0 - endLatitude) / stepLatitude).toLong() + 1

        // Handling 180/-180 overlap
        if (startX > endX) {
            endX += tileCount
        }

        for (x in startX..endX) {
            // keep tileX in [0; tileCount) range
            val tileX = x % tileCount

            for (tileY in startY..endY) {
                val north = 90.0 - tileY * stepLatitude
                val west = tileX * stepLongitude - 180.0
                val south = north - stepLatitude
                val east = west + stepLongitude

                val points = mQuadTree.queryRange(north, west, south, east)

                if (points.isNotEmpty()) {
                    if (points.size >= mMinClusterSize) {
                        var totalLatitude = 0.0
                        var totalLongitude = 0.0

                        for (point in points) {
                            totalLatitude += point.latitude
                            totalLongitude += point.longitude
                        }

                        val latitude = totalLatitude / points.size
                        val longitude = totalLongitude / points.size

                        clusters.add(Cluster(latitude, longitude,
                                points, north, west, south, east))
                    } else {
                        points.mapTo(clusters) {
                            Cluster(it.latitude, it.longitude,
                                    listOf(it), north, west, south, east)
                        }
                    }
                }
            }
        }

        return clusters
    }

    @SuppressLint("StaticFieldLeak")
    private inner class QuadTreeTask internal constructor(private val mClusterItems: List<T>) : AsyncTask<Void?, Void?, Void?>() {

        override fun doInBackground(vararg params: Void?): Void? {
            mQuadTree.clear()
            for (i in mClusterItems.indices) {
                mQuadTree.insert(mClusterItems[i])
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            cluster()
            mQuadTreeTask = null
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class ClusterTask internal constructor(private val mLatLngBounds: LatLngBounds, private val mZoomLevel: Float) : AsyncTask<Void, Void, List<Cluster<T>>>() {

        override fun doInBackground(vararg params: Void): List<Cluster<T>> {
            return getClusters(mLatLngBounds, mZoomLevel)
        }

        override fun onPostExecute(clusters: List<Cluster<T>>) {
            mRenderer.render(clusters)
            mClusterTask = null
        }
    }

    companion object {
        private const val QUAD_TREE_BUCKET_CAPACITY = 4
        private const val DEFAULT_MIN_CLUSTER_SIZE = 1
    }

}
