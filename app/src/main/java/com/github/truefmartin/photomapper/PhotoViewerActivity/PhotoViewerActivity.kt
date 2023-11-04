package com.github.truefmartin.photomapper.PhotoViewerActivity

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.github.truefmartin.photomapper.Model.PhotoPath
import com.github.truefmartin.photomapper.PhotoMapper
import com.github.truefmartin.photomapper.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class PhotoViewerActivity : AppCompatActivity() {
    // Data about new/current photo
    var currentPhotoPath: String = ""
    lateinit var imageView: ImageView
    var geoPhotoId:Int = -1
    lateinit var newPhotoGeoPoint: GeoPoint
    // Data from/for text views
    private var originalDescription = ""
    private var hasDescriptionChanged = false
    private lateinit var photosTime: LocalDateTime
    private val timeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm a")
    // Views
    private lateinit var etDescription: EditText
    private lateinit var tvTimeStamp: TextView
    // Registered result launcher for taking a new photo
    private val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        // If user canceled photo taking, return to main activity
        if(result.resultCode == Activity.RESULT_CANCELED){
            Log.d("MainActivity","Take Picture Activity Cancelled")
            setResult(RESULT_CANCELED)
            Toast.makeText(applicationContext,"Map marker creation was canceled", Toast.LENGTH_LONG).show()
            finish()
        }else{
            Log.d("MainActivity", "Picture Taken")
            tvTimeStamp.text = photosTime.format(timeFormatter)
            setPic()
        }
    }

    private val photoViewerViewModel: PhotoViewerViewModel by viewModels {
        PhotoViewerModelFactory((application as PhotoMapper).repository,-1)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)
        imageView = findViewById(R.id.iv_picture_frame)
        etDescription = findViewById(R.id.edit_text_description)
        tvTimeStamp = findViewById(R.id.tv_time_stamp)

        findViewById<FloatingActionButton>(R.id.fab_save).setOnClickListener {
            var retIntent:Intent = Intent()
            if (geoPhotoId == -1) {
                // Add photo to database. This is a blocking call, we wait for the return
                // of the new geoPhotoID
                updateDatabase()
                intent.putExtra("GEOPOINT-LAT", newPhotoGeoPoint.latitude)
                intent.putExtra("GEOPOINT-LONG", newPhotoGeoPoint.longitude)
                // geoPhotoID has been updated, send ID as result
                intent.putExtra("GEOPHOTO_ID", geoPhotoId)
            // If its a previous photo, but user updated the description, update the DB
            } else if (!originalDescription.equals(etDescription.text)) {
                 updateDatabase()
            }
            setResult(RESULT_OK,retIntent)
            finish()
        }

        val intent = getIntent()
        geoPhotoId = intent.getIntExtra("GEOPHOTO_ID",-1)
        if (geoPhotoId != -1) {
            photoViewerViewModel.updateId(geoPhotoId)
            newPhotoGeoPoint = GeoPoint(0.0, 0.0)
//            currentPhotoPath = intent.getStringExtra("GEOPHOTO_LOC").toString()
        } else {
            val latitude = intent.getDoubleExtra("GEOPOINT-LAT", 0.0)
            val longitude = intent.getDoubleExtra("GEOPOINT-LONG", 0.0)
            newPhotoGeoPoint = GeoPoint(latitude, longitude)
        }
        photoViewerViewModel.curPhotoPath.observe(this){
            photo->photo?.let {

                photosTime = photo.date
                tvTimeStamp.text = photosTime.format(timeFormatter)
                etDescription.setText(photo.description)
                originalDescription = photo.description
                currentPhotoPath = photo.fileName
            }
        }
        if (geoPhotoId == -1) {
            takeAPicture()
        }
    }

    private fun updateDatabase() {
            if(geoPhotoId==-1) {

                    val tempPhotoPath = PhotoPath(
                        null,
                        currentPhotoPath,
                        etDescription.text.toString(),
                        photosTime,
                        newPhotoGeoPoint
                        )

                    geoPhotoId = photoViewerViewModel.insertGetID(tempPhotoPath).toInt()
                    // Updating a task instead of creating a new one

            } else {

                CoroutineScope(SupervisorJob()).launch {

                    val tempPhotoPath = PhotoPath(
                        geoPhotoId,
                        currentPhotoPath,
                        etDescription.text.toString(),
                        photosTime,
                        newPhotoGeoPoint
                    )

                    // Updating a photo instead of creating a new one
                    photoViewerViewModel.insertGetID(tempPhotoPath).toInt()
                }
            }
    }
    override fun onResume() {
        super.onResume()
        if(geoPhotoId != -1){
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    Thread.sleep(200)
                    withContext(Dispatchers.Main){
                        setPic()
                    }
                }
            }
        }
    }



    private fun setPic() {
        val targetW: Int = imageView.getWidth()

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        val photoRatio:Double = (photoH.toDouble())/(photoW.toDouble())
        val targetH: Int = (targetW * photoRatio).roundToInt()
        // Determine how much to scale down the image
        val scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH))


        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        val bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        imageView.setImageBitmap(bitmap)
    }

    private fun takeAPicture() {
        val picIntent: Intent =  Intent().setAction(MediaStore.ACTION_IMAGE_CAPTURE)
        if(picIntent.resolveActivity(packageManager) != null){
            val filepath: String = createFilePath()
            val myFile = File(filepath)
            currentPhotoPath = filepath
            val photoUri = FileProvider.getUriForFile(this,"com.github.truefmartin.photomapper.fileprovider",myFile)
            picIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
            takePictureResultLauncher.launch(picIntent)
        }
    }

    private fun createFilePath(): String {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        photosTime = LocalDateTime.now()
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intent
        return image.absolutePath
    }

}