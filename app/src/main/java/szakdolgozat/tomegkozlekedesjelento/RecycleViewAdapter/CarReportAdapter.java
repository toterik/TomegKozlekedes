package szakdolgozat.tomegkozlekedesjelento.RecycleViewAdapter;

import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import szakdolgozat.tomegkozlekedesjelento.CarReportsActivity;
import szakdolgozat.tomegkozlekedesjelento.Model.CarReport;
import szakdolgozat.tomegkozlekedesjelento.Model.Report;
import szakdolgozat.tomegkozlekedesjelento.R;
import szakdolgozat.tomegkozlekedesjelento.ReportsActivity;

public class CarReportAdapter extends RecyclerView.Adapter<CarReportAdapter.ViewHolder>
{

    private ArrayList<CarReport> CarReportArrayList;
    private ArrayList<CarReport> CarReportArrayListAll;
    private Context context;

    public CarReportAdapter(Context context, ArrayList<CarReport> itemsData)
    {
        this.CarReportArrayList = itemsData;
        this.CarReportArrayListAll = itemsData;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.custom_car_report_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull CarReportAdapter.ViewHolder holder, int position) {
        CarReport currentItem = CarReportArrayList.get(position);
        holder.bindTo(currentItem, position);
    }


    @Override
    public int getItemCount() {
        return CarReportArrayListAll.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView startingCity;
        private TextView destinationCity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            startingCity = itemView.findViewById(R.id.reportsStartingCity);
            destinationCity = itemView.findViewById(R.id.reportsDestinationCity);
        }

        public void bindTo(CarReport currentItem, int position)
        {
            startingCity.setText(currentItem.getFromCity());
            destinationCity.setText(currentItem.getToCity());

            itemView.findViewById(R.id.btn_applicants).setOnClickListener(view ->
                    ((CarReportsActivity)context).showApplicants(currentItem));

        }
    };

}
