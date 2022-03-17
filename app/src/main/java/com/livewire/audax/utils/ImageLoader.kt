package com.livewire.audax.utils

import androidx.annotation.DrawableRes
import android.widget.ImageView
import java.io.File

interface ImageLoader {
    fun loadImageView(url: String?, view: ImageView, signature: String? = null)
    fun loadImageFromFile(file: File?, view: ImageView): Boolean
    fun loadImageViewWithRoundedBitmap(url: String, view: ImageView)
    fun loadImageView(@DrawableRes resource: Int, view: ImageView, centerCrop: Boolean = false, completed: () -> Unit = {})

}
