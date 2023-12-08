package com.example.revision_app.tasks

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.revision_app.application.RevisionApp
import com.example.revision_app.databinding.FragmentTaskDialogBinding
import com.example.revision_app.retrofit.revision.Goal
import com.example.revision_app.retrofit.task.CreateTask
import com.example.revision_app.retrofit.task.TaskDtoResponse
import com.example.revision_app.retrofit.task.TaskRepository
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

class TaskDialog( private var goals : List<Goal>, private var updateUI : (value: Boolean) -> Unit): DialogFragment() {

    private var _binding : FragmentTaskDialogBinding? = null;
    private val binding get() = _binding!!

    private lateinit var builder: AlertDialog.Builder

    private lateinit var dialog: Dialog

    private lateinit var repository: TaskRepository

    private var goalSelected: Goal? = null

    private var saveButton: Button? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentTaskDialogBinding.inflate(requireActivity().layoutInflater)
        builder = AlertDialog.Builder(requireContext())

        repository = (requireContext().applicationContext as RevisionApp).repositoryTask

        dialog = builder.setView(binding.root)
            .setTitle("Tarea")
            .setPositiveButton("Guardar", DialogInterface.OnClickListener { dialog, which ->
                var title = binding.tietTitle.text.toString()
                var kpi = binding.tietKPI.text.toString()
                val sharedPreferences = requireActivity().getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
                var token = sharedPreferences.getString("token", "string")!!

                if(goalSelected == null){
                    Toast.makeText(requireContext(), "Debes seleccionar una meta" , Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        lifecycleScope.launch {
                            val call : Call<TaskDtoResponse> = repository.createTask(CreateTask(title, goalSelected?._id!!, kpi), token)

                            call.enqueue(object: retrofit2.Callback<TaskDtoResponse>{
                                override fun onResponse(
                                    call: Call<TaskDtoResponse>,
                                    response: Response<TaskDtoResponse>
                                ) {
                                    Log.d("RESPONSE", "Respuesta del servidor: ${response.body()}")
                                    response.body()?.let{ taskResponse ->
                                        if(taskResponse.ok!!){
                                            updateUI(true)
                                            dialog.dismiss()
                                        }
                                    }

                                }

                                override fun onFailure(call: Call<TaskDtoResponse>, t: Throwable) {
                                    Log.d("RESPONSE", "Error: ${t.message}")
                                    updateUI(false)
                                    dialog.dismiss()
                                }

                            })
                        }

                    }catch(e: IOException){
                        e.printStackTrace()
                        Log.d("E", "Error: ${e.toString()}")
                        updateUI(false)
                    }
                }
            })
            .setNegativeButton("Cancelar"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()

        setupSpinner()
        return dialog
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        val alertDialog = dialog as AlertDialog //Lo usamos para poder emplear el método getButton (no lo tiene el dialog)
        saveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        saveButton?.isEnabled = false

        binding.tietTitle.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                saveButton?.isEnabled = validateFields()
            }
        })

        binding.tietKPI.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                saveButton?.isEnabled = validateFields()
            }

        })


    }

    private fun setupSpinner(){
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, goals.map{ it.name })

        // Especifica el diseño de la lista desplegable
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Asigna el ArrayAdapter al Spinner
        binding.rvSpinnerGoalTask.adapter = adapter

        binding.rvSpinnerGoalTask.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                goalSelected = goals[position]
                //Toast.makeText(requireContext(), "Seleccionaste: ${selectedOption.toString()}", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejar el caso en el que no se selecciona ningún elemento
            }
        }
    }

    private fun validateFields() =
        (binding.tietTitle.text.toString().isNotEmpty() && binding.tietKPI.text.toString()
            .isNotEmpty())

}