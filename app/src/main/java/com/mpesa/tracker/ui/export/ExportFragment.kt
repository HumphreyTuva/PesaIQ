package com.mpesa.tracker.ui.export

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.mpesa.tracker.MpesaTrackerApp
import com.mpesa.tracker.databinding.FragmentExportBinding
import com.mpesa.tracker.utils.CsvExporter
import com.mpesa.tracker.utils.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ExportFragment : Fragment() {

    private var _binding: FragmentExportBinding? = null
    private val binding get() = _binding!!
    private val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private var startMs = 0L
    private var endMs   = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ThemeManager.applyToRoot(requireContext(), binding.root)

        // Default: current month
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        startMs = cal.timeInMillis
        endMs   = System.currentTimeMillis()
        updateDateLabel()
        refreshSummary()

        binding.btnPickDates.setOnClickListener { showDateRangePicker() }
        binding.btnExportCsv.setOnClickListener { exportCsv() }
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select date range")
            .build()
        picker.addOnPositiveButtonClickListener { sel ->
            startMs = sel.first ?: startMs
            endMs   = sel.second ?: endMs
            updateDateLabel()
            refreshSummary()
        }
        picker.show(parentFragmentManager, "datePicker")
    }

    private fun updateDateLabel() {
        binding.tvDateRange.text = "${sdf.format(Date(startMs))} – ${sdf.format(Date(endMs))}"
    }

    private fun refreshSummary() {
        val repo = (requireActivity().application as MpesaTrackerApp).repository
        lifecycleScope.launch {
            val txList  = withContext(Dispatchers.IO) { repo.getTransactionsForExport(startMs, endMs) }
            val income  = withContext(Dispatchers.IO) { repo.getTotalIncome(startMs, endMs).first() }
            val expense = withContext(Dispatchers.IO) { repo.getTotalExpenses(startMs, endMs).first() }
            binding.tvTxCount.text       = txList.size.toString()
            binding.tvExportIncome.text  = "Ksh %,.0f".format(income)
            binding.tvExportExpenses.text= "Ksh %,.0f".format(expense)
        }
    }

    private fun exportCsv() {
        val repo = (requireActivity().application as MpesaTrackerApp).repository
        binding.progressExport.visibility = View.VISIBLE
        binding.btnExportCsv.isEnabled    = false

        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO) { repo.getTransactionsForExport(startMs, endMs) }
            if (transactions.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No transactions in this range", Toast.LENGTH_SHORT).show()
                    binding.progressExport.visibility = View.GONE
                    binding.btnExportCsv.isEnabled    = true
                }
                return@launch
            }

            val uri = withContext(Dispatchers.IO) { CsvExporter.export(requireContext(), transactions) }

            withContext(Dispatchers.Main) {
                binding.progressExport.visibility  = View.GONE
                binding.btnExportCsv.isEnabled     = true
                binding.tvExportSummary.text        = "✓ Exported ${transactions.size} transactions"
                binding.tvExportSummary.visibility  = View.VISIBLE
                startActivity(CsvExporter.shareIntent(uri))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
