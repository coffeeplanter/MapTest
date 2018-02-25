package ru.coffeeplanter.maptest.data

import android.util.Log
import ru.coffeeplanter.maptest.data.model.Point
import java.util.*

class PointsRepository private constructor(private val pointsRemoteDataSource: DataSource, private val pointsLocalDataSource: DataSource) : DataSource {

    private val cachedPoints: MutableList<Point>
    private var areAllPointsLoadedInCache = false

    override val isEmpty: Boolean
        get() = pointsLocalDataSource.isEmpty || pointsRemoteDataSource.isEmpty

    init {
        cachedPoints = ArrayList()
    }

    // ========== DataSource ==========

    override fun getPoints(callback: DataSource.GetDataCallback) {
        getPoints(DataSource.DEFAULT_REMOTE_START_POSITION, DataSource.DEFAULT_REMOTE_PAGE_SIZE, callback)
    }

    override fun getPoints(callback: DataSource.GetDataCallback, isActivityRecreated: Boolean, arePointsDownloaded: Boolean) {
        when {
            areAllPointsLoadedInCache -> {
                callback.onSuccess(cachedPoints)
                callback.onComplete()
                Log.d(TAG, "Data got from memory cache")
            }
            arePointsDownloaded -> getPointsFromLocalDataSource(callback)
            isActivityRecreated -> getPointsFromLocalAndRemoteDataSource(callback)
            else -> getPointsFromRemoteDataSource(callback, DataSource.DEFAULT_REMOTE_START_POSITION)
        }
    }

    private fun getPointsFromLocalDataSource(callback: DataSource.GetDataCallback) {
        pointsLocalDataSource.getPoints(object : DataSource.GetDataCallback {
            override fun onSuccess(points: List<Point>) {
                cachedPoints.clear()
                cachedPoints.addAll(points)
                callback.onSuccess(cachedPoints)
                onComplete()
            }

            override fun onComplete() {
                areAllPointsLoadedInCache = true
                callback.onComplete()
                Log.d(TAG, "Data got from local data source")
            }

            override fun onFailure() {
                Log.e(TAG, "Error while getting points from local database")
            }
        })
    }

    private fun getPointsFromLocalAndRemoteDataSource(callback: DataSource.GetDataCallback) {
        pointsLocalDataSource.getPoints(object : DataSource.GetDataCallback {
            override fun onSuccess(points: List<Point>) {
                cachedPoints.clear()
                cachedPoints.addAll(points)
                callback.onSuccess(cachedPoints)
                val lastPointStored = points[points.size - 1]
                getPointsFromRemoteDataSource(callback, lastPointStored.id)
                onComplete()
            }

            override fun onComplete() {
                Log.d(TAG, "Data partially stored in the local database, continue to download it from the remote server")
            }

            override fun onFailure() {
                getPointsFromRemoteDataSource(callback, DataSource.DEFAULT_REMOTE_START_POSITION)
            }
        })
    }

    private fun getPointsFromRemoteDataSource(callback: DataSource.GetDataCallback, startPosition: Int) {
        pointsRemoteDataSource.getPoints(startPosition, DataSource.DEFAULT_REMOTE_PAGE_SIZE, object : DataSource.GetDataCallback {
            override fun onSuccess(points: List<Point>) {
                pointsLocalDataSource.savePoints(points)
                cachedPoints.addAll(points)
                callback.onSuccess(cachedPoints)
            }

            override fun onComplete() {
                areAllPointsLoadedInCache = true
                callback.onComplete()
                Log.d(TAG, "Data got from remote data source")
            }

            override fun onFailure() {
                Log.e(TAG, "Error while getting points from remote server")
            }
        })
    }

    override fun getPoints(startFrom: Int, callback: DataSource.GetDataCallback) {
        getPoints(startFrom, DataSource.DEFAULT_REMOTE_PAGE_SIZE, callback)
    }

    override fun getPoints(startFrom: Int, pageSize: Int, callback: DataSource.GetDataCallback) {}

    override fun savePoints(points: List<Point>) {}

    companion object {

        private val TAG = PointsRepository::class.java.simpleName

        private var instance: PointsRepository? = null

        fun getInstance(tasksRemoteDataSource: DataSource, tasksLocalDataSource: DataSource): PointsRepository {
            if (instance == null) {
                instance = PointsRepository(tasksRemoteDataSource, tasksLocalDataSource)
            }
            return instance as PointsRepository
        }

        @Suppress("unused")
        fun destroyInstance() {
            instance = null
        }
    }

}
