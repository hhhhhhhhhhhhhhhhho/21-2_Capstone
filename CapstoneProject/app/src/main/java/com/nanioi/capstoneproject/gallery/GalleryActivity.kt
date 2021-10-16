package com.nanioi.capstoneproject.gallery

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.nanioi.capstoneproject.R
import com.nanioi.capstoneproject.databinding.ActivityGalleryBinding

class GalleryActivity : AppCompatActivity() {
    companion object {
        fun newIntent(activity: Activity) = Intent(activity, GalleryActivity::class.java)
    }
    private val binding by lazy { ActivityGalleryBinding.inflate(layoutInflater) }

//    private val adapter = GalleryPhotoListAdapter {
//        viewModel.selectPhoto(it)
//    }
//    private val viewModel by viewModels<GalleryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() = with(binding) {
//        recyclerView.adapter = adapter
//        recyclerView.addItemDecoration(GridDividerDecoration(this@GalleryActivity, R.drawable.bg_frame_gallery))
//        confirmButton.setOnClickListener {
//            viewModel.confirmCheckedPhotos()
//        }
    }
//    fun selectPhoto(galleryPhoto: GalleryPhoto) {
//        val findGalleryPhoto = photoList.find { it.id == galleryPhoto.id }
//        findGalleryPhoto?.let { photo ->
//            photoList[photoList.indexOf(photo)] =
//                photo.copy(
//                    isSelected = photo.isSelected.not()
//                )
//            setState(
//                GalleryState.Success(
//                    photoList = photoList
//                )
//            )
//        }
//    }
}