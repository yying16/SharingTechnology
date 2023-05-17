package com.sharingtechnology.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object BlobConverter {

    // 针对头像: 实现 Bitmap 和 Blob 之间的转化
    fun avatarEncoder(bm: Bitmap?): ByteArray {
        val os = ByteArrayOutputStream()
        bm?.compress(Bitmap.CompressFormat.PNG, 30, os)
        return os.toByteArray()
    }
    fun avatarDecoder(ba: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(ba, 0, ba.size)
    }


    // 针对词云

    // 针对指数表
}