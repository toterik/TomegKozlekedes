package szakdolgozat.tomegkozlekedesjelento;

import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import szakdolgozat.tomegkozlekedesjelento.Model.Report;
import szakdolgozat.tomegkozlekedesjelento.RecycleViewAdapter.ReportAdapter;

public class ReportsActivity extends MenuForAllActivity
{
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ArrayList<Report> reportList;
    private ReportAdapter reportAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setup spinner
        setupSpinner();

        // Initialize the report list
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(this, reportList);
        recyclerView.setAdapter(reportAdapter);

        // Fetch data from Firestore
        getData();

    }

    public void getData()
    {
        db.collection("reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {
                    ArrayList<Report> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots)
                    {
                        Report currentReport = document.toObject(Report.class);
                        list.add(currentReport);
                    }

                    reportList.clear();
                    reportList.addAll(list);
                    reportAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                {
                    Log.e("Marker_Fail", "Error fetching markers", e);
                });
    }

    public void detailsBottomSheet(Report currentItem, String startingCity, String destination)
    {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.show_all_reports, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.setCanceledOnTouchOutside(true);


        TextView twMeanOfTransport = sheetView.findViewById(R.id.tv_transport_value);
        TextView twTypeOfProblem = sheetView.findViewById(R.id.tv_problem_type_value);
        TextView twStartingCity = sheetView.findViewById(R.id.tv_starting_city_value);
        TextView twDestinationCity = sheetView.findViewById(R.id.tv_destination_value);
        TextView twDelay = sheetView.findViewById(R.id.tv_delay_duration_value);
        TextView twDescription = sheetView.findViewById(R.id.tv_problem_details_value);

        twMeanOfTransport.setText(currentItem.getMeanOfTransport());
        twTypeOfProblem.setText(currentItem.getType());
        twStartingCity.setText(startingCity);
        twDestinationCity.setText(destination);
        twDelay.setText(String.valueOf(currentItem.getDelay()));
        twDescription.setText(currentItem.getDescription());

        Button cancelButton = sheetView.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(v ->
        {
            bottomSheetDialog.dismiss();
        });


        bottomSheetDialog.show();
    }

    public void filterCity(View view)
    {
        reportList.clear();
        EditText et_CityFilter = findViewById(R.id.et_city_filter);
        Spinner spinner = findViewById(R.id.spinner_transport);
        String spinnerSelectedItem = spinner.getSelectedItem().toString();
        if ((et_CityFilter.getText().toString().isEmpty() || et_CityFilter.getText() == null) && spinnerSelectedItem.equals("All"))
        {
            getData();
            return;
        }
        Geocoder geocoder = new Geocoder(this);
        String city = et_CityFilter.getText().toString().toLowerCase().trim();
        String cityFilter = et_CityFilter.getText().toString().toLowerCase().trim();

        db.collection("reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {
                    ArrayList<Report> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots)
                    {
                        Report currentReport = document.toObject(Report.class);

                        String startingCity = currentReport.getCity(geocoder, currentReport.getStartingLatitude(), currentReport.getStartingLongitude()).toLowerCase();
                        String destinationCity = currentReport.getCity(geocoder, currentReport.getDestinationLatitude(), currentReport.getDestinationLongitude()).toLowerCase();

                        // Check city filter with partial match
                        boolean matchesCity = cityFilter.isEmpty() ||
                                startingCity.contains(cityFilter) ||
                                destinationCity.contains(cityFilter);


                        // Check transport filter
                        boolean matchesTransport = spinnerSelectedItem.equals("Összes") ||
                                currentReport.getMeanOfTransport().equalsIgnoreCase(spinnerSelectedItem);

                        // Add to list if it matches both filters
                        if ((city.isEmpty() || matchesCity) && matchesTransport)
                        {
                            list.add(currentReport);
                        }
                    }

                    reportList.clear();
                    reportList.addAll(list);
                    reportAdapter.notifyDataSetChanged();
                });
    }
    public void setupSpinner()
    {
        //set means of transport spinner
        Spinner spinnerTransport = (Spinner) findViewById(R.id.spinner_transport);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapterTransport = ArrayAdapter.createFromResource(
                this,
                R.array.filter_means_of_transport,
                android.R.layout.simple_spinner_item
        );
        adapterTransport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransport.setAdapter(adapterTransport);
    }

}