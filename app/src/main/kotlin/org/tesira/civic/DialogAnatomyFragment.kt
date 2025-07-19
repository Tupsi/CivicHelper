package org.tesira.civic

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import org.tesira.civic.db.CivicViewModel

/**
 * Dialog for the extra card when you buy Anatomy
 * Call the ExtraCreditsDialog if one chooses Written Record
 * https://developer.android.com/guide/topics/ui/dialogs
 */
class DialogAnatomyFragment : DialogFragment() {
    private val mCivicViewModel: CivicViewModel by activityViewModels()
    private lateinit var greenCardsArray: Array<String>

    override fun onStart() {
        super.onStart()
        val dialog = getDialog()
        if (dialog != null && dialog.window != null) {
            dialog.window!!
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            val greenCardsList = requireArguments().getStringArrayList(ARG_GREEN_CARDS_LIST)
            if (greenCardsList != null && !greenCardsList.isEmpty()) {
                this.greenCardsArray = greenCardsList.toTypedArray<String>()
            } else {
                Log.d("AnatomyDialogFragment", "Green cards list is null or empty in arguments.")
                dismissAllowingStateLoss()
            }
        } else {
            Log.d("AnatomyDialogFragment", "Arguments are null.")
            dismissAllowingStateLoss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(R.string.dialog_anatomy)
        builder.setItems(
            greenCardsArray,
            DialogInterface.OnClickListener { dialogInterface: DialogInterface?, which: Int ->
                val selectedGreenCard = greenCardsArray[which]
                mCivicViewModel.onAnatomyCardSelected(selectedGreenCard)
                mCivicViewModel.insertPurchase(selectedGreenCard)
                mCivicViewModel.addBonus(selectedGreenCard)
                mCivicViewModel.requestPriceRecalculation()

                val result = Bundle()
                result.putString("selected_card_name", selectedGreenCard)
                getParentFragmentManager().setFragmentResult(REQUEST_KEY, result)
                dismiss()
            })

        val dialog: Dialog = builder.create()
        if (dialog.window != null) {
//            dialog.window!!.setWindowAnimations(R.style.DialogAnimation)
        }
        return dialog
    }

    companion object {
        private const val ARG_GREEN_CARDS_LIST = "arg_green_cards_list"
        private const val REQUEST_KEY = "anatomySelectionResult"

        fun newInstance(greenCardNames: List<String>): DialogAnatomyFragment {
            val fragment = DialogAnatomyFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_GREEN_CARDS_LIST, ArrayList<String>(greenCardNames))
            fragment.setArguments(args)
            fragment.setCancelable(false)
            return fragment
        }
    }
}