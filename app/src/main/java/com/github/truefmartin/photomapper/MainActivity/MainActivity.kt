package com.github.truefmartin.photomapper.MainActivity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.truefmartin.MainActivity.TaskListViewModel
import com.github.truefmartin.MainActivity.TaskListViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.github.truefmartin.photomapper.NewEditTaskActivity.EXTRA_ID
import com.github.truefmartin.photomapper.NewEditTaskActivity.NewTaskActivity
import com.github.truefmartin.photomapper.R
import com.github.truefmartin.photomapper.PhotoMapper
import com.github.truefmartin.photomapper.MyReceiver
import com.github.truefmartin.photomapper.NewEditTaskActivity.RecurringState
import com.github.truefmartin.photomapper.NotificationHandler
import com.github.truefmartin.photomapper.PhotoViewerActivity.PhotoViewerActivity
import com.github.truefmartin.photomapper.util.LocationUtilCallback
import com.github.truefmartin.photomapper.util.createLocationCallback
import com.github.truefmartin.photomapper.util.createLocationRequest
import com.github.truefmartin.photomapper.util.getLastLocation
import com.github.truefmartin.photomapper.util.replaceFragmentInActivity
import com.github.truefmartin.photomapper.util.stopLocationUpdates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var mapsFragment: OpenStreetMapFragment
    private var numMarkers:Int = 0

    //Boolean to keep track of whether permissions have been granted
    private var locationPermissionEnabled: Boolean = false

    //Boolean to keep track of whether activity is currently requesting location Updates
    private var locationRequestsEnabled: Boolean = false

    //Member object for the FusedLocationProvider
    private lateinit var locationProviderClient: FusedLocationProviderClient

    //Member object for the last known location
    private lateinit var mCurrentLocation: Location

    //Member object to hold onto locationCallback object
    //Needed to remove requests for location updates
    private lateinit var mLocationCallback: LocationCallback

    val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_CANCELED){
            Log.d("MainActivity","Take Picture Activity Cancelled")
        }else{
            Log.d("MainActivity", "Picture Taken")
            val takeShowPictureActivityIntent: Intent = Intent(this,PhotoViewerActivity::class.java)
            takeShowPictureActivityIntent.putExtra("GEOPHOTO_LOC",
                result.data?.getStringExtra("GEOPHOTO_LOC"))
            takeShowPictureActivityIntent.putExtra("GEOPHOTO_ID",10)
            startActivity(takeShowPictureActivityIntent)
        }
    }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            //If successful, startLocationRequests
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                locationPermissionEnabled = true
                startLocationRequests()
            }
            //If successful at coarse detail, we still want those
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                locationPermissionEnabled = true
                startLocationRequests()
            }

            else -> {
                //Otherwise, send toast saying location is not enabled
                locationPermissionEnabled = false
                Toast.makeText(this, "Location Not Enabled", Toast.LENGTH_LONG)
            }
        }
    }

    //This is our viewModel instance for the MainActivity class
    private val taskListViewModel: TaskListViewModel by viewModels {
        TaskListViewModelFactory((application as PhotoMapper).repository)
    }
    //This is our ActivityResultContracts value that defines
    //the behavior of our application when the activity has finished.
    private val startNewTaskActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode== Activity.RESULT_OK){
            //Note that all we are doing is logging that we completed
            //This means that the other activity is handling updates to the data
            Log.d("MainActivity","Completed")
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //Get preferences for tile cache
        Configuration.getInstance().load(this, getSharedPreferences(
            "${packageName}_preferences", Context.MODE_PRIVATE))
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        //Check for location permissions
        checkForLocationPermission()

        //Attempt to get the last known location
        //Will either require permission check or will return last known location
        //through the locationUtilCallback
        //getLastLocation(this, locationProviderClient, locationUtilCallback)

        //Get access to mapsFragment object
        mapsFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                as OpenStreetMapFragment? ?:OpenStreetMapFragment.newInstance().also{
            replaceFragmentInActivity(it,R.id.fragment_container_view)
        }
        mapsFragment.changeCenterLocation(GeoPoint(mCurrentLocation.latitude, mCurrentLocation.longitude))
        mapsFragment.setMarkerClickListener(::markerClickCallBack)
        val fab = findViewById<FloatingActionButton>(R.id.fab_take_picture)
        fab.setOnClickListener {
            takeNewPhoto()
        }
    }

    override fun onStart() {
        super.onStart()
        //Start location updates
        startLocationRequests()
    }

    override fun onStop() {
        super.onStop()
        //if we are currently getting updates
        if (locationRequestsEnabled) {
            //stop getting updates
            locationRequestsEnabled = false
            stopLocationUpdates(locationProviderClient, mLocationCallback)
        }
    }
    private fun takeNewPhoto(){
        val takeShowPictureActivityIntent: Intent = Intent(this,PhotoViewerActivity::class.java)

        takeShowPictureActivityIntent.putExtra("GEOPOINT-LAT", mCurrentLocation.latitude)
        takeShowPictureActivityIntent.putExtra("GEOPOINT-LONG", mCurrentLocation.longitude)

        takePictureResultLauncher.launch(takeShowPictureActivityIntent)
    }

    private fun markerClickCallBack(id: Int) {
        val takeShowPictureActivityIntent: Intent = Intent(this,PhotoViewerActivity::class.java)

        takeShowPictureActivityIntent.putExtra("GEOPHOTO_ID", id)
        startActivity(takeShowPictureActivityIntent)
    }
    private fun checkForLocationPermission(){
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationRequests()
                //getLastKnownLocation()
                //registerLocationUpdateCallbacks()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    //LocationUtilCallback object
    //Dynamically defining two results from locationUtils
    //Namely requestPermissions and locationUpdated
    private val locationUtilCallback = object : LocationUtilCallback {
        //If locationUtil request fails because of permission issues
        //Ask for permissions
        override fun requestPermissionCallback() {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        //If locationUtil returns a Location object
        //Populate the current location and log
        override fun locationUpdatedCallback(location: Location) {
            mCurrentLocation = location
            mapsFragment.changeCenterLocation(GeoPoint(location.latitude,location.longitude))
            Log.d(
                "MainActivity",
                "Location is [Lat: ${location.latitude}, Long: ${location.longitude}]"
            )
        }
    }

    private fun startLocationRequests() {
        //If we aren't currently getting location updates
        if (!locationRequestsEnabled) {
            //create a location callback
            mLocationCallback = createLocationCallback(locationUtilCallback)
            //and request location updates, setting the boolean equal to whether this was successful
            locationRequestsEnabled =
                createLocationRequest(this, locationProviderClient, mLocationCallback)
        }
    }
    private fun recyclerListener(id: Int) {
        //This is the callback function to be executed
        //when a view in the TaskListAdapter is clicked

        //First we log the task
        Log.d("MainActivity", "Task being opened ID: $id")
        //Then create a new intent with the ID of the word
        val intent = Intent(this@MainActivity, NewTaskActivity::class.java)
        intent.putExtra(EXTRA_ID, id)
        //And start the activity through the results contract
        startNewTaskActivity.launch(intent)

        val receiverIntent = Intent(this@MainActivity, MyReceiver::class.java)
        receiverIntent.putExtra(EXTRA_ID, id)
        sendBroadcast(receiverIntent)
    }

    private fun recyclerButtonListener(id: Int, isComplete: Boolean) {
        //This is the callback function to be executed
        //when a radio button in the TaskListAdapter is clicked

        //First we log the task
        Log.d("MainActivity", "Task ID: $id Radio button clicked")

        // Update completed status of task in DB
        CoroutineScope(SupervisorJob()).launch {
            // This is a blocking call, don't move on until we get the task
            val task = taskListViewModel.getTaskByID(id)
            if (isComplete) {
                // If the task isn't recurring, mark it as complete in DB and remove notification
                if (task.repeated == RecurringState.NONE) {
                    task.completed = true
                    notificationHandler.removeNotification(task.noteID)
                // Since it is recurring, don't change 'completion' just change date
                } else {
                    // Change date of task to it's next repeated time
                    task.date = task.repeated.modifyDate(task.date)
                    // Update the notification to the next recurrence date
                    notificationHandler.scheduleNotification(task)
                    // Don't change the 'completed' status since it's recurring. Inform user why
                    runOnUiThread( kotlinx.coroutines.Runnable {
                        Toast.makeText(applicationContext,"New recurring task set to future date as 'ToDo'", Toast.LENGTH_SHORT).show()
                    }

                    )
                }
            } else {
                notificationHandler.scheduleNotification(task)
                task.completed = false
            }
            // Update task in DB (either completion status or the date if recurring)
            taskListViewModel.update(task)

        }
    }

    private fun instructionPopup(){
        val builder = AlertDialog.Builder(this)
        // Set contents of dialog
        builder.setTitle("A note about repeating tasks:")
        val body = "Recurring tasks can never be 'completed'. When they are marked as complete, " +
                "a new task will be scheduled on the next recurrence date.\n" +
                "Change the recurrence to 'None' if you no longer need the task to repeat.\n" +
                "We hope you enjoy our app!"
        builder.setMessage(body)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Okay"){_, _->}
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

}

