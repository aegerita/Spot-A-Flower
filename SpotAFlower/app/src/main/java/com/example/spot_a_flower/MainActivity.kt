package com.example.spot_a_flower

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.middle_man.*

class MainActivity : AppCompatActivity() {

    private val permissionRequest = 1
    private val requestImageCapture = 2
    private val requestGalleryPhoto = 3
    private var galleryPermitted = true

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

        // ask for camera permission if haven't got one
        if (ContextCompat.checkSelfPermission(
                this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), permissionRequest
            )
        }

        // take photo when button is clicked
        cameraButton.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent, requestImageCapture)
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
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    cameraButton.setOnClickListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Pls Allow Camera Access",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    galleryPermitted = false
                    invalidateOptionsMenu()
                }
                if (grantResults[2] == PackageManager.PERMISSION_DENIED) {
                }
            }
        }
        return
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED)
            return

        // save the photo after it's taken, pass it to the neural network
        if (requestCode == requestImageCapture) {
            val imageBitmap = data?.extras!!["data"] as Bitmap?

            //if (ContextCompat.checkSelfPermission(this,
            //      Manifest.permission.WRITE_EXTERNAL_STORAGE)
            //!= PackageManager.PERMISSION_GRANTED) {
            MediaStore.Images.Media.insertImage(
                contentResolver, imageBitmap,
                "Flower", "from Spot-A-Flower"
            )
            //} else {
            Toast.makeText(
                this,
                "Allow Written Access to Storage to Save Your Image!",
                Toast.LENGTH_SHORT
            ).show()
            //}

            searchFlower(imageBitmap)

            // get photo from gallery and pass it to neural network
        } else if (requestCode == requestGalleryPhoto) {
            if (data != null) {
                val selectedImage: Uri? = data.data
                val imageBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                //logo.setImageURI(selectedImage)
                searchFlower(imageBitmap)
            } else {
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // call neural network to determine result
    private fun searchFlower(imageBitmap: Bitmap?) {
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
        val intent = Intent(this, SearchSuccess::class.java)
        intent.putExtra("flower1", "red flower")
        intent.putExtra("flower2", "yellow flower")
        intent.putExtra("flower3", "blue flower")
        startActivity(intent)
    }

    // when menu items are clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.gallery -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, requestGalleryPhoto)
                true
            }
            R.id.falseGallery -> {
                Toast.makeText(
                    this@MainActivity,
                    "Pls Allow Access to Photo Storage",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.options, menu)
        menu.findItem(R.id.gallery).isVisible = galleryPermitted
        menu.findItem(R.id.falseGallery).isVisible = !galleryPermitted
        menu.findItem(R.id.saved).isVisible = false
        return true
    }

}
