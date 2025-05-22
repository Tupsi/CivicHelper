package org.tesira.civic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tesira.civic.db.CivicViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeCalamityAdapter extends RecyclerView.Adapter<HomeCalamityAdapter.ViewHolder> {

    private List<Calamity> calamityList = new ArrayList<>();
    private Context mContext;
    private final CivicViewModel viewModel;

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView calamity;
        private final TextView bonus;
        public final View mView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
            calamity = itemView.findViewById(R.id.tvCalamity);
            bonus = itemView.findViewById(R.id.tvBonus);
        }

        public TextView getCalamity() { return calamity;}
        public TextView getBonus() { return bonus;}
    }

    public HomeCalamityAdapter(CivicViewModel viewModel, Context context) {
        this.viewModel = viewModel;
        this.mContext = context;
    }
    public void clearData() {
        calamityList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_calamity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Log.v("HOLDER", "Position: " + position + " : Name: " + calamityList.get(position).getCalamity() + " : Bonus: " + calamityList.get(position).getBonus() );
        holder.getCalamity().setText(calamityList.get(position).getCalamity());
        holder.getBonus().setText(calamityList.get(position).getBonus());
        if (Integer.parseInt(calamityList.get(position).getBonus()) > 0) {
            holder.getCalamity().setTextColor(mContext.getResources().getColor(R.color.calamity_red, null));
            holder.bonus.setTextColor(mContext.getResources().getColor(R.color.calamity_red, null));
        } else {
            holder.getCalamity().setTextColor(mContext.getResources().getColor(R.color.calamity_green, null));
            holder.bonus.setTextColor(mContext.getResources().getColor(R.color.calamity_green, null));
        }
        if (Integer.parseInt(calamityList.get(position).getBonus()) == -99) {
            holder.bonus.setText(" ");
        }
            holder.calamity.setBackgroundResource(R.drawable.specials_background);
            holder.bonus.setBackgroundResource(R.drawable.specials_background);
    }


    @Override
    public int getItemCount() {
        return calamityList.size();
    }

    public void updateData() {
        calamityList = viewModel.getCalamityBonus();
        notifyDataSetChanged();
    }
    public void submitCalamityList(List<Calamity> newCalamities) {
        this.calamityList.clear();
        if (newCalamities != null) {
            this.calamityList.addAll(newCalamities);
        }
        notifyDataSetChanged();
    }
}
