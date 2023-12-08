package com.example.revision_app.edit_goals

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.example.revision_app.R
import com.example.revision_app.databinding.FragmentEditGoalsBinding
import com.example.revision_app.retrofit.goal.Goal
import com.example.revision_app.retrofit.goal.GoalInfo
import com.example.revision_app.room.goal.data.model.GoalEntity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class EditGoalsFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private val PICK_IMAGE_REQUEST = 1

    private var binary : String = ""
    private var user_id : String = ""
    private var mIsEditMode: Boolean = false
    private var isOffline: Boolean = false

    private lateinit var mGoal: Goal
    private lateinit var mGoalLocal: GoalEntity

    private lateinit var mBinding: FragmentEditGoalsBinding
    private lateinit var mEditGoalsViewModel: EditGoalsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mEditGoalsViewModel = ViewModelProvider(requireActivity()).get(EditGoalsViewModel::class.java)

        isOffline = mEditGoalsViewModel.getIsOffline() == true

        Log.d("OFLINE", isOffline.toString())

        mBinding = FragmentEditGoalsBinding.inflate(inflater, container, false)

        setupForm()
        setupViewModel()
        return mBinding.root
    }

    fun setupViewModel(){
        mEditGoalsViewModel.getGoalSelected().observe(viewLifecycleOwner) {
            mGoal = it
            if (it._id != "") {
                mIsEditMode = true
                setUiGoals(it)
            } else {
                mIsEditMode = false
                mBinding.btDelete.visibility = View.GONE
                mBinding.btGoals.text = "Guardar"
            }

        }

        mEditGoalsViewModel.getUserId().observe(viewLifecycleOwner) {
            user_id = it
        }

        mEditGoalsViewModel.getLocalGoalSelected().observe(viewLifecycleOwner) {
            mGoalLocal = it
        }

        mEditGoalsViewModel.getResult().observe(viewLifecycleOwner) { result ->
            hideKeyboard()

            when (result) {
                is String -> {
                    val keyValueMap = HashMap<String, String>()

                    keyValueMap["delete-success"] = "Meta eliminada con exito"
                    keyValueMap["delete-error"] = "Lo sentimos , no pudimos eliminar la meta"
                    keyValueMap["update-success"] = "Meta actualizada con exito"
                    keyValueMap["update-error"] = "Lo sentimos , no pudimos actualizar la meta"
                    keyValueMap["create-success"] = "Meta creada con exito"
                    keyValueMap["create-error"] = "Lo sentimos , no pudimos creat la meta"

                    message(keyValueMap[result]!!)

                    mEditGoalsViewModel.setResult(false)
                    returnToActivity()
                }
            }
        }
    }

    fun returnToActivity() {

        mEditGoalsViewModel.setShowFab(true)

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val emptyFragment = Fragment()
        fragmentTransaction.replace(R.id.containerMain, emptyFragment)
        fragmentTransaction.addToBackStack(null)

        fragmentTransaction.commit()
    }

    fun setUiGoals(goal: Goal){
        with(mBinding){
            etName.setText(goal.name)
            etAim.setText(goal.aim)
            etDesc.setText(goal.description)

            Picasso.get()
                .load(goal.image_url)
                .error(R.drawable.ic_people)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imagePreview)


            btDelete.visibility = View.VISIBLE
            btGoals.text = "Actualizar"
        }
    }

    fun setupForm() {
        imagePreview = mBinding.imagePreview
        val btnPickImage: Button = mBinding.btnPickImage

        handleChangeListener(mBinding.etAim)
        handleChangeListener(mBinding.etName)
        handleChangeListener(mBinding.etDesc)

        btnPickImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        mBinding.btGoals.setOnClickListener {

            val sharedPreferences = requireActivity().getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", "string")

            val name = mBinding.etName.text.toString()
            val desc = mBinding.etDesc.text.toString()
            val aim = mBinding.etAim.text.toString()

            if(mIsEditMode){

                if(isOffline) {
                    mGoalLocal.name = name
                    mGoalLocal.aim = aim
                    mGoalLocal.description = desc

                    val editor = sharedPreferences.edit()
                    editor.putBoolean("localUpdates", true)
                    editor.apply()

                    mEditGoalsViewModel.updateGoalLocal(mGoalLocal)

                } else {
                    mGoal.name = name
                    mGoal.aim = aim
                    mGoal.description = desc
                    mGoal.file = binary
                    mEditGoalsViewModel.updateGoal(token!!, mGoal)
                }

            } else {

                if(isOffline) {
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("localUpdates", true)
                    editor.apply()
                    mEditGoalsViewModel.createGoalLocal(user_id,  GoalInfo(name, desc , binary, aim))
                } else {
                    if(binary.isEmpty()){
                        message("Favor de agregar una imagen para la meta")
                    } else {
                        mEditGoalsViewModel.createGoal(token!!, GoalInfo(name, desc , binary, aim))
                    }
                }

            }

        }

        mBinding.btDelete.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", "string")
            if(isOffline) {
                val editor = sharedPreferences.edit()
                editor.putBoolean("localUpdates", true)
                editor.apply()
                mEditGoalsViewModel.deleteGoalLocal(mGoalLocal)
            } else {
                mEditGoalsViewModel.deleteGoal(token!!, mGoal)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val selectedImageUri: Uri? = data?.data
                    selectedImageUri?.let {
                        val activity = requireActivity()
                        val contentResolver = activity.contentResolver
                        val bitmap = if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                        } else {
                            val source = ImageDecoder.createSource(contentResolver, selectedImageUri)
                            ImageDecoder.decodeBitmap(source)
                        }
                        imagePreview.setImageBitmap(bitmap)
                        binary = bitmapToBase64(bitmap)
                    }
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    private fun handleChangeListener(input: TextInputEditText){
        input.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                mBinding.btGoals?.isEnabled = validateFields()
            }
        })
    }

    private fun validateFields() =
        (mBinding.etName.text.toString().isNotEmpty() && mBinding.etAim.text.toString()
            .isNotEmpty() && mBinding.etDesc.text.toString().isNotEmpty())


    private fun message(text: String){
        Snackbar.make(mBinding.nsv, text, Snackbar.LENGTH_SHORT)
            .setTextColor(Color.parseColor("#FFFFFF"))
            .setBackgroundTint(Color.parseColor("#9E1734"))
            .show()
    }

    private fun hideKeyboard(){
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}