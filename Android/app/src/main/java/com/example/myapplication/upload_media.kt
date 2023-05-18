//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.RecyclerView
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
//    @POST("d")
//    fun getVideoList(): Call<List<Media>>
//}
//
//class upload_media: AppCompatActivity(){
//    private val retrofit = Retrofit.Builder()
//        .baseUrl("")
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
//        call.enqueue(object : Callback<List<Media>> {
//            override fun onResponse(call: Call<List<Media>>, response: Response<List<Media>>) {
//                if (response.isSuccessful && mediaList != null) {
//                    val mediaList = response.body()
//                    val adapter = Mediadapter(mediaList)
//                    recyclerView.adapter = adapter
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
