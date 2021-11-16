package com.nanioi.closetapplication.User

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.User.utils.PathUtil
import com.nanioi.closetapplication.closetApplication
import com.nanioi.closetapplication.databinding.ActivityMainBinding
import com.nanioi.closetapplication.databinding.ActivitySignUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    private var ivSignUpFace: ImageView? = null //얼굴 사진 뷰 변수 선언
    private var ivSignUpBody: ImageView? = null //전신 사진 뷰 변수 선언
    private var btnSignUpFace: Button? = null //얼굴 사진 버튼 변수 선언
    private var btnSignUpBody: Button? = null //전신 사진 버튼 변수 선언
    private var btnSignUpPass: Button? = null //회원가입 완료 버튼 변수 선언

    private var faceImageUri: Uri? = null
    private var bodyImageUri: Uri? = null
    lateinit var faceImageFilePath: File
    lateinit var bodyImageFilePath: File

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

        ivSignUpFace = binding.ivSignUpFace
        ivSignUpBody = binding.ivSignUpBody
        btnSignUpFace = binding.btnSignUpFace
        btnSignUpBody = binding.btnSignUpBody
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
                                        if (faceImageUri != null)
                                            if (bodyImageUri != null) {
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
                                                                    //uploadImage(userUid)
                                                                    val faceImageFileName =
                                                                        userUid + "_img_face.jpg"
                                                                    val bodyImageFileName =
                                                                        userUid + "_img_body.jpg"
                                                                    storage.reference.child("user/face")
                                                                        .child(faceImageFileName)
                                                                        .putFile(faceImageUri!!)
                                                                        .addOnCompleteListener { // 성공했는지 확인 리스너
                                                                            if (it.isSuccessful) { // 성공시 -> 업로드 완료
                                                                                storage.reference.child(
                                                                                    "user/face"
                                                                                ).child(
                                                                                    faceImageFileName
                                                                                )
                                                                                    .downloadUrl
                                                                                    .addOnSuccessListener {
                                                                                        Toast.makeText(
                                                                                            this@SignUpActivity,
                                                                                            "얼굴사진 업로드 성공",
                                                                                            Toast.LENGTH_SHORT
                                                                                        ).show()
                                                                                    }
                                                                                    .addOnFailureListener {
                                                                                        Log.d(
                                                                                            "aaaa",
                                                                                            "얼굴사진 업로드 실패"
                                                                                        )
                                                                                    }
                                                                            } else { // 업로드 실패
                                                                                Toast.makeText(
                                                                                    this@SignUpActivity,
                                                                                    "얼굴사진 업로드에 실패했습니다.",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }
                                                                        }
                                                                    storage.reference.child("user/body")
                                                                        .child(bodyImageFileName)
                                                                        .putFile(bodyImageUri!!)
                                                                        .addOnCompleteListener { // 성공했는지 확인 리스너
                                                                            if (it.isSuccessful) { // 성공시 -> 업로드 완료
                                                                                storage.reference.child(
                                                                                    "user/body"
                                                                                ).child(
                                                                                    bodyImageFileName
                                                                                )
                                                                                    .downloadUrl
                                                                                    .addOnSuccessListener {
                                                                                        Toast.makeText(
                                                                                            this@SignUpActivity,
                                                                                            "전신사진 업로드 성공",
                                                                                            Toast.LENGTH_SHORT
                                                                                        ).show()
                                                                                    }
                                                                                    .addOnFailureListener {
                                                                                        Toast.makeText(
                                                                                            this@SignUpActivity,
                                                                                            "전신사진 업로드에 실패했습니다.",
                                                                                            Toast.LENGTH_SHORT
                                                                                        ).show()
                                                                                    }
                                                                            } else { // 업로드 실패
                                                                                Toast.makeText(
                                                                                    this@SignUpActivity,
                                                                                    "전신사진 업로드에 실패했습니다.",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }
                                                                        }
                                                                    val userInfo = userModel()
                                                                    userInfo.uid = userUid
                                                                    userInfo.email = user!!.email
                                                                    userInfo.name =
                                                                        etSignUpName!!.text.toString()
                                                                    userInfo.gender =
                                                                        if (rbSignUpMan?.isChecked == true) "남자" else "여자"
                                                                    userInfo.cm =
                                                                        etSignUpCm!!.text.toString()
                                                                    userInfo.kg =
                                                                        etSignUpKg!!.text.toString()
                                                                    userInfo.faceImageUri =
                                                                        faceImageUri.toString()
                                                                    userInfo.bodyImageUri =
                                                                        bodyImageUri.toString()

//                                                                    val fileBodyRequestBody =
//                                                                        RequestBody.create(
//                                                                            MediaType.parse("image/*"),
//                                                                            bodyImageFilePath
//                                                                        )
//                                                                    val userBody =
//                                                                        MultipartBody.Part.createFormData(
//                                                                            "image",
//                                                                            bodyImageFilePath.name,
//                                                                            fileBodyRequestBody
//                                                                        )
//
//                                                                    val fileFaceRequestBody =
//                                                                        RequestBody.create(
//                                                                            MediaType.parse("image/*"),
//                                                                            faceImageFilePath
//                                                                        )
//                                                                    val userFace =
//                                                                        MultipartBody.Part.createFormData(
//                                                                            "image",
//                                                                            faceImageFilePath.name,
//                                                                            fileFaceRequestBody
//                                                                        )
//                                                                    ClientThread().start()

//                                                                    (application as closetApplication).service.createAvatar(
//                                                                        userFace,
//                                                                        userBody
//                                                                    ).enqueue(
//                                                                        object : Callback<User> {
//                                                                            override fun onResponse(
//                                                                                call: Call<User>,
//                                                                                response: Response<User>
//                                                                            ) {
//                                                                                if (response.isSuccessful) {
//                                                                                    val avatar =
//                                                                                        response.body()
//                                                                                    userInfo.avatarImageUri = avatar!!.userAvatarImage
//                                                                                }
//                                                                            }
//
//                                                                            override fun onFailure(
//                                                                                call: Call<User>,
//                                                                                t: Throwable
//                                                                            ) {
//                                                                                Log.w(
//                                                                                    "aaa",
//                                                                                    "실패  : " + t.toString()
//                                                                                )
//                                                                            }
//
//                                                                        }
//                                                                    )
                                                                    userDB.reference.child(DBkey.DB_USERS)
                                                                        .child(userUid)
                                                                        .setValue(userInfo)

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
                                                    "전신 사진을 업로드 해 주세요.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                        else
                                            Toast.makeText(
                                                this@SignUpActivity,
                                                "얼굴 사진을 업로드 해 주세요.",
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

        btnSignUpFace?.setOnClickListener {
            showPictureUploadDialog(1)
        }

        btnSignUpBody?.setOnClickListener {
            showPictureUploadDialog(2)
        }
    }

    //소켓통신
    inner class ClientThread() : Thread() {
        override fun run() {
            super.run()
            Log.w("aaaaaaaaa", "clientThread")

            val host = "172.30.1.55"
            val port = 9999

            try {
                val socket = Socket(host, port)
                val outstream = DataOutputStream(socket.getOutputStream())
                outstream.writeUTF(faceImageFilePath.toString())

                outstream.flush()
                Log.w("aaaaaaaaa", "Sent to server.")

                val instream = ObjectInputStream(socket.getInputStream())
                val input: userObject = instream.readObject() as userObject
                Log.w("aaaaaaaaa", "Received data: $input")
                //todo 받은거 스타일링 탭 전송
            } catch (e: Exception) {
                e.printStackTrace()
                Log.w("aaaaaaaaa", "error" + e.toString())
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
        if(Environment.MEDIA_MOUNTED.equals(state)){
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
                            "$packageName.fileprovider",
                            it
                        )
                        Log.w ( "aaaaaaaaa", "photoURI" + photoURI.toString())
                        Log.w ( "aaaaaaaaa", "photoFile" + photoFile.toString())
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        if (imageType == 1) {
                            faceImageUri = photoURI
                            startActivityForResult(
                                takePictureIntent,
                                FACE_CAMERA_REQUEST_CODE
                            )
                        } else {
                            bodyImageUri = photoURI
                            startActivityForResult(
                                takePictureIntent,
                                BODY_CAMERA_REQUEST_CODE
                            )
                        }
                    }
                }
            }
        }else{
            Log.w ( "aaaaaaa", "저장공간에 접근 불가능")
        }
        //기본 카메라 앱 실행

    }

    //by 나연. 이미지 파일 생성 함수 (21.10.23)
    private fun createImageFile(): File? {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFile : File ? =null
        val storageDir: File? = File(Environment.getExternalStorageDirectory(),"DCIM/CameraCaptures")
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
                    ivSignUpFace!!.setImageURI(uri)
                    faceImageUri = uri // 이미지 업로드 버튼을 눌러야 저장되므로 그전까지 이 변수에 저장
                    faceImageFilePath = File(getImageFilePath(uri))
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            FACE_CAMERA_REQUEST_CODE -> {
                faceImageFilePath = File(curPhotoPath)
                ivSignUpFace!!.setImageURI(faceImageUri)
            }
            BODY_GALLERY_REQUEST_CODE -> { //갤러리 요청일 경우 받아온 data에서 사진에 대한 uri 저장
                val uri = data?.data
                if (uri != null) {
                    ivSignUpBody!!.setImageURI(uri)
                    bodyImageUri = uri // 이미지 업로드 버튼을 눌러야 저장되므로 그전까지 이 변수에 저장
                    bodyImageFilePath = File(getImageFilePath(uri))
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            BODY_CAMERA_REQUEST_CODE -> {
                bodyImageFilePath = File(curPhotoPath)
                ivSignUpBody!!.setImageURI(bodyImageUri)
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
}

