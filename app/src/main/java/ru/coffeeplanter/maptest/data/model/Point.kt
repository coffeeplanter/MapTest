package ru.coffeeplanter.maptest.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import net.sharewire.googlemapsclustering.ClusterItem

@Suppress("MemberVisibilityCanBePrivate")
open class Point : RealmObject, ClusterItem {

    @PrimaryKey
    var id: Int = 0
    var name: String? = null
    var lat: Float = 0f
    var lng: Float = 0f
    var categoryId: Int = 0
    var marker: Marker? = null

    constructor()

    constructor(id: Int, name: String, lat: Float, lng: Float, categoryId: Int) {
        this.id = id
        this.name = name
        this.lat = lat
        this.lng = lng
        this.categoryId = categoryId
    }

    // ========== ClusterItem ==========

    override val latitude: Double
        get() = lat.toDouble()

    override val longitude: Double
        get() = lng.toDouble()

    override val title: String?
        get() = name

    override val snippet: String?
        get() = null

}
