package co.cropbit.sahathanahomecare

import android.app.Activity
import android.content.Context
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
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

import java.util.ArrayList
import java.util.Date

import co.cropbit.sahathanahomecare.activity.LoginActivity
import co.cropbit.sahathanahomecare.activity.SentActivity
import co.cropbit.sahathanahomecare.model.Request
import kotlinx.android.synthetic.main.treatment_type_list_item.view.*

class TreatmentRequestActivity : AppCompatActivity() {

    private var mLocationPermissionGranted: Boolean = false
    private var mLastKnownLocation: Location? = null
    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mGoogleApiClient: GoogleApiClient? = null

    internal val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    internal val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    internal var waiting_list: MutableList<Request> = ArrayList()

    internal var adapter: RequestTypeAdapter = RequestTypeAdapter()

    val treatmentTypes: Array<String>
        get() = resources.getStringArray(R.array.treatment_type_array)

    val treatmentInfos: Array<String>
        get() = resources.getStringArray(R.array.treatment_type_info_array)

    private val treatmentTypeListView: RecyclerView
        get() = findViewById<View>(R.id.treatment_types) as RecyclerView

    val treatmentRequestActivityContext: Context
        get() = this

    private val layoutManager: RecyclerView.LayoutManager
        get() = LinearLayoutManager(treatmentRequestActivityContext)

    var selectedType = "UNSET"
    var lastRadio: RadioButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treatment_request)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        treatmentTypeListView.isFocusable = false
        treatmentTypeListView.adapter = getAdapter()
        treatmentTypeListView.layoutManager = layoutManager

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

        mAuth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                val intent = Intent(treatmentRequestActivityContext, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                treatmentRequestActivityContext.startActivity(intent)
            }
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(treatmentRequestActivityContext.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(treatmentRequestActivityContext as Activity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }

        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest()) { location ->
                mLastKnownLocation = location
                if (waiting_list.size > 0) {
                    for (i in waiting_list.indices) {
                        val request = waiting_list[i]
                        val loc = co.cropbit.sahathanahomecare.model.Location(mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude)
                        request.location = loc
                        database.getReference("requests").child(mAuth.currentUser!!.uid).push().setValue(request).addOnSuccessListener {
                            val intent = Intent(treatmentRequestActivityContext, SentActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                    waiting_list.clear()
                }
            }
        }
    }

    private fun createLocationRequest(): LocationRequest {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return mLocationRequest
    }

    private fun getAdapter(): RequestTypeAdapter {
        return adapter
    }

    internal inner class RequestTypeAdapter : RecyclerView.Adapter<RequestTypeAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var treatmentType: TextView
            var treatmentInfo: TextView
            var selected: RadioButton

            init {

                treatmentType = itemView.findViewById<View>(R.id.treatment_type) as TextView
                treatmentInfo = itemView.findViewById<View>(R.id.treatment_info) as TextView
                selected = itemView.findViewById<View>(R.id.selected) as RadioButton
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestTypeAdapter.ViewHolder {
            val thisItemsView = LayoutInflater.from(treatmentRequestActivityContext).inflate(R.layout.treatment_type_list_item,
                    parent, false)
            thisItemsView.setOnClickListener { view ->
                val type = treatmentTypes[treatmentTypeListView.indexOfChild(view)]

                lastRadio?.isChecked = false
                lastRadio = thisItemsView.selected
                lastRadio?.isChecked = true
                selectedType = type
            }
            return ViewHolder(thisItemsView)
        }

        override fun onBindViewHolder(holder: RequestTypeAdapter.ViewHolder, position: Int) {
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
                    getLocation()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.treatment_request_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    fun fab(view: View) {
        val builder = AlertDialog.Builder(treatmentRequestActivityContext)
        builder.setTitle(selectedType)
                .setMessage(getString(R.string.treatment_request_confirm_message))
                .setPositiveButton(getString(R.string.treatment_request_confirm_action)) { dialogInterface, i ->
                    if (mLastKnownLocation == null) {
                        val request = Request()
                        request.uid = mAuth.currentUser!!.uid
                        request.datetime = Date().time
                        request.status = Request.SENT
                        request.type = selectedType
                        request.isEmergency = false
                        waiting_list.add(request)
                    } else {
                        val location = co.cropbit.sahathanahomecare.model.Location(mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude)
                        val request = Request()
                        request.uid = mAuth.currentUser!!.uid
                        request.location = location
                        request.datetime = Date().time
                        request.type = selectedType
                        request.isEmergency = false
                        database.getReference("requests").child(mAuth.currentUser!!.uid).push().setValue(request).addOnSuccessListener {
                            val intent = Intent(treatmentRequestActivityContext, SentActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
        val dialog = builder.create()
        dialog.show()
    }
}
