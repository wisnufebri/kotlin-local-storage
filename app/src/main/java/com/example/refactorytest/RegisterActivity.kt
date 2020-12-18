package com.example.refactorytest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.refactorytest.helper.DatabaseHelper
import com.example.refactorytest.helper.InputValidation
import com.example.refactorytest.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private val activity = this@RegisterActivity

    private lateinit var scrollView: ScrollView

    private lateinit var textInputLayoutName: TextInputLayout
    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputLayoutPassword: TextInputLayout
    private lateinit var textInputLayoutConfirmPassword: TextInputLayout

    private lateinit var textInputEditTextName: TextInputEditText
    private lateinit var textInputEditTextEmail: TextInputEditText
    private lateinit var textInputEditTextPassword: TextInputEditText
    private lateinit var textInputEditTextConfirmPassword: TextInputEditText

    private lateinit var imageView: ImageView

    private lateinit var buttonRegister: Button
    private lateinit var textViewLoginLink: Button
    private lateinit var buttonOpenFile: Button

    private lateinit var inputValidation: InputValidation
    private lateinit var databaseHelper: DatabaseHelper

    private val pickImage = 200
    private var imageUri: Uri? = null

    private val cameraRequest= 1888

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayUseLogoEnabled(true)
            setLogo(R.drawable.refactoryhd)
        }
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED)
        buttonOpenFile = findViewById(R.id.buttonLoadPicture)
        imageView = findViewById(R.id.imageView)
        val photoButton: Button = findViewById(R.id.buttonTakePicture)
        buttonOpenFile.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }
        photoButton.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, cameraRequest)
        }
        initViews()
        initListeners()
        initObjects()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequest) {
            val photo: Bitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(photo)
        }
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun initViews() {
        scrollView = findViewById<View>(R.id.scroll) as ScrollView

        textInputLayoutName = findViewById<View>(R.id.layoutName) as TextInputLayout
        textInputLayoutEmail = findViewById<View>(R.id.layoutEmail) as TextInputLayout
        textInputLayoutPassword = findViewById<View>(R.id.layoutPassword) as TextInputLayout
        textInputLayoutConfirmPassword =
            findViewById<View>(R.id.layoutPasswordConfirm) as TextInputLayout

        textInputEditTextName = findViewById<View>(R.id.inputName) as TextInputEditText
        textInputEditTextEmail = findViewById<View>(R.id.inputEmail) as TextInputEditText
        textInputEditTextPassword = findViewById<View>(R.id.inputPassword) as TextInputEditText
        textInputEditTextConfirmPassword =
            findViewById<View>(R.id.inputPasswordConfirm) as TextInputEditText

        buttonRegister = findViewById<View>(R.id.submit) as Button
        textViewLoginLink = findViewById<View>(R.id.login) as Button

    }

    private fun initListeners() {
        buttonRegister!!.setOnClickListener(this)
        textViewLoginLink!!.setOnClickListener(this)

    }

    private fun initObjects() {
        inputValidation = InputValidation(activity)
        databaseHelper = DatabaseHelper(activity)


    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.submit -> postDataToSQLite()

            R.id.login -> finish()
        }
    }

    private fun postDataToSQLite() {
        if (!inputValidation!!.isInputEditTextFilled(
                textInputEditTextName,
                textInputLayoutName,
                getString(R.string.error_message_name)
            )
        ) {
            return
        }
        if (!inputValidation!!.isInputEditTextFilled(
                textInputEditTextEmail,
                textInputLayoutEmail,
                getString(R.string.error_message_email)
            )
        ) {
            return
        }
        if (!inputValidation!!.isInputEditTextEmail(
                textInputEditTextEmail,
                textInputLayoutEmail,
                getString(R.string.error_message_email)
            )
        ) {
            return
        }
        if (!inputValidation!!.isInputEditTextFilled(
                textInputEditTextPassword,
                textInputLayoutPassword,
                getString(R.string.error_message_password)
            )
        ) {
            return
        }
        if (!inputValidation!!.isInputEditTextMatches(
                textInputEditTextPassword, textInputEditTextConfirmPassword,
                textInputLayoutConfirmPassword, getString(R.string.error_password_match)
            )
        ) {
            return
        }

        if (!databaseHelper!!.checkUser(textInputEditTextEmail!!.text.toString().trim())) {

            var user = User(
                name = textInputEditTextName!!.text.toString().trim(),
                email = textInputEditTextEmail!!.text.toString().trim(),
                password = textInputEditTextPassword!!.text.toString().trim()
            )

            databaseHelper!!.addUser(user)

            val text = R.string.success_message
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()
//            Snackbar.make(scrollView!!, getString(R.string.success_message), Snackbar.LENGTH_LONG)
//                .show()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            emptyInputEditText()

        } else {
            Snackbar.make(
                scrollView!!,
                getString(R.string.error_email_exists),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun emptyInputEditText() {
        textInputEditTextName!!.text = null
        textInputEditTextEmail!!.text = null
        textInputEditTextPassword!!.text = null
        textInputEditTextConfirmPassword!!.text = null
    }
}
