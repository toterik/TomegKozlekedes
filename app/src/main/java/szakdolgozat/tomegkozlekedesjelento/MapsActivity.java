package szakdolgozat.tomegkozlekedesjelento;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

public class MapsActivity extends MenuForAllActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter
{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore db;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private ArrayList<Marker> markersList = new ArrayList<>();
    private Marker currentMarker;
    private boolean userIsLoggedIn = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng userCurrentLocation;
    private Spinner spinnerTransport;
    private Spinner spinnerProblem;
    private Place destinationPlace;
    private Geocoder geocoder;

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

        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

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

        fetchCurrentLocation();

        // Set custom InfoWindowAdapter
        mMap.setInfoWindowAdapter(this);

        //gets the marker positions from the database and displays them
        displayAllMarkers();


        //zoom and rotation settings on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation()
    {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                userCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

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
        if (userIsLoggedIn) openReportSheetDialog();
        else Toast.makeText(this, "You need to login to report a problem", Toast.LENGTH_SHORT).show();
    }
    public void displayAllMarkers()
    {
        var markers = db.collection("reports");
        markers.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots)
            {
                Map<String, Object> currentItem = document.getData();
                String meanOfTransport = (String) currentItem.get("mean_of_transport");
                String type = (String) currentItem.get("type");
                String description = (String) currentItem.get("description");
                long delay = (long) currentItem.get("delay");
                double startingLatitude = (Double) currentItem.get("starting_latitude");
                double startinglongitude = (Double) currentItem.get("starting_longitude");
                double destinationLatitude = (Double) currentItem.get("destination_latitude");
                double destinationlongitude = (Double) currentItem.get("destination_longitude");

                List<Address> destinationAddress = null;
                List<Address> startingAddress = null;
                try
                {
                    startingAddress = geocoder.getFromLocation(startingLatitude, startinglongitude, 1);
                    destinationAddress = geocoder.getFromLocation(destinationLatitude, destinationlongitude, 1);
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }


                String markerTitle = "Type: " + type + "\n" +
                        "Transport: " + meanOfTransport + "\n" +
                        "Description: " + description + "\n" +
                        "Delay: " + delay + " minutes\n" +
                        "Destination:"+destinationAddress.get(0).getLocality()+" \n. ";
                LatLng startinglatLng = new LatLng(startingLatitude, startinglongitude);
                this.markersList.add(mMap.addMarker(new MarkerOptions()
                        .position(startinglatLng)
                        .title("Starting Marker")
                        .snippet(markerTitle)));

                markerTitle = "Type: " + type + "\n" +
                        "Transport: " + meanOfTransport + "\n" +
                        "Description: " + description + "\n" +
                        "Delay: " + delay + " minutes\n" +
                        "Starting city:"+startingAddress.get(0).getLocality()+" \n. ";
                LatLng destinationlatLng = new LatLng(destinationLatitude, destinationlongitude);
                this.markersList.add(mMap.addMarker(new MarkerOptions()
                        .position(destinationlatLng)
                        .title("Destination Marker")
                        .snippet(markerTitle)));


            }
            Log.d("MarkersList", "Marker list size: " + markersList.size());
        }).addOnFailureListener(e ->
        {
            Log.e("Marker_Fail", "Error fetching markers", e);
        });

    }
    public void openReportSheetDialog()
    {
        currentMarker = mMap.addMarker(new MarkerOptions()
                .position(userCurrentLocation)
                .draggable(false)
                .title("This is your location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker.getPosition(), 10));

        if (currentMarker == null) {
            Toast.makeText(this, "Please place a marker on the map first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and inflate the bottom sheet layout
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_report_details, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);

        setupAutoCompleteFragment();

        setupSpinnersForReportSheet(sheetView);

        EditText detailsEditText = sheetView.findViewById(R.id.et_problem_details);
        EditText delayEditText = sheetView.findViewById(R.id.et_delay_duration);
        Button cancelButton = sheetView.findViewById(R.id.btn_cancel);

        cancelButton.setOnClickListener(v ->
        {
            bottomSheetDialog.dismiss();
            if (currentMarker != null)
            {
                currentMarker.remove();
                currentMarker = null;
            }
            removeAutoFragment();
        });

        Button submitButton = sheetView.findViewById(R.id.btn_submit);
        submitButton.setOnClickListener(v ->
        {
            //get input datas
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
            String problemDetails = detailsEditText.getText().toString();

            submitReport(meanOfTransport,problemType,delay,problemDetails);
            bottomSheetDialog.dismiss();
            removeAutoFragment();

        });

        // Show the bottom sheet dialog
        bottomSheetDialog.show();
    }

    private void removeAutoFragment()
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(autocompleteSupportFragment);
        fragmentTransaction.commit();
        if (currentMarker != null) {
            currentMarker.remove();
        }
    }

    private void setupSpinnersForReportSheet(View sheetView)
    {

        //set means of transport spinner
        spinnerTransport = (Spinner) sheetView.findViewById(R.id.spinner_transport);
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
        spinnerProblem = (Spinner) sheetView.findViewById(R.id.spinner_problem_type);
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
    }

    private void setupAutoCompleteFragment()
    {
        autocompleteSupportFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteSupportFragment.setTypeFilter(TypeFilter.CITIES);
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
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG // Include latitude and longitude explicitly
        ));
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
                destinationPlace = place;
            }
        });
    }

    public void submitReport(String meanOfTransport,String problemType,int delay, String problemDetails)
    {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        reportData.put("starting_latitude", userCurrentLocation.latitude);
        reportData.put("starting_longitude", userCurrentLocation.longitude);
        reportData.put("mean_of_transport", meanOfTransport);
        reportData.put("type", problemType);
        reportData.put("destination_latitude", destinationPlace.getLocation().latitude);
        reportData.put("destination_longitude", destinationPlace.getLocation().longitude);
        reportData.put("delay", delay);
        reportData.put("description", problemDetails);


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

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker)
    {
        return null;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        // Inflate custom layout
        View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);

        // Populate title and snippet
        TextView title = infoWindow.findViewById(R.id.title);
        TextView snippet = infoWindow.findViewById(R.id.snippet);

        title.setText(marker.getTitle());
        snippet.setText(marker.getSnippet());

        return infoWindow;
    }
}
