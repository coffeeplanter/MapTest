package net.sharewire.googlemapsclustering

/**
 * An object representing a cluster of items (markers) on the map.
 */
internal class Cluster<out T : ClusterItem> internal constructor(
        /**
         * The latitude of the cluster.
         *
         * @return the latitude of the cluster
         */
        val latitude: Double,
        /**
         * The longitude of the cluster.
         *
         * @return the longitude of the cluster
         */
        val longitude: Double,
        /**
         * The items contained in the cluster.
         *
         * @return the items contained in the cluster
         */
        val items: List<T>,
        private val north: Double, private val west: Double, private val south: Double, private val east: Double) {

    fun contains(latitude: Double, longitude: Double): Boolean {
        return (longitude in west..east
                && latitude <= north && latitude >= south)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val cluster = o as Cluster<*>?
        return java.lang.Double.compare(cluster!!.latitude, latitude) == 0 && java.lang.Double.compare(cluster.longitude, longitude) == 0
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long = java.lang.Double.doubleToLongBits(latitude)
        result = (temp xor temp.ushr(32)).toInt()
        temp = java.lang.Double.doubleToLongBits(longitude)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        return result
    }
}
