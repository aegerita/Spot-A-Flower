package com.example.spot_a_flower

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.middle_man.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var currentPhotoPath: String
    private val permissionRequest = 0
    private val requestImageCapture = 1
    private val requestGalleryPhoto = 2
    private var storagePermitted = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)
        setContentView(R.layout.activity_main)

        // declare the button and toolbar for future use
        val cameraButton = findViewById<Button>(R.id.cameraButton)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // add toolbar
        setSupportActionBar(toolbar)

        // add navigation to toolbar
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph, drawer_layout)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        // set navigation drawer
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.account -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.history -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, FlowerSearch::class.java)
                    intent.putExtra("Parent", "history")
                    startActivity(intent)
                    true
                }
                R.id.saved -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, FlowerSearch::class.java)
                    intent.putExtra("Parent", "saved")
                    startActivity(intent)
                    true
                }
                R.id.setting -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.help -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    false
                }
            }
        }

        // ask for permissions if haven't got one
        if (ContextCompat.checkSelfPermission(
                this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
                ).toString()
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
                ), permissionRequest
            )
        }

        // take photo when button is clicked
        cameraButton.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        Toast.makeText(this, "Can't Save Image", Toast.LENGTH_SHORT).show()
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.spot_a_flower.fileProvider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, requestImageCapture)
                    }
                }
            }
        }
    }

    // if the permission is not granted
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            permissionRequest -> {
                // camera
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    cameraButton.setOnClickListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Pls Allow Camera Access",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                // storage
                if (grantResults[1] == PackageManager.PERMISSION_DENIED ||
                    grantResults[2] == PackageManager.PERMISSION_DENIED
                ) {
                    storagePermitted = false
                }
            }
        }
        return
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // do nothing if activity cancelled
        if (resultCode == Activity.RESULT_CANCELED)
            return

        // save the photo after it's taken, pass it to the neural network
        if (requestCode == requestImageCapture) {
            // get full size image
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath, options)

            // save image to Pictures
            if (storagePermitted) {
                MediaStore.Images.Media.insertImage(
                    contentResolver, imageBitmap,
                    "Flower_" + SimpleDateFormat("yyyyMMdd_HHMMSS", Locale.CANADA).format(Date()),
                    "from Spot-A-Flower"
                )
            } else {
                // if permission not granted
                Toast.makeText(
                    this,
                    "Allow Storage Access to Save Your Image",
                    Toast.LENGTH_SHORT
                ).show()
            }
            //logo.setImageBitmap(imageBitmap)
            searchFlower()

            // get photo from gallery and pass it to neural network
        } else if (requestCode == requestGalleryPhoto) {
            if (data != null) {
                val selectedImage: Uri? = data.data
                val imageBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                //logo.setImageURI(selectedImage)
                searchFlower()
            } else {
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // call neural network to determine result
    private fun searchFlower() {
        if (Math.random() < 0.5) {
            searchSuccess()
        } else
            searchFailed()
    }

    // go to failed page
    private fun searchFailed() {
        val intent = Intent(this, SearchFailed::class.java)
        startActivity(intent)
    }

    // go to success page
    private fun searchSuccess() {
        val intent = Intent(this, FlowerSearch::class.java)
        intent.putExtra("Parent", "search")
        startActivity(intent)
    }

    // add menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        return true
    }

    // when menu items are clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.gallery -> {
                if (storagePermitted) {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, requestGalleryPhoto)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Pls Allow Access to Photo Storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHMMSS", Locale.CANADA).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

}
