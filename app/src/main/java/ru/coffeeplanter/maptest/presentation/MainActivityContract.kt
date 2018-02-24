package ru.coffeeplanter.maptest.presentation

import android.os.Bundle

import ru.coffeeplanter.maptest.data.model.Point

interface MainActivityContract {

    interface View {

        fun addMapFragment(savedInstanceState: Bundle?)

        fun setUpMap()

        fun showMarkers(points: List<Point>)

        fun makeCompleteActions()

    }

    interface Presenter {

        fun attachView(view: View)

        fun detachView()

        fun onActivityCreate(savedInstanceState: Bundle?)

        fun onMapReady()

        fun getPoints(isActivityRecreated: Boolean, arePointsDownloaded: Boolean)

    }

}
