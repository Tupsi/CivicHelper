package org.tesira.civic;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tesira.civic.db.CivicViewModel;

import java.util.List;

/**
 * Adapter for showing a list of Special Abilities or Immunities against on the dashboard.
 */
public class HomeSpecialsAdapter extends RecyclerView.Adapter<HomeSpecialsAdapter.ViewHolder> {

    private String[] localDataSet = new String[0];
    private final CivicViewModel viewModel;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = view.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public HomeSpecialsAdapter(CivicViewModel viewModel) {
        this.viewModel = viewModel;
        this.localDataSet = new String[0]; // initial leer
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_row_specials, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(localDataSet[position]);
        viewHolder.getTextView().setBackgroundResource(R.drawable.specials_background);
        if (viewHolder.getTextView().getText().toString().startsWith("_")) {
            viewHolder.getTextView().setTypeface(viewHolder.getTextView().getTypeface(), Typeface.BOLD);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.length;
    }

    public void submitSpecialsList(List<String> newSpecialsAndImmunities) {
        if (newSpecialsAndImmunities != null) {
            this.localDataSet = newSpecialsAndImmunities.toArray(new String[0]);
        } else {
            this.localDataSet = new String[0];
        }
        notifyDataSetChanged();
    }

}