package com.github.dhaval2404.imagepicker.provider

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.ImagePickerActivity
import com.github.dhaval2404.imagepicker.util.FileUtil
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File

/**
 * Compress Selected/Captured Image
 *
 * @author Dhaval Patel
 * @version 1.0
 * @since 04 January 2019
 */
class CompressionProvider(activity: ImagePickerActivity) : BaseProvider(activity) {

    private val mMaxWidth: Int
    private val mMaxHeight: Int
    private val mMaxFileSize: Long

    private val mFileDir: File

    init {
        val bundle = activity.intent.extras ?: Bundle()

        // Get Max Width/Height parameter from Intent
        mMaxWidth = bundle.getInt(ImagePicker.EXTRA_MAX_WIDTH, 0)
        mMaxHeight = bundle.getInt(ImagePicker.EXTRA_MAX_HEIGHT, 0)

        // Get Maximum Allowed file size
        mMaxFileSize = bundle.getLong(ImagePicker.EXTRA_IMAGE_MAX_SIZE, 0)

        // Get File Directory
        val fileDir = bundle.getString(ImagePicker.EXTRA_SAVE_DIRECTORY)
        mFileDir = getFileDir(fileDir)
    }

    /**
     * Check if compression should be enabled or not
     *
     * @return Boolean. True if Compression should be enabled else false.
     */
    private fun isCompressEnabled(): Boolean {
        return mMaxFileSize > 0L
    }

    /**
     * Check if compression is required
     * @param uri Uri object to apply Compression
     */
    fun isCompressionRequired(uri: Uri): Boolean {
        val status = isCompressEnabled() && getSizeDiff(uri) > 0L
        if (!status && mMaxWidth > 0 && mMaxHeight > 0) {
            // Check image resolution
            val resolution = FileUtil.getImageResolution(this, uri)
            return resolution.first > mMaxWidth || resolution.second > mMaxHeight
        }
        return status
    }

    private fun getSizeDiff(uri: Uri): Long {
        val length = FileUtil.getImageSize(this, uri)
        return length - mMaxFileSize
    }

    /**
     * Compress given file if enabled.
     *
     * @param uri Uri to compress
     */
    fun compress(uri: Uri) {
        startCompressionWorker(uri)
    }

    /**
     * Start Compression in Background
     */
    @SuppressLint("StaticFieldLeak")
    private fun startCompressionWorker(uri: Uri) {
        Luban.with(activity).load(uri).ignoreBy(
            (mMaxFileSize / 1024L).toInt()
        ).setTargetDir("$mFileDir").setCompressListener(object : OnNewCompressListener {
            override fun onStart() {
            }

            override fun onSuccess(source: String?, compressFile: File?) {
                if (compressFile != null) {
                    handleResult(compressFile)
                } else {
                    setError(com.github.dhaval2404.imagepicker.R.string.error_failed_to_compress_image)
                }
            }

            override fun onError(source: String?, e: Throwable?) {
                setError(com.github.dhaval2404.imagepicker.R.string.error_failed_to_compress_image)
            }
        }).launch()
    }

    /**
     * This method will be called when final result fot this provider is enabled.
     */
    private fun handleResult(file: File) {
        activity.setCompressedImage(Uri.fromFile(file))
    }
}
