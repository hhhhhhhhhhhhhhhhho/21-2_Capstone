package com.nanioi.closetapplication.User

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.util.Log
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
    private var etSignUpName: EditText? = null //이름 입력창 변수 선언
    private var etSignUpEmail: EditText? = null //이메일 입력창 변수 선언
    private var etSignUpPassword: EditText? = null //비밀번호 입력창 변수 선언
    private var etSignUpPasswordCheck: EditText? = null //비밀번호 확인 입력창 변수 선언
    private var etSignUpCm: EditText? = null //키 입력창 변수 선언
    private var etSignUpKg: EditText? = null //몸무게 입력창 변수 선언
    private var rgSignUpGender: RadioGroup? = null //성별 선택 그룹 변수 선언
    private var rbSignUpMan: RadioButton? = null //남자 선택 변수 선언
    private var rbSignUpWoman: RadioButton? = null //여자 선택 변수 선언
    private var ivSignUpBody1: ImageView? = null //얼굴 사진 뷰 변수 선언
    private var ivSignUpBody2: ImageView? = null //전신 사진 뷰 변수 선언
    private var btnSignUpBody1: Button? = null //얼굴 사진 버튼 변수 선언
    private var btnSignUpBody2: Button? = null //전신 사진 버튼 변수 선언
    private var btnSignUpPass: Button? = null //회원가입 완료 버튼 변수 선언

    private var body1ImageUri: Uri? = null
    private var body2ImageUri: Uri? = null
    private var body_front_uri: Uri? = null
    private var body_back_uri: Uri? = null
    lateinit var body1ImageFilePath: File
    lateinit var body2ImageFilePath: File
    private lateinit var curPhotoPath: String

    private val storage: FirebaseStorage by lazy { Firebase.storage }
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val userDB: FirebaseDatabase by lazy { Firebase.database }
    private val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        etSignUpName = binding.etSignUpName
        etSignUpEmail = binding.etSignUpEmail
        etSignUpPassword = binding.etSignUpPassword
        etSignUpPasswordCheck = binding.etSignUpPasswordCheck
        etSignUpCm = binding.etSignUpCm
        etSignUpKg = binding.etSignUpKg
        rgSignUpGender = binding.rgSignUpGender
        rbSignUpMan = binding.rbSignUpMan
        rbSignUpWoman = binding.rbSignUpWoman

        ivSignUpBody1 = binding.ivSignUpBody1
        ivSignUpBody2 = binding.ivSignUpBody2
        btnSignUpBody1 = binding.btnSignUpBody1
        btnSignUpBody2 = binding.btnSignUpBody2
        btnSignUpPass = binding.btnSignUpPass

        rgSignUpGender?.check(R.id.rb_sign_up_man) //아무것도 체크가 되어있지 않으면 안되니 기본적으로 남자로 체크되어 있음

        btnSignUpPass?.setOnClickListener {
            if (etSignUpName?.text!!.isNotEmpty())
                if (etSignUpEmail?.text!!.isNotEmpty())
                    if (etSignUpPassword?.text!!.isNotEmpty())
                        if (etSignUpPasswordCheck?.text!!.isNotEmpty())
                            if (etSignUpPassword!!.text.toString() == etSignUpPasswordCheck?.text.toString())
                                if (etSignUpCm?.text!!.isNotEmpty())
                                    if (etSignUpKg?.text!!.isNotEmpty())
                                        if (body1ImageUri != null)
                                            if (body2ImageUri != null) {
                                                Toast.makeText(
                                                    this@SignUpActivity,
                                                    "회원가입 중...",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    auth.createUserWithEmailAndPassword(
                                                        etSignUpEmail?.text.toString(),
                                                        etSignUpPassword?.text.toString()
                                                    ) //파이어 베이스 함수 중 회원가 함수 createUserWithEmailAndPassword 사용
                                                        .addOnCompleteListener(this@SignUpActivity) { task ->
                                                            if (task.isSuccessful) {
                                                                val user = auth.currentUser
                                                                user?.let {
                                                                    val userUid = user.uid

                                                                    val body1ImageFileName =
                                                                        userUid + "_img_body1.jpg"
                                                                    val  body2ImageFileName =
                                                                        userUid + "_img_body2.jpg"

                                                                    uploadImage(body2ImageFileName,
                                                                        body2ImageUri,2,
                                                                        successHandler = { uri ->
                                                                            body2ImageUri  = Uri.parse(uri)
                                                                        },
                                                                        errorHandler = {
                                                                            Toast.makeText(
                                                                                this@SignUpActivity,
                                                                                "전신 사진(앞) 업로드에 실패했습니다.",
                                                                                Toast.LENGTH_SHORT
                                                                            )
                                                                                .show()
                                                                        })

                                                                    uploadImage(body1ImageFileName,
                                                                        body1ImageUri,1,
                                                                        successHandler = { uri ->
                                                                            uploadUserDB(userUid, user.email,uri)
                                                                        },
                                                                        errorHandler = {
                                                                            Toast.makeText(
                                                                                this@SignUpActivity,
                                                                                "전신 사진(앞) 업로드에 실패했습니다.",
                                                                                Toast.LENGTH_SHORT
                                                                            )
                                                                                .show()
                                                                        })
                                                                }
                                                                runOnUiThread {
                                                                    Toast.makeText(
                                                                        this@SignUpActivity,
                                                                        "회원가입 완료",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    finish()
                                                                } //회원가입을 완료했다면 해당 액티비티는 필요없으니 종료
                                                            } else
                                                                runOnUiThread {
                                                                    Toast.makeText(
                                                                        this@SignUpActivity,
                                                                        "회원가입 실패",
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
                                                    "전신 사진(뒤)을 업로드 해 주세요.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                        else
                                            Toast.makeText(
                                                this@SignUpActivity,
                                                "전신 사진(앞)을 업로드 해 주세요.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                    else
                                        Toast.makeText(
                                            this@SignUpActivity,
                                            "몸무게를 입력해 주세요.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                else
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "키를 입력해 주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            else
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "비밀번호가 일치하지 않습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                        else
                            Toast.makeText(
                                this@SignUpActivity,
                                "비밀번호 확인을 입력해 주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                    else
                        Toast.makeText(
                            this@SignUpActivity,
                            "비밀번호를 입력해 주세요.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                else
                    Toast.makeText(
                        this@SignUpActivity,
                        "이메일을 입력해 주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
            else
                Toast.makeText(
                    this@SignUpActivity,
                    "이름을 입력해 주세요.",
                    Toast.LENGTH_SHORT
                ).show()
        }

        btnSignUpBody1?.setOnClickListener {
            showPictureUploadDialog(1)
        }

        btnSignUpBody2?.setOnClickListener {
            showPictureUploadDialog(2)
        }
    }

    private fun uploadUserDB(userUid: String, email: String?, uri: String) {
        val userInfo = userModel()
        userInfo.uid = userUid
        userInfo.email = email
        userInfo.name =
            etSignUpName!!.text.toString()
        userInfo.gender =
            if (rbSignUpMan?.isChecked == true) "남자" else "여자"
        userInfo.cm =
            etSignUpCm!!.text.toString()
        userInfo.kg =
            etSignUpKg!!.text.toString()
        userInfo.body_front_imageUri = uri
        userInfo.body_back_imageUri = body2ImageUri.toString()

        userDB.reference.child(DBkey.DB_USERS)
            .child(userUid)
            .setValue(userInfo)
            .addOnCompleteListener {
                Log.w(
                    "aaaaaaaaa",
                    "user정보 업로드 성공! "
                )
            }.addOnFailureListener {
                Log.w(
                    "aaaaaaaaa",
                    "user정보 업로드 실패! " + it.toString()
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

    //by나연. 이미지 파일 절대경로 알아내기 (21.11.14)
    fun getImageFilePath(contentUri: Uri): String {
        var columnIndex = 0
        val projection = arrayOf(MediaStore.Images.Media.DATA) // 걸러내기
        val cursor = contentResolver.query(contentUri, projection, null, null, null)
        // list index 가르키기 , content 관리하는 resolver에 검색(query) 부탁
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        return cursor.getString(columnIndex)
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
                Toast.makeText(this@SignUpActivity, "권한이 허용 되었습니다.", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { // 설정해놓은 위험권한들 중 거부한 경우 이곳 수행
                Toast.makeText(this@SignUpActivity, "권한이 거부 되었습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        TedPermission.with(this@SignUpActivity)
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
                        Log.w("aaaaaaaaa", "photoURI : " + photoURI.toString())
                        Log.w("aaaaaaaaa", "photoFile : " + photoFile.toString())
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
            Log.w("aaaaaaa", "저장공간에 접근 불가능")
        }
        //기본 카메라 앱 실행

    }

    //by 나연. 이미지 파일 생성 함수 (21.10.23)
    private fun createImageFile(): File? {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFile: File? = null
        val storageDir: File? =
            File(Environment.getExternalStorageDirectory(), "DCIM/CameraCaptures")
        if (!storageDir!!.exists()) {
            storageDir!!.mkdirs()
        }
        imageFile = File(storageDir, timestamp)
        curPhotoPath = imageFile.getAbsolutePath()

        return imageFile
    }

    // by 나연. 앨범에서 선택한 이미지 받아오기 함수 (21.10.16)
    private fun startContentProvider(imageType: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI) // 외부저장소로 가겠다.
        intent.setType("image/*") // 이미지만 보여지게
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            BODY1_GALLERY_REQUEST_CODE -> { //갤러리 요청일 경우 받아온 data에서 사진에 대한 uri 저장
                val uri = data?.data
                if (uri != null) {
                    ivSignUpBody1!!.setImageURI(uri)
                    body1ImageUri = uri // 이미지 업로드 버튼을 눌러야 저장되므로 그전까지 이 변수에 저장
                    body1ImageFilePath = File(getImageFilePath(uri))
                    Log.w("aaaaaaaaa", "body1ImageUri : " + body1ImageUri.toString())
                    Log.w("aaaaaaaaa", "body1ImageFilePath : " + body1ImageFilePath.toString())
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            BODY1_CAMERA_REQUEST_CODE -> {
                body1ImageFilePath = File(curPhotoPath)
                ivSignUpBody1!!.setImageURI(body1ImageUri)
            }
            BODY2_GALLERY_REQUEST_CODE -> { //갤러리 요청일 경우 받아온 data에서 사진에 대한 uri 저장
                val uri = data?.data
                if (uri != null) {
                    ivSignUpBody2!!.setImageURI(uri)
                    body2ImageUri = uri // 이미지 업로드 버튼을 눌러야 저장되므로 그전까지 이 변수에 저장
                    body2ImageFilePath = File(getImageFilePath(uri))
                    Log.w("aaaaaaaaa", "body2ImageUri : " + body2ImageUri.toString())
                    Log.w("aaaaaaaaa", "body2ImageFilePath : " + body2ImageFilePath.toString())
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            BODY2_CAMERA_REQUEST_CODE -> {
                body2ImageFilePath = File(curPhotoPath)
                ivSignUpBody2!!.setImageURI(body2ImageUri)
            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
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

