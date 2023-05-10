package com.example.myapplication

import android.view.View
import android.widget.TextView
import android.widget.ImageView
import android.app.DownloadManager
import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.provider.MediaStore.Images.Thumbnails
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import java.io.File


data class Media(val title: String, val url: String, val upload_date: String)


class media_adapter(private val context: Context, private val mediaList: List<Media>)
    : RecyclerView.Adapter<media_adapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(context).inflate(R.layout.media_list, parent, false)
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
    }

    // 미디어 파일 다운로드 및 저장 메소드
    private fun downloadMediaFile(url: String, holder: ViewHolder){
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Downloading media file")
        request.setDescription("Please wait")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "media.mp4")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val query = DownloadManager.Query().apply { setFilterById(downloadId) }

        GlobalScope.launch{
            var downloading = true
            while (downloading){
                val cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL){
                    downloading = false
                    val fileUri = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)))
                    val filePath = fileUri.path
                    if(filePath != null){
                        val file = File(filePath)
                        val bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND)
                        GlobalScope.launch(Dispatchers.Main){
                            holder.mediaImageView.setImageBitmap(bitmap)
                        }
                    }
                }
                cursor.close()
                delay(1000)
            }
        }
    }
}