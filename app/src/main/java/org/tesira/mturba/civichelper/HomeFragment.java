package org.tesira.mturba.civichelper;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.tesira.mturba.civichelper.databinding.FragmentHomeBinding;
import org.tesira.mturba.civichelper.db.CardColor;
import org.tesira.mturba.civichelper.db.CivicViewModel;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CivicViewModel mCivicViewModel;
    private CalamityAdapter calamityAdapter;
    private SpecialsAdapter specialsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentHomeBinding.inflate(inflater, container,false);
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
//        binding.startBtn.setOnClickListener(this::onClickButton);
//        binding.resetBtn.setOnClickListener(this::onClickButton);
        setHasOptionsMenu(true);
//        mCivicViewModel.getAllPurchases().observeForever(purchases -> {
//            for (Purchase name: purchases) {
//                Log.v("BUY", "observer : "+name.getName());
//            }
//        });
        View rootView = binding.getRoot();
        RecyclerView mRecyclerView = rootView.findViewById(R.id.listCalamity);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        calamityAdapter = new CalamityAdapter(mCivicViewModel.getCalamityBonus(), this.getContext());
        mRecyclerView.setAdapter(calamityAdapter);

        mRecyclerView = rootView.findViewById(R.id.listAbility);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        List<String> specialsList = mCivicViewModel.getSpecialAbilities();
        specialsList.add(0,"___Special Abilities");
        if (specialsList.size() % 2 == 0) {
            specialsList.add(               "___Immunities");
        } else {
            specialsList.add(               "");
            specialsList.add(               "___Immunities");
        }
        specialsList.addAll(mCivicViewModel.getImmunities());
        specialsAdapter = new SpecialsAdapter(specialsList.toArray(new String[0]));
        mRecyclerView.setAdapter(specialsAdapter);
        binding.tvVp.setText("VP: " + mCivicViewModel.sumVp());
        checkAST();
        return rootView;
    }

    private void checkAST() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onClickButton(View v) {
        switch (v.getId()) {
//            case R.id.startBtn:
//                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_advancesFragment);
//                break;
//            case R.id.resetBtn:
//                resetGame();
//                break;
        }
    }

    public void resetGame() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        ((MainActivity) getActivity()).newGame();
                        loadBonus();
                        calamityAdapter.clearData();
                        specialsAdapter.clearData();
                        binding.tvVp.setText("VP: 0");
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    public void loadBonus() {
        binding.bonusBlue.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.BLUE,0)));
        binding.bonusBlue.setBackgroundResource(R.color.arts);
        binding.bonusGreen.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.GREEN,0)));
        binding.bonusGreen.setBackgroundResource(R.color.science);
        binding.bonusOrange.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.ORANGE,0)));
        binding.bonusOrange.setBackgroundResource(R.color.crafts);
        binding.bonusRed.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.RED,0)));
        binding.bonusRed.setBackgroundResource(R.color.civic);
        binding.bonusYellow.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.YELLOW,0)));
        binding.bonusYellow.setBackgroundResource(R.color.religion);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_newGame:
                resetGame();
                return true;

            default:
                return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(requireView())) || super.onOptionsItemSelected(item);
        }
//        return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(requireView())) || super.onOptionsItemSelected(item);
    }

    public void onPause() {
        super.onPause();
        Log.v("HOME","---> onPause() <--- ");
    }

    public void onStart() {
        super.onStart();
        Log.v("HOME","---> onStart() <--- ");
    }

    public void onResume() {
        super.onResume();
        Log.v("HOME","---> onResume() <--- ");
        loadBonus();
    }

    public void onStop() {
        super.onStop();
        Log.v("HOME","---> onStop() <--- ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        Log.v("HOME","---> onDestroy() <--- ");
    }

}