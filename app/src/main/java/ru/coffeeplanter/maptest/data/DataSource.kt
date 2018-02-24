package ru.coffeeplanter.maptest.data

import ru.coffeeplanter.maptest.data.model.Point

interface DataSource {

    val isEmpty: Boolean

    interface GetDataCallback {

        fun onSuccess(points: List<Point>)

        fun onComplete()

        fun onFailure()
    }

    fun getPoints(callback: GetDataCallback)

    fun getPoints(callback: GetDataCallback, isActivityRecreated: Boolean, arePointsDownloaded: Boolean)

    fun getPoints(startFrom: Int, callback: GetDataCallback)

    fun getPoints(startFrom: Int, pageSize: Int, callback: GetDataCallback)

    fun savePoints(points: List<Point>)

    companion object {
        const val DEFAULT_REMOTE_START_POSITION = 1
        const val DEFAULT_REMOTE_PAGE_SIZE = 150
    }

}
