package com.example.revision_app.login

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.example.revision_app.HomeActivity
import com.example.revision_app.R
import com.example.revision_app.databinding.FragmentRegisterBinding
import com.example.revision_app.retrofit.Constants
import com.example.revision_app.retrofit.login.UserInfo
import com.example.revision_app.retrofit.login.UserService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var binding : FragmentRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)
        setUpRegister()
        checkTerms()
    }

    private fun checkTerms () {
        val terms =  binding.cbTerms
        val btnRegister = binding.btRegister

        terms.setOnCheckedChangeListener { view, isChecked ->
            btnRegister.isEnabled = isChecked
        }
    }

    private fun setUpRegister() {
        val btnRegister = binding.btRegister

        btnRegister.setOnClickListener {

            val etName = binding.etName
            val etEmail = binding.etEmail
            val etPassword = binding.etPassword

            val validationName = validateText(etName, "Nombre requerido", false)
            val validationEmail = validateText(etEmail, "Usuario requerido", false)
            val validationPassword = validateText(etPassword, "Contraseña requerida", false)

            if(validationName && validationEmail && validationPassword) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val service = retrofit.create(UserService::class.java)
                register(etName.text.toString().trim(), etEmail.text.toString().trim(), etPassword.text.toString().trim(), service)
            }

        }
    }

    private fun register(name: String, email: String, password: String, service: UserService) {
        lifecycleScope.launch (Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.progressBarRegister.visibility = View.VISIBLE
                }
                val result = service.createUser(UserInfo(name, email, password))
                Log.e("T", result.newUser.toString())

                if(result.ok){
                    val context: Context = requireContext()
                    val sharedPreferences = context.getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("token", result.token)
                    editor.apply()

                    val intent = Intent(context, HomeActivity::class.java).apply {
                        putExtra("EXTRA_USER", result.newUser)
                    }
                    startActivity(intent)
                } else  {
                    withContext(Dispatchers.Main) {
                        message("Lo sentimos no pudimos crear tu usuario")
                    }
                }

                withContext(Dispatchers.Main) {
                    binding.progressBarRegister.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                Log.e("E", "Cayo en algun error al crear al usuario")
                Log.e("E", e.toString())
                withContext(Dispatchers.Main) {
                    binding.progressBarRegister.visibility = View.VISIBLE
                    message("Lo sentimos no pudimos crear tu usuario")
                }
                (e as? HttpException)?.let { checkError(e) }
            }
        }
    }

    private fun validateText(textView: EditText, message: String, isEmail: Boolean): Boolean{
        val inputText = textView.text.toString().trim()
        val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+") // Expresión regular para validar el formato del correo electrónico

        if (inputText.isEmpty()) {
            textView.error = message
            return false
        } else if(isEmail && !inputText.matches(emailPattern)){
            textView.error = message
            return false
        }else {
            textView.error = null
            textView.setTextColor(Color.BLACK)
            return true
        }
    }

    private suspend fun checkError(e: HttpException)= when(e.code()){
        400 -> {
            val context: Context = requireContext()
            Log.e("E", getString(R.string.main_error_server))
            // Toast.makeText(context, getString(R.string.main_error_server), Toast.LENGTH_SHORT).show()
        }
        else -> {
            val context: Context = requireContext()
            Log.e("E", getString(R.string.main_error_server))
            //Toast.makeText(context, getString(R.string.main_error_server), Toast.LENGTH_SHORT).show()
        }
    }

    private fun message(text: String){
        Snackbar.make(binding.fmRegister, text, Snackbar.LENGTH_SHORT)
            .setTextColor(Color.parseColor("#FFFFFF"))
            .setBackgroundTint(Color.parseColor("#9E1734"))
            .show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = RegisterFragment()
    }

}