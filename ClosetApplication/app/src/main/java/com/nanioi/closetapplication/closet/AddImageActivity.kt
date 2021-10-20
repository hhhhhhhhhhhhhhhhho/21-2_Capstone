package com.nanioi.closetapplication.closet

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.nanioi.closetapplication.DBkey.Companion.DB_ITEM
import com.nanioi.closetapplication.databinding.ActivityAddImageBinding
import java.io.File

class AddImageActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAddImageBinding.inflate(layoutInflater) }

    private var imageUri: Uri? = null
    private lateinit var photoFile:File
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val storage: FirebaseStorage by lazy { Firebase.storage }
    private val itemDB: DatabaseReference by lazy { Firebase.database.reference.child(DB_ITEM) }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1000
        const val GALLERY_REQUEST_CODE = 1001
        const val CAMERA_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() = with(binding) {
        //by 나연. 카테고리 numberPicker 세팅 (21.10.16)
        val Kategorie = arrayOf("Top", "Pants", "Accessory", "Shoes")

        itemCategory.minValue = 0
        itemCategory.maxValue = Kategorie.size - 1
        itemCategory.displayedValues = Kategorie

        addImageButton.visibility = View.VISIBLE
        //by 나연. 이미지 부분 버튼 클릭 시 이미지 업로드 함수 (21.10.16)
        addImageButton.setOnClickListener {
            // todo 갤러리, 사진 선택 이미지 없로드 코드
            showPictureUploadDialog()

        }

        //by 나연. 이미지 부분 버튼 클릭 시 이미지 업로드 함수 (21.10.16)
        itemUploadButton.setOnClickListener {
            val userId = auth.currentUser?.uid.orEmpty()
            val category_number = itemCategory.value
            showProgress()

            if (imageUri != null) {
                val photoUri = imageUri ?: return@setOnClickListener // 널이면 리턴, if문으로 널처리 했지만 한번 더 check
                // 비동기
                uploadPhoto(photoUri,
                    successHandler = { uri ->
                        uploadItem(userId, category_number, uri)
                    },
                    errorHandler = {
                        Toast.makeText(this@AddImageActivity, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        hideProgress()
                    }
                )
            } else { // 동기
                Toast.makeText(this@AddImageActivity, "이미지를 추가해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //by 나연. 이미지 업로드 함수 (21.10.17)
    //storage 사용
    private fun uploadPhoto(uri:Uri,successHandler:(String)->Unit,errorHandler :()->Unit){
        val fileName = "${System.currentTimeMillis()}.jpg"
        storage.reference.child("item/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener{ // 성공했는지 확인 리스너
                if(it.isSuccessful){ // 성공시 -> 업로드 완료
                    storage.reference.child("item/photo").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri->
                            successHandler(uri.toString()) // downloadUrl을 잘 가져온 경우
                        }.addOnFailureListener {
                            errorHandler()
                        }
                }else{ // 업로드 실패
                    errorHandler()
                }
            }
    }

    private fun uploadItem(userId: String, categoryNumber: Int, uri: String) {
        //todo itemId 어케할건지 회의
        val model = ItemModel(userId, System.currentTimeMillis(), categoryNumber, uri,false)
        itemDB.push().setValue(model) // DB에 모델을 넣어줌

        hideProgress()
        finish()
    }

    //by 나연. 업로드 중임을 알리는 프로그래스바 설정 함수  (21.10.16)
    private fun showProgress() {
        binding.progressBar.isVisible = true
    }

    private fun hideProgress() {
        binding.progressBar.isVisible = false
    }

    //by 나연. 사진 첨부할 방식 선택 함수 (21.10.16)
    private fun showPictureUploadDialog() {
        AlertDialog.Builder(this)
            .setTitle("사진첨부")
            .setMessage("사진첨부할 방식을 선택하세요")
            .setPositiveButton("카메라") { _, _ ->
                checkExternalStoragePermission {
                    startCameraCapture()
                }
            }
            .setNegativeButton("갤러리") { _, _ ->
                checkExternalStoragePermission {
                    //startGalleryScreen()
                    startContentProvider()
                }
            }
            .create()
            .show()
    }

    //by 나연. 카메라/갤러리 권한 확인 함수 (21.10.16)
    private fun checkExternalStoragePermission(uploadAction: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> { // 허용된경우
                uploadAction()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> { // 교육용 팝업이 필요한경우
                showPermissionContextPopup()
            }
            else -> { // 그 외 해당권한 요청
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    //by 나연. 카메라 실행 함수 (21.10.16)
    private fun startCameraCapture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager)!=null){
            val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("photo_",".jpg",dir)
            val uri = FileProvider.getUriForFile(this,"$packageName.fileprovider",file) // file ->uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
            photoFile = file
            imageUri = uri
        }
    }

    // by 나연. 앨범에서 선택한 이미지 받아오기 함수 (21.10.16)
    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" // 이미지타입만 가져오도록
        startActivityForResult(intent, GALLERY_REQUEST_CODE) // 이미지 가져온 것에 대해 데이터 받아오기
    }

    //by 나연. 카메라/갤러리 권한 동의 교육용 팝업 구현 함수 (21.10.16)
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 가져오기 위해 필요합니다.")
            .setPositiveButton("동의") { _, _ ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
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

        when(requestCode){
            PERMISSION_REQUEST_CODE->
                if(grantResults.isNotEmpty()&& grantResults[0] == PackageManager.PERMISSION_GRANTED){ // 승낙된경우
                    startContentProvider()
                }else{
                    Toast.makeText(this,"권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            GALLERY_REQUEST_CODE -> { //갤러리 요청일 경우 받아온 data에서 사진에 대한 uri 저장
                val uri = data?.data
                if (uri != null) {
                    binding.addImageButton.visibility = View.INVISIBLE
                    binding.photoImage.setImageURI(uri)
                    imageUri = uri // 이미지 업로드 버튼을 눌러야 저장되므로 그전까지 이 변수에 저장
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_REQUEST_CODE -> {
                //todo
                val uri = data?.data
                if (uri != null) {
                    binding.addImageButton.visibility = View.INVISIBLE
                    binding.photoImage.setImageURI(uri)
                    //Glide.with(this).load(photoFile).into(binding.photoImage)
                    imageUri = uri
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}