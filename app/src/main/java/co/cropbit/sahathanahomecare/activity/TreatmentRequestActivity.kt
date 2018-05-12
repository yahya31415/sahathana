package co.cropbit.sahathanahomecare.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import co.cropbit.sahathanahomecare.R
import co.cropbit.sahathanahomecare.model.Hospital

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth

import java.util.ArrayList
import java.util.Date

import co.cropbit.sahathanahomecare.model.Request
import co.cropbit.sahathanahomecare.model.User
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_treatment_request.*
import kotlinx.android.synthetic.main.treatment_type_list_item.view.*

class TreatmentRequestActivity : AppCompatActivity(), OnMapReadyCallback {

    var mLocationPermissionGranted: Boolean = false
    var mLastKnownLocation: Location? = null
    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    var mGoogleApiClient: GoogleApiClient? = null
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val adapter: RequestTypeAdapter = RequestTypeAdapter()
    val treatmentTypes
            get() = resources.getStringArray(R.array.treatment_type_array)
    val treatmentInfos
            get() = resources.getStringArray(R.array.treatment_type_info_array)
    var selectedType = "UNSET"
    var lastRadio: RadioButton? = null

    var nearbyHospitals: ArrayList<Hospital>? = null
    val nearbyHospitalNames = ArrayList<String>()
    var hospitalAdapter: ArrayAdapter<String>? = null
    var defaultHospital: Hospital? = null

    var map: GoogleMap? = null
    var mapFragment: SupportMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treatment_request)

        mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment?.getMapAsync(this)

        if(mAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        } else {
            User.fromId(mAuth.currentUser!!.uid) { user ->
                if(user.defaultHospital != null) {
                    defaultHospital = user.defaultHospital!!
                }
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        treatment_types.isFocusable = false
        treatment_types.adapter = adapter
        treatment_types.layoutManager = LinearLayoutManager(this)

        hospitalAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nearbyHospitalNames)
        hospitalAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        Hospital.nearbyHospitals { nh ->
            nearbyHospitals = nh
            nh.forEach({ hospital: Hospital ->
                nearbyHospitalNames.add(hospital.displayName)
                hospitalAdapter?.notifyDataSetChanged()
            })
            loadMarkers()
        }

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this) { }
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(bundle: Bundle?) {
                        getLocation()
                    }

                    override fun onConnectionSuspended(i: Int) {

                    }
                })
                .addApi(LocationServices.API)
                .build()
        mGoogleApiClient!!.connect()
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }

        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest()) { location ->
                mLastKnownLocation = location
            }

            centerCamera()
        }
    }

    private fun createLocationRequest(): LocationRequest {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return mLocationRequest
    }

    override fun onMapReady(p0: GoogleMap?) {
        this.map = p0
        if (nearbyHospitals != null && nearbyHospitals!!.size > 0) {
            loadMarkers()
            centerCamera()
        }
    }

    fun loadMarkers() {
        nearbyHospitals?.forEach { hospital: Hospital ->
            this.map?.addMarker(MarkerOptions().position(LatLng(hospital.location.lat, hospital.location.lng)).title(hospital.displayName))
        }
    }

    fun centerCamera() {
        if (mLastKnownLocation != null) {
            try {
                this.map?.isMyLocationEnabled = true
            } catch (exception: SecurityException) {
                Log.v("Sahathana Exception", exception.localizedMessage)
            }
            this.map?.moveCamera(
                    CameraUpdateFactory
                            .newLatLngZoom(LatLng(mLastKnownLocation?.latitude!!, mLastKnownLocation?.longitude!!), (13).toFloat())
            )
        }
    }

    inner class RequestTypeAdapter : RecyclerView.Adapter<RequestTypeAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var treatmentType: TextView
            var treatmentInfo: TextView
            var selected: RadioButton

            init {

                treatmentType = itemView.findViewById<View>(R.id.treatment_type) as TextView
                treatmentInfo = itemView.findViewById<View>(R.id.treatment_info) as TextView
                selected = itemView.findViewById<View>(R.id.selected) as RadioButton
                selected.isClickable = false
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val thisItemsView = LayoutInflater.from(this@TreatmentRequestActivity).inflate(R.layout.treatment_type_list_item,
                    parent, false)
            thisItemsView.setOnClickListener { view ->
                val type = treatmentTypes[treatment_types.indexOfChild(view)]

                lastRadio?.isChecked = false
                lastRadio = thisItemsView.selected
                lastRadio?.isChecked = true
                selectedType = type
            }
            return ViewHolder(thisItemsView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // Set item views based on your views and data model
            val textView = holder.treatmentType
            textView.text = treatmentTypes[position]
            val textView1 = holder.treatmentInfo
            textView1.text = treatmentInfos[position]
        }

        override fun getItemCount(): Int {
            return treatmentTypes.size
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.treatment_request_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.profile -> {
//                val intent = Intent(this, ProfileActivity::class.java)
//                startActivity(intent)
//            }
//        }
//        return true
//    }

    fun fab(view: View) {
        var dialogView = LayoutInflater.from(this).inflate(R.layout.treatment_confirm_dialog, null)
        dialogView.findViewById<Spinner>(R.id.alert_spinner).adapter = hospitalAdapter
        if (defaultHospital != null) dialogView.findViewById<Spinner>(R.id.alert_spinner).setSelection(nearbyHospitalNames.indexOf(defaultHospital!!.displayName))
        dialogView.findViewById<TextView>(R.id.alert_title).text = selectedType
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
                .setPositiveButton(getString(R.string.treatment_request_confirm_action)) { dialogInterface, i ->
                    if (mLastKnownLocation != null) {
                        val location = co.cropbit.sahathanahomecare.model.Location(mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude)
                        val request = Request("", mAuth.currentUser!!.uid, location, Date(), 0, selectedType, nearbyHospitals!!.get(nearbyHospitalNames.indexOf(dialogView.findViewById<Spinner>(R.id.alert_spinner).selectedItem.toString())))
                        request.push {
                            val intent = Intent(this, SentActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
        val dialog = builder.create()
        dialog.show()
    }
}
