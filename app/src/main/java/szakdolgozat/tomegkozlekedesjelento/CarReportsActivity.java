package szakdolgozat.tomegkozlekedesjelento;

import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import szakdolgozat.tomegkozlekedesjelento.Model.CarReport;
import szakdolgozat.tomegkozlekedesjelento.Model.Report;
import szakdolgozat.tomegkozlekedesjelento.RecycleViewAdapter.ApplicantsAdapter;
import szakdolgozat.tomegkozlekedesjelento.RecycleViewAdapter.CarReportAdapter;
import szakdolgozat.tomegkozlekedesjelento.RecycleViewAdapter.ReportAdapter;

public class CarReportsActivity extends MenuForAllActivity
{
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ArrayList<CarReport> carReportList;
    private CarReportAdapter carReportAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_car_reports);
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.car_report_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the report list
        carReportList = new ArrayList<>();
        carReportAdapter = new CarReportAdapter(this, carReportList);
        recyclerView.setAdapter(carReportAdapter);


        getData();

    }

    public void getData()
    {
        db.collection("carReports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {
                    ArrayList<CarReport> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots)
                    {
                        CarReport currentCarReport = document.toObject(CarReport.class);
                        var a = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        if(Objects.equals(currentCarReport.getUid(), FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        {
                            list.add(currentCarReport);
                        }
                    }

                    carReportList.clear();
                    carReportList.addAll(list);
                    carReportAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                {
                    Log.e("Fetch error", "Error fetching car reports", e);
                });
    }


    public void showApplicants(CarReport currentItem) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_applicants, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView rvApplicants = sheetView.findViewById(R.id.rv_applicants);
        rvApplicants.setLayoutManager(new LinearLayoutManager(this));

        // Adapter példány
        ApplicantsAdapter adapter = new ApplicantsAdapter(this, currentItem);
        rvApplicants.setAdapter(adapter);

        bottomSheetDialog.show();
    }


}