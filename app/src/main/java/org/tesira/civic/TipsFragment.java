package org.tesira.civic;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.tesira.civic.databinding.FragmentTipsBinding;
import org.tesira.civic.db.CivicViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TipsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TipsFragment extends Fragment {

    private FragmentTipsBinding binding;
    private CivicViewModel mCivicViewModel;
    private ScaleGestureDetector scaleGestureDetector;

    public TipsFragment() {}
    public static TipsFragment newInstance() {
        return new TipsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
        scaleGestureDetector = new ScaleGestureDetector(this.getContext(), new PinchToZoomGestureListener() );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTipsBinding.inflate(inflater, container,false);
        View rootView = binding.getRoot();

        // Speichere die ursprünglichen Padding-Werte der View, auf die die Insets angewendet werden
        final int initialPaddingLeft = rootView.getPaddingLeft();
        final int initialPaddingTop = rootView.getPaddingTop();
        final int initialPaddingRight = rootView.getPaddingRight();
        final int initialPaddingBottom = rootView.getPaddingBottom();


        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Wende die Systemleisten-Insets zusätzlich zum ursprünglichen Padding an
            v.setPadding(
                    initialPaddingLeft + systemBarInsets.left,
                    initialPaddingTop,
                    initialPaddingRight + systemBarInsets.right,
                    initialPaddingBottom + systemBarInsets.bottom
            );

            // Es ist wichtig, die WindowInsets (ggf. modifiziert) zurückzugeben,
            // damit Kind-Views sie auch konsumieren können.
            // Wenn du hier nichts an den windowInsets selbst änderst, gib sie einfach weiter.
            return windowInsets;
        });

        ViewCompat.requestApplyInsets(rootView);

        ArrayAdapter<CharSequence> civicsAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.civilizations_entries, android.R.layout.simple_spinner_item);
        civicsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.tipsSpinner.setAdapter(civicsAdapter);
        return rootView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Beobachte den selectedTipIndex aus dem CivicViewModel
        mCivicViewModel.selectedTipIndex.observe(getViewLifecycleOwner(), index -> {
            if (index != null) {
                // Spinner-Auswahl setzen, wenn sie sich vom aktuellen ViewModel-Wert unterscheidet
                // um endlose Schleifen zu vermeiden, wenn setSelection onItemSelected auslöst.
                if (binding.tipsSpinner.getSelectedItemPosition() != index) {
                    if (index >= 0 && index < binding.tipsSpinner.getCount()) {
                        binding.tipsSpinner.setSelection(index, false); // false, um onItemSelected nicht unnötig zu triggern
                    }
                }

                String tipText = mCivicViewModel.getTipForIndex(index);
                if (tipText != null && !tipText.isEmpty()) {
                    binding.tipsTextView.setText(tipText + getString(R.string.no_war_game));
                } else {
                    // Fallback, wenn kein Tipp gefunden wurde oder der Index ungültig ist
                    binding.tipsTextView.setText(getString(R.string.no_war_game) + " (Tip not found for index: " + index + ")");
                }
            } else {
                binding.tipsTextView.setText(getString(R.string.no_war_game) + " (No tip index selected)");
            }
        });

        binding.tipsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedItemView, int position, long id) {
                // Aktualisiere das ViewModel, wenn der Benutzer eine Auswahl trifft
                // Prüfe, ob die Auswahl tatsächlich vom Benutzer stammt und sich geändert hat
                Integer currentIndexInViewModel = mCivicViewModel.selectedTipIndex.getValue();
                if (currentIndexInViewModel == null || currentIndexInViewModel != position) {
                    mCivicViewModel.setSelectedTipIndex(position);
                }

                // Absturzsicherung für setTextSize
                View firstChild = parent.getChildAt(0);
                if (firstChild instanceof TextView) {
                    ((TextView) firstChild).setTextSize(20); // Oder hole die Größe aus dimen
                } else {
                    Log.w("TipsFragment", "Spinner's selected view (getChildAt(0)) is not a TextView or is null in onItemSelected.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Standard-Tipp anzeigen oder Index auf einen Standardwert setzen
                // mCivicViewModel.setSelectedTipIndex(-1); // Beispiel für "keine Auswahl"
            }
        });

        binding.tipsTextView.setMovementMethod(new ScrollingMovementMethod());
        // was für das MovementMethod wichtig ist.
        binding.tipsTextView.setClickable(true);
        binding.tipsTextView.setFocusable(true);
        binding.tipsTextView.setFocusableInTouchMode(true);

        binding.tipsTextView.setOnTouchListener((v, event) -> {
            // Zuerst Zoom-Gesten behandeln
            scaleGestureDetector.onTouchEvent(event);

            // Gib das Event an die TextView weiter, damit sie scrollen kann
            v.getParent().requestDisallowInterceptTouchEvent(scaleGestureDetector.isInProgress());
            return false; // Wichtig: false zurückgeben, um Scroll-Events nicht zu blockieren
        });
    }

    public class PinchToZoomGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        float minSp = 12f;
        float maxSp = 48f;
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float minSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, minSp, getResources().getDisplayMetrics());
            float maxSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, maxSp, getResources().getDisplayMetrics());
            float size = binding.tipsTextView.getTextSize();
            float factor = detector.getScaleFactor();
            float newSize = Math.max(minSizePx, Math.min(size * factor, maxSizePx));
            binding.tipsTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
            return true;
        }
    }
}