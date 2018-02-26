package net.sharewire.googlemapsclustering

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.support.v4.view.animation.FastOutSlowInInterpolator
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

internal class ClusterRenderer<T : ClusterItem>(context: Context, private val mGoogleMap: GoogleMap) : GoogleMap.OnMarkerClickListener {

    private val mClusters = ArrayList<Cluster<T>>()

    private val mMarkers = HashMap<Cluster<T>, Marker>()

    private var mIconGenerator: IconGenerator<T>? = null

    private var mClusterIconGenerator: IconGenerator<T>? = null

    private var mCallbacks: ClusterManager.Callbacks<T>? = null

    init {
        mGoogleMap.setOnMarkerClickListener(this)
        mIconGenerator = DefaultIconGenerator(context)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val markerTag = marker.tag
        if (markerTag is Cluster<*>) {

            @Suppress("UNCHECKED_CAST")
            val cluster = marker.tag as Cluster<T>?

            val clusterItems = cluster!!.items

            if (mCallbacks != null) {
                return if (clusterItems.size > 1) {
                    mCallbacks!!.onClusterClick(cluster)
                } else {
                    mCallbacks!!.onClusterItemClick(clusterItems[0])
                }
            }
        }

        return false
    }

    fun setCallbacks(listener: ClusterManager.Callbacks<T>?) {
        mCallbacks = listener
    }

    fun setIconGenerator(iconGenerator: IconGenerator<T>) {
        mIconGenerator = iconGenerator
    }

    fun setClusterIconGenerator(iconGenerator: IconGenerator<T>) {
        mClusterIconGenerator = iconGenerator
    }

    fun render(clusters: List<Cluster<T>>) {
        val clustersToAdd = clusters.filterNot { mMarkers.containsKey(it) }

        val clustersToRemove = mMarkers.keys.filterNot { clusters.contains(it) }

        mClusters.addAll(clustersToAdd)
        mClusters.removeAll(clustersToRemove)

        // Remove the old clusters.
        for (clusterToRemove in clustersToRemove) {
            val markerToRemove = mMarkers[clusterToRemove]
            markerToRemove?.zIndex = BACKGROUND_MARKER_Z_INDEX.toFloat()

            val parentCluster = findParentCluster(mClusters, clusterToRemove.latitude,
                    clusterToRemove.longitude)
            if (parentCluster != null) {
                animateMarkerToLocation(markerToRemove, LatLng(parentCluster.latitude,
                        parentCluster.longitude), true)
            } else {
                markerToRemove?.remove()
            }

            mMarkers.remove(clusterToRemove)
        }

        // Add the new clusters.
        for (clusterToAdd in clustersToAdd) {
            val markerToAdd: Marker

            val markerIcon = getMarkerIcon(clusterToAdd)
            val markerTitle = getMarkerTitle(clusterToAdd)
            val markerSnippet = getMarkerSnippet(clusterToAdd)

            val parentCluster = findParentCluster(clustersToRemove, clusterToAdd.latitude,
                    clusterToAdd.longitude)
            if (parentCluster != null) {
                markerToAdd = mGoogleMap.addMarker(MarkerOptions()
                        .position(LatLng(parentCluster.latitude, parentCluster.longitude))
                        .icon(markerIcon)
                        .title(markerTitle)
                        .snippet(markerSnippet)
                        .zIndex(FOREGROUND_MARKER_Z_INDEX.toFloat()))
                animateMarkerToLocation(markerToAdd,
                        LatLng(clusterToAdd.latitude, clusterToAdd.longitude), false)
            } else {
                markerToAdd = mGoogleMap.addMarker(MarkerOptions()
                        .position(LatLng(clusterToAdd.latitude, clusterToAdd.longitude))
                        .icon(markerIcon)
                        .title(markerTitle)
                        .snippet(markerSnippet)
                        .alpha(0.0f)
                        .zIndex(FOREGROUND_MARKER_Z_INDEX.toFloat()))
                animateMarkerAppearance(markerToAdd)
            }
            markerToAdd.tag = clusterToAdd

            mMarkers[clusterToAdd] = markerToAdd
        }
    }

    private fun getMarkerIcon(cluster: Cluster<T>): BitmapDescriptor {
        val clusterIcon: BitmapDescriptor

        val clusterItems = cluster.items
        clusterIcon = if (clusterItems.size > 1) {
            mClusterIconGenerator!!.getClusterIcon(cluster)
        } else {
            mIconGenerator!!.getClusterItemIcon(clusterItems[0])
        }

        return Preconditions.checkNotNull(clusterIcon)
    }

    private fun getMarkerTitle(cluster: Cluster<T>): String? {
        val clusterItems = cluster.items
        return if (clusterItems.size > 1) {
            null
        } else {
            clusterItems[0].title
        }
    }

    private fun getMarkerSnippet(cluster: Cluster<T>): String? {
        val clusterItems = cluster.items
        return if (clusterItems.size > 1) {
            null
        } else {
            clusterItems[0].snippet
        }
    }

    private fun findParentCluster(clusters: List<Cluster<T>>,
                                  latitude: Double, longitude: Double): Cluster<T>? {

        return clusters.firstOrNull { it.contains(latitude, longitude) }
    }

    private fun animateMarkerToLocation(marker: Marker?, targetLocation: LatLng,
                                        removeAfter: Boolean) {
        val objectAnimator = ObjectAnimator.ofObject(marker, "position",
                LatLngTypeEvaluator(), targetLocation)
        objectAnimator.interpolator = FastOutSlowInInterpolator()
        objectAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (removeAfter) {
                    marker?.remove()
                }
            }
        })
        objectAnimator.start()
    }

    private fun animateMarkerAppearance(marker: Marker) {
        ObjectAnimator.ofFloat(marker, "alpha", 1.0f).start()
    }

    private class LatLngTypeEvaluator : TypeEvaluator<LatLng> {

        override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
            val latitude = (endValue.latitude - startValue.latitude) * fraction + startValue.latitude
            val longitude = (endValue.longitude - startValue.longitude) * fraction + startValue.longitude
            return LatLng(latitude, longitude)
        }
    }

    companion object {
        private const val BACKGROUND_MARKER_Z_INDEX = 0
        private const val FOREGROUND_MARKER_Z_INDEX = 1
    }

}
