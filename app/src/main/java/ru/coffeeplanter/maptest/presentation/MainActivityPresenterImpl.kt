package ru.coffeeplanter.maptest.presentation

import android.os.Bundle

import ru.coffeeplanter.maptest.data.DataSource
import ru.coffeeplanter.maptest.data.PointsRepository
import ru.coffeeplanter.maptest.data.local.PointsLocalDataSource
import ru.coffeeplanter.maptest.data.model.Point
import ru.coffeeplanter.maptest.data.remote.PointsRemoteDataSource


class MainActivityPresenterImpl(private var mainActivityView: MainActivityContract.View?) : MainActivityContract.Presenter, DataSource.GetDataCallback {

    private val pointsRepository: PointsRepository? =
            PointsRepository.getInstance(PointsRemoteDataSource.getInstance(), PointsLocalDataSource.getInstance())

    private var isGettingPoints = false
    private var arePointsStoredLocally = false


    // ========== MainActivityContract.Presenter ==========

    override fun attachView(view: MainActivityContract.View) {
        mainActivityView = view
    }

    override fun detachView() {
        mainActivityView = null
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        if (mainActivityView != null) {
            mainActivityView?.addMapFragment(savedInstanceState)
        }
    }

    override fun onMapReady() {
        if (mainActivityView != null) {
            mainActivityView?.setUpMap()
        }
    }

    override fun getPoints(isActivityRecreated: Boolean, arePointsDownloaded: Boolean) {
        arePointsStoredLocally = arePointsDownloaded
        if (!isGettingPoints) {
            isGettingPoints = true
            pointsRepository?.getPoints(this, isActivityRecreated, arePointsDownloaded)
        }
    }

    // ========== DataSource.GetDataCallback ==========

    override fun onSuccess(points: List<Point>) {
        if (mainActivityView != null) {
            mainActivityView?.showMarkers(points)
        }
    }

    override fun onComplete() {
        isGettingPoints = false
        if (!arePointsStoredLocally) {
            if (mainActivityView != null) {
                mainActivityView?.makeCompleteActions()
            }
            NotificationShower.showNotification()
            arePointsStoredLocally = true
        }
    }

    override fun onFailure() {
        isGettingPoints = false
    }

    companion object {
        @Suppress("unused")
        private val TAG = MainActivityPresenterImpl::class.java.simpleName
    }

}
