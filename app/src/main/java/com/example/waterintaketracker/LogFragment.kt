package com.example.waterintaketracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Screen 1: log a new entry and see the full history list. */
class LogFragment : Fragment() {

    private lateinit var dao: WaterDao
    private val adapter = WaterAdapter()

    private lateinit var amountInput: EditText
    private lateinit var notesInput: EditText
    private lateinit var saveButton: Button
    private lateinit var todayTotalText: TextView
    private lateinit var emptyText: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_log, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dao = AppDatabase.getInstance(requireContext()).waterDao()

        amountInput = view.findViewById(R.id.amountInput)
        notesInput = view.findViewById(R.id.notesInput)
        saveButton = view.findViewById(R.id.saveButton)
        todayTotalText = view.findViewById(R.id.todayTotalText)
        emptyText = view.findViewById(R.id.emptyText)
        recyclerView = view.findViewById(R.id.entriesRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        saveButton.setOnClickListener { saveEntry() }

        observeDatabase()
    }

    private fun observeDatabase() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    dao.getAllEntries().collect { entries ->
                        adapter.submitList(entries) {
                            if (entries.isNotEmpty()) recyclerView.scrollToPosition(0)
                        }
                        emptyText.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    dao.getTotalSince(DateUtils.startOfToday()).collect { total ->
                        todayTotalText.text = getString(
                            R.string.today_total_format, WaterAdapter.formatOz(total)
                        )
                    }
                }
            }
        }
    }

    private fun saveEntry() {
        val amount = amountInput.text.toString().trim().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            amountInput.error = getString(R.string.error_invalid_amount)
            return
        }
        val notes = notesInput.text.toString().trim().ifBlank { null }

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                dao.insert(WaterEntry(amountOz = amount, notes = notes))
            }
            amountInput.text.clear()
            notesInput.text.clear()
            amountInput.clearFocus()
            notesInput.clearFocus()
            hideKeyboard()
            Toast.makeText(requireContext(), R.string.entry_saved, Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(saveButton.windowToken, 0)
    }
}
