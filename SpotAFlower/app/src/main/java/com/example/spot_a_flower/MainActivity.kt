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
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.middle_man.*

class MainActivity : AppCompatActivity() {

    private val cameraRequest = 1
    private val requestImageCapture = 1

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

        // ask for permission if haven't got one
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), cameraRequest
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
            cameraRequest -> {
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
        if (requestCode == requestImageCapture && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            val imageBitmap = extras!!["data"] as Bitmap?
            searchFlower(imageBitmap)
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

    // add menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        menu.findItem(R.id.saved).isVisible = false
        return true
    }

    // when menu items are clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            // TODO
            R.id.saved -> {
                true
            }
            R.id.gallery -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
