package com.example.myapplication

import android.view.View
import android.widget.TextView
import android.widget.ImageView
import android.app.DownloadManager
import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri

import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import java.io.File




data class Media(val title: String, val url: String, val upload_date: String)

class media_adapter(private val mediaList: List<Media>)
    : RecyclerView.Adapter<media_adapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.media_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val media = mediaList[position]
        holder.titleTextView.text = media.title
        // 미디어 파일 다운로드 및 저장
        downloadMediaFile(mediaList[position].url, holder)
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val mediaImageView: ImageView = itemView.findViewById(R.id.mediaImageView)
        val context: Context = itemView.context
    }

    // 미디어 파일 다운로드 및 저장 메소드
    private fun downloadMediaFile(url: String, holder: ViewHolder){
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Downloading media file")
        request.setDescription("Please wait")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(holder.context, Environment.DIRECTORY_DOWNLOADS, "${holder.itemId}.mp4")

        val downloadManager = holder.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val query = DownloadManager.Query().apply { setFilterById(downloadId) }

        GlobalScope.launch{
            var downloading = true
            while (downloading){
                val cursor = downloadManager.query(query)
                cursor.moveToFirst()
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))

                when(status){
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        downloading = false
                        val fileUri =
                            Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)))
                        val filePath = fileUri.path
                        if (filePath != null) {
                            val file = File(filePath)
                            val bitmap = ThumbnailUtils.createVideoThumbnail(
                                filePath,
                                MediaStore.Images.Thumbnails.MINI_KIND
                            )
                            GlobalScope.launch(Dispatchers.Main) {
                                holder.mediaImageView.setImageBitmap(bitmap)
                            }
                        }
                    }
                    DownloadManager.STATUS_FAILED ->{
                        downloading = false
                        // 에러처리를 수행하세요
                        val errorReason = getDownloadErrorReason(reason)
                        Log.e("Download Error", "Download failed: $errorReason")
                    }
                }
                cursor.close()
                delay(1000)
            }
        }
    }
}

private fun getDownloadErrorReason(reason: Int): String {
    return when (reason) {
        DownloadManager.ERROR_CANNOT_RESUME -> "ERROR_CANNOT_RESUME"
        DownloadManager.ERROR_DEVICE_NOT_FOUND -> "ERROR_DEVICE_NOT_FOUND"
        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "ERROR_FILE_ALREADY_EXISTS"
        DownloadManager.ERROR_FILE_ERROR -> "ERROR_FILE_ERROR"
        DownloadManager.ERROR_HTTP_DATA_ERROR -> "ERROR_HTTP_DATA_ERROR"
        DownloadManager.ERROR_INSUFFICIENT_SPACE -> "ERROR_INSUFFICIENT_SPACE"
        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "ERROR_TOO_MANY_REDIRECTS"
        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "ERROR_UNHANDLED_HTTP_CODE"
        DownloadManager.ERROR_UNKNOWN -> "ERROR_UNKNOWN"
        else -> "Unknown Error"
    }
}