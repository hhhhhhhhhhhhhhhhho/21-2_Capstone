package com.nanioi.closetapplication.User

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSerializer
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_BODY_BACK
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_BODY_FRONT
import com.nanioi.closetapplication.databinding.ActivitySignUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.lang.Exception
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : AppCompatActivity() {

    private var body1ImageUri: Uri? = null
    private var body2ImageUri: Uri? = null
    private var body_front_imageUrl: String? = null
    private var body_back_imageUrl: String? = null
 //   private lateinit var curPhotoPath: String

    private val storage: FirebaseStorage by lazy { Firebase.storage }
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val userDB: FirebaseDatabase by lazy { Firebase.database }
    private val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rgSignUpGender.check(R.id.rb_sign_up_man) //???????????? ????????? ???????????? ????????? ????????? ??????????????? ????????? ???????????? ??????

        //numberPicker
        val dialog = AlertDialog.Builder(this).create()
        val edialog : LayoutInflater = LayoutInflater.from(this)

        val year : NumberPicker = binding.yearPicker
        val month : NumberPicker = binding.monthPicker
        val day : NumberPicker = binding.dayPicker

        year.apply {
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            minValue = 1980
            maxValue = 2021
        }
        month.apply {
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            minValue = 1
            maxValue = 12
        }
        day.apply {
            //wrapSelectorWheel = false
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            minValue = 1
            maxValue = 31
        }



        binding.btnSignUpPass.setOnClickListener {
            if (binding.etSignUpName.text.isNotEmpty())
                if (binding.etSignUpEmail.text.isNotEmpty())
                    if (binding.etSignUpPassword.text.isNotEmpty())
                        if (binding.etSignUpPasswordCheck.text.isNotEmpty())
                            if (binding.etSignUpPassword.text.toString() == binding.etSignUpPasswordCheck.text.toString())
                                if (binding.etSignUpCm.text.isNotEmpty())
                                    if (binding.etSignUpKg.text.isNotEmpty())
                                        if (body1ImageUri != null)
                                            if (body2ImageUri != null) {
                                                Toast.makeText(
                                                    this@SignUpActivity,
                                                    "???????????? ???...",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    auth.createUserWithEmailAndPassword(
                                                        binding.etSignUpEmail.text.toString(),
                                                        binding.etSignUpPassword.text.toString()
                                                    ) //????????? ????????? ?????? ??? ????????? ?????? createUserWithEmailAndPassword ??????
                                                        .addOnCompleteListener(this@SignUpActivity) { task ->
                                                            if (task.isSuccessful) {
                                                                val user = auth.currentUser
                                                                user?.let {
                                                                    val userUid = user.uid

                                                                    val body1ImageFileName =
                                                                        userUid + "_img_body1.jpg"
                                                                    val  body2ImageFileName =
                                                                        userUid + "_img_body2.jpg"

                                                                    uploadImage(body1ImageFileName,
                                                                        body1ImageUri,1,
                                                                        successHandler = { uri ->
                                                                            uploadUserDB(userUid, user.email,uri)
                                                                            Log.w(
                                                                                "SignUpActivity",
                                                                                "?????? ??????(???) ????????? ?????? " + it.toString()
                                                                            )
                                                                        },
                                                                        errorHandler = {
                                                                            Log.w(
                                                                                "SignUpActivity",
                                                                                "user?????? ????????? ?????? : " + it.toString()
                                                                            )
                                                                        })
                                                                    uploadImage(body2ImageFileName,
                                                                        body2ImageUri,2,
                                                                        successHandler = { uri ->
                                                                            body_back_imageUrl = uri
                                                                            Log.w(
                                                                                "SignUpActivity",
                                                                                "?????? ??????(???) ????????? ?????? " + it.toString()
                                                                            )
                                                                        },
                                                                        errorHandler = {
                                                                            Log.w(
                                                                                "SignUpActivity",
                                                                                "?????? ??????(???) ???????????? ??????????????????. " + it.toString()
                                                                            )
                                                                        })

                                                                }
                                                                runOnUiThread {
                                                                    Toast.makeText(
                                                                        this@SignUpActivity,
                                                                        "???????????? ??????",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    finish()
                                                                } //??????????????? ??????????????? ?????? ??????????????? ??????????????? ??????
                                                            } else
                                                                runOnUiThread {
                                                                    Toast.makeText(
                                                                        this@SignUpActivity,
                                                                        "???????????? ??????",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    val user = auth.currentUser
                                                                    user?.delete()
                                                                }
                                                        }
                                                }
                                            } else
                                                Toast.makeText(
                                                    this@SignUpActivity,
                                                    "?????? ??????(???)??? ????????? ??? ?????????.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                        else
                                            Toast.makeText(
                                                this@SignUpActivity,
                                                "?????? ??????(???)??? ????????? ??? ?????????.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                    else
                                        Toast.makeText(
                                            this@SignUpActivity,
                                            "???????????? ????????? ?????????.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                else
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "?????? ????????? ?????????.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            else
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "??????????????? ???????????? ????????????.",
                                    Toast.LENGTH_SHORT
                                ).show()
                        else
                            Toast.makeText(
                                this@SignUpActivity,
                                "???????????? ????????? ????????? ?????????.",
                                Toast.LENGTH_SHORT
                            ).show()
                    else
                        Toast.makeText(
                            this@SignUpActivity,
                            "??????????????? ????????? ?????????.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                else
                    Toast.makeText(
                        this@SignUpActivity,
                        "???????????? ????????? ?????????.",
                        Toast.LENGTH_SHORT
                    ).show()
            else
                Toast.makeText(
                    this@SignUpActivity,
                    "????????? ????????? ?????????.",
                    Toast.LENGTH_SHORT
                ).show()
        }

        binding.btnSignUpBody1.setOnClickListener {
            showPictureUploadDialog(1)
        }

        binding.btnSignUpBody2.setOnClickListener {
            showPictureUploadDialog(2)
        }
    }

    private fun uploadUserDB(userUid: String, email: String?, uri: String) = with(binding) {
        val userInfo = userModel()
        userInfo.uid = userUid
        userInfo.email = email
        userInfo.name = binding.etSignUpName.text.toString()
        userInfo.gender = if ( binding.rbSignUpMan.isChecked == true) "??????" else "??????"
        userInfo.cm = binding.etSignUpCm.text.toString()
        userInfo.kg = binding.etSignUpKg.text.toString()
        userInfo.body_front_imageUrl = uri
        userInfo.body_back_imageUrl = body2ImageUri.toString()

        userDB.reference.child(DBkey.DB_USERS)
            .child(userUid)
            .setValue(userInfo)
            .addOnCompleteListener {
                Log.w(
                    "SignUpActivity",
                    "user?????? ????????? ??????! "
                )
            }.addOnFailureListener {
                Log.w(
                    "SignUpActivity",
                    "user?????? ????????? ?????? : " + it.toString()
                )
            }
    }

    private fun uploadImage(
        fileName: String,
        ImageUri: Uri?,
        type : Int,
        successHandler: (String) -> Unit,
        errorHandler: () -> Unit
    ) {
        var path : String ? =null
        if(type==1)
            path = "user/body1"
        else
            path = "user/body2"
        storage.reference.child( path)
            .child(fileName)
            .putFile(ImageUri!!)
            .addOnCompleteListener { // ??????????????? ?????? ?????????
                if (it.isSuccessful) { // ????????? -> ????????? ??????
                    storage.reference.child( path).child(
                        fileName
                    )
                        .downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                } else { // ????????? ??????
                    errorHandler()
                }
            }
    }

    //by??????. ????????? ?????? ???????????? ???????????? (21.11.14)
    fun getImageFilePath(contentUri: Uri): String {
        var columnIndex = 0
        val projection = arrayOf(MediaStore.Images.Media.DATA) // ????????????
        val cursor = contentResolver.query(contentUri, projection, null, null, null)
        // list index ???????????? , content ???????????? resolver??? ??????(query) ??????
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        return cursor.getString(columnIndex)
    }

    //by ??????. ?????? ????????? ?????? ?????? ?????? (21.10.16)
    private fun showPictureUploadDialog(imageType: Int) {
        android.app.AlertDialog.Builder(this)
            .setTitle("????????????")
            .setMessage("??????????????? ????????? ???????????????")
            .setPositiveButton("?????????") { _, _ ->
                setPermission()
                startCameraCapture(imageType)
            }
            .setNegativeButton("?????????") { _, _ ->
                checkExternalStoragePermission(imageType) {
                    startContentProvider(imageType)
                }
            }
            .create()
            .show()
    }

    //by ??????. ????????? ?????? ?????? ?????? (21.10.16)
    private fun checkExternalStoragePermission(imageType: Int, uploadAction: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> { // ???????????????
                uploadAction()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> { // ????????? ????????? ???????????????
                showPermissionContextPopup(imageType)
            }
            else -> { // ??? ??? ???????????? ??????
                if (imageType == 1) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        BODY1_PERMISSION_REQUEST_CODE
                    )
                } else {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        BODY2_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    //by ??????. ????????? ?????? ?????? (?????? ????????? ??????) ?????? (21.10.23)
    private fun setPermission() {
        val permission = object : PermissionListener {
            override fun onPermissionGranted() { // ??????????????? ?????????????????? ????????? ?????? ??? ??? ??????
                Toast.makeText(this@SignUpActivity, "????????? ?????? ???????????????.", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { // ??????????????? ??????????????? ??? ????????? ?????? ?????? ??????
                Toast.makeText(this@SignUpActivity, "????????? ?????? ???????????????.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        TedPermission.with(this@SignUpActivity)
            .setPermissionListener(permission)
            .setRationaleMessage("????????? ?????? ?????????????????? ????????? ??????????????????.")
            .setDeniedMessage("????????? ?????????????????????. [??? ??????] -> [??????] ???????????? ??????????????????.")
            .setPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            )
            .check()
    }

    //by ??????. ????????? ?????? ?????? (21.10.23)
    private fun startCameraCapture(imageType: Int) {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        null
                    }
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "$packageName",
                            it
                        )
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        if (imageType == 1) {
                            body1ImageUri = photoURI
                            startActivityForResult(
                                takePictureIntent,
                                BODY1_CAMERA_REQUEST_CODE
                            )
                        } else {
                            body2ImageUri = photoURI
                            startActivityForResult(
                                takePictureIntent,
                                BODY2_CAMERA_REQUEST_CODE
                            )
                        }
                    }
                }
            }
        } else {
            Log.w("SignUpActivity", "??????????????? ?????? ?????????")
        }
        //?????? ????????? ??? ??????

    }

    //by ??????. ????????? ?????? ?????? ?????? (21.10.23)
    private fun createImageFile(): File? {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFile: File? = null
        val storageDir: File? =
            File(Environment.getExternalStorageDirectory(), "DCIM/CameraCaptures")
        if (!storageDir!!.exists()) {
            storageDir!!.mkdirs()
        }
        imageFile = File(storageDir, timestamp)
        //curPhotoPath = imageFile.getAbsolutePath()

        return imageFile
    }

    // by ??????. ???????????? ????????? ????????? ???????????? ?????? (21.10.16)
    private fun startContentProvider(imageType: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI) // ?????????????????? ?????????.
        intent.setType("image/*") // ???????????? ????????????
        if (imageType == 1) {
            startActivityForResult(
                intent,
                BODY1_GALLERY_REQUEST_CODE
            )
        } else {
            startActivityForResult(
                intent,
                BODY2_GALLERY_REQUEST_CODE
            )
        }
    }

    //by ??????. ?????????/????????? ?????? ?????? ????????? ?????? ?????? ?????? (21.10.16)
    private fun showPermissionContextPopup(imageType: Int) {
        android.app.AlertDialog.Builder(this)
            .setTitle("????????? ???????????????.")
            .setMessage("????????? ???????????? ?????? ???????????????.")
            .setPositiveButton("??????") { _, _ ->
                if (imageType == 1) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        BODY1_PERMISSION_REQUEST_CODE
                    )
                } else {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        BODY2_PERMISSION_REQUEST_CODE
                    )
                }
            }
            .create()
            .show()
    }

    override fun onRequestPermissionsResult( // ????????? ?????? ????????? ?????? ?????? ??? ?????? ????????????.
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            BODY1_PERMISSION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // ???????????????
                    startContentProvider(1)
                } else {
                    Toast.makeText(
                        this,
                        "????????? ?????????????????????. [??? ??????] -> [??????] ???????????? ??????????????????.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            BODY2_PERMISSION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // ???????????????
                    startContentProvider(2)
                } else {
                    Toast.makeText(
                        this,
                        "????????? ?????????????????????. [??? ??????] -> [??????] ???????????? ??????????????????.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = with(binding){
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            BODY1_GALLERY_REQUEST_CODE -> { //????????? ????????? ?????? ????????? data?????? ????????? ?????? uri ??????
                val uri = data?.data
                if (uri != null) {
                    binding.ivSignUpBody1.setImageURI(uri)
                    body1ImageUri = uri // ????????? ????????? ????????? ????????? ??????????????? ???????????? ??? ????????? ??????

                } else {
                    Toast.makeText(this@SignUpActivity, "????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
                }
            }
            BODY1_CAMERA_REQUEST_CODE -> {
                binding.ivSignUpBody1.setImageURI(body1ImageUri)
            }
            BODY2_GALLERY_REQUEST_CODE -> { //????????? ????????? ?????? ????????? data?????? ????????? ?????? uri ??????
                val uri = data?.data
                if (uri != null) {
                    binding.ivSignUpBody2.setImageURI(uri)
                    body2ImageUri = uri // ????????? ????????? ????????? ????????? ??????????????? ???????????? ??? ????????? ??????

                } else {
                    Toast.makeText(this@SignUpActivity, "????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
                }
            }
            BODY2_CAMERA_REQUEST_CODE -> {
                binding.ivSignUpBody2.setImageURI(body2ImageUri)
            }
            else -> {
                Toast.makeText(this@SignUpActivity, "????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val BODY1_PERMISSION_REQUEST_CODE = 1000
        const val BODY1_GALLERY_REQUEST_CODE = 1001
        const val BODY1_CAMERA_REQUEST_CODE = 1002

        const val BODY2_PERMISSION_REQUEST_CODE = 2000
        const val BODY2_GALLERY_REQUEST_CODE = 2001
        const val BODY2_CAMERA_REQUEST_CODE = 2002
    }
}

