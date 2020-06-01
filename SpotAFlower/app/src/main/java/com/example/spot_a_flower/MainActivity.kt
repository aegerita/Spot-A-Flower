package com.example.spot_a_flower

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ml.custom.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.slide_header.view.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var currentPhotoPath: String
    private val permissionRequest = 0
    private val requestImageCapture = 1
    private val requestGalleryPhoto = 2
    private val signInRequest = 3
    private var storagePermitted = true

    // Firebase instance variables
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mFirebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // do this to get internet connection
        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, null /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance()
        updateUI()

        // add toolbar and add navigation toggle
        setSupportActionBar(toolbar)
        val actionBarDrawerToggle =
            ActionBarDrawerToggle(
                this,
                drawer_layout,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
            )
        drawer_layout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // set navigation drawer
        nav_view.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.account -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    if (mFirebaseAuth.currentUser == null) {
                        // log in account
                        progressBar.isVisible = true
                        val signInIntent =
                            Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
                        startActivityForResult(signInIntent, signInRequest)
                    } else {
                        // sign out account
                        mFirebaseAuth.signOut()
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                        Toast.makeText(this, "Your account is signed out", Toast.LENGTH_SHORT)
                            .show()
                        updateUI()
                    }
                    true
                }
                R.id.history -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, FlowerSearch::class.java)
                    intent.putExtra("Parent", getString(R.string.history))
                    startActivity(intent)
                    true
                }
                R.id.saved -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, FlowerSearch::class.java)
                    intent.putExtra("Parent", getString(R.string.saved))
                    startActivity(intent)
                    true
                }
                R.id.setting -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.putExtra("Parent", getString(R.string.setting))
                    startActivity(intent)
                    true
                }
                R.id.help -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.putExtra("Parent", getString(R.string.helpsAbout))
                    startActivity(intent)
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
        progressBar.isVisible = false
        // do nothing if activity cancelled
        if (resultCode == Activity.RESULT_CANCELED)
            return

        // save the photo after it's taken, pass it to the neural network
        else if (requestCode == requestImageCapture) {
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

            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        } else if (requestCode == signInRequest) {
            progressBar.isVisible = false
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result!!.isSuccess) {
                // Google Sign-In was successful, authenticate with Firebase
                val account: GoogleSignInAccount = result.signInAccount!!
                firebaseAuthWithGoogle(account.idToken!!)
            } else {
                Toast.makeText(this, "Google Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // connect to neural network
    private fun searchFlower(imageBitmap: Bitmap) {
        val intent = Intent(this, FlowerSearch::class.java)
        intent.putExtra("Parent", getString(R.string.search))

        // get image from the main activity
        val imageSize = 44
        val bitmap = Bitmap.createScaledBitmap(imageBitmap, imageSize, imageSize, true)

        // load model
        val localModel = FirebaseCustomLocalModel.Builder()
            .setAssetFilePath("flower_model.tflite").build()
        val options = FirebaseModelInterpreterOptions.Builder(localModel).build()
        val interpreter = FirebaseModelInterpreter.getInstance(options)

        // set input output format
        val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, imageSize, imageSize, 3))
            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 102)).build()

        // set input to proper format
        val batchNum = 0
        val input = Array(1) {Array(imageSize){Array(imageSize){FloatArray(3)}}}
        for (x in 0 until imageSize-1) {
            for (y in 0 until imageSize-1) {
                val pixel = bitmap.getPixel(x, y)
                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
                input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 255.0f
                input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 255.0f
                input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 255.0f
            }
        }

        // pass in the input
        val inputs = FirebaseModelInputs.Builder().add(input).build()
        interpreter!!.run(inputs, inputOutputOptions)
            .addOnSuccessListener { result ->
                val output = result.getOutput<Array<FloatArray>>(0)
                val probabilities = output[0]
                val reader = BufferedReader(
                    InputStreamReader(assets.open("flower_labels.txt"))
                )
                val myDataset: MutableList<Flower> = ArrayList()
                for (i in probabilities.indices) {
                    val label = reader.readLine()
                    reader.readLine()
                    myDataset.add(Flower(label, (probabilities[i]*100).toInt()))
                    Log.i("MLKit", label+": "+(probabilities[i]*100).toInt())
                }
                myDataset.sortDescending()
                intent.putExtra("flower1_name", myDataset[0].name)
                intent.putExtra("flower2_name", myDataset[1].name)
                intent.putExtra("flower3_name", myDataset[2].name)
                intent.putExtra("flower1_detail", myDataset[0].detail)
                intent.putExtra("flower2_detail", myDataset[1].detail)
                intent.putExtra("flower3_detail", myDataset[2].detail)

                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "There is something wrong with the AI, mind trying again?",
                    Toast.LENGTH_SHORT
                ).show()
                Log.w("TAG", "loadData:onCancelled", e)
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

    // sign in from firebase
    private fun firebaseAuthWithGoogle(acct: String) {
        val credential = GoogleAuthProvider.getCredential(acct, null)
        mFirebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Authentication Successful!", Toast.LENGTH_SHORT).show()
                    updateUI()
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // change UI depending on the user login or not
    private fun updateUI() {
        if (mFirebaseAuth.currentUser != null) {
            // sign in
            mFirebaseUser = mFirebaseAuth.currentUser!!
            nav_view.getHeaderView(0).username.text = mFirebaseUser.displayName
            if (mFirebaseUser.photoUrl != null) {
                val profilePicture =
                    BitmapFactory.decodeStream(
                        URL(mFirebaseUser.photoUrl.toString()).content as InputStream?
                    )
                nav_view.getHeaderView(0).user_profile.setImageBitmap(profilePicture)
            }
            nav_view.menu.findItem(R.id.account).title = "Sign out"
        } else {
            // sign out
            nav_view.getHeaderView(0).username.text = getString(R.string.app_name)
            nav_view.getHeaderView(0).user_profile.setImageResource(R.mipmap.ic_launcher)
            nav_view.menu.findItem(R.id.account).title = getString(R.string.sign_in)
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
