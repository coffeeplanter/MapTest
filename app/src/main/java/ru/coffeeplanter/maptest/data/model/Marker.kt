package ru.coffeeplanter.maptest.data.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.ByteArrayOutputStream

@Suppress("MemberVisibilityCanBePrivate")
open class Marker : RealmObject() {

    @PrimaryKey
    var id: Int = 0
    var name: String? = null
    var markerUrl: String? = null
    var bitmapByteArray: ByteArray? = null

    val bitmap: Bitmap
        get() = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray?.size?:0)

    fun setBitmapByteArray(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        this.bitmapByteArray = stream.toByteArray()
    }

}
