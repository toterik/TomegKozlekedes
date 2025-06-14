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

import java.util.ArrayList;

import szakdolgozat.tomegkozlekedesjelento.Model.Report;
import szakdolgozat.tomegkozlekedesjelento.R;
import szakdolgozat.tomegkozlekedesjelento.ReportsActivity;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder>
{

    private ArrayList<Report> reportArrayList;
    private ArrayList<Report> reportArrayListAll;
    private Context context;

    public ReportAdapter(Context context, ArrayList<Report> itemsData)
    {
        this.reportArrayList = itemsData;
        this.reportArrayListAll = itemsData;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.custom_report_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ViewHolder holder, int position) {
        Report currentItem = reportArrayList.get(position);
        holder.bindTo(currentItem, position);
    }


    @Override
    public int getItemCount() {
        return reportArrayListAll.size();
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

        public void bindTo(Report currentItem, int position)
        {
            Geocoder geocoder = new Geocoder(context);
            startingCity.setText(currentItem.getCity(geocoder,currentItem.getStartingLatitude(),currentItem.getStartingLongitude()));
            destinationCity.setText(currentItem.getCity(geocoder,currentItem.getDestinationLatitude(),currentItem.getDestinationLongitude()));

            itemView.findViewById(R.id.btnDetails).setOnClickListener(view ->
                    ((ReportsActivity)context).detailsBottomSheet(currentItem,startingCity.getText().toString(),destinationCity.getText().toString()));
        }
    };

}
