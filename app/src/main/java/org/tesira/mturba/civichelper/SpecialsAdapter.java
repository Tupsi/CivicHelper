package org.tesira.mturba.civichelper;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

/**
 * Adapter for showing a list of Special Abilities or Immunities against on the dashboard.
 */
public class SpecialsAdapter extends RecyclerView.Adapter<SpecialsAdapter.ViewHolder> {

    private String[] localDataSet;

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

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public SpecialsAdapter(String[] dataSet) {
        // remove duplicates as every special has two counters
        localDataSet = Arrays.stream(dataSet).distinct().toArray(String[]::new);
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

    public void clearData() {
        localDataSet = new String[0];
        notifyDataSetChanged();
    }
}