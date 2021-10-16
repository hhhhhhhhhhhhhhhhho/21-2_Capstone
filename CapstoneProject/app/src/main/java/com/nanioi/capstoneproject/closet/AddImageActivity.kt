package com.nanioi.capstoneproject.closet

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.nanioi.capstoneproject.DBkey.Companion.DB_ITEM
import com.nanioi.capstoneproject.camera.CameraActivity
import com.nanioi.capstoneproject.databinding.ActivityAddImageBinding
import com.nanioi.capstoneproject.gallery.GalleryActivity

class AddImageActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAddImageBinding.inflate(layoutInflater) }

    private var imageUri: Uri? = null

    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val storage: FirebaseStorage by lazy { Firebase.storage }
    private val articleDB: DatabaseReference by lazy { Firebase.database.reference.child(DB_ITEM) }

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
            //todo 아이템 서버에 저장 코드 구현

            val userId = auth.currentUser?.uid.orEmpty()
            val category_number = itemCategory.value
            showProgress()

            if (imageUri != null) {
                val photoUri = imageUri ?: return@setOnClickListener
                uploadPhoto(photoUri,
                    successHandler = { uri ->
                        uploadItem(userId, category_number, uri)
                    },
                    errorHandler = {
                        Toast.makeText(this@AddImageActivity, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        hideProgress()
                    }
                )
            } else {
                Toast.makeText(this@AddImageActivity, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //by 나연. 이미지 업로드 함수 (21.10.17)
    private fun uploadPhoto(uri: Uri, successHandler: (Uri) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    storage.reference.child("article/photo").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri)
                        }.addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }
    private fun uploadItem(userId: String, categoryNumber: Int, uri: Uri) {
        //todo itemId 어케할건지 회의
        val model = ItemModel(userId, System.currentTimeMillis(), categoryNumber, uri)
        articleDB.push().setValue(model)

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
                    startCameraScreen()
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
            ) == PackageManager.PERMISSION_GRANTED -> {
                uploadAction()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                showPermissionContextPopup()
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    //by 나연. 카메라/갤러리 권한 동의 함수 (21.10.16)
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

    //by 나연. 카메라 실행 함수 (21.10.16)
    private fun startCameraScreen() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    //by 나연. 갤러리 실행 함수 (21.10.16)
//    private fun startGalleryScreen() {
//        startActivityForResult(
//                GalleryActivity.newIntent(this),
//                GALLERY_REQUEST_CODE
//        )
//    }

    // by 나연. 앨범에서 선택한 이미지 받아오기 함수 (21.10.16)
    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                val uri = data?.data
                if (uri != null) {
                    binding.addImageButton.visibility = View.INVISIBLE
                    binding.photoImage.setImageURI(uri)
                    imageUri = uri
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_REQUEST_CODE -> {
                //todo
            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}