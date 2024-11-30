package szakdolgozat.tomegkozlekedesjelento;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.internal.maps.zzah;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import szakdolgozat.tomegkozlekedesjelento.databinding.ActivityMapsBinding;

public class MapsActivity extends MenuForAllActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore db;
    private ArrayList<Marker> markersList = new ArrayList<>();
    private Toolbar reportToolbar;
    public boolean reportToolbarShowing = false;
    private Marker currentMarker;
    private boolean userIsLoggedIn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Get database instance
        db =  FirebaseFirestore.getInstance();

        userIsLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;


        //set the menu toolbar
        Toolbar menuToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(menuToolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        //gets the marker positions from the database and displays them
        displayAllMarkers();

        //moves the camera to a specific position
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.1625, 19.5033),10));

        //zoom and rotation settings on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }
    public void showReportToolbar(View view)
    {
        if (userIsLoggedIn) startProblemReport();
        else Toast.makeText(this, "You need to login to report a problem", Toast.LENGTH_SHORT).show();
    }


    public void displayAllMarkers() {
        var markers = db.collection("markers");
        markers.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Map<String, Object> currentItem = document.getData();
                String uid = currentItem.get("uid").toString();
                double latitude = (Double) currentItem.get("latitude");
                double longitude = (Double) currentItem.get("longitude");

                LatLng latLng = new LatLng(latitude, longitude);
                this.markersList.add(mMap.addMarker(new MarkerOptions().position(latLng)));
            }
            Log.d("MarkersList", "Marker list size: " + markersList.size());
        }).addOnFailureListener(e ->
        {
            Log.e("Marker_Fail", "Error fetching markers", e);
        });

    }

    public void startProblemReport()
    {
        Toast.makeText(this, "Select a location on the map", Toast.LENGTH_SHORT).show();

        mMap.setOnMapClickListener(point -> {
            if (currentMarker != null) {
                currentMarker.remove();
            }
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .draggable(true)
                    .title("Drag to adjust location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10));
            // show the report details dialog
            openReportSheetDialog();
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) {}
            @Override
            public void onMarkerDragEnd(Marker marker) {
                currentMarker = marker;
            }
        });
    }

    public void openReportSheetDialog()
    {
        if (currentMarker == null) {
            Toast.makeText(this, "Please place a marker on the map first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and inflate the bottom sheet layout
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_report_details, null);
        bottomSheetDialog.setContentView(sheetView);

        //set means of transport spinner
        Spinner spinnerTransport = (Spinner) sheetView.findViewById(R.id.spinner_transport);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapterTransport = ArrayAdapter.createFromResource(
                this,
                R.array.means_of_transport,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapterTransport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinnerTransport.setAdapter(adapterTransport);


        //set type of problem spinner
        Spinner spinnerProblem = (Spinner) sheetView.findViewById(R.id.spinner_problem_type);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapterProblem = ArrayAdapter.createFromResource(
                this,
                R.array.type_of_problem,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapterProblem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinnerProblem.setAdapter(adapterProblem);


        EditText detailsEditText = sheetView.findViewById(R.id.et_problem_details);
        EditText destinationEditText = sheetView.findViewById(R.id.et_destination);
        EditText delayEditText = sheetView.findViewById(R.id.et_delay_duration);

        Button cancelButton = sheetView.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(v ->
        {
            bottomSheetDialog.dismiss();
            //remove marker if canceled
            if (currentMarker != null) {
                currentMarker.remove();
                currentMarker = null;
            }
        });

        Button submitButton = sheetView.findViewById(R.id.btn_submit);
        submitButton.setOnClickListener(v ->
        {
            String problemDetails = detailsEditText.getText().toString();
            String destination = destinationEditText.getText().toString();
            String meanOfTransport = spinnerTransport.getSelectedItem().toString();
            String problemType = spinnerProblem.getSelectedItem().toString();
            int delay;
            try
            {
                delay = Integer.parseInt(delayEditText.getText().toString());
            }catch (Exception e)
            {
                Toast.makeText(this, "Please provide a number in the duration (0 if there is no delay)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (destination.isEmpty())
            {
                Toast.makeText(this, "Please provide details about the problem.", Toast.LENGTH_SHORT).show();
            } else
            {
                submitReport(meanOfTransport, problemType, destination,delay,problemDetails, currentMarker.getPosition());
                bottomSheetDialog.dismiss();
            }
        });

        // Show the bottom sheet dialog
        bottomSheetDialog.show();
    }
    public void submitReport(String meanOfTransport,String problemType,String destination,int delay, String problemDetails, LatLng position)
    {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("mean_of_transport", meanOfTransport);
        reportData.put("type", problemType);
        reportData.put("destination", destination);
        reportData.put("delay", delay);
        reportData.put("description", problemDetails);
        reportData.put("latitude", position.latitude);
        reportData.put("longitude", position.longitude);
        reportData.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection("reports")
                .add(reportData)
                .addOnSuccessListener(documentReference ->
                {
                    Toast.makeText(this, "Report submitted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                {
                    Toast.makeText(this, "Failed to submit report.", Toast.LENGTH_SHORT).show();
                });
    }
}
