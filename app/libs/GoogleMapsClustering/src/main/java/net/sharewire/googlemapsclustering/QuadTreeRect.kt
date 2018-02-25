package net.sharewire.googlemapsclustering

internal class QuadTreeRect(val north: Double, val west: Double, val south: Double, val east: Double) {

    fun contains(latitude: Double, longitude: Double): Boolean {
        return longitude in west..east && latitude <= north && latitude >= south
    }

    fun intersects(bounds: QuadTreeRect): Boolean {
        return west <= bounds.east && east >= bounds.west && north >= bounds.south && south <= bounds.north
    }

}
