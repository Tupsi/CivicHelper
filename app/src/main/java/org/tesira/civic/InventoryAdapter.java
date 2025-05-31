package org.tesira.civic;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tesira.civic.databinding.ItemRowInventoryBinding;
import org.tesira.civic.db.Card;
import org.tesira.civic.db.CivicViewModel;

import java.util.ArrayList;
import java.util.List;



/**
 * {@link RecyclerView.Adapter} that can display a {@link Card}.
 * Shows all Civilization Advances and highlights already purchases ones.
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<Card> mValues = new ArrayList<>();
    public InventoryAdapter() {
    }

    /**
     * Aktualisiert die Liste der anzuzeigenden Karten und benachrichtigt den Adapter.
     * @param newValues Die neue Liste der Karten.
     */
    public void submitList(List<Card> newValues) {
        if (newValues != null) {
            this.mValues = new ArrayList<>(newValues);
        } else {
            this.mValues = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemRowInventoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.binding.name.setText(mValues.get(position).getName());
        holder.binding.name.setBackground(CivicViewModel.getItemBackgroundColor(holder.mItem, holder.itemView.getResources()));
        holder.binding.price.setText(Integer.toString(mValues.get(position).getPrice()));
        holder.binding.vp.setText(Integer.toString(mValues.get(position).getVp()));
        holder.binding.heart.setVisibility(mValues.get(position).getHasHeart() ? View.VISIBLE : View.INVISIBLE);

        if (holder.mItem.getBonus() > 0) {
            holder.binding.familybonus.setVisibility(View.VISIBLE);
            String bonus = "+" + holder.mItem.getBonus() + " to " + holder.mItem.getBonusCard();
            holder.binding.familybonus.setText(bonus);
        }
        else {
            holder.binding.familybonus.setVisibility(View.INVISIBLE);
        }

        switch (mValues.get(position).getGroup1()) {
            case YELLOW:
            case GREEN:
                holder.binding.name.setTextColor(Color.BLACK);
                break;
            default:
                holder.binding.name.setTextColor(Color.WHITE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemRowInventoryBinding binding;
        public Card mItem;

        public ViewHolder(ItemRowInventoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + binding.name.getText() + "'";
        }
    }
}