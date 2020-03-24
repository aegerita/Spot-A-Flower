package com.example.spot_a_flower

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.middle_man.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val CAMERA_REQUEST = 1
    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)
        setContentView(R.layout.activity_main)

        // declare the button to take photo
        val cameraButton = findViewById<Button>(R.id.cameraButton)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // add toolbar
        setSupportActionBar(toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        // ask for permission if haven't got one
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST
            )
        }

        // take photo when button is clicked
        cameraButton.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
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
            CAMERA_REQUEST -> {
                if ((grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                    //cameraButton.isEnabled = false
                    cameraButton.setOnClickListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Pls Allow Camera Access",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        return
    }

    // save the photo after it's taken
    // for now just replace the logo, later pass it to the neural network
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            val imageBitmap = extras!!["data"] as Bitmap?
            logo.setImageBitmap(imageBitmap)
        }
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
            R.id.history -> {
                true
            }
            R.id.help -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
