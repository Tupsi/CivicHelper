package org.tesira.civic;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log; // Import für android.util.Log
import android.view.LayoutInflater; // Hinzugefügt, falls für binding.inflate benötigt
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Hinzugefügt für onCreate
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider; // Hinzugefügt

import org.tesira.civic.databinding.DialogCreditsBinding;
// import org.tesira.civic.db.CardColor; // Nicht direkt verwendet, kann entfernt werden, wenn nicht benötigt
import org.tesira.civic.db.CivicViewModel;

/**
 * Dialog for Advances Written Record, Monument, Library
 *
 * https://developer.android.com/guide/topics/ui/dialogs
 */
public class ExtraCreditsDialogFragment extends DialogFragment {

    private static final String ARG_CREDITS = "arg_credits"; // Schlüssel für das Argument
    private static final String REQUEST_KEY = "extraCreditsDialogResult";

    private DialogCreditsBinding binding;
    private String[] items; // Wird in onCreate basierend auf 'credits' initialisiert
    private int initialCredits; // Umbenannt von 'credits' zur Klarheit
    private int blue, green, orange, red, yellow; // Diese bleiben für die Auswahl
    // private int oldblue, oldgreen, oldorange, oldred, oldyellow; // Nicht verwendet, kann entfernt werden

    private AlertDialog dialogInstance; // Umbenannt von 'dialog' zur Klarheit, um Kollision mit Parametername zu vermeiden
    private CivicViewModel mCivicViewModel;

    // WICHTIG: Parameterloser Konstruktor
    public ExtraCreditsDialogFragment() {
        // super(R.layout.dialog_credits); // Nicht hier, da das Layout in onCreateDialog über Binding geladen wird
    }

    // Factory-Methode
    public static ExtraCreditsDialogFragment newInstance(int creditsAmount) {
        ExtraCreditsDialogFragment fragment = new ExtraCreditsDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CREDITS, creditsAmount);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. ViewModel beziehen
        if (getActivity() != null) {
            mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
        } else {
            Log.e("ExtraCreditsDialog", "Activity is null, cannot get ViewModel.");
            dismissAllowingStateLoss();
            return;
        }

        // 2. Argumente abrufen
        if (getArguments() != null) {
            this.initialCredits = getArguments().getInt(ARG_CREDITS, 0); // Standardwert 0, falls Argument fehlt
            if (this.initialCredits == 0 && getArguments().getInt(ARG_CREDITS, -1) == -1) { // Striktere Prüfung, ob Argument wirklich fehlt
                Log.e("ExtraCreditsDialog", "Credits argument is missing or zero, which might be unintended.");
                // Hier entscheiden, ob ein Wert von 0 gültig ist oder ein Fehler.
                // Wenn 0 ungültig ist:
                // dismissAllowingStateLoss();
                // return;
            }
        } else {
            Log.e("ExtraCreditsDialog", "Arguments are null.");
            dismissAllowingStateLoss();
            return;
        }

        // Initialisiere 'items' basierend auf 'initialCredits'
        // Diese Logik war vorher im Konstruktor
        switch (initialCredits) {
            case 10:
                items = new String[]{"0", "5", "10"};
                break;
            case 20:
                items = new String[]{"0", "5", "10", "15", "20"};
                break;
            case 30:
                items = new String[]{"0", "5", "10", "15", "20", "25", "30"};
                break;
            default:
                // Fallback, falls 'initialCredits' einen unerwarteten Wert hat
                Log.w("ExtraCreditsDialog", "Unexpected number of credits: " + initialCredits + ". Using default items.");
                items = new String[]{"0"}; // Oder eine andere sinnvolle Standardeinstellung
                break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog currentDialog = getDialog(); // Lokale Variable verwenden
        if (currentDialog != null && currentDialog.getWindow() != null) {
            // Verwende WRAP_CONTENT für Höhe, es sei denn, MATCH_PARENT ist wirklich gewollt
            currentDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Binding initialisieren
        // Stelle sicher, dass getLayoutInflater() hier korrekt funktioniert.
        // Wenn du `super(R.layout.dialog_credits)` nicht mehr im Konstruktor hast,
        // musst du LayoutInflater explizit holen, falls onCreateDialog vor onCreateView aufgerufen wird
        // oder wenn das Fragment kein eigenes View-Layout hat (was bei DialogFragmenten der Fall sein kann, die nur einen Dialog erstellen).
        // Für DialogFragment, das setView verwendet, ist dies der übliche Weg:
        binding = DialogCreditsBinding.inflate(LayoutInflater.from(getContext()));


        // Initialisiere die lokalen Variablen für die ausgewählten Werte (optional, aber gut für Klarheit)
        blue = 0;
        green = 0;
        orange = 0;
        red = 0;
        yellow = 0;

        // Stelle sicher, dass 'items' in onCreate korrekt initialisiert wurde
        if (items == null || items.length == 0) {
            Log.e("ExtraCreditsDialog", "Spinner items not initialized!");
            // Erstelle einen einfachen Fehlerdialog oder schließe diesen Dialog
            return new AlertDialog.Builder(requireActivity())
                    .setTitle("Error")
                    .setMessage("Could not initialize dialog options.")
                    .setPositiveButton(android.R.string.ok, (d, w) -> dismiss())
                    .create();
        }


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spinnerblue.setAdapter(spinnerAdapter);
        binding.spinnerblue.setOnItemSelectedListener(new MyOnItemSelectedListener());
        // Setze initiale Auswahl, z.B. auf "0" (erstes Element)
        binding.spinnerblue.setSelection(0, false);


        binding.spinnerred.setAdapter(spinnerAdapter);
        binding.spinnerred.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinnerred.setSelection(0, false);

        binding.spinnerorange.setAdapter(spinnerAdapter);
        binding.spinnerorange.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinnerorange.setSelection(0, false);

        binding.spinneryellow.setAdapter(spinnerAdapter);
        binding.spinneryellow.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinneryellow.setSelection(0, false);

        binding.spinnergreen.setAdapter(spinnerAdapter);
        binding.spinnergreen.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinnergreen.setSelection(0, false);

        binding.creditsremaining.setText(String.valueOf(initialCredits)); // Verwende initialCredits
        // Aktuelle Boni aus dem ViewModel anzeigen
        binding.bonusblue.setText(String.valueOf(mCivicViewModel.getBlue()));
        binding.bonusgreen.setText(String.valueOf(mCivicViewModel.getGreen()));
        binding.bonusorange.setText(String.valueOf(mCivicViewModel.getOrange()));
        binding.bonusred.setText(String.valueOf(mCivicViewModel.getRed()));
        binding.bonusyellow.setText(String.valueOf(mCivicViewModel.getYellow()));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.dialog_extra_credits);
        View spinnerView = binding.getRoot();
        builder.setView(spinnerView);
        builder.setPositiveButton(R.string.ok, (dialogInterface, id) -> { // dialogInterface statt dialog
            mCivicViewModel.updateBonus(blue, green, orange, red, yellow);
            mCivicViewModel.saveBonus(); // Überlege, ob das hier oder im ViewModel besser aufgehoben ist
            mCivicViewModel.requestPriceRecalculation();

            Bundle result = new Bundle();
            // Keine spezifischen Daten benötigt, leeres Bundle ist ok
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

            dismiss();
        });
        // Den negativen Button nicht vergessen, falls der Dialog abbrechbar sein soll (obwohl setCancelable(false) ist)
        // builder.setNegativeButton(R.string.cancel, (dialogInterface, id) -> dismiss());


        dialogInstance = builder.create(); // Verwende dialogInstance
        if (dialogInstance.getWindow() != null) {
            dialogInstance.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }

        // Initialisiere den Zustand des OK-Buttons (wichtig, da calculateCredits jetzt davon abhängt)
        // Muss nach dialogInstance = builder.create() erfolgen, aber bevor der Dialog angezeigt wird.
        // Da calculateCredits in onItemSelected aufgerufen wird, wird es beim ersten Setzen der Auswahl getriggert.
        // Um sicherzustellen, dass der Button initial korrekt gesetzt ist:
        if (dialogInstance.isShowing()) { // Nur wenn der Dialog schon angezeigt wird (unwahrscheinlich hier, aber sicher ist sicher)
            updateOkButtonState();
        } else {
            // Wenn der Dialog noch nicht angezeigt wird, wird der Button-Status beim ersten Anzeigen
            // durch die onItemSelected Listener korrekt gesetzt, wenn die Spinner ihre erste Auswahl erhalten.
            // Alternativ könnte man hier manuell den initialen Zustand setzen:
            dialogInstance.setOnShowListener(dialogInterface -> updateOkButtonState());
        }


        return dialogInstance;
    }

    // Methode zum Aktualisieren des OK-Button-Status extrahiert
    private void updateOkButtonState() {
        if (dialogInstance != null && binding != null) { // Sicherstellen, dass alles initialisiert ist
            int totalSelected = blue + green + orange + red + yellow;
            binding.creditsremaining.setText(String.valueOf(initialCredits - totalSelected));
            dialogInstance.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(totalSelected == initialCredits);
        }
    }


    // onDismiss bleibt wie es ist

    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (items == null || position >= items.length) { // Sicherheitscheck
                return;
            }
            int selectedValue = Integer.parseInt(items[position]);

            int parentId = parent.getId();
            if (parentId == R.id.spinnerblue) {
                blue = selectedValue;
            } else if (parentId == R.id.spinnergreen) {
                green = selectedValue;
            } else if (parentId == R.id.spinnerorange) {
                orange = selectedValue;
            } else if (parentId == R.id.spinnerred) {
                red = selectedValue;
            } else if (parentId == R.id.spinneryellow) {
                yellow = selectedValue;
            }
            // calculateCredits(); // Wird jetzt von updateOkButtonState() übernommen
            updateOkButtonState(); // Rufe die zentrale Methode auf
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

        // Die calculateCredits() Logik ist jetzt in updateOkButtonState()
        // public void calculateCredits() { ... } // Kann entfernt werden
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Wichtig für View Binding in Fragmenten, um Memory Leaks zu vermeiden
    }
}