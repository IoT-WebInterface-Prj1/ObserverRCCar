import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Media
import com.example.myapplication.MediaAdapter
import com.example.myapplication.R
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

        val recyclerView = findViewById<RecyclerView>(R.id.RecyclerView)

        val call = apiService.getVideoList()
        call.enqueue(object : Callback<List<Media>> {
            override fun onResponse(call: Call<List<Media>>, response: Response<List<Media>>){
                if (response.isSuccessful){
                    val mediaList = response.body()
                    if(mediaList != null) {
                        recyclerView.adapter = MediaAdapter(mediaList)
                    }else{
                        showToast("Empty media list")
                    }
                }else{
                    showToast("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Media>>, t: Throwable) {
                showToast("Network requset failed")
            }
        })
    }

    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
