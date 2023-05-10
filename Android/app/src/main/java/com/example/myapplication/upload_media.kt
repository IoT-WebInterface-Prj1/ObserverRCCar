import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Media
import com.example.myapplication.R
import com.example.myapplication.media_adapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST


interface ApiService{
    @POST("d")
    fun getVideoList(): Call<List<Media>>
}

class upload_media: AppCompatActivity(){
    private val retrofit = Retrofit.Builder()
        .baseUrl("")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_list)

        val call = apiService.getVideoList()
        call.enqueue(object : Callback<List<Media>> {
            override fun onResponse(call: Call<List<Media>>, response: Response<List<Media>>){
                if (response.isSuccessful){
                    val mediaList = response.body()
                    val adapter = media_adapter(mediaList)
                    recyclerView.adapter = adapter
                    else{

                    }
                }
                override fun onFailure(call: Call<List<Media>>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            }
        })
    }
}
