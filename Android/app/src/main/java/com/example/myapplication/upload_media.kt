//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.RecyclerView.Adapter
//import com.example.myapplication.Media
//import com.example.myapplication.Mediadapter
//import com.example.myapplication.R
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Retrofit
//import retrofit2.Response
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.POST
//
//
//interface ApiService{
//    @POST("upload/")
//    fun getVideoList(): Call<Media>
//}
//
//class upload_media: AppCompatActivity(){
//    private val retrofit = Retrofit.Builder()
//        .baseUrl("http://127.0.0.1:8000/mjpeg/")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    private val apiService = retrofit.create(ApiService::class.java)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.media_list)
//
//        val call = apiService.getVideoList()
//        call.enqueue(object : Callback<Media> {
//            override fun onResponse(call: Call<Media>, response: Response<Media>) {
//                if (response.isSuccessful && apiService!= null) {
//                    val media = response.body()
//                    val adapter = Mediadapter(media)
//                    val recyclerView = Adapter(adapter)
//                } else {
//                    TODO("Not yet implemented")
//                }
//            override fun onFailure(call: Call<List<Media>>, t: Throwable) {
//                TODO("Not yet implemented")
//            }
//            }
//        })
//    }
//}
