package com.nanioi.closetapplication.User

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.utils.ImageResizeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.soundcloud.android.crop.Crop

class SignUpActivity : AppCompatActivity() {
    private var etSignUpName: EditText? = null //이름 입력창 변수 선언
    private var etSignUpEmail: EditText? = null //이메일 입력창 변수 선언
    private var etSignUpPassword: EditText? = null //비밀번호 입력창 변수 선언
    private var etSignUpPasswordCheck: EditText? = null //비밀번호 확인 입력창 변수 선언
    private var etSignUpCm: EditText? = null //키 입력창 변수 선언
    private var etSignUpKg: EditText? = null //몸무게 입력창 변수 선언
    private var rgSignUpGender: RadioGroup? = null //성별 선택 그룹 변수 선언
    private var rbSignUpMan: RadioButton? = null //남자 선택 변수 선언
    private var rbSignUpWoman: RadioButton? = null //여자 선택 변수 선언
    private var ivSignUpFace: ImageView? = null //얼굴 사진 뷰 변수 선언
    private var ivSignUpBody: ImageView? = null //전신 사진 뷰 변수 선언
    private var btnSignUpFace: Button? = null //얼굴 사진 버튼 변수 선언
    private var btnSignUpBody: Button? = null //전신 사진 버튼 변수 선언
    private var btnSignUpPass: Button? = null //회원가입 완료 버튼 변수 선언

    private var imgFaceBitmap: Bitmap? = null
    private var imgBodyBitmap: Bitmap? = null
    private var isCamera = false

    private val REQUEST_CODE_FACE_CAMERA = 1001
    private val REQUEST_CODE_FACE_GALLARY = 1002
    private val REQUEST_CODE_BODY_CAMERA = 1003
    private val REQUEST_CODE_BODY_GALLARY = 1004
    private var tempFile: File? = null
    private var requestCodeEmt = 0

    private var firebaseAuth: FirebaseAuth? = null //파이어 베이스 인스턴스 변수 선언
    private val fbStorage = FirebaseStorage.getInstance("gs://closet-89ea8.appspot.com")
    private val fbStorageRef: StorageReference = fbStorage.reference
    private var uploadFaceFile: Uri? = null
    private var uploadBodyFile: Uri? = null
    private var uploadRef: StorageReference? = null
    private var uploadTask: UploadTask? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        etSignUpName = findViewById(R.id.et_sign_up_name)
        etSignUpEmail = findViewById(R.id.et_sign_up_email)
        etSignUpPassword = findViewById(R.id.et_sign_up_password)
        etSignUpPasswordCheck = findViewById(R.id.et_sign_up_password_check)
        etSignUpCm = findViewById(R.id.et_sign_up_cm)
        etSignUpKg = findViewById(R.id.et_sign_up_kg)
        rgSignUpGender = findViewById(R.id.rg_sign_up_gender)
        rbSignUpMan = findViewById(R.id.rb_sign_up_man)
        rbSignUpWoman = findViewById(R.id.rb_sign_up_woman)
        ivSignUpFace = findViewById(R.id.iv_sign_up_face)
        ivSignUpBody = findViewById(R.id.iv_sign_up_body)
        btnSignUpFace = findViewById(R.id.btn_sign_up_face)
        btnSignUpBody = findViewById(R.id.btn_sign_up_body)
        btnSignUpPass = findViewById(R.id.btn_sign_up_pass)

        firebaseAuth = FirebaseAuth.getInstance().also { firebaseAuth = it } //파이어 베이스 인스턴스 생성

        rgSignUpGender?.check(R.id.rb_sign_up_man) //아무것도 체크가 되어있지 않으면 안되니 기본적으로 남자로 체크되어 있음

        btnSignUpPass?.setOnClickListener {
            if (etSignUpName?.text!!.isNotEmpty())
                if (etSignUpEmail?.text!!.isNotEmpty())
                    if (etSignUpPassword?.text!!.isNotEmpty())
                        if (etSignUpPasswordCheck?.text!!.isNotEmpty())
                            if (etSignUpPassword!!.text.toString() == etSignUpPasswordCheck?.text.toString())
                                if (etSignUpCm?.text!!.isNotEmpty())
                                    if (etSignUpKg?.text!!.isNotEmpty())
                                        if (imgFaceBitmap != null)
                                            if (imgBodyBitmap != null) {
                                                Toast.makeText(this@SignUpActivity, "회원가입 중...", Toast.LENGTH_SHORT).show()
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    firebaseAuth!!.createUserWithEmailAndPassword(etSignUpEmail?.text.toString(), etSignUpPassword?.text.toString()) //파이어 베이스 함수 중 회원가 함수 createUserWithEmailAndPassword 사용
                                                        .addOnCompleteListener(this@SignUpActivity) { task ->
                                                            if (task.isSuccessful) {
                                                                val user = firebaseAuth!!.currentUser
                                                                val hashMap = HashMap<Any, String?>()

                                                                assert(user != null)
                                                                hashMap["uid"] = user!!.uid
                                                                hashMap["email"] = user.email
                                                                hashMap["name"] = etSignUpName!!.text.toString()
                                                                hashMap["gender"] = if (rbSignUpMan?.isChecked == true) "남자" else "여자"
                                                                hashMap["cm"] = etSignUpCm!!.text.toString()
                                                                hashMap["kg"] = etSignUpKg!!.text.toString()

                                                                uploadRef = fbStorageRef.child("user/${user.uid}/img_face.jpg")
                                                                uploadTask = uploadRef?.putFile(uploadFaceFile!!)
                                                                uploadTask!!.addOnFailureListener {
                                                                    runOnUiThread { Toast.makeText(this@SignUpActivity, "얼굴사진 업로드 실패", Toast.LENGTH_SHORT).show() }
                                                                }.addOnSuccessListener {
                                                                    runOnUiThread { Toast.makeText(this@SignUpActivity, "얼굴사진 업로드 성공", Toast.LENGTH_SHORT).show() }
                                                                    uploadRef = fbStorageRef.child("user/${user.uid}/img_body.jpg")
                                                                    uploadTask = uploadRef?.putFile(uploadBodyFile!!)
                                                                    uploadTask!!.addOnFailureListener {
                                                                        runOnUiThread { Toast.makeText(this@SignUpActivity, "전신사진 업로드 실패", Toast.LENGTH_SHORT).show() }
                                                                    }.addOnSuccessListener {
                                                                        runOnUiThread { Toast.makeText(this@SignUpActivity, "전신사진 업로드 성공", Toast.LENGTH_SHORT).show() }
                                                                        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                                                                        val reference: DatabaseReference = database.getReference("Users")
                                                                        reference.child(user.uid).setValue(hashMap)

                                                                        runOnUiThread { Toast.makeText(this@SignUpActivity, "회원가입 완료", Toast.LENGTH_SHORT).show()
                                                                            finish()} //회원가입을 완료했다면 해당 액티비티는 필요없으니 종료
                                                                    }
                                                                }
                                                            } else {
                                                                runOnUiThread { Toast.makeText(this@SignUpActivity, "회원가입 실패", Toast.LENGTH_SHORT).show()}
                                                            }
                                                        }
                                                }
                                            } else
                                                Toast.makeText(this@SignUpActivity, "전신 사진을 업로드 해 주세요.", Toast.LENGTH_SHORT).show()
                                        else
                                            Toast.makeText(this@SignUpActivity, "얼굴 사진을 업로드 해 주세요.", Toast.LENGTH_SHORT).show()
                                    else
                                        Toast.makeText(this@SignUpActivity, "몸무게를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(this@SignUpActivity, "키를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(this@SignUpActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(this@SignUpActivity, "비밀번호 확인을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this@SignUpActivity, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@SignUpActivity, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this@SignUpActivity, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
        }

        btnSignUpFace?.setOnClickListener {
            val pictureFaceValue = arrayOf("사진 촬영", "갤러리에서 가져오기")

            var pictureFaceDialog: AlertDialog.Builder = AlertDialog.Builder(this@SignUpActivity)
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

        btnSignUpBody?.setOnClickListener {
            val pictureBodyValue = arrayOf("사진 촬영", "갤러리에서 가져오기")

            var pictureBodyDialog: AlertDialog.Builder = AlertDialog.Builder(this@SignUpActivity)
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
                        val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
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
                        val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
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
                    var cropFile = File(Crop.getOutput(data).path!!)
                    when (requestCodeEmt) {
                        REQUEST_CODE_FACE_CAMERA -> {
                            ImageResizeUtils.resizeFile(cropFile, cropFile, 1280, 90, isCamera)
                            val bitmapOptions = BitmapFactory.Options()
                            val bitmapImage = BitmapFactory.decodeFile(cropFile.absolutePath, bitmapOptions)

                            ivSignUpFace?.setImageBitmap(bitmapImage)
                            imgFaceBitmap = bitmapImage
                            uploadFaceFile = Uri.fromFile(cropFile)
                        }
                        REQUEST_CODE_FACE_GALLARY -> {
                            ImageResizeUtils.resizeFile(cropFile, cropFile, 1280, 0, isCamera)
                            val bitmapOptions = BitmapFactory.Options()
                            val bitmapImage = BitmapFactory.decodeFile(cropFile.absolutePath, bitmapOptions)

                            ivSignUpFace?.setImageBitmap(bitmapImage)
                            imgFaceBitmap = bitmapImage
                            uploadFaceFile = Uri.fromFile(cropFile)
                        }
                        REQUEST_CODE_BODY_CAMERA -> {
                            ImageResizeUtils.resizeFile(cropFile, cropFile, 1280, 90, isCamera)
                            val bitmapOptions = BitmapFactory.Options()
                            val bitmapImage = BitmapFactory.decodeFile(cropFile.absolutePath, bitmapOptions)

                            ivSignUpBody?.setImageBitmap(bitmapImage)
                            imgBodyBitmap = bitmapImage
                            uploadBodyFile = Uri.fromFile(cropFile)
                        }
                        REQUEST_CODE_BODY_GALLARY -> {
                            ImageResizeUtils.resizeFile(cropFile, cropFile, 1280, 0, isCamera)
                            val bitmapOptions = BitmapFactory.Options()
                            val bitmapImage = BitmapFactory.decodeFile(cropFile.absolutePath, bitmapOptions)

                            ivSignUpBody?.setImageBitmap(bitmapImage)
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

    private fun cropImage(photoUri: Uri) {
        if (tempFile == null) {
            try {
                val timeStamp: String = SimpleDateFormat("HHmmss").format(Date())
                val imageFileName = "Closet_" + timeStamp + "_"
                var storageDir = File(this.getExternalFilesDir("/Closet"), "/Img")
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

