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

    override fun getLatitude(): Double {
        return lat.toDouble()
    }

    override fun getLongitude(): Double {
        return lng.toDouble()
    }

    override fun getTitle(): String? {
        return name
    }

    override fun getSnippet(): String? {
        return null
    }

}
