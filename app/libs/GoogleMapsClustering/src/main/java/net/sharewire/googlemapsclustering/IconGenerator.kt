package net.sharewire.googlemapsclustering

import com.google.android.gms.maps.model.BitmapDescriptor

/**
 * Generates icons for clusters and cluster items. Note that its implementations
 * should cache generated icons for subsequent use. For the example implementation see
 * [DefaultIconGenerator].
 */
internal interface IconGenerator<in T : ClusterItem> {
    /**
     * Returns an icon for the given cluster.
     *
     * @param cluster the cluster to return an icon for
     * @return the icon for the given cluster
     */
    fun getClusterIcon(cluster: Cluster<T>): BitmapDescriptor

    /**
     * Returns an icon for the given cluster item.
     *
     * @param clusterItem the cluster item to return an icon for
     * @return the icon for the given cluster item
     */
    fun getClusterItemIcon(clusterItem: T): BitmapDescriptor
}
