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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.nanioi.closetapplication.DBkey
import com.nanioi.closetapplication.DBkey.Companion.DB_USERS
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.SignInActivity
import com.nanioi.closetapplication.User.userModel
import com.nanioi.closetapplication.User.LoginUserData
import com.nanioi.closetapplication.User.userDBkey.Companion.DB_BODY_BACK
import com.nanioi.closetapplication.databinding.ActivityEditProfileBinding
import com.nanioi.closetapplication.databinding.ActivitySignUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private var editBody1ImageUri: Uri? = null
    private var editBody2ImageUri: Uri? = null
    private var editBodyFrontImageUrl: String? = null
    private var editBodyBackImageUrl: String? = null
    private lateinit var curPhotoPath: String

    private val storage: FirebaseStorage by lazy { Firebase.storage }
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val userDB : FirebaseDatabase by lazy { Firebase.database}
    private val binding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }

    var body1ImageFileName : String ? =null
    var body2ImageFileName  : String ? =null
    var userID : String ?=null

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
                                                            Toast.makeText(this@EditProfileActivity, "비밀번호 변경 완료", Toast.LENGTH_SHORT).show()
                                                            changeUserData()
                                                        }
                                                    }
                                            } else
                                                Toast.makeText(this@EditProfileActivity, "변경할 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                                        else
                                            Toast.makeText(this@EditProfileActivity, "변경할 비밀번호 확인을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                                    else
                                        Toast.makeText(this@EditProfileActivity, "변경할 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                                else
                                    changeUserData()
                            else
                                Toast.makeText(this@EditProfileActivity, "전신 사진을 업로드 해 주세요.", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(this@EditProfileActivity, "얼굴 사진을 업로드 해 주세요.", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this@EditProfileActivity, "몸무게를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@EditProfileActivity, "키를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this@EditProfileActivity, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
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
    private fun initView() = with(binding){
        userID = auth.currentUser!!.uid
        body1ImageFileName =
            userID + "_img_body1.jpg"
        body2ImageFileName =
            userID + "_img_body2.jpg"

        binding.cbEditUserDataPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                binding.llEditUserDataPassword.visibility = View.VISIBLE //체크할 경우 비밀번호 수정 레이아웃을 보여줌
            else
                binding.llEditUserDataPassword.visibility = View.GONE //체크 해제 할 경우 비밀번호 수정 레이아웃을 사라지게 함
        }

        binding.tvEditUserDataEmail.text = LoginUserData.email
        binding.etEditUserDataName.setText(LoginUserData.name)
        binding.etEditUserDataCm.setText(LoginUserData.cm)
        binding.etEditUserDataKg.setText(LoginUserData.kg)
        binding.rgEditUserDataGender.check(if (LoginUserData.gender == "남자") R.id.rb_edit_user_data_man else R.id.rb_edit_user_data_woman)

        editBodyFrontImageUrl = LoginUserData.body_front_ImageUrl
        editBodyBackImageUrl = LoginUserData.body_back_ImageUrl

        Glide.with(binding.root)
            .load(editBodyFrontImageUrl)
            .into(binding.ivEditUserDataBody1)

        Glide.with(binding.root)
            .load(editBodyBackImageUrl)
            .into(binding.ivEditUserDataBody2)
    }
    private fun clickEventListener()= with(binding){

    }
    //by 나연. user정보 수정 ( 21.11.05 )
    private fun changeUserData() {
        Toast.makeText(this@EditProfileActivity, "회원정보 수정 중...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Default).launch {

            deleteImage(body1ImageFileName!!,1)
            uploadImage(body1ImageFileName!!,editBody1ImageUri,1,successHandler = { uri ->
                uploadUserDB(uri)
            },
                errorHandler = {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "전신 사진(앞) 업로드에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                })
            deleteImage(body2ImageFileName!!,2)
            uploadImage(body1ImageFileName!!,editBody1ImageUri,2,successHandler = { uri ->
                LoginUserData.body_back_ImageUrl = uri
                userDB.reference.child(DB_USERS).child(userID!!).child(DB_BODY_BACK).setValue(uri).addOnCompleteListener {
                    Toast.makeText(this@EditProfileActivity, "회원정보 수정 완료", Toast.LENGTH_SHORT).show()
                    finish()
                }
            },
                errorHandler = {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "전신 사진(앞) 업로드에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                })
        }
    }
    private fun deleteImage(fileName: String,type : Int){
        var path : String
        if(type == 1){
            path = "user/body1"
        }else{
            path = "user/body2"
        }
        storage.reference.child(path).child(fileName).delete()
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
            .addOnCompleteListener { // 성공했는지 확인 리스너
                if (it.isSuccessful) { // 성공시 -> 업로드 완료
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
                } else { // 업로드 실패
                    errorHandler()
                }
            }
    }
    private fun uploadUserDB( uri: String) = with(binding) {
        val userModel = userModel()
        userModel.uid = userID
        userModel.email =binding.tvEditUserDataEmail.text.toString()
        userModel.name = binding.etEditUserDataName.text.toString()
        userModel.gender= if (binding.rbEditUserDataMan.isChecked == true) "남자" else "여자"
        userModel.cm = binding.etEditUserDataCm.text.toString()
        userModel.kg = binding.etEditUserDataKg.text.toString()
        userModel.body_front_imageUrl = uri
        userModel.body_back_imageUrl = editBodyBackImageUrl

        userDB.reference.child(DB_USERS).child(userID!!).removeValue()
        userDB.reference.child(DB_USERS).child(userID!!).setValue(userModel).addOnCompleteListener {
            Toast.makeText(this@EditProfileActivity, "회원정보 수정 완료", Toast.LENGTH_SHORT).show()
            finish()
        }
        updateLoginUserDB()
    }
    //by 나연. user정보 삭제 ( 21.11.05 )
    private fun deleteUserData() {
        var signOutDialog: AlertDialog.Builder = AlertDialog.Builder(this@EditProfileActivity)
        signOutDialog.setTitle("알림")
        signOutDialog.setMessage("탈퇴하시겠습니까?")

        signOutDialog.setPositiveButton("확인") { dialog, _ ->
            val user = auth.currentUser
            user?.let {
                user.delete().addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        userDB.reference.child(DB_USERS).child(user.uid).removeValue()
                            .addOnCompleteListener(this@EditProfileActivity) { task ->
                                if (task.isSuccessful) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        storage.reference.child("user/body1").child(body1ImageFileName!!).delete()
                                        storage.reference.child("user/body2").child(body2ImageFileName!!).delete()

                                        Toast.makeText(
                                            this@EditProfileActivity,
                                            "회원 탈퇴 완료",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        dialog.dismiss()
                                        startActivity(
                                            Intent(
                                                this@EditProfileActivity,
                                                SignInActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                }
                            }
                    }
                }
            }
        }
        signOutDialog.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        signOutDialog.setCancelable(false)
        signOutDialog.show()
    }

    private fun updateLoginUserDB() = with(binding){
        LoginUserData.email = binding.tvEditUserDataEmail.text.toString()
        LoginUserData.name = binding.etEditUserDataName.text.toString()
        LoginUserData.gender = if (binding.rbEditUserDataMan.isChecked == true) "남자" else "여자"
        LoginUserData.cm = binding.etEditUserDataCm.text.toString()
        LoginUserData.kg = binding.etEditUserDataKg.text.toString()
        LoginUserData.body_front_ImageUrl = editBodyFrontImageUrl
        LoginUserData.body_back_ImageUrl = editBodyBackImageUrl
    }


    //by 나연. 사진 첨부할 방식 선택 함수 (21.10.16)
    private fun showPictureUploadDialog(imageType: Int) {
        android.app.AlertDialog.Builder(this)
            .setTitle("사진첨부")
            .setMessage("사진첨부할 방식을 선택하세요")
            .setPositiveButton("카메라") { _, _ ->
                setPermission()
                startCameraCapture(imageType)
            }
            .setNegativeButton("갤러리") { _, _ ->
                checkExternalStoragePermission(imageType) {
                    startContentProvider(imageType)
                }
            }
            .create()
            .show()
    }

    //by 나연. 갤러리 권한 확인 함수 (21.10.16)
    private fun checkExternalStoragePermission(imageType: Int, uploadAction: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> { // 허용된경우
                uploadAction()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> { // 교육용 팝업이 필요한경우
                showPermissionContextPopup(imageType)
            }
            else -> { // 그 외 해당권한 요청
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

    //by 나연. 카메라 권한 확인 (테드 퍼미션 설정) 함수 (21.10.23)
    private fun setPermission() {
        val permission = object : PermissionListener {
            override fun onPermissionGranted() { // 설정해놓은 위험권한들이 허용된 경우 이 곳 수행
                Toast.makeText(this@EditProfileActivity, "권한이 허용 되었습니다.", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { // 설정해놓은 위험권한들 중 거부한 경우 이곳 수행
                Toast.makeText(this@EditProfileActivity, "권한이 거부 되었습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        TedPermission.with(this@EditProfileActivity)
            .setPermissionListener(permission)
            .setRationaleMessage("카메라 앱을 사용하시려면 권한을 허용해주세요.")
            .setDeniedMessage("권한을 거부하셨습니다. [앱 설정] -> [권한] 항목에서 허용해주세요.")
            .setPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            )
            .check()
    }

    //by 나연. 카메라 실행 함수 (21.10.23)
    private fun startCameraCapture(imageType: Int) {
        //기본 카메라 앱 실행
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

    //by 나연. 이미지 파일 생성 함수 (21.10.23)
    private fun createImageFile(): File? {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
            .apply { curPhotoPath = absolutePath }
    }

    // by 나연. 앨범에서 선택한 이미지 받아오기 함수 (21.10.16)
    private fun startContentProvider(imageType: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" // 이미지타입만 가져오도록

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

    //by 나연. 카메라/갤러리 권한 동의 교육용 팝업 구현 함수 (21.10.16)
    private fun showPermissionContextPopup(imageType: Int) {
        android.app.AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 가져오기 위해 필요합니다.")
            .setPositiveButton("동의") { _, _ ->
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

    override fun onRequestPermissionsResult( // 권한에 대한 결과가 오게 되면 이 함수 호출된다.
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            BODY1_PERMISSION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // 승낙된경우
                    startContentProvider(1)
                } else {
                    Toast.makeText(
                        this,
                        "권한을 거부하셨습니다. [앱 설정] -> [권한] 항목에서 허용해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            BODY2_PERMISSION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // 승낙된경우
                    startContentProvider(2)
                } else {
                    Toast.makeText(
                        this,
                        "권한을 거부하셨습니다. [앱 설정] -> [권한] 항목에서 허용해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = with(binding) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            BODY1_GALLERY_REQUEST_CODE -> { //갤러리 요청일 경우 받아온 data에서 사진에 대한 uri 저장
                val uri = data?.data
                if (uri != null) {
                    binding.ivEditUserDataBody1.setImageURI(uri)
                    editBody1ImageUri = uri // 이미지 업로드 버튼을 눌러야 저장되므로 그전까지 이 변수에 저장
                } else {
                    Toast.makeText(this@EditProfileActivity, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            BODY1_CAMERA_REQUEST_CODE -> {
                binding.ivEditUserDataBody1.setImageURI(editBody1ImageUri)
            }
            BODY2_GALLERY_REQUEST_CODE -> { //갤러리 요청일 경우 받아온 data에서 사진에 대한 uri 저장
                val uri = data?.data
                if (uri != null) {
                    binding.ivEditUserDataBody2.setImageURI(uri)
                    editBody2ImageUri = uri // 이미지 업로드 버튼을 눌러야 저장되므로 그전까지 이 변수에 저장
                } else {
                    Toast.makeText(this@EditProfileActivity, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            BODY2_CAMERA_REQUEST_CODE -> {
                binding.ivEditUserDataBody2.setImageURI(editBody2ImageUri)
            }
            else -> {
                Toast.makeText(this@EditProfileActivity, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
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