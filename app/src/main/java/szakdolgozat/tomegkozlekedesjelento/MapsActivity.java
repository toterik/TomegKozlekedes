package szakdolgozat.tomegkozlekedesjelento;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.service.autofill.Field;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.internal.maps.zzah;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
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

import android.Manifest;
import android.Manifest.permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import szakdolgozat.tomegkozlekedesjelento.databinding.ActivityMapsBinding;

public class MapsActivity extends MenuForAllActivity implements OnMapReadyCallback
{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore db;
    private ArrayList<Marker> markersList = new ArrayList<>();
    private Marker currentMarker;
    private boolean userIsLoggedIn = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng userCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Get database instance
        db =  FirebaseFirestore.getInstance();

        userIsLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        initializePlacesAPI();

        //set the menu toolbar
        Toolbar menuToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(menuToolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initializePlacesAPI()
    {
        // Define a variable to hold the Places API key.
        String apiKey = BuildConfig.PLACES_API_KEY;

        // Log an error if apiKey is not set.
        if (TextUtils.isEmpty(apiKey))
        {
            finish();
            return;
        }

        // Initialize the SDK
        Places.initialize(getApplicationContext(), apiKey);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation()
    {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        requestPermissions(new String[]{permission.ACCESS_COARSE_LOCATION,permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        //if there is no permission, the user is sent back to the main page
        enableMyLocation();

        fetchAndMarkCurrentLocation();

        //gets the marker positions from the database and displays them
        displayAllMarkers();


        //zoom and rotation settings on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }

    @SuppressLint("MissingPermission")
    private void fetchAndMarkCurrentLocation()
    {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                userCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Add a marker at the user's current location
                mMap.addMarker(new MarkerOptions()
                        .position(userCurrentLocation)
                        .title("You are here"));

                // Move the camera to the user's location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 11));
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else
            {
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    public void startProblemReport(View view)
    {
        if (userIsLoggedIn) selectReportLocation();
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
    public void selectReportLocation()
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

        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment)
               getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteSupportFragment.setTypeFilter(TypeFilter.CITIES);

        autocompleteSupportFragment.setLocationBias(RectangularBounds.newInstance(
               new LatLng(userCurrentLocation.latitude,userCurrentLocation.longitude),
               new LatLng(47.1625,19.5033)
        ));
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> address = null;
        try
        {
            address = geocoder.getFromLocation(userCurrentLocation.latitude,userCurrentLocation.longitude,1);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        var country = address.get(0);
        autocompleteSupportFragment.setCountries(country.getCountryCode());
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onError(@NonNull Status status)
            {
                Log.i("error",status.toString());
            }

            @Override
            public void onPlaceSelected(@NonNull Place place)
            {
                Log.i("tag", "Place: ${place.name}, ${place.id}");
            }
        });


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
            selectReportLocation();
        });

        Button submitButton = sheetView.findViewById(R.id.btn_submit);
        submitButton.setOnClickListener(v ->
        {
            String problemDetails = detailsEditText.getText().toString();
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

            bottomSheetDialog.dismiss();
            selectReportLocation();

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
