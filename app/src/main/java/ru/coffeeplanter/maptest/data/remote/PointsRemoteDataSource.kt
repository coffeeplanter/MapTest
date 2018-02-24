package ru.coffeeplanter.maptest.data.remote

import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.coffeeplanter.maptest.data.DataSource
import ru.coffeeplanter.maptest.data.model.Marker
import ru.coffeeplanter.maptest.data.model.Point
import ru.coffeeplanter.maptest.data.model.ServerPointsAnswer
import ru.coffeeplanter.maptest.platform.App
import java.io.IOException

class PointsRemoteDataSource : DataSource {

    private var cachedMarkers: List<Marker>? = null

    override val isEmpty: Boolean
        get() {
            class Bool {
                var isEmpty: Boolean = false
            }

            val dataSource = Bool()
            App.serverApi?.getPoints(DataSource.DEFAULT_REMOTE_START_POSITION, DataSource.DEFAULT_REMOTE_PAGE_SIZE)?.enqueue(object : Callback<ServerPointsAnswer> {
                override fun onResponse(call: Call<ServerPointsAnswer>, response: Response<ServerPointsAnswer>) {
                    if (response.isSuccessful && response.body() != null) {
                        val serverAnswer = response.body()
                        if (serverAnswer != null) {
                            val data = serverAnswer.data
                            dataSource.isEmpty = data == null || data.isEmpty()
                        }
                    }
                }

                override fun onFailure(call: Call<ServerPointsAnswer>, t: Throwable) {
                    dataSource.isEmpty = true
                    Log.e(TAG, "Error while getting test points from server", t)
                }
            })
            return dataSource.isEmpty
        }

    // ========== DataSource ==========

    override fun getPoints(callback: DataSource.GetDataCallback) {
        getPoints(DataSource.DEFAULT_REMOTE_START_POSITION, DataSource.DEFAULT_REMOTE_PAGE_SIZE, callback)
    }

    override fun getPoints(callback: DataSource.GetDataCallback, isActivityRecreated: Boolean, arePointsDownloaded: Boolean) {

    }

    override fun getPoints(startFrom: Int, callback: DataSource.GetDataCallback) {
        getPoints(startFrom, DataSource.DEFAULT_REMOTE_PAGE_SIZE, callback)
    }

    override fun getPoints(startFrom: Int, pageSize: Int, callback: DataSource.GetDataCallback) {
        App.serverApi?.markers?.enqueue(object : Callback<List<Marker>> {
            override fun onResponse(call: Call<List<Marker>>, response: Response<List<Marker>>) {
                if (response.isSuccessful && response.body() != null) {
                    cachedMarkers = response.body()
                    val okHttpClient = OkHttpClient()

                    for (i in cachedMarkers!!.indices) {
                        val marker = cachedMarkers!![i]

                        // Fixing mistake in rest api output
                        val markerUrl = marker.markerUrl
                        if (markerUrl?.contains("mytransporter.ru") == true) {
                            marker.markerUrl = markerUrl.replace("mytransporter.ru", "gofura.com")
                        }

                        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                        val request = Request.Builder()
                                .url(marker.markerUrl ?: "")
                                .build()

                        // Downloading images
                        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
                            @Throws(IOException::class)
                            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                                if (response.isSuccessful && response.body() != null) {

                                    val bitmap = BitmapFactory.decodeStream(response.body()!!.byteStream())
                                    marker.setBitmapByteArray(bitmap)
                                    if (i >= cachedMarkers!!.size - 1) {
                                        getPointsWithoutMarkers(startFrom, pageSize, callback)
                                        Log.d(TAG, "Markers got from remote data source")
                                    }
                                }
                            }

                            override fun onFailure(call: okhttp3.Call, e: IOException) {
                                Log.e(TAG, "Error while getting marker images from server")
                            }
                        })
                    }
                }
            }

            override fun onFailure(call: Call<List<Marker>>, t: Throwable) {
                Log.e(TAG, "Error while getting markers from server", t)
            }
        })
    }

    private fun getPointsWithoutMarkers(startFrom: Int, pageSize: Int, callback: DataSource.GetDataCallback) {
        App.serverApi?.getPoints(startFrom, pageSize)?.enqueue(object : Callback<ServerPointsAnswer> {
            override fun onResponse(call: Call<ServerPointsAnswer>, response: Response<ServerPointsAnswer>) {
                if (response.isSuccessful && response.body() != null) {
                    val serverAnswer = response.body()
                    if (serverAnswer != null) {
                        val data = serverAnswer.data
                        for (point in data!!) {
                            point.marker = getMarkerById(point.categoryId)
                        }
                        callback.onSuccess(data)
                        if (serverAnswer.hasMorePages()) {
                            val newStartFrom = data[data.size - 1].id
                            getPointsWithoutMarkers(newStartFrom, pageSize, callback)
                        } else {
                            callback.onComplete()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ServerPointsAnswer>, t: Throwable) {
                Log.e(TAG, "Error while getting points from server", t)
            }
        })
    }

    private fun getMarkerById(id: Int): Marker? {
        var result: Marker? = null
        if (cachedMarkers != null) {
            cachedMarkers!!
                    .filter { it.id == id }
                    .forEach { result = it }
        }
        return result
    }

    override fun savePoints(points: List<Point>) {

    }

    companion object {

        private val TAG = PointsRemoteDataSource::class.java.simpleName

        private var instance: PointsRemoteDataSource? = null

        internal fun getInstance(): PointsRemoteDataSource {
            if (instance == null) {
                instance = PointsRemoteDataSource()
            }
            return instance as PointsRemoteDataSource
        }
    }

}
