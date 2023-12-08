package com.example.revision_app.edit_revisions

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.revision_app.R
import com.example.revision_app.databinding.FragmentEditRevisionsBinding
import com.example.revision_app.retrofit.goal.Goal
import com.example.revision_app.retrofit.login.User
import com.example.revision_app.retrofit.revision.RevisionCreate
import com.example.revision_app.retrofit.revision.RevisionDto
import com.example.revision_app.retrofit.revision.RevisionUpdate
import com.example.revision_app.room.revision.data.model.RevisionEntity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date


class EditRevisionsFragment : Fragment(){

    private lateinit var mBinding: FragmentEditRevisionsBinding
    private var mIsEditMode: Boolean = false
    private var isOffline: Boolean = false
    private lateinit var mRevision: RevisionDto
    private lateinit var mRevisionLocal: RevisionEntity
    private var datePickerSelected: String = "left"
    private var user_id : String = ""

    private lateinit var mEditRevisionsViewModel: EditRevisionsViewModel
    private var selectedDate: String = ""
    private lateinit var goalSelected : Goal

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentEditRevisionsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mEditRevisionsViewModel = ViewModelProvider(requireActivity()).get(EditRevisionsViewModel::class.java)
        isOffline = mEditRevisionsViewModel.getIsOffline() == true

        setupView()
        setupViewModel()
    }

    fun setupView(){

        val calendar = Calendar.getInstance()

        mBinding.datePicker.init(
            calendar.get(Calendar.YEAR), // Año predeterminado
            calendar.get(Calendar.MONTH), // Mes predeterminado (enero)
            calendar.get(Calendar.DAY_OF_MONTH), // Día predeterminado (1)
            onDateChangedListener
        )

        calendar.get(Calendar.YEAR)

        mBinding.btnDateRevision.text = getActualDateFormat()

        mBinding.btnDateRevisionEnd.visibility = View.GONE
        mBinding.tilFeedback.visibility = View.GONE
        mBinding.tilRealProgress.visibility = View.GONE

        handleChangeListener(mBinding.etKPI)
        handleChangeListener(mBinding.etDescKpi)

        mBinding.btnDateRevision.setOnClickListener {
            datePickerSelected = "left"
            mBinding.datePicker.visibility = View.VISIBLE
        }

        mBinding.btnRevisionUpdate.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", "string")

            val kpi: Int =  mBinding.etKPI.text.toString().toInt()
            val desc = mBinding.etDescKpi.text.toString()

            if(mIsEditMode){

                val real_progress : Int = mBinding.etRealProgress.text.toString().toInt()
                val feedback : String = mBinding.etFeedback.text.toString()

                if(isOffline){
                    mRevisionLocal.kpi_desc = desc
                    mRevisionLocal.kpi = kpi.toString()
                    mRevisionLocal.feedback = feedback
                    mRevisionLocal.closed = false.toString()
                    mRevisionLocal.real_progress = real_progress.toString()
                    mRevisionLocal.goal_id = goalSelected._id
                    mRevisionLocal.date_evaluation = selectedDate
                    if(isSunday(selectedDate)){
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("localUpdatesRevision", true)
                        editor.apply()
                        mEditRevisionsViewModel.updateRevisionLocal(mRevisionLocal)
                    } else {
                        message("Por ahora las revisiones deben ser en domingo")
                    }
                } else {
                    var newRevision = RevisionUpdate(
                        mRevision._id,
                        selectedDate,
                        mRevision.color,
                        kpi,
                        real_progress,
                        mRevision.closed,
                        feedback,
                        desc,
                        goalSelected._id
                    )
                    mEditRevisionsViewModel.updateRevision(newRevision, token!!)
                }
            } else {
                if(selectedDate.isEmpty()){
                    message("Favor de agregar una fecha para la revision")
                } else {

                    if(isOffline) {

                        if(isSunday(selectedDate)){
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("localUpdatesRevision", true)
                            editor.apply()
                            mEditRevisionsViewModel.createRevisionLocal(user_id, RevisionCreate(selectedDate, kpi, desc, goalSelected._id))
                        }else {
                            message("Por ahora las revisiones deben ser en domingo")
                        }
                    }else {
                        mEditRevisionsViewModel.createRevision(RevisionCreate(selectedDate, kpi, desc, goalSelected._id), token!!)
                    }
                }
            }
        }

        mBinding.btnRevisionDelete.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", "string")

            if(mIsEditMode){
                if(isOffline){
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("localUpdatesRevision", true)
                    editor.apply()
                    mEditRevisionsViewModel.deleteRevisionLocal(mRevisionLocal)
                }else {
                    mEditRevisionsViewModel.deleteRevision(token!!)
                }
            }else {
                returnToActivity()
            }
        }
    }

    private val onDateChangedListener = DatePicker.OnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
        selectedDate = "$year-${monthOfYear + 1}-${formatNumberTwoDigits(dayOfMonth)}" // Formato: dd/MM/yyyy
        // Realizar alguna acción con la fecha seleccionada
        mBinding.btnDateRevision.text = selectedDate
        mBinding.datePicker.visibility = View.GONE
    }

    fun setupViewModel(){
        mEditRevisionsViewModel.getRevisionSelected().observe(viewLifecycleOwner) {
            mRevision = it
            if (it._id != "") {
                mIsEditMode = true
                setUiRevision(it)
            } else {
                mIsEditMode = false
                mBinding.btnRevisionDelete.text = "Cancelar"
                mBinding.btnRevisionUpdate.text = "Guardar"
                mBinding.btnRevisionUpdate.isEnabled = false
                mBinding.btnDateRevisionEnd.visibility = View.GONE
            }

        }

        mEditRevisionsViewModel.getUser().observe(viewLifecycleOwner) {
            user_id = it.uid
        }

        mEditRevisionsViewModel.getLocalRevisionSelected().observe(viewLifecycleOwner){
            mRevisionLocal = it
        }

        mEditRevisionsViewModel.getResult().observe(viewLifecycleOwner) { result ->
            hideKeyboard()

            when (result) {
                is String -> {
                    val keyValueMap = HashMap<String, String>()

                    keyValueMap["delete-success"] = "Revision eliminada con exito"
                    keyValueMap["delete-error"] = "Lo sentimos , no pudimos eliminar la revision"
                    keyValueMap["update-success"] = "Revision actualizada con exito"
                    keyValueMap["update-error"] = "Lo sentimos , no pudimos actualizar la revision"
                    keyValueMap["create-success"] = "Revision creada con exito"
                    keyValueMap["create-error"] = "Lo sentimos , no pudimos crear la revision"

                    message(keyValueMap[result]!!)

                    mEditRevisionsViewModel.setResult(false)
                    returnToActivity()
                }
            }
        }

        mEditRevisionsViewModel.goals.observe(requireActivity(), Observer { goals ->

            if (this.isAdded) {
                // Crea un ArrayAdapter para el Spinner
                val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, goals.map{ it.name })

                // Especifica el diseño de la lista desplegable
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                // Asigna el ArrayAdapter al Spinner
                mBinding.rvSpinnerGoal.adapter = adapter

                if(mIsEditMode) {
                    val valorPorDefecto = mRevision.goal_id?._id!!
                    val indiceValorPorDefecto = buscarIndicePorId(valorPorDefecto, goals)
                    goalSelected = goals[indiceValorPorDefecto]
                    if (indiceValorPorDefecto != -1) {
                        mBinding.rvSpinnerGoal.setSelection(indiceValorPorDefecto)
                    }
                }

                mBinding.rvSpinnerGoal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        goalSelected = goals[position]
                        //Toast.makeText(requireContext(), "Seleccionaste: ${selectedOption.toString()}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Manejar el caso en el que no se selecciona ningún elemento
                    }
                }
            }

        })
        if (isOffline){
            callGetLocalGoals()
        }else{
            callGetGoals()
        }
    }

    fun callGetLocalGoals() {
        mEditRevisionsViewModel.getUser().observe(viewLifecycleOwner) { user ->
            val user: User = user

            if (user != null) {
                mEditRevisionsViewModel.loadGoalsForUserLocal(user.uid)
            }
        }
    }

    fun callGetGoals() {

        mEditRevisionsViewModel.getUser().observe(viewLifecycleOwner) { user ->
            val user: User = user
            val sharedPreferences = requireContext().getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)

            if (user != null) {
                val token = sharedPreferences.getString("token", "string")
                if (token != null) {
                    mEditRevisionsViewModel.loadGoalsForUser(user.uid, token)
                }
            }
        }

    }

    fun setUiRevision(revision: RevisionDto){
        with(mBinding){
            etKPI.setText(revision.kpi.toString())
            etFeedback.setText(revision.feedback)
            etRealProgress.setText(revision.real_progress.toString())
            etDescKpi.setText(revision.kpi_desc)

            val datePart = revision.date_evaluation!!.substringBefore('T')

            val parts = datePart.split('-')
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]

            val calendar = Calendar.getInstance()
            selectedDate = datePart

            datePicker.init(
                parts[0].toInt(), // Año predeterminado
                parts[1].toInt(), // Mes predeterminado (enero)
                parts[2].toInt(), // Día predeterminado (1)
                onDateChangedListener
            )

            mBinding.btnDateRevision.text = datePart

            btnRevisionDelete.visibility = View.VISIBLE
            mBinding.tilFeedback.visibility = View.VISIBLE
            mBinding.tilRealProgress.visibility = View.VISIBLE

            btnRevisionDelete.text = "Borrar"
            btnRevisionUpdate.text = "Actualizar"
        }
    }

    fun returnToActivity() {

        mEditRevisionsViewModel.setShowFab(true)

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val emptyFragment = Fragment()
        fragmentTransaction.replace(R.id.container_revision, emptyFragment)
        fragmentTransaction.addToBackStack(null)

        fragmentTransaction.commit()
    }

    private fun hideKeyboard(){
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun handleChangeListener(input: TextInputEditText){
        input.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                mBinding.btnRevisionUpdate.isEnabled = validateFields()
            }
        })
    }

    private fun validateFields() =
        (mBinding.etKPI.text.toString().isNotEmpty() && mBinding.etDescKpi.text.toString()
            .isNotEmpty())

    fun isSunday(dateString: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val fecha = LocalDate.parse(dateString, formatter)
        val dayOfTheWeek = fecha.dayOfWeek
        return dayOfTheWeek == DayOfWeek.SUNDAY
    }

    private fun message(text: String){
        Snackbar.make(mBinding.lyRevisionUpdate, text, Snackbar.LENGTH_SHORT)
            .setTextColor(Color.parseColor("#FFFFFF"))
            .setBackgroundTint(Color.parseColor("#9E1734"))
            .show()
    }

    private fun getActualDateFormat(): String {
        val formatDateActual = SimpleDateFormat("yyyy-MM-dd")
        val today = Date()
        return formatDateActual.format(today)
    }

    private fun buscarIndicePorId(id: String, goals: List<Goal>): Int {
        for ((index, goal) in goals.withIndex()) {
            if (goal._id == id) {
                return index
            }
        }
        return -1 // Retorna -1 si no se encuentra el valor
    }

    fun formatNumberTwoDigits(num: Int): String {
        return String.format("%02d", num)
    }

}