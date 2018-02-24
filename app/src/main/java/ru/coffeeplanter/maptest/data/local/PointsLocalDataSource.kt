package ru.coffeeplanter.maptest.data.local

import io.realm.Realm
import ru.coffeeplanter.maptest.data.DataSource
import ru.coffeeplanter.maptest.data.model.Point

class PointsLocalDataSource : DataSource {

    private val realm = Realm.getDefaultInstance()

    override val isEmpty: Boolean
        get() {
            val point = realm.where(Point::class.java).findFirst()
            return point == null
        }

    // ========== DataSource ==========

    override fun getPoints(callback: DataSource.GetDataCallback) {
        val points = realm.where(Point::class.java).findAll()
        if (points != null && points.size > 0) {
            callback.onSuccess(realm.copyFromRealm(points))
            callback.onComplete()
        } else {
            callback.onFailure()
        }
    }

    override fun getPoints(callback: DataSource.GetDataCallback, isActivityRecreated: Boolean, arePointsDownloaded: Boolean) {
        getPoints(callback)
    }

    override fun getPoints(startFrom: Int, callback: DataSource.GetDataCallback) {
        getPoints(callback)
    }

    override fun getPoints(startFrom: Int, pageSize: Int, callback: DataSource.GetDataCallback) {
        getPoints(callback)
    }

    override fun savePoints(points: List<Point>) {
        realm.beginTransaction()
        realm.insertOrUpdate(points)
        realm.commitTransaction()
    }

    companion object {

        @Suppress("unused")
        private val TAG = PointsLocalDataSource::class.java.simpleName

        private var instance: PointsLocalDataSource? = null

        internal fun getInstance(): PointsLocalDataSource {
            if (instance == null) {
                instance = PointsLocalDataSource()
            }
            return instance as PointsLocalDataSource
        }
    }

}
