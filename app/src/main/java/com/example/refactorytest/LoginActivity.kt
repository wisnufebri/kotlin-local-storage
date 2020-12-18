package com.example.refactorytest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.refactorytest.helper.DatabaseHelper
import com.example.refactorytest.helper.InputValidation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val activity = this@LoginActivity

    private lateinit var scrollview: ScrollView

    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputLayoutPassword: TextInputLayout

    private lateinit var textInputEditTextEmail: TextInputEditText
    private lateinit var textInputEditTextPassword: TextInputEditText

    private lateinit var buttonLogin: Button

    private lateinit var buttonRegister: Button

    private lateinit var inputValidation: InputValidation
    private lateinit var databaseHelper: DatabaseHelper

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private var latitudeLabel: String? = null
    private var longitudeLabel: String? = null
    private var latitudeText: TextView? = null
    private var longitudeText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar!!.hide()

        latitudeLabel = resources.getString(R.string.latitude)
        longitudeLabel = resources.getString(R.string.longitude)
        latitudeText = findViewById<View>(R.id.latitude) as TextView
        longitudeText = findViewById<View>(R.id.longitude) as TextView
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        initListeners()
        initObjects()
    }

    public override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions()
            }
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient?.lastLocation!!.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                latitudeText!!.text = latitudeLabel + ": " + (lastLocation)!!.latitude
                longitudeText!!.text = longitudeLabel + ": " + (lastLocation)!!.longitude
            } else {
                Log.w(TAG, "getLastLocation:exception", task.exception)
                showMessage("No location detected. Make sure location is enabled on the device.")
            }
        }
    }

    private fun showMessage(string: String) {
        val container = findViewById<View>(R.id.loginlayout)
        if (container != null) {
            Toast.makeText(this@LoginActivity, string, Toast.LENGTH_LONG).show()
        }
    }

    private fun showSnackbar(
        mainTextStringId: String, actionStringId: String,
        listener: View.OnClickListener
    ) {
        Toast.makeText(this@LoginActivity, mainTextStringId, Toast.LENGTH_LONG).show()
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this@LoginActivity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar("Location permission is needed for core functionality", "Okay",
                View.OnClickListener {
                    startLocationPermissionRequest()
                })
        } else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted.
                    getLastLocation()
                }
                else -> {
                    showSnackbar("Permission was denied", "Settings",
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                Build.DISPLAY, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    companion object {
        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

    private fun initViews() {

        scrollview = findViewById<View>(R.id.scroll) as ScrollView

        textInputLayoutEmail = findViewById<View>(R.id.inputLayoutEmail) as TextInputLayout
        textInputLayoutPassword = findViewById<View>(R.id.inputLayoutPassword) as TextInputLayout

        textInputEditTextEmail = findViewById<View>(R.id.inputEmail) as TextInputEditText
        textInputEditTextPassword = findViewById<View>(R.id.inputPassword) as TextInputEditText

        buttonLogin = findViewById<View>(R.id.btnLogin) as Button

        buttonRegister = findViewById<View>(R.id.btnSignup) as Button

    }

    private fun initListeners() {

        buttonLogin!!.setOnClickListener(this)
        buttonRegister!!.setOnClickListener(this)
    }

    private fun initObjects() {

        databaseHelper = DatabaseHelper(activity)
        inputValidation = InputValidation(activity)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnLogin -> verifyFromSQLite()
            R.id.btnSignup -> {
                val intentRegister = Intent(applicationContext, RegisterActivity::class.java)
                startActivity(intentRegister)
            }
        }
    }

    private fun verifyFromSQLite() {

        if (!inputValidation!!.isInputEditTextFilled(
                textInputEditTextEmail!!,
                textInputLayoutEmail!!,
                getString(R.string.error_message_email)
            )
        ) {
            return
        }
        if (!inputValidation!!.isInputEditTextEmail(
                textInputEditTextEmail!!,
                textInputLayoutEmail!!,
                getString(R.string.error_message_email)
            )
        ) {
            return
        }
        if (!inputValidation!!.isInputEditTextFilled(
                textInputEditTextPassword!!,
                textInputLayoutPassword!!,
                getString(R.string.error_message_email)
            )
        ) {
            return
        }

        if (databaseHelper!!.checkUser(
                textInputEditTextEmail!!.text.toString().trim { it <= ' ' },
                textInputEditTextPassword!!.text.toString().trim { it <= ' ' })
        ) {


            val accountsIntent = Intent(activity, Dashboard::class.java)
            accountsIntent.putExtra(
                "EMAIL",
                textInputEditTextEmail!!.text.toString().trim { it <= ' ' })
            emptyInputEditText()
            startActivity(accountsIntent)


        } else {
            Snackbar.make(
                scrollview!!,
                getString(R.string.error_valid_email_password),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun emptyInputEditText() {
        textInputEditTextEmail!!.text = null
        textInputEditTextPassword!!.text = null
    }
}

