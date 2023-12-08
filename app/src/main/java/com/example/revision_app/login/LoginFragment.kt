package com.example.revision_app.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.example.revision_app.retrofit.Constants
import com.example.revision_app.retrofit.login.LoginService
import com.example.revision_app.databinding.FragmentLoginBinding
import com.example.revision_app.retrofit.login.LoginInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import android.graphics.Color
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executor
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.revision_app.HomeActivity
import com.example.revision_app.R
import com.example.revision_app.application.RevisionApp
import com.example.revision_app.retrofit.login.User
import com.example.revision_app.room.user.data.model.UserEntity
import com.example.revision_app.utils.NetworkUtil
import com.example.revision_app.workers.users.UserWorker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import retrofit2.converter.gson.GsonConverterFactory


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    private lateinit var biometricManager: BiometricManager
    private lateinit var executor: Executor

    private lateinit var localUsers: List<UserEntity>

    private var banderaLectorHuella = true //el disp. cuenta con lector
    private var textoErrorLectorHuella = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val btnRegister = binding.btnLoginRegister

        biometricManager = BiometricManager.from(requireContext())
        executor = ContextCompat.getMainExecutor(requireContext())

        if(NetworkUtil.hasInternetContection(requireContext())){
            btnRegister.setOnClickListener {
                val fragmentManager = requireActivity().supportFragmentManager

                val fragmentB = RegisterFragment()

                val transaction = fragmentManager.beginTransaction()

                transaction.replace(R.id.fragment_login, fragmentB)

                transaction.addToBackStack(null)

                transaction.commit()
            }

            setupLogin()
        } else {
            when(biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)){
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    Log.d("LOGS", "La aplicación puede autenticar usando biometría")
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    banderaLectorHuella = false
                    textoErrorLectorHuella = "El dispositivo no cuenta con lector de huella digital"
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    banderaLectorHuella = false
                    textoErrorLectorHuella = "El lector de huella no está disponible actualmente"
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    banderaLectorHuella = false
                    textoErrorLectorHuella = "Favor de asociar una huella digital al dispositivo primeramente"
                }
            }

            binding.btLogin.visibility = View.GONE
            btnRegister.visibility = View.GONE
            binding.ibtnHuella.visibility = View.VISIBLE

            var repository = (RevisionApp.appContext  as RevisionApp).repositoryUserLocal

            CoroutineScope(Dispatchers.IO).launch {
                localUsers = repository.getUsers()

                if(localUsers.isEmpty()) {
                    withContext(Dispatchers.Main){
                        message("No hay usuarios almacenados de manera local, por favor loggearse con internet")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.ibtnHuella.setOnClickListener{
                            showBiometricPrompt()
                        }
                    }
                }
            }
        }

        return root
    }

    private fun setupLogin() {
        val btnLogin = binding.btLogin

        btnLogin.setOnClickListener {
            val tvEmail = binding.etEmailLogin
            val tvPassword = binding.etLoginPassword
            val validationEmail = validateText(tvEmail, "Usuario requerido")
            val validationPassword = validateText(tvPassword, "Contraseña requerida")

            if(validationEmail && validationPassword) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val service = retrofit.create(LoginService::class.java)
                login(tvEmail.text.toString().trim(), tvPassword.text.toString().trim(), service)
            }
        }
    }

    private fun login(email: String, password: String, service: LoginService) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.VISIBLE
                }
                val result = service.loginUser(LoginInfo(email, password))
                val context: Context = requireContext()
                val sharedPreferences = context.getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("token", result.token)
                editor.apply()
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                }

                userWorker(result.usuario)

                val intent = Intent(context, HomeActivity::class.java).apply {
                    putExtra("EXTRA_USER", result.usuario)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("E", "Cayo en algun error")
                Log.e("E", e.toString())
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                }
                (e as? HttpException)?.let { checkError(e) }
            }
        }
    }

    private fun showBiometricPrompt(){
        if(biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)!= BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE){
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación")
                .setSubtitle("Ingrese usando su huella digital")
                .setNegativeButtonText("Cancelar")
                .build()

            val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    if(banderaLectorHuella){
                        message( "No se pudo autenticar")
                    }else{
                        message(textoErrorLectorHuella)
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    val fragmentManager = requireActivity().supportFragmentManager

                    val fragmentB = UserListFragment()

                    val transaction = fragmentManager.beginTransaction()

                    transaction.replace(R.id.fragment_login, fragmentB)

                    transaction.addToBackStack(null)

                    transaction.commit()

                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    //Toast.makeText(applicationContext, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })

            //Desplegando el biometric prompt
            biometricPrompt.authenticate(promptInfo)
        }else{
            message("Error al autenticar")
        }
    }


    private fun userWorker(user: User) {
        val data = workDataOf(
            "name" to user.name,
            "email_address" to user.email_address,
            "uid" to user.uid,
            "user_name" to user.user_name,
            "image_url" to user.image_url
        )

        val workRequest1 = OneTimeWorkRequestBuilder<UserWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(
                        true
                    )
                    .build()
            )
            .setInputData(data)
            .build()

        val workManager = WorkManager.getInstance(requireContext())

        workManager.beginUniqueWork("workUser", ExistingWorkPolicy.KEEP, workRequest1)
                .enqueue()

    }

    private fun validateText(textView: EditText, message: String): Boolean {
        val inputText = textView.text.toString().trim()

        if (inputText.isEmpty()) {
            textView.error = message
            return false
        }

        textView.error = null
        textView.setTextColor(Color.BLACK)
        return true

    }

    private suspend fun checkError(e: HttpException)= when(e.code()){
        400 -> {
            val context: Context = requireContext()
            Log.e("E", getString(R.string.main_error_server))
            message(getString(R.string.main_error_server))
            // Toast.makeText(context, getString(R.string.main_error_server), Toast.LENGTH_SHORT).show()
        }
        401 -> {
            val context: Context = requireContext()
            Log.e("E", getString(R.string.main_error_server))
            message("Contraseña o correo invalidos")
            // Toast.makeText(context, getString(R.string.main_error_server), Toast.LENGTH_SHORT).show()
        }
        else -> {
            val context: Context = requireContext()
            Log.e("E", getString(R.string.main_error_server))
            message(getString(R.string.main_error_server))
            //Toast.makeText(context, getString(R.string.main_error_server), Toast.LENGTH_SHORT).show()
        }
    }

    private fun message(text: String){
        Snackbar.make(binding.fmLogin, text, Snackbar.LENGTH_SHORT)
            .setTextColor(Color.parseColor("#FFFFFF"))
            .setBackgroundTint(Color.parseColor("#9E1734"))
            .show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }

}