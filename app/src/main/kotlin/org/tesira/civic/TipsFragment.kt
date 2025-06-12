package org.tesira.civic

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.tesira.civic.databinding.FragmentTipsBinding
import org.tesira.civic.db.CivicViewModel
import kotlin.math.max
import kotlin.math.min

/**
 * A simple [Fragment] subclass.
 * Use the [TipsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TipsFragment : Fragment() {
    private lateinit var binding: FragmentTipsBinding
    val civicViewModel: CivicViewModel by activityViewModels()
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scaleGestureDetector =
            ScaleGestureDetector(this.requireContext(), PinchToZoomGestureListener())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTipsBinding.inflate(inflater, container, false)
        val rootView: View = binding.root

        // Speichere die ursprünglichen Padding-Werte der View, auf die die Insets angewendet werden
        val initialPaddingLeft = rootView.paddingLeft
        val initialPaddingTop = rootView.paddingTop
        val initialPaddingRight = rootView.paddingRight
        val initialPaddingBottom = rootView.paddingBottom


        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v: View, windowInsets: WindowInsetsCompat ->
            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Wende die Systemleisten-Insets zusätzlich zum ursprünglichen Padding an
            v.setPadding(
                initialPaddingLeft + systemBarInsets.left,
                initialPaddingTop,
                initialPaddingRight + systemBarInsets.right,
                initialPaddingBottom + systemBarInsets.bottom
            )
            windowInsets
        }

        //ViewCompat.requestApplyInsets(rootView)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.civilizations_entries,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.tipsSpinner.adapter = adapter
        }
        return rootView
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Beobachte den selectedTipIndex aus dem CivicViewModel
        civicViewModel.selectedTipIndex.observe(
            viewLifecycleOwner
        ) { index: Int? ->
            if (index != null) {
                // Spinner-Auswahl setzen, wenn sie sich vom aktuellen ViewModel-Wert unterscheidet
                // um endlose Schleifen zu vermeiden, wenn setSelection onItemSelected auslöst.
                if (binding.tipsSpinner.selectedItemPosition != index) {
                    if (index >= 0 && index < binding.tipsSpinner.count) {
                        binding.tipsSpinner.setSelection(
                            index,
                            false
                        ) // false, um onItemSelected nicht unnötig zu triggern
                    }
                }

                val tipText = civicViewModel.getTipForIndex(index)
                if (tipText != null && tipText.isNotEmpty()) {
                    binding.tipsTextView.text = getString(
                        R.string.tips_text_combined,
                        tipText,
                        getString(R.string.no_war_game)
                    )
                }
            }
        }

        binding.tipsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Aktualisiere das ViewModel, wenn der Benutzer eine Auswahl trifft
                // Prüfe, ob die Auswahl tatsächlich vom Benutzer stammt und sich geändert hat
                val currentIndexInViewModel = civicViewModel.selectedTipIndex.value
                if (currentIndexInViewModel == null || currentIndexInViewModel != position) {
                    civicViewModel.setSelectedTipIndex(position)
                }

                // Absturzsicherung für setTextSize
                (selectedItemView as? TextView)?.textSize = 20f
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: Standard-Tipp anzeigen oder Index auf einen Standardwert setzen
                // mCivicViewModel.setSelectedTipIndex(-1); // Beispiel für "keine Auswahl"
            }
        }

        binding.tipsTextView.movementMethod = ScrollingMovementMethod()
        // was für das MovementMethod wichtig ist.
        binding.tipsTextView.isClickable = true
        binding.tipsTextView.isFocusable = true
        binding.tipsTextView.isFocusableInTouchMode = true

        binding.tipsTextView.setOnTouchListener { v: View, event: MotionEvent? ->
            // Zuerst Zoom-Gesten behandeln
            scaleGestureDetector.onTouchEvent(event!!)

            // Gib das Event an die TextView weiter, damit sie scrollen kann
            v.parent.requestDisallowInterceptTouchEvent(scaleGestureDetector.isInProgress)
            false // Wichtig: false zurückgeben, um Scroll-Events nicht zu blockieren
        }
    }

    inner class PinchToZoomGestureListener : SimpleOnScaleGestureListener() {
        private var minSp: Float = 12f
        private var maxSp: Float = 48f
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val minSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                minSp,
                resources.displayMetrics
            )
            val maxSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                maxSp,
                resources.displayMetrics
            )
            val size = binding.tipsTextView.textSize
            val factor = detector.scaleFactor
            val newSize = max(
                minSizePx.toDouble(),
                min((size * factor).toDouble(), maxSizePx.toDouble())
            ).toFloat()
            binding.tipsTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize)
            return true
        }
    }

    companion object {
        fun newInstance(): TipsFragment {
            return TipsFragment()
        }
    }

}