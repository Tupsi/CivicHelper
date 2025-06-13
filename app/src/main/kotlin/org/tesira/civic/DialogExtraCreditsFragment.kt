package org.tesira.civic

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import org.tesira.civic.databinding.DialogCreditsBinding
import org.tesira.civic.db.Card
import org.tesira.civic.db.CardColor
import org.tesira.civic.db.CivicViewModel

/**
 * Dialog for Advances Written Record, Monument, Library
 *
 * https://developer.android.com/guide/topics/ui/dialogs
 */
class DialogExtraCreditsFragment : DialogFragment() {
    private lateinit var binding: DialogCreditsBinding
    private lateinit var items: Array<String>
    private lateinit var dialogInstance: AlertDialog
    private val mCivicViewModel: CivicViewModel by activityViewModels()
    private var initialCredits = 0
    private var blue = 0
    private var green = 0
    private var orange = 0
    private var red = 0
    private var yellow = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            this.initialCredits = requireArguments().getInt(ARG_CREDITS, 0)
            if (this.initialCredits == 0 && requireArguments().getInt(ARG_CREDITS, -1) == -1) {
                Log.e(
                    "ExtraCreditsDialog",
                    "Credits argument is missing or zero, which might be unintended."
                )
                // Hier entscheiden, ob ein Wert von 0 gültig ist oder ein Fehler.
                // Wenn 0 ungültig ist:
                // dismissAllowingStateLoss();
                // return;
            }
        } else {
            Log.e("ExtraCreditsDialog", "Arguments are null.")
            dismissAllowingStateLoss()
            return
        }

        items = when (initialCredits) {
            10 -> arrayOf("0", "5", "10")
            20 -> arrayOf("0", "5", "10", "15", "20")
            30 -> arrayOf("0", "5", "10", "15", "20", "25", "30")
            else -> {
                // Fallback, falls 'initialCredits' einen unerwarteten Wert hat
                arrayOf("0")
            }
        }
    }


    override fun onStart() {
        super.onStart()
        val currentDialog = dialog
        if (currentDialog != null && currentDialog.window != null) {
            currentDialog.window!!
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCreditsBinding.inflate(getLayoutInflater())
        blue = 0
        green = 0
        orange = 0
        red = 0
        yellow = 0

        if (items.isEmpty()) {
            Log.e("ExtraCreditsDialog", "Spinner items not initialized!")
            // Erstelle einen einfachen Fehlerdialog oder schließe diesen Dialog
            return AlertDialog.Builder(requireActivity())
                .setTitle("Error")
                .setMessage("Could not initialize dialog options.")
                .setPositiveButton(
                    android.R.string.ok,
                    DialogInterface.OnClickListener { d: DialogInterface?, w: Int -> dismiss() })
                .create()
        }


        //        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        val spinnerAdapter =
            ArrayAdapter<String?>(requireContext(), R.layout.spinner_item_right, items)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_right)

        binding.spinnerblue.adapter = spinnerAdapter
        binding.spinnerblue.onItemSelectedListener = MyOnItemSelectedListener()
        binding.spinnerblue.setSelection(0, false)

        binding.spinnerred.adapter = spinnerAdapter
        binding.spinnerred.onItemSelectedListener = MyOnItemSelectedListener()
        binding.spinnerred.setSelection(0, false)

        binding.spinnerorange.adapter = spinnerAdapter
        binding.spinnerorange.onItemSelectedListener = MyOnItemSelectedListener()
        binding.spinnerorange.setSelection(0, false)

        binding.spinneryellow.adapter = spinnerAdapter
        binding.spinneryellow.onItemSelectedListener = MyOnItemSelectedListener()
        binding.spinneryellow.setSelection(0, false)

        binding.spinnergreen.adapter = spinnerAdapter
        binding.spinnergreen.onItemSelectedListener = MyOnItemSelectedListener()
        binding.spinnergreen.setSelection(0, false)

        binding.creditsremaining.text = initialCredits.toString()
        // Aktuelle Boni aus dem ViewModel anzeigen
        binding.bonusblue.text = mCivicViewModel.blue.toString()
        binding.bonusgreen.text = mCivicViewModel.green.toString()
        binding.bonusorange.text = mCivicViewModel.orange.toString()
        binding.bonusred.text = mCivicViewModel.red.toString()
        binding.bonusyellow.text = mCivicViewModel.yellow.toString()

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(R.string.dialog_extra_credits)
        val spinnerView = binding.getRoot()
        builder.setView(spinnerView)
        builder.setPositiveButton(
            R.string.ok,
            DialogInterface.OnClickListener { dialogInterface: DialogInterface?, id: Int ->
                var name = "Extra Credits WR & Monument"
                // die Wahl der Farbe ist im Moment nicht relevant, weil der Hintergrund im ViewModel selektiert wird nach name für diese Ausnahmen
                var group1 = CardColor.YELLOW
                var group2 = CardColor.YELLOW
                when (initialCredits) {
                    10 -> {
                        name = "Written Record Extra Credits"
                        group1 = CardColor.RED
                        group2 = CardColor.GREEN
                    }

                    20 -> {
                        name = "Monument Extra Credits"
                        group1 = CardColor.ORANGE
                        group2 = CardColor.YELLOW
                    }
                }

                val card = Card(
                    name = name,
                    creditsBlue = blue,
                    creditsGreen = green,
                    creditsOrange = orange,
                    creditsRed = red,
                    creditsYellow = yellow,
                    family = 0,
                    vp = 0,
                    price = 0,
                    group1 = group1,
                    group2 = group2,
                    bonusCard = null,
                    bonus = 0,
                    isBuyable = false,
                    currentPrice = 0,
                    buyingPrice = 0,
                    hasHeart = false,
                    info = null
                )
                mCivicViewModel.insertPurchase(card.name)
                mCivicViewModel.addBonus(card)
                mCivicViewModel.requestPriceRecalculation()
                mCivicViewModel.insertCard(card)

                val result = Bundle()
                // Keine spezifischen Daten benötigt, leeres Bundle ist ok
                getParentFragmentManager().setFragmentResult(REQUEST_KEY, result)
                dismiss()
            })

        dialogInstance = builder.create() // Verwende dialogInstance
        if (dialogInstance.window != null) {
            dialogInstance.window!!.setWindowAnimations(R.style.DialogAnimation)
        }

        if (dialogInstance.isShowing) {
            updateOkButtonState()
        } else {
            dialogInstance.setOnShowListener(DialogInterface.OnShowListener { dialogInterface: DialogInterface? -> updateOkButtonState() })
        }

        if (savedInstanceState != null) {
            blue = savedInstanceState.getInt(STATE_BLUE, 0)
        }
        binding.spinnerblue.post(Runnable { binding.spinnerblue.setSelection(items.indexOf(blue.toString())) })
        return dialogInstance
    }

    internal fun updateOkButtonState() {
        val totalSelected = blue + green + orange + red + yellow
        binding.creditsremaining.text = (initialCredits - totalSelected).toString()
        dialogInstance.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
            totalSelected == initialCredits
    }

    private inner class MyOnItemSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (position >= items.size) {
                return
            }
            val selectedValue = items[position].toInt()
            val parentId = parent.id

            if (parentId == R.id.spinnerblue) {
                blue = selectedValue
            } else if (parentId == R.id.spinnergreen) {
                green = selectedValue
            } else if (parentId == R.id.spinnerorange) {
                orange = selectedValue
            } else if (parentId == R.id.spinnerred) {
                red = selectedValue
            } else if (parentId == R.id.spinneryellow) {
                yellow = selectedValue
            }
            updateOkButtonState()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_BLUE, blue)
    }

    companion object {
        private const val ARG_CREDITS = "arg_credits"
        private const val REQUEST_KEY = "extraCreditsDialogResult"
        private const val STATE_BLUE = "state_blue"

        //@JvmStatic
        fun newInstance(creditsAmount: Int): DialogExtraCreditsFragment {
            val fragment = DialogExtraCreditsFragment()
            val args = Bundle()
            args.putInt(ARG_CREDITS, creditsAmount)
            fragment.setArguments(args)
            fragment.setCancelable(false)
            fragment.setStyle(STYLE_NORMAL, R.style.Theme_CivicHelper_Dialog)
            return fragment
        }
    }
}