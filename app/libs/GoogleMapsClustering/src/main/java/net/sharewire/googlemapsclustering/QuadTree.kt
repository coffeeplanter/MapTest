package net.sharewire.googlemapsclustering

import java.util.*

internal class QuadTree<T : QuadTreePoint>(private val bucketSize: Int) {

    private var root: QuadTreeNode<T>? = null

    init {
        this.root = createRootNode(bucketSize)
    }

    fun insert(point: T) {
        root!!.insert(point)
    }

    fun queryRange(north: Double, west: Double, south: Double, east: Double): List<T> {
        val points = ArrayList<T>()
        root!!.queryRange(QuadTreeRect(north, west, south, east), points)
        return points
    }

    fun clear() {
        root = createRootNode(bucketSize)
    }

    private fun createRootNode(bucketSize: Int): QuadTreeNode<T> {
        return QuadTreeNode(90.0, -180.0, -90.0, 180.0, bucketSize)
    }

}
