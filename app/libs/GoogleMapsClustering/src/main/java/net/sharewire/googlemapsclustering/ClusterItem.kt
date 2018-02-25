package net.sharewire.googlemapsclustering

/**
 * An object representing a single cluster item (marker) on the map.
 */
internal interface ClusterItem : QuadTreePoint {

    /**
     * The title of the item.
     *
     * @return the title of the item
     */
    val title: String?

    /**
     * The snippet of the item.
     *
     * @return the snippet of the item
     */
    val snippet: String?

    /**
     * The latitude of the item.
     *
     * @return the latitude of the item
     */
    override val latitude: Double

    /**
     * The longitude of the item.
     *
     * @return the longitude of the item
     */
    override val longitude: Double

}
