package szakdolgozat.tomegkozlekedesjelento.RecycleViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import szakdolgozat.tomegkozlekedesjelento.Model.User;
import szakdolgozat.tomegkozlekedesjelento.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;
    private List<String> globalTop3Ordered = new ArrayList<>();

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    public void setGlobalTop3Ordered(List<String> top3) {
        this.globalTop3Ordered = top3 != null ? top3 : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setFilteredList(List<User> filteredList) {
        this.userList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getUsername());
        holder.userPoints.setText(user.getPoints() + " pts");
        holder.rankText.setText(String.valueOf(user.getRank()));
        holder.medalIcon.setVisibility(View.GONE);
        holder.rankText.setVisibility(View.GONE);

        int indexInTop = globalTop3Ordered.indexOf(user.getId());
        if (indexInTop == 0) {
            holder.medalIcon.setVisibility(View.VISIBLE);
            holder.medalIcon.setImageResource(R.drawable.ic_gold_medal);
        } else if (indexInTop == 1) {
            holder.medalIcon.setVisibility(View.VISIBLE);
            holder.medalIcon.setImageResource(R.drawable.ic_silver_medal);
        } else if (indexInTop == 2) {
            holder.medalIcon.setVisibility(View.VISIBLE);
            holder.medalIcon.setImageResource(R.drawable.ic_bronze_medal);
        } else {
            holder.rankText.setVisibility(View.VISIBLE);
            holder.rankText.setText(String.valueOf(user.getRank()) + ".");
        }
    }

    @Override public int getItemCount() { return userList.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userPoints, rankText;
        ImageView medalIcon;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userPoints = itemView.findViewById(R.id.userPoints);
            medalIcon = itemView.findViewById(R.id.medalIcon);
            rankText = itemView.findViewById(R.id.textRank);
        }
    }
}
