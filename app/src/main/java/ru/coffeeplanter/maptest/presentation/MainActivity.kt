package ru.coffeeplanter.maptest.presentation

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import net.sharewire.googlemapsclustering.Cluster
import net.sharewire.googlemapsclustering.ClusterManager
import net.sharewire.googlemapsclustering.DefaultIconGenerator
import net.sharewire.googlemapsclustering.IconGenerator
import ru.coffeeplanter.maptest.R
import ru.coffeeplanter.maptest.data.model.Point

class MainActivity : AppCompatActivity(), OnMapReadyCallback, MainActivityContract.View {

    private var isActivityRecreated = false

    private var presenter: MainActivityContract.Presenter? = null

    private var map: GoogleMap? = null
    private var clusterManager: ClusterManager<Point>? = null
    private var currentMapCenter = LatLng(55.75399399999374, 37.62209300000001) // Center of Moscow (default location).
    private var currentZoom: Float? = 9f // Default zoom.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isActivityRecreated = savedInstanceState != null
        attachPresenter()
        presenter = MainActivityPresenterImpl(this)
        presenter?.onActivityCreate(savedInstanceState)
    }

    private fun attachPresenter() {
        presenter = lastCustomNonConfigurationInstance as? MainActivityContract.Presenter
        if (presenter == null) {
            presenter = MainActivityPresenterImpl(this)
        }
        presenter?.attachView(this)
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        return presenter
    }

    override fun onDestroy() {
        presenter?.detachView()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val cameraPosition = map?.cameraPosition
        outState?.putFloat("zoom", cameraPosition?.zoom ?: 9f)
        outState?.putDouble("latitude", cameraPosition?.target?.latitude ?: 55.75399399999374)
        outState?.putDouble("longitude", cameraPosition?.target?.longitude ?: 37.62209300000001)
    }

    // ========== OnMapReadyCallback ==========

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        presenter?.onMapReady()
        val arePointsDownloaded = PreferenceManager.getDefaultSharedPreferences(this).contains(PREF_POINTS_DOWNLOAD_TIME)
        presenter?.getPoints(isActivityRecreated, arePointsDownloaded)
    }

    // ========== MainActivityContract.View ==========

    override fun addMapFragment(savedInstanceState: Bundle?) {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        if (isActivityRecreated) {
            val latitude = savedInstanceState?.getDouble("latitude", 55.75399399999374)
            val longitude = savedInstanceState?.getDouble("longitude", 37.62209300000001)
            currentMapCenter = LatLng(latitude ?: 0.0, longitude ?: 0.0)
            currentZoom = savedInstanceState?.getFloat("zoom", 9f)
        }
    }

    override fun setUpMap() {
        clusterManager = ClusterManager(this, map!!)
        map?.setOnCameraIdleListener(clusterManager)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentMapCenter, currentZoom ?: 0f))
    }

    override fun showMarkers(points: List<Point>) {
        if (clusterManager != null) {
            clusterManager?.setClusterIconGenerator(DefaultIconGenerator(this))
            clusterManager?.setIconGenerator(object : IconGenerator<Point> {
                override fun getClusterIcon(cluster: Cluster<Point>): BitmapDescriptor {
                    return BitmapDescriptorFactory.defaultMarker()
                }

                override fun getClusterItemIcon(clusterItem: Point): BitmapDescriptor {
                    return BitmapDescriptorFactory.fromBitmap(clusterItem.marker?.bitmap)
                }
            })
            clusterManager?.setItems(points)
        }
    }

    override fun makeCompleteActions() {
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit()
                .putLong(PREF_POINTS_DOWNLOAD_TIME, System.nanoTime())
                .apply()
    }

    companion object {
        @Suppress("unused")
        private val TAG = MainActivity::class.java.simpleName
        private const val PREF_POINTS_DOWNLOAD_TIME = "point_downloaded_time"
    }

}
