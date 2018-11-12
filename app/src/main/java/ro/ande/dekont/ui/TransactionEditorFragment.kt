package ro.ande.dekont.ui


import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_transaction_editor.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.TransactionEditorViewModel
import ro.ande.dekont.vo.Transaction
import java.util.*
import javax.inject.Inject

/**
 * A [Fragment] for editing a [Transaction].
 *
 */
class TransactionEditorFragment : Fragment(), Injectable {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var editorViewModel: TransactionEditorViewModel

    private lateinit var onEditFinishedListener: OnTransactionEditFinishedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_editor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        editorViewModel = ViewModelProviders.of(this, mViewModelFactory).get(TransactionEditorViewModel::class.java)

        onEditFinishedListener = this.activity as OnTransactionEditFinishedListener

        // Observe date and update the date_view
        editorViewModel.date.observe(this, Observer { date -> setDateViewText(date) })

        // Setup data
        editorViewModel.setDate(LocalDate.now())
        populateCurrencySpinner()

        this.date_view.setOnClickListener {
            openDatePicker()
        }
    }

    // Populates the spinner with currencies
    private fun populateCurrencySpinner() {
        val currencies = Currency.getAvailableCurrencies().map { it.currencyCode }.sorted()

        val adapter = ArrayAdapter<String>(this.context!!, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        this.currency_spinner.adapter = adapter
    }

    // DatePicker date set callback
    private val onDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        editorViewModel.setDate(LocalDate.of(year, month, dayOfMonth))
    }

    private fun openDatePicker() {
        // Open DatePicker with the date
        editorViewModel.date.value!!.let { date ->
            val picker = DatePickerDialog(this.activity!!, R.style.DatePickerDialog, onDateSetListener, date.year, date.monthValue, date.dayOfMonth)
            picker.show()
        }
    }

    private fun setDateViewText(date: LocalDate) {
        this.date_view.text = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    /** Interface for transaction edit callback. */
    interface OnTransactionEditFinishedListener {
        fun onTransactionEditFinished(transaction: Transaction)
    }

    companion object {
        const val ARG_ACTION = "ACTION"

        const val ACTION_CREATE = 0
        const val ACTION_EDIT = 1
    }
}
