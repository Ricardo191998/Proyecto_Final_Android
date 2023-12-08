package com.example.revision_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.revision_app.login.LoginActivity
import com.example.revision_app.retrofit.Constants
import com.example.revision_app.retrofit.login.LoginService
import com.example.revision_app.utils.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        context= this

        if(NetworkUtil.hasInternetContection(context)){
            validateLogin()
        } else {
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateLogin() {

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(LoginService::class.java)

        val sharedPreferences = this.getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)

        val token = sharedPreferences.getString("token", null)

        if(token != null){

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val result = service.auth(token)
                    val context: Context = context
                    val sharedPreferences = context.getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("token", result.token)
                    editor.apply()
                    val intent = Intent(context, HomeActivity::class.java).apply {
                        putExtra("EXTRA_USER", result.usuario)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("E", "Cayo en algun error")
                    Log.e("E", e.toString())
                    (e as? HttpException)?.let { checkError(e) }
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private suspend fun checkError(e: HttpException)= when(e.code()){
        400 -> {
            val context: Context = context
            Log.e("E", getString(R.string.main_error_server))
            // Toast.makeText(context, getString(R.string.main_error_server), Toast.LENGTH_SHORT).show()
        }
        else -> {
            val context: Context = context
            Log.e("E", getString(R.string.main_error_server))
            //Toast.makeText(context, getString(R.string.main_error_server), Toast.LENGTH_SHORT).show()
        }
    }
}
