package com.nanioi.closetapplication.mypage

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.DBkey.Companion.DB_ITEM
import com.nanioi.closetapplication.DBkey.Companion.DB_USERS
import com.nanioi.closetapplication.MainActivity
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.SignInActivity
import com.nanioi.closetapplication.User.userModel
import com.nanioi.closetapplication.User.LoginUserData
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_BODY_BACK
import com.nanioi.closetapplication.closet.ClosetFragment
import com.nanioi.closetapplication.closet.ItemModel
import com.nanioi.closetapplication.closet.ItemState
import com.nanioi.closetapplication.databinding.ActivityEditProfileBinding
import com.nanioi.closetapplication.databinding.ActivitySignUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewModelScope

class EditProfileActivity : AppCompatActivity() {

    private var editBody1ImageUri: Uri? = null
    private var editBody2ImageUri: Uri? = null
    private var editBodyFrontImageUrl: String? = null
    private var editBodyBackImageUrl: String? = null
    private lateinit var curPhotoPath: String

    private val storage: FirebaseStorage by lazy { Firebase.storage }
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val userDB: FirebaseDatabase by lazy { Firebase.database }
    val db = FirebaseFirestore.getInstance()
    private val binding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }

    var body1ImageFileName: String? = null
    var body2ImageFileName: String? = null
    var userID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()

        binding.btnEditUserDataPass.setOnClickListener {
            if (binding.etEditUserDataName.text.isNotEmpty())
                if (binding.etEditUserDataCm.text.isNotEmpty())
                    if (binding.etEditUserDataKg.text.isNotEmpty())
                        if (editBody1ImageUri != null)
                            if (editBody2ImageUri != null)
                                if (binding.cbEditUserDataPassword.isChecked == true)
                                    if (binding.etEditUserDataChangePassword.text.isNotEmpty())
                                        if (binding.etEditUserDataChangePasswordCheck.text.isNotEmpty())
                                            if (binding.etEditUserDataChangePassword.text.toString() == binding.etEditUserDataChangePasswordCheck.text.toString()) {
                                                auth?.currentUser?.updatePassword(binding.etEditUserDataChangePassword.text.toString())
                                                    ?.addOnCompleteListener(this) {
                                                        if (it.isSuccessful) {
                                                            Toast.makeText(
                                                                this@EditProfileActivity,
                                                                "???????????? ?????? ??????",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            changeUserData()
                                                        }
                                                    }
                                            } else
                                                Toast.makeText(
                                                    this@EditProfileActivity,
                                                    "????????? ??????????????? ???????????? ????????????.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                        else
                                            Toast.makeText(
                                                this@EditProfileActivity,
                                                "????????? ???????????? ????????? ????????? ?????????.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                    else
                                        Toast.makeText(
                                            this@EditProfileActivity,
                                            "????????? ??????????????? ????????? ?????????.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                else
                                    changeUserData()
                            else
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "?????? ????????? ????????? ??? ?????????.",
                                    Toast.LENGTH_SHORT
                                ).show()
                        else
                            Toast.makeText(
                                this@EditProfileActivity,
                                "?????? ????????? ????????? ??? ?????????.",
                                Toast.LENGTH_SHORT
                            ).show()
                    else
                        Toast.makeText(
                            this@EditProfileActivity,
                            "???????????? ????????? ?????????.",
                            Toast.LENGTH_SHORT
                        ).show()
                else
                    Toast.makeText(this@EditProfileActivity, "?????? ????????? ?????????.", Toast.LENGTH_SHORT)
                        .show()
            else
                Toast.makeText(this@EditProfileActivity, "????????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditUserDataSignOut.setOnClickListener {
            deleteUserData()
        }

        binding.btnEditUserDataBody1.setOnClickListener {
            showPictureUploadDialog(1)
        }

        binding.btnEditUserDataBody2.setOnClickListener {
            showPictureUploadDialog(2)
        }
    }

    private fun initView() = with(binding) {
        userID = auth.currentUser!!.uid
        body1ImageFileName =
            userID + "_img_body1.jpg"
        body2ImageFileName =
            userID + "_img_body2.jpg"

        binding.cbEditUserDataPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                binding.llEditUserDataPassword.visibility = View.VISIBLE //????????? ?????? ???????????? ?????? ??????????????? ?????????
            else
                binding.llEditUserDataPassword.visibility =
                    View.GONE //?????? ?????? ??? ?????? ???????????? ?????? ??????????????? ???????????? ???
        }

        binding.tvEditUserDataEmail.text = LoginUserData.email
        binding.etEditUserDataName.setText(LoginUserData.name)
        binding.etEditUserDataCm.setText(LoginUserData.cm)
        binding.etEditUserDataKg.setText(LoginUserData.kg)
        binding.rgEditUserDataGender.check(if (LoginUserData.gender == "??????") R.id.rb_edit_user_data_man else R.id.rb_edit_user_data_woman)

        editBodyFrontImageUrl = LoginUserData.body_front_ImageUrl
        editBody2ImageUri = LoginUserData.body_back_ImageUrl

        Glide.with(binding.root)
            .load(editBodyFrontImageUrl)
            .into(binding.ivEditUserDataBody1)

        binding.ivEditUserDataBody2.setImageURI(editBody2ImageUri)
    }

    private fun clickEventListener() = with(binding) {

    }

    //by ??????. user?????? ?????? ( 21.11.05 )
    private fun changeUserData() {
        Toast.makeText(this@EditProfileActivity, "???????????? ?????? ???...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Default).launch {

            deleteImage(body1ImageFileName!!, 1)
            uploadImage(body1ImageFileName!!, editBody1ImageUri, 1, successHandler = { uri ->
                uploadUserDB(uri)
            },
                errorHandler = {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "?????? ??????(???) ???????????? ??????????????????.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                })
            deleteImage(body2ImageFileName!!, 2)
            uploadImage(body1ImageFileName!!, editBody1ImageUri, 2, successHandler = { url ->
                editBodyBackImageUrl = url
            },
                errorHandler = {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "?????? ??????(???) ???????????? ??????????????????.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                })
        }
    }

    private fun deleteImage(fileName: String, type: Int) {
        var path: String
        if (type == 1) {
            path = "user/body1"
        } else {
            path = "user/body2"
        }
        storage.reference.child(path).child(fileName).delete()
    }

    private fun uploadImage(
        fileName: String,
        ImageUri: Uri?,
        type: Int,
        successHandler: (String) -> Unit,
        errorHandler: () -> Unit
    ) {
        var path: String? = null
        if (type == 1)
            path = "user/body1"
        else
            path = "user/body2"

        storage.reference.child(path)
            .child(fileName)
            .putFile(ImageUri!!)
            .addOnCompleteListener { // ??????????????? ?????? ?????????
                if (it.isSuccessful) { // ????????? -> ????????? ??????
                    storage.reference.child(path).child(
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

    private fun uploadUserDB(uri: String) = with(binding) {
        val userModel = userModel()
        userModel.uid = userID
        userModel.email = binding.tvEditUserDataEmail.text.toString()
        userModel.name = binding.etEditUserDataName.text.toString()
        userModel.gender = if (binding.rbEditUserDataMan.isChecked == true) "??????" else "??????"
        userModel.cm = binding.etEditUserDataCm.text.toString()
        userModel.kg = binding.etEditUserDataKg.text.toString()
        userModel.body_front_imageUrl = uri
        userModel.body_back_imageUrl = editBody2ImageUri.toString()

        userDB.reference.child(DB_USERS).child(userID!!).removeValue()
        userDB.reference.child(DB_USERS).child(userID!!).setValue(userModel).addOnCompleteListener {
            Toast.makeText(this@EditProfileActivity, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show()
            finish()
        }
        updateLoginUserDB()
    }

    //by ??????. user?????? ?????? ( 21.11.05 )
    private fun deleteUserData() {
        var signOutDialog: AlertDialog.Builder = AlertDialog.Builder(this@EditProfileActivity)
        signOutDialog.setTitle("??????")
        signOutDialog.setMessage("?????????????????????????")

        signOutDialog.setPositiveButton("??????") { dialog, _ ->
            val user = auth.currentUser
            val userId: String = user!!.uid

            user.delete().addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    userDB.reference.child(DB_USERS).child(userId).removeValue()
                        .addOnCompleteListener(this@EditProfileActivity) { task ->
                            if (task.isSuccessful) {
                                Log.d("bbbbb", "userDelete")
                                deleteImage(body1ImageFileName!!, 1)
                                deleteImage(body2ImageFileName!!, 2)

                                Toast.makeText(this@EditProfileActivity, "?????? ?????? ??????", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                startActivity(Intent(this@EditProfileActivity, SignInActivity::class.java))
                                finish()
                            }
                        }.addOnFailureListener {
                            Log.d("EditProfileActivity", "userDelete error" + it.toString())
                        }
                }
            }
        }
        signOutDialog.setNegativeButton("??????") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        signOutDialog.setCancelable(false)
        signOutDialog.show()
    }

    private fun updateLoginUserDB() = with(binding) {
        LoginUserData.email = binding.tvEditUserDataEmail.text.toString()
        LoginUserData.name = binding.etEditUserDataName.text.toString()
        LoginUserData.gender = if (binding.rbEditUserDataMan.isChecked == true) "??????" else "??????"
        LoginUserData.cm = binding.etEditUserDataCm.text.toString()
        LoginUserData.kg = binding.etEditUserDataKg.text.toString()
        LoginUserData.body_front_ImageUrl = editBodyFrontImageUrl
        LoginUserData.body_back_ImageUrl = editBody2ImageUri
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
                Toast.makeText(this@EditProfileActivity, "????????? ?????? ???????????????.", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { // ??????????????? ??????????????? ??? ????????? ?????? ?????? ??????
                Toast.makeText(this@EditProfileActivity, "????????? ?????? ???????????????.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        TedPermission.with(this@EditProfileActivity)
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
        //?????? ????????? ??? ??????
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
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    if (imageType == 1) {
                        editBody1ImageUri = photoURI
                        startActivityForResult(
                            takePictureIntent,
                            BODY1_CAMERA_REQUEST_CODE
                        )
                    } else {
                        editBody2ImageUri = photoURI
                        startActivityForResult(
                            takePictureIntent,
                            BODY2_CAMERA_REQUEST_CODE
                        )
                    }
                }
            }
        }
    }

    //by ??????. ????????? ?????? ?????? ?????? (21.10.23)
    private fun createImageFile(): File? {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
            .apply { curPhotoPath = absolutePath }
    }

    // by ??????. ???????????? ????????? ????????? ???????????? ?????? (21.10.16)
    private fun startContentProvider(imageType: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" // ?????????????????? ???????????????

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        with(binding) {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode != Activity.RESULT_OK) {
                return
            }

            when (requestCode) {
                BODY1_GALLERY_REQUEST_CODE -> { //????????? ????????? ?????? ????????? data?????? ????????? ?????? uri ??????
                    val uri = data?.data
                    if (uri != null) {
                        binding.ivEditUserDataBody1.setImageURI(uri)
                        editBody1ImageUri = uri // ????????? ????????? ????????? ????????? ??????????????? ???????????? ??? ????????? ??????
                    } else {
                        Toast.makeText(
                            this@EditProfileActivity,
                            "????????? ???????????? ???????????????.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                BODY1_CAMERA_REQUEST_CODE -> {
                    binding.ivEditUserDataBody1.setImageURI(editBody1ImageUri)
                }
                BODY2_GALLERY_REQUEST_CODE -> { //????????? ????????? ?????? ????????? data?????? ????????? ?????? uri ??????
                    val uri = data?.data
                    if (uri != null) {
                        binding.ivEditUserDataBody2.setImageURI(uri)
                        editBody2ImageUri = uri // ????????? ????????? ????????? ????????? ??????????????? ???????????? ??? ????????? ??????
                    } else {
                        Toast.makeText(
                            this@EditProfileActivity,
                            "????????? ???????????? ???????????????.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                BODY2_CAMERA_REQUEST_CODE -> {
                    binding.ivEditUserDataBody2.setImageURI(editBody2ImageUri)
                }
                else -> {
                    Toast.makeText(this@EditProfileActivity, "????????? ???????????? ???????????????.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.e("aaa", "backButton Click!")
        finish()
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