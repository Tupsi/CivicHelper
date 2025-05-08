package org.tesira.civic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tesira.civic.Calamity;
import org.tesira.civic.R;

import java.util.List;

public class CalamityAdapter extends RecyclerView.Adapter<CalamityAdapter.ViewHolder> {

    private List<Calamity> calamityList;
    private Context mContext;

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

    public CalamityAdapter(List<Calamity> data, Context context) {
        calamityList = data;
        mContext = context;
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

}
