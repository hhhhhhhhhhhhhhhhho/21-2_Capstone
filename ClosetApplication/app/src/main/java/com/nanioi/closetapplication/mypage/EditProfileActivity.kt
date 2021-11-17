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
import com.nanioi.closetapplication.User.utils.LoginUserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private var tvEditUserDataEmail: TextView? = null //이메일 확인 텍스트 변수 선언
    private var etEditUserDataName: EditText? = null //이름 입력창 변수 선언
    private var cbEditUserDataPassword: CheckBox? = null //비밀번호 변경 여부 체크박스 변수 선언
    private var llEditUserDataPassword: ConstraintLayout? = null //비밀번호 변경 레이아웃 변수 선언
    private var etEditUserDataChangePassword: EditText? = null //변경할 비밀번호 입력창 변수 선언
    private var etEditUserDataChangePasswordCheck: EditText? = null //변경할 비밀번호 확인 입력창 변수 선언
    private var etEditUserDataCm: EditText? = null //키 입력창 변수 선언
    private var etEditUserDataKg: EditText? = null //몸무게 입력창 변수 선언
    private var rgEditUserDataGender: RadioGroup? = null //성별 선택 그룹 변수 선언
    private var rbEditUserDataMan: RadioButton? = null //남자 선택 변수 선언
    private var rbEditUserDataWoman: RadioButton? = null //여자 선택 변수 선언
    private var ivEditUserDataFace: ImageView? = null //얼굴 사진 뷰 변수 선언
    private var ivEditUserDataBody: ImageView? = null //전신 사진 뷰 변수 선언
    private var btnEditUserDataFace: Button? = null //얼굴 사진 버튼 변수 선언
    private var btnEditUserDataBody: Button? = null //전신 사진 버튼 변수 선언
    private var btnEditUserDataPass: Button? = null //회원정보 수정 버튼 변수 선언
    private var btnEditUserDataSignOut: Button? = null //회원탈퇴 버튼 변수 선언

    private var editFaceImageUri: Uri? = null
    private var editBodyImageUri: Uri? = null
    private lateinit var curPhotoPath: String
    private val storage: FirebaseStorage by lazy { Firebase.storage }
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val userDB : FirebaseDatabase by lazy { Firebase.database}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        tvEditUserDataEmail = findViewById(R.id.tv_edit_user_data_email)
        etEditUserDataName = findViewById(R.id.et_edit_user_data_name)
        cbEditUserDataPassword = findViewById(R.id.cb_edit_user_data_password)
        llEditUserDataPassword = findViewById(R.id.ll_edit_user_data_password)
        etEditUserDataChangePassword = findViewById(R.id.et_edit_user_data_change_password)
        etEditUserDataChangePasswordCheck = findViewById(R.id.et_edit_user_data_change_password_check)
        etEditUserDataCm = findViewById(R.id.et_edit_user_data_cm)
        etEditUserDataKg = findViewById(R.id.et_edit_user_data_kg)
        rgEditUserDataGender = findViewById(R.id.rg_edit_user_data_gender)
        rbEditUserDataMan = findViewById(R.id.rb_edit_user_data_man)
        rbEditUserDataWoman = findViewById(R.id.rb_edit_user_data_woman)
        ivEditUserDataFace = findViewById(R.id.iv_edit_user_data_face)
        ivEditUserDataBody = findViewById(R.id.iv_edit_user_data_body)
        btnEditUserDataFace = findViewById(R.id.btn_edit_user_data_face)
        btnEditUserDataBody = findViewById(R.id.btn_edit_user_data_body)
        btnEditUserDataPass = findViewById(R.id.btn_edit_user_data_pass)
        btnEditUserDataSignOut = findViewById(R.id.btn_edit_user_data_sign_out)

        cbEditUserDataPassword?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                llEditUserDataPassword?.visibility = View.VISIBLE //체크할 경우 비밀번호 수정 레이아웃을 보여줌
            else
                llEditUserDataPassword?.visibility = View.GONE //체크 해제 할 경우 비밀번호 수정 레이아웃을 사라지게 함
        }

        tvEditUserDataEmail?.text = LoginUserData.email
        etEditUserDataName?.setText(LoginUserData.name)
        etEditUserDataCm?.setText(LoginUserData.cm)
        etEditUserDataKg?.setText(LoginUserData.kg)
        rgEditUserDataGender?.check(if (LoginUserData.gender == "남자") R.id.rb_edit_user_data_man else R.id.rb_edit_user_data_woman)

        editFaceImageUri = LoginUserData.faceImageUri
        editBodyImageUri = LoginUserData.bodyImageUri

        Glide.with(this@EditProfileActivity)
            .load(LoginUserData.faceImageUri)
            .into(ivEditUserDataFace!!)

        Glide.with(this@EditProfileActivity)
            .load(LoginUserData.bodyImageUri)
            .into(ivEditUserDataBody!!)


        btnEditUserDataPass?.setOnClickListener {
            if (etEditUserDataName?.text!!.isNotEmpty())
                if (etEditUserDataCm?.text!!.isNotEmpty())
                    if (etEditUserDataKg?.text!!.isNotEmpty())
                        if (editFaceImageUri != null)
                            if (editBodyImageUri != null)
                                if (cbEditUserDataPassword?.isChecked == true)
                                    if (etEditUserDataChangePassword?.text!!.isNotEmpty())
                                        if (etEditUserDataChangePasswordCheck?.text!!.isNotEmpty())
                                            if (etEditUserDataChangePassword!!.text.toString() == etEditUserDataChangePasswordCheck?.text.toString()) {
                                                auth?.currentUser?.updatePassword(etEditUserDataChangePassword!!.text.toString())
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

        btnEditUserDataSignOut?.setOnClickListener {
            deleteUserData()
        }

        btnEditUserDataFace?.setOnClickListener {
            showPictureUploadDialog(1)
        }

        btnEditUserDataBody?.setOnClickListener {
            showPictureUploadDialog(2)
        }
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
                                    val faceImageFileName =
                                        user.uid + "_img_face.jpg"
                                    val bodyImageFileName =
                                        user.uid + "_img_body.jpg"
                                    storage.reference.child("user/face").child(faceImageFileName).delete()
                                    storage.reference.child("user/body").child(bodyImageFileName).delete()

                                    LoginUserData.uid = null
                                    LoginUserData.email = null
                                    LoginUserData.name = null
                                    LoginUserData.gender = null
                                    LoginUserData.cm = null
                                    LoginUserData.kg = null

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
    //by 나연. user정보 수정 ( 21.11.05 )
    private fun changeUserData() {
        Toast.makeText(this@EditProfileActivity, "회원정보 수정 중...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Default).launch {
            val userUid = LoginUserData.uid
            //uploadImage(userUid!!)

            val faceImageFileName =
                userUid + "_img_face.jpg"
            val bodyImageFileName =
                userUid + "_img_body.jpg"

            storage.reference.child("user/face").child(faceImageFileName).delete()
            storage.reference.child("user/face")
                .child(faceImageFileName)
                .putFile(editFaceImageUri!!)
                .addOnCompleteListener { // 성공했는지 확인 리스너
                    if (it.isSuccessful) { // 성공시 -> 업로드 완료
                        storage.reference.child(
                            "user/face"
                        ).child(
                            faceImageFileName
                        )
                            .downloadUrl
                            .addOnSuccessListener {
                                Log.d(
                                    "aaaa",
                                    "얼굴사진 수정 성공"
                                )
                            }
                            .addOnFailureListener {
                                Log.d(
                                    "aaaa",
                                    "얼굴사진 수정 실패"
                                )
                            }
                    } else { // 업로드 실패
                        Toast.makeText(
                            this@EditProfileActivity,
                            "얼굴사진 수정에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            storage.reference.child("user/body").child(bodyImageFileName).delete()
            storage.reference.child("user/body")
                .child(bodyImageFileName)
                .putFile(editBodyImageUri!!)
                .addOnCompleteListener { // 성공했는지 확인 리스너
                    if (it.isSuccessful) { // 성공시 -> 업로드 완료
                        storage.reference.child(
                            "user/body"
                        ).child(
                            bodyImageFileName
                        )
                            .downloadUrl
                            .addOnSuccessListener {
                                Log.d(
                                    "aaaa",
                                    "전신사진 수정 성공"
                                )
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "전신사진 수정에 실패했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else { // 업로드 실패
                        Toast.makeText(
                            this@EditProfileActivity,
                            "전신사진 수정에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            LoginUserData.email = tvEditUserDataEmail!!.text.toString()
            LoginUserData.name = etEditUserDataName!!.text.toString()
            LoginUserData.gender = if (rbEditUserDataMan?.isChecked == true) "남자" else "여자"
            LoginUserData.cm = etEditUserDataCm!!.text.toString()
            LoginUserData.kg = etEditUserDataKg!!.text.toString()
            LoginUserData.faceImageUri = editFaceImageUri
            LoginUserData.bodyImageUri = editBodyImageUri

            val userModel = userModel()
            userModel.uid = userUid
            userModel.email =tvEditUserDataEmail!!.text.toString()
            userModel.name = etEditUserDataName!!.text.toString()
            userModel.gender= if (rbEditUserDataMan?.isChecked == true) "남자" else "여자"
            userModel.cm = etEditUserDataCm!!.text.toString()
            userModel.kg = etEditUserDataKg!!.text.toString()
            userModel.faceImageUri = editFaceImageUri.toString()
            userModel.bodyImageUri = editBodyImageUri.toString()

            userDB.reference.child(DB_USERS).child(userUid!!).removeValue()
            userDB.reference.child(DB_USERS).child(userUid!!).setValue(userModel).addOnCompleteListener {
                Toast.makeText(this@EditProfileActivity, "회원정보 수정 완료", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this@EditProfileActivity, MyPageFragment::class.java))
                finish()
            }
        }
    }

//    //by 나연. 수정 이미지 업로드 ( 21.11.05 )
//    private fun uploadImage(uid: String) {
//        val faceImageFileName =
//            uid + "_img_face.jpg"
//        val bodyImageFileName =
//            uid + "_img_body.jpg"
//
//
//        storage.reference.child("user/face").child(faceImageFileName).delete()
//        storage.reference.child("user/face")
//            .child(faceImageFileName)
//            .putFile(editFaceImageUri!!)
//            .addOnCompleteListener { // 성공했는지 확인 리스너
//                if (it.isSuccessful) { // 성공시 -> 업로드 완료
//                    storage.reference.child(
//                        "user/face"
//                    ).child(
//                        faceImageFileName
//                    )
//                        .downloadUrl
//                        .addOnSuccessListener {
//                            Log.d(
//                                "aaaa",
//                                "얼굴사진 업로드 성공"
//                            )
//                        }
//                        .addOnFailureListener {
//                            Log.d(
//                                "aaaa",
//                                "얼굴사진 업로드 실패"
//                            )
//                        }
//                } else { // 업로드 실패
//                    Toast.makeText(
//                        this@EditProfileActivity,
//                        "얼굴사진 업로드에 실패했습니다.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        storage.reference.child("user/body").child(bodyImageFileName).delete()
//        storage.reference.child("user/body")
//            .child(bodyImageFileName)
//            .putFile(editBodyImageUri!!)
//            .addOnCompleteListener { // 성공했는지 확인 리스너
//                if (it.isSuccessful) { // 성공시 -> 업로드 완료
//                    storage.reference.child(
//                        "user/body"
//                    ).child(
//                        bodyImageFileName
//                    )
//                        .downloadUrl
//                        .addOnSuccessListener {
//                            Log.d(
//                                "aaaa",
//                                "전신사진 업로드 성공"
//                            )
//                        }
//                        .addOnFailureListener {
//                            Toast.makeText(
//                                this@EditProfileActivity,
//                                "전신사진 업로드에 실패했습니다.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                } else { // 업로드 실패
//                    Toast.makeText(
//                        this@EditProfileActivity,
//                        "전신사진 업로드에 실패했습니다.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//    }

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
                        FACE_PERMISSION_REQUEST_CODE
                    )
                } else {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        BODY_PERMISSION_REQUEST_CODE
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
                        editFaceImageUri = photoURI
                        startActivityForResult(
                            takePictureIntent,
                            FACE_CAMERA_REQUEST_CODE
                        )
                    } else {
                        editBodyImageUri = photoURI
                        startActivityForResult(
                            takePictureIntent,
                            BODY_CAMERA_REQUEST_CODE
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
                FACE_GALLERY_REQUEST_CODE
            )
        } else {
            startActivityForResult(
                intent,
                BODY_GALLERY_REQUEST_CODE
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
                        FACE_PERMISSION_REQUEST_CODE
                    )
                } else {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        BODY_PERMISSION_REQUEST_CODE
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
            FACE_PERMISSION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // 승낙된경우
                    startContentProvider(1)
                } else {
                    Toast.makeText(
                        this,
                        "권한을 거부하셨습니다. [앱 설정] -> [권한] 항목에서 허용해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            BODY_PERMISSION_REQUEST_CODE ->
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            FACE_GALLERY_REQUEST_CODE -> { //갤러리 요청일 경우 받아온 data에서 사진에 대한 uri 저장
                val uri = data?.data
                if (uri != null) {
                    ivEditUserDataFace!!.setImageURI(uri)
                    editFaceImageUri = uri // 이미지 업로드 버튼을 눌러야 저장되므로 그전까지 이 변수에 저장
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            FACE_CAMERA_REQUEST_CODE -> {
                //startActivityForResult를 통해 기본카메라 앱으로 부터 받아온 사진 결과값
                val bitmap: Bitmap
                val file = File(curPhotoPath)
                if (Build.VERSION.SDK_INT < 28) { // 안드로이드 9.0(Pie)버전보다 낮은 경우
                    bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        Uri.fromFile(file)
                    )
                    ivEditUserDataFace!!.setImageBitmap(bitmap)
                } else {
                    val decode = ImageDecoder.createSource(
                        this.contentResolver,
                        Uri.fromFile(file)
                    )
                    bitmap = ImageDecoder.decodeBitmap(decode)
                    ivEditUserDataFace!!.setImageBitmap(bitmap)
                }
            }
            BODY_GALLERY_REQUEST_CODE -> { //갤러리 요청일 경우 받아온 data에서 사진에 대한 uri 저장
                val uri = data?.data
                if (uri != null) {
                    ivEditUserDataBody!!.setImageURI(uri)
                    editBodyImageUri = uri // 이미지 업로드 버튼을 눌러야 저장되므로 그전까지 이 변수에 저장
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            BODY_CAMERA_REQUEST_CODE -> {
                //startActivityForResult를 통해 기본카메라 앱으로 부터 받아온 사진 결과값
                val bitmap: Bitmap
                val file = File(curPhotoPath)
                if (Build.VERSION.SDK_INT < 28) { // 안드로이드 9.0(Pie)버전보다 낮은 경우
                    bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        Uri.fromFile(file)
                    )
                    ivEditUserDataBody!!.setImageBitmap(bitmap)
                } else {
                    val decode = ImageDecoder.createSource(
                        this.contentResolver,
                        Uri.fromFile(file)
                    )
                    bitmap = ImageDecoder.decodeBitmap(decode)
                    ivEditUserDataBody!!.setImageBitmap(bitmap)
                }
            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val FACE_PERMISSION_REQUEST_CODE = 1000
        const val FACE_GALLERY_REQUEST_CODE = 1001
        const val FACE_CAMERA_REQUEST_CODE = 1002

        const val BODY_PERMISSION_REQUEST_CODE = 2000
        const val BODY_GALLERY_REQUEST_CODE = 2001
        const val BODY_CAMERA_REQUEST_CODE = 2002
    }



    override fun onBackPressed() {
        super.onBackPressed()
        Log.e("aaa", "backButton Click!")
//        startActivity(Intent(this@EditProfileActivity, MyPageFragment::class.java))
        finish()
    }
}