package com.nanioi.closetapplication.mypage

import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.nanioi.closetapplication.MainActivity
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.SignInActivity
import com.nanioi.closetapplication.User.utils.ImageResizeUtils
import com.nanioi.closetapplication.User.utils.LoginUserData
import com.soundcloud.android.crop.Crop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class EditProfileActivity : AppCompatActivity() {

    private var tvEditUserDataEmail: TextView? = null //이메일 확인 텍스트 변수 선언
    private var etEditUserDataName: EditText? = null //이름 입력창 변수 선언
    private var cbEditUserDataPassword: CheckBox? = null //비밀번호 변경 여부 체크박스 변수 선언
    private var llEditUserDataPassword: LinearLayout? = null //비밀번호 변경 레이아웃 변수 선언
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

    private var firebaseAuth: FirebaseAuth? = null //파이어 베이스 인스턴스 변수 선언
    private val fbStorage = FirebaseStorage.getInstance("gs://closet-89ea8.appspot.com")
    private val fbStorageRef: StorageReference = fbStorage.reference
    private var uploadFaceFile: Uri? = null
    private var uploadBodyFile: Uri? = null
    private var uploadRef: StorageReference? = null
    private var uploadTask: UploadTask? = null

    private var imgFaceBitmap: Bitmap? = null
    private var imgBodyBitmap: Bitmap? = null

    private val REQUEST_CODE_FACE_CAMERA = 1005
    private val REQUEST_CODE_FACE_GALLARY = 1006
    private val REQUEST_CODE_BODY_CAMERA = 1007
    private val REQUEST_CODE_BODY_GALLARY = 1008
    private var tempFile: File? = null
    private var requestCodeEmt = 0
    private var isCamera = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        //TODO 준승님이 해야할 것 각각 EditText 부분에 회원정보 불러서 setText 시키기
        // 즉 클릭 전에 이미 저장된 자신의 정보들이 칸마다 정보가 불러와 져있고 화면에 그게 떠야 합니다.
        // 클릭해서 정보 입력하면 그 정보들은 그 프로필 수정 버튼이 눌려야 저장된 정보가 바뀌게 구현해주세요
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

        firebaseAuth = FirebaseAuth.getInstance().also { firebaseAuth = it } //파이어 베이스 인스턴스 생성

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
        CoroutineScope(Dispatchers.Default).launch {
            fbStorageRef.child("user/${LoginUserData.uid}/img_face.jpg").downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this@EditProfileActivity).asBitmap().load(uri)
                    .into(object : SimpleTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            ivEditUserDataFace?.setImageBitmap(resource)
                            imgFaceBitmap = resource
                        }
                    })

            }.addOnFailureListener {
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "얼굴 사진 가져오기 실패", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            fbStorageRef.child("user/${LoginUserData.uid}/img_body.jpg").downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this@EditProfileActivity).asBitmap().load(uri)
                    .into(object : SimpleTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            ivEditUserDataBody?.setImageBitmap(resource)
                            imgBodyBitmap = resource
                        }
                    })
            }.addOnFailureListener {
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "전신 사진 가져오기 실패", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        btnEditUserDataPass?.setOnClickListener {
            if (etEditUserDataName?.text!!.isNotEmpty())
                if (etEditUserDataCm?.text!!.isNotEmpty())
                    if (etEditUserDataKg?.text!!.isNotEmpty())
                        if (imgFaceBitmap != null)
                            if (imgBodyBitmap != null)
                                if (cbEditUserDataPassword?.isChecked == true)
                                    if (etEditUserDataChangePassword?.text!!.isNotEmpty())
                                        if (etEditUserDataChangePasswordCheck?.text!!.isNotEmpty())
                                            if (etEditUserDataChangePassword!!.text.toString() == etEditUserDataChangePasswordCheck?.text.toString()) {
                                                firebaseAuth?.currentUser?.updatePassword(
                                                    etEditUserDataChangePassword!!.text.toString()
                                                )
                                                    ?.addOnCompleteListener(this) {
                                                        if (it.isSuccessful) {
                                                            Toast.makeText(
                                                                this@EditProfileActivity,
                                                                "비밀번호 변경 완료",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            changeUserData()
                                                        }
                                                    }
                                            } else
                                                Toast.makeText(
                                                    this@EditProfileActivity,
                                                    "변경할 비밀번호가 일치하지 않습니다.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                        else
                                            Toast.makeText(
                                                this@EditProfileActivity,
                                                "변경할 비밀번호 확인을 입력해 주세요.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                    else
                                        Toast.makeText(
                                            this@EditProfileActivity,
                                            "변경할 비밀번호를 입력해 주세요.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                else
                                    changeUserData()
                            else
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "전신 사진을 업로드 해 주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                        else
                            Toast.makeText(
                                this@EditProfileActivity,
                                "얼굴 사진을 업로드 해 주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                    else
                        Toast.makeText(
                            this@EditProfileActivity,
                            "몸무게를 입력해 주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                else
                    Toast.makeText(this@EditProfileActivity, "키를 입력해 주세요.", Toast.LENGTH_SHORT)
                        .show()
            else
                Toast.makeText(this@EditProfileActivity, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
        }

        btnEditUserDataSignOut?.setOnClickListener {
            var signOutDialog: AlertDialog.Builder = AlertDialog.Builder(this@EditProfileActivity)
            signOutDialog.setTitle("알림")
            signOutDialog.setMessage("탈퇴하시겠습니까?")

            signOutDialog.setPositiveButton("확인") { dialog, _ ->
                firebaseAuth?.currentUser?.delete()
                    ?.addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            val databaseOut: FirebaseDatabase = FirebaseDatabase.getInstance()
                            val referenceOut: DatabaseReference = databaseOut.getReference("Users")
                            referenceOut.child(LoginUserData.uid!!).removeValue()
                                .addOnCompleteListener(this@EditProfileActivity) { task ->
                                    if (task.isSuccessful) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            fbStorageRef.child("user/${LoginUserData.uid}/img_face.jpg")
                                                .delete().addOnSuccessListener {
                                                fbStorageRef.child("user/${LoginUserData.uid}/img_body.jpg")
                                                    .delete().addOnSuccessListener {
                                                    runOnUiThread {
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
                                                }.addOnFailureListener {
                                                    runOnUiThread {
                                                        Toast.makeText(
                                                            this@EditProfileActivity,
                                                            "사진 삭제 실패",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }.addOnFailureListener {
                                                runOnUiThread {
                                                    Toast.makeText(
                                                        this@EditProfileActivity,
                                                        "사진 삭제 실패",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
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

        btnEditUserDataFace?.setOnClickListener {
            val pictureFaceValue = arrayOf("사진 촬영", "갤러리에서 가져오기")

            var pictureFaceDialog: AlertDialog.Builder =
                AlertDialog.Builder(this@EditProfileActivity)
            pictureFaceDialog.setTitle("얼굴 사진 업로드 방법")

            pictureFaceDialog.setSingleChoiceItems(pictureFaceValue, -1) { dialog, item ->
                dialog.dismiss()
                when (item) {
                    0 -> {
                        isCamera = true
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        try {
                            val timeStamp: String = SimpleDateFormat("HHmmss").format(Date())
                            val imageFileName = "Closet_" + timeStamp + "_"
                            var storageDir = File(this.getExternalFilesDir("/Closet"), "/Img")

                            if (!storageDir.exists()) storageDir.mkdirs()
                            val image = File.createTempFile(imageFileName, ".jpg", storageDir)

                            tempFile = image
                        } catch (e: IOException) {
                            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show()

                            Log.e("kdh err", e.message.toString())
                        }
                        if (tempFile != null) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                val photoUri = FileProvider.getUriForFile(
                                    this,
                                    "com.nanioi.closetapplication.fileprovider", tempFile!!
                                )
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                                startActivityForResult(intent, REQUEST_CODE_FACE_CAMERA)
                            } else {
                                val photoUri = Uri.fromFile(tempFile)
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                                startActivityForResult(intent, REQUEST_CODE_FACE_CAMERA)
                            }
                        }
                    }
                    1 -> {
                        isCamera = false
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = MediaStore.Images.Media.CONTENT_TYPE
                        startActivityForResult(intent, REQUEST_CODE_FACE_GALLARY)
                    }
                }
            }
            pictureFaceDialog.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            pictureFaceDialog.setCancelable(false)
            pictureFaceDialog.show()
        }

        btnEditUserDataBody?.setOnClickListener {
            val pictureBodyValue = arrayOf("사진 촬영", "갤러리에서 가져오기")

            var pictureBodyDialog: AlertDialog.Builder =
                AlertDialog.Builder(this@EditProfileActivity)
            pictureBodyDialog.setTitle("전신 사진 업로드 방법")

            pictureBodyDialog.setSingleChoiceItems(pictureBodyValue, -1) { dialog, item ->
                dialog.dismiss()
                when (item) {
                    0 -> {
                        isCamera = true
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        try {
                            val timeStamp: String = SimpleDateFormat("HHmmss").format(Date())
                            val imageFileName = "Closet_" + timeStamp + "_"
                            var storageDir = File(this.getExternalFilesDir("/Closet"), "/Img")
                            if (!storageDir.exists()) storageDir.mkdirs()
                            val image = File.createTempFile(imageFileName, ".jpg", storageDir)

                            tempFile = image
                        } catch (e: IOException) {
                            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                        if (tempFile != null) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                val photoUri = FileProvider.getUriForFile(
                                    this,
                                    "com.nanioi.closetapplication.fileprovider", tempFile!!
                                )
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                                startActivityForResult(intent, REQUEST_CODE_BODY_CAMERA)
                            } else {
                                val photoUri = Uri.fromFile(tempFile)
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                                startActivityForResult(intent, REQUEST_CODE_BODY_CAMERA)
                            }
                        }
                    }
                    1 -> {
                        isCamera = false
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = MediaStore.Images.Media.CONTENT_TYPE
                        startActivityForResult(intent, REQUEST_CODE_BODY_GALLARY)
                    }
                }
            }
            pictureBodyDialog.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            pictureBodyDialog.setCancelable(false)
            pictureBodyDialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_FACE_CAMERA -> {
                    requestCodeEmt = REQUEST_CODE_FACE_CAMERA
                    val photoUri = Uri.fromFile(tempFile)
                    cropImage(photoUri)
                }
                REQUEST_CODE_FACE_GALLARY -> {
                    requestCodeEmt = REQUEST_CODE_FACE_GALLARY
                    var photoUri: Uri = data?.data!!
                    var cursor: Cursor? = null

                    try {
                        val proj = arrayOf(MediaStore.Images.Media.DATA)
                        assert(photoUri != null)
                        cursor = contentResolver.query(photoUri, proj, null, null, null)
                        assert(cursor != null)
                        val column_index: Int =
                            cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        cursor.moveToFirst()
                        tempFile = File(cursor.getString(column_index))
                    } finally {
                        if (cursor != null) {
                            cursor.close()
                        }
                    }

                    photoUri = Uri.fromFile(tempFile)
                    cropImage(photoUri)
                }
                REQUEST_CODE_BODY_CAMERA -> {
                    requestCodeEmt = REQUEST_CODE_BODY_CAMERA
                    val photoUri = Uri.fromFile(tempFile)
                    cropImage(photoUri)
                }
                REQUEST_CODE_BODY_GALLARY -> {
                    requestCodeEmt = REQUEST_CODE_BODY_GALLARY
                    var photoUri: Uri = data?.data!!
                    var cursor: Cursor? = null

                    try {
                        val proj = arrayOf(MediaStore.Images.Media.DATA)
                        assert(photoUri != null)
                        cursor = contentResolver.query(photoUri, proj, null, null, null)
                        assert(cursor != null)
                        val column_index: Int =
                            cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        cursor.moveToFirst()
                        tempFile = File(cursor.getString(column_index))
                    } finally {
                        if (cursor != null) {
                            cursor.close()
                        }
                    }

                    photoUri = Uri.fromFile(tempFile)
                    cropImage(photoUri)
                }
                Crop.REQUEST_CROP -> {
                    val cropFile = File(Crop.getOutput(data).path!!)
                    when (requestCodeEmt) {
                        REQUEST_CODE_FACE_CAMERA -> {
                            ImageResizeUtils.resizeFile(cropFile, cropFile, 1280, 90, isCamera)
                            val bitmapOptions = BitmapFactory.Options()
                            val bitmapImage =
                                BitmapFactory.decodeFile(cropFile.absolutePath, bitmapOptions)

                            ivEditUserDataFace?.setImageBitmap(bitmapImage)
                            imgFaceBitmap = bitmapImage
                            uploadFaceFile = Uri.fromFile(cropFile)
                        }
                        REQUEST_CODE_FACE_GALLARY -> {
                            ImageResizeUtils.resizeFile(cropFile, cropFile, 1280, 90, isCamera)
                            val bitmapOptions = BitmapFactory.Options()
                            val bitmapImage =
                                BitmapFactory.decodeFile(cropFile.absolutePath, bitmapOptions)

                            ivEditUserDataFace?.setImageBitmap(bitmapImage)
                            imgFaceBitmap = bitmapImage
                            uploadFaceFile = Uri.fromFile(cropFile)
                        }
                        REQUEST_CODE_BODY_CAMERA -> {
                            ImageResizeUtils.resizeFile(cropFile, cropFile, 1280, 90, isCamera)
                            val bitmapOptions = BitmapFactory.Options()
                            val bitmapImage =
                                BitmapFactory.decodeFile(cropFile.absolutePath, bitmapOptions)

                            ivEditUserDataBody?.setImageBitmap(bitmapImage)
                            imgBodyBitmap = bitmapImage
                            uploadBodyFile = Uri.fromFile(cropFile)
                        }
                        REQUEST_CODE_BODY_GALLARY -> {
                            ImageResizeUtils.resizeFile(cropFile, cropFile, 1280, 90, isCamera)
                            val bitmapOptions = BitmapFactory.Options()
                            val bitmapImage =
                                BitmapFactory.decodeFile(cropFile.absolutePath, bitmapOptions)

                            ivEditUserDataBody?.setImageBitmap(bitmapImage)
                            imgBodyBitmap = bitmapImage
                            uploadBodyFile = Uri.fromFile(cropFile)
                        }
                    }
                    requestCodeEmt = 0
                }
            }
        } else {
            requestCodeEmt = 0
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            if (tempFile != null) {
                if (tempFile!!.exists()) {
                    if (tempFile!!.delete()) {
                        Log.e("ImageDelete", tempFile!!.absolutePath + " 삭제 성공");
                        tempFile = null
                    }
                }
            }
            return
        }
    }

    private fun changeUserData() {
        Toast.makeText(this@EditProfileActivity, "회원정보 수정 중...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Default).launch {
            val hashMap = HashMap<Any, String?>()

            hashMap["uid"] = LoginUserData.uid
            hashMap["email"] = tvEditUserDataEmail!!.text.toString()
            hashMap["name"] = etEditUserDataName!!.text.toString()
            hashMap["gender"] = if (rbEditUserDataMan?.isChecked == true) "남자" else "여자"
            hashMap["cm"] = etEditUserDataCm!!.text.toString()
            hashMap["kg"] = etEditUserDataKg!!.text.toString()

            LoginUserData.email = tvEditUserDataEmail!!.text.toString()
            LoginUserData.name = etEditUserDataName!!.text.toString()
            LoginUserData.gender = if (rbEditUserDataMan?.isChecked == true) "남자" else "여자"
            LoginUserData.cm = etEditUserDataCm!!.text.toString()
            LoginUserData.kg = etEditUserDataKg!!.text.toString()

            if (uploadFaceFile != null) {
                withContext(Dispatchers.IO) {
                    fbStorageRef.child("user/${LoginUserData.uid}/img_face.jpg").delete()
                        .addOnSuccessListener {
                            uploadRef = fbStorageRef.child("user/${LoginUserData.uid}/img_face.jpg")
                            uploadTask = uploadRef?.putFile(uploadFaceFile!!)
                            uploadTask!!.addOnFailureListener {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@EditProfileActivity,
                                        "얼굴사진 업로드 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.addOnSuccessListener {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@EditProfileActivity,
                                        "얼굴사진 업로드 성공",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }.addOnFailureListener {
                        runOnUiThread {
                            Toast.makeText(
                                this@EditProfileActivity,
                                "얼굴 사진 삭제 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            if (uploadBodyFile != null) {
                withContext(Dispatchers.IO) {
                    fbStorageRef.child("user/${LoginUserData.uid}/img_body.jpg").delete()
                        .addOnSuccessListener {
                            uploadRef = fbStorageRef.child("user/${LoginUserData.uid}/img_body.jpg")
                            uploadTask = uploadRef?.putFile(uploadBodyFile!!)
                            uploadTask!!.addOnFailureListener {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@EditProfileActivity,
                                        "전신사진 업로드 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.addOnSuccessListener {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@EditProfileActivity,
                                        "전신사진 업로드 성공",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }.addOnFailureListener {
                            runOnUiThread {
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "전신 사진 삭제 실패",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }

            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val reference: DatabaseReference = database.getReference("Users")
            reference.child(LoginUserData.uid!!).setValue(hashMap)
                .addOnCompleteListener(this@EditProfileActivity) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "회원정보 수정 완료", Toast.LENGTH_SHORT)
                            .show()

                        startActivity(Intent(this@EditProfileActivity, MainActivity::class.java))
                        Log.e("ang", "aldajosda")
                        finish()
                    } else
                        Toast.makeText(this@EditProfileActivity, "회원정보 수정 실패", Toast.LENGTH_SHORT)
                            .show()
                }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.e("ang", "durldlsrk")
        startActivity(Intent(this@EditProfileActivity, MainActivity::class.java))
        finish()
    }

    private fun cropImage(photoUri: Uri) {
        if (tempFile == null) {
            try {
                val timeStamp: String = SimpleDateFormat("HHmmss").format(Date())
                val imageFileName = "Closet_" + timeStamp + "_"
                val storageDir: File =
                    File(Environment.getExternalStorageDirectory().toString() + "/Closet/")
                if (!storageDir.exists()) storageDir.mkdirs()
                val image = File.createTempFile(imageFileName, ".jpg", storageDir)
                tempFile = image
            } catch (e: IOException) {
                Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                finish()
                e.printStackTrace()
            }
        }
        val savingUri = Uri.fromFile(tempFile)
        Crop.of(photoUri, savingUri).asSquare().start(this)
    }
}