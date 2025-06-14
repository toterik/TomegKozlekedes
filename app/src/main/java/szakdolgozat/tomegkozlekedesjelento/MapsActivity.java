package szakdolgozat.tomegkozlekedesjelento;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.Manifest.permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


import szakdolgozat.tomegkozlekedesjelento.BottomSheetFragment.ReportBottomSheetFragmentModifying;
import szakdolgozat.tomegkozlekedesjelento.Model.MarkerPair;
import szakdolgozat.tomegkozlekedesjelento.Model.Report;
import szakdolgozat.tomegkozlekedesjelento.databinding.ActivityMapsBinding;

public class MapsActivity extends MenuForAllActivity implements OnMapReadyCallback
{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Marker selectedMarker;
    private static final double TOLERANCE = 0.00005;
    Set<MarkerPair> markerPairs = new HashSet<>();
    private String currentUserRole = "";
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore db;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private ArrayList<Marker> markersList = new ArrayList<>();
    private ArrayList<Report> reportsList = new ArrayList<>();
    private Marker startingMarker;
    private Marker destinationMarker;
    private boolean userIsLoggedIn = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    public static LatLng userCurrentLocation;
    private Spinner spinnerTransport;
    private Spinner spinnerProblem;

    private FirebaseUser user;
    private Geocoder geocoder;
    private ImageView okIMG;
    private ImageView noIMG;
    private int selectedTransport = 0;
    private int selectedProblem = 0;
    private String problemDetailsText = "";
    private int delayMinutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        userIsLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (userIsLoggedIn)
        {
            user = FirebaseAuth.getInstance().getCurrentUser();
            fetchCurrentUserRole();
        }
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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        okIMG = findViewById(R.id.img_ok);
        noIMG = findViewById(R.id.img_no);
        okIMG.setVisibility(View.INVISIBLE);
        noIMG.setVisibility(View.INVISIBLE);
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
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        requestPermissions(new String[]{permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        //if there is no permission, the user is sent back to the main page
        enableMyLocation();

        fetchCurrentLocation();

        //gets the marker positions from the database and displays them
        displayAllMarkers();

        //zoom and rotation settings on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        //show changes to makers (e.g. new marker is added)
        liveMarkerTracker();

        mMap.setOnMarkerClickListener(marker ->
        {
            selectedMarker = marker;
            markersList.add(selectedMarker);
            Report report = (Report) marker.getTag();
            if (report != null)
            {
               // ReportBottomSheetFragmentModifying bottomSheet = ReportBottomSheetFragmentModifying.newInstance(report,selectedMarker, markerPairs,currentUserRole,mMap);
                //bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            }
            return true;
        });
    }

    private void liveMarkerTracker()
    {
        db.collection("reports")
                .addSnapshotListener(new EventListener<QuerySnapshot>()
                {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e)
                    {
                        if (e != null)
                        {
                            Log.w("TAG", "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges())
                        {
                            switch (dc.getType())
                            {
                                case ADDED:
                                    addMarkersFromReport(dc.getDocument().toObject(Report.class));
                                    break;
                                case MODIFIED:
                                    //displayAllMarkers();
                                    break;
                                case REMOVED:
                                    removeMarkerLive(dc.getDocument().getData());
                                    break;
                            }
                        }

                    }
                });
    }

    private void modifyMarkerLive(Map<String, Object> markerData)
    {
        //TODO
        double starting_latitude = (double) markerData.get("starting_latitude");
        double starting_longitude = (double) markerData.get("starting_longitude");
        double destination_latitude = (double) markerData.get("destination_latitude");
        double destination_longitude = (double) markerData.get("destination_longitude");

        Iterator<Marker> iterator = markersList.iterator();
        while (iterator.hasNext())
        {
            Marker marker = iterator.next();
            LatLng markerPosition = marker.getPosition();

            if (areLatLngEqual(markerPosition, new LatLng(starting_latitude, starting_longitude)) ||
                    areLatLngEqual(markerPosition, new LatLng(destination_latitude, destination_longitude)))
            {

            }

        }
    }

    private void removeMarkerLive(Map<String, Object> markerData)
    {
        double starting_latitude = (double) markerData.get("starting_latitude");
        double starting_longitude = (double) markerData.get("starting_longitude");
        double destination_latitude = (double) markerData.get("destination_latitude");
        double destination_longitude = (double) markerData.get("destination_longitude");

        Iterator<Marker> iterator = markersList.iterator();
        while (iterator.hasNext())
        {
            Marker marker = iterator.next();
            LatLng markerPosition = marker.getPosition();

            if (areLatLngEqual(markerPosition, new LatLng(starting_latitude, starting_longitude)) ||
                    areLatLngEqual(markerPosition, new LatLng(destination_latitude, destination_longitude)))
            {
                marker.remove();
                iterator.remove();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation()
    {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, task ->
        {
            if (task.isSuccessful() && task.getResult() != null)
            {
                Location location = task.getResult();
                userCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Move the camera to the user's location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 11));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
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
        else
            Toast.makeText(this, "You need to login to report a problem", Toast.LENGTH_SHORT).show();
    }

    public void displayAllMarkers()
    {
        var markers = db.collection("reports");
        markers.get().addOnSuccessListener(queryDocumentSnapshots ->
        {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots)
            {
                Report currentReport = document.toObject(Report.class);
                reportsList.add(currentReport);

                addMarkersFromReport(currentReport);
            }
        }).addOnFailureListener(e ->
        {
            Log.e("Marker_Fail", "Error fetching markers", e);
        });
    }
    private boolean markerExists(LatLng position) {
        for (Marker m : markersList) {
            if (m.getPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }
    private void addMarkersFromReport(Report report)
    {
        LatLng startLatLng = new LatLng(report.getStartingLatitude(), report.getStartingLongitude());
        LatLng endLatLng = new LatLng(report.getDestinationLatitude(), report.getDestinationLongitude());

        Marker startMarker = null;
        if (!markerExists(startLatLng))
        {
            startMarker = mMap.addMarker(new MarkerOptions()
                    .position(startLatLng)
                    .title(report.getMarkerTitle(true))

                    .snippet(report.getMarkerSnippet(true, geocoder)));
        }

        Marker endMarker = null;
        if (!markerExists(endLatLng))
        {
            endMarker = mMap.addMarker(new MarkerOptions()
                    .position(endLatLng)
                    .title(report.getMarkerTitle(false))
                    .snippet(report.getMarkerSnippet(false, geocoder)));
        }

        if (startMarker != null && endMarker != null)
        {
            startMarker.setTag(report);
            endMarker.setTag(report);

            markersList.add(startMarker);
            markersList.add(endMarker);

            markerPairs.add(new MarkerPair(startMarker, endMarker));
        }
    }

    public void openReportSheetDialog()
    {
        if (startingMarker == null)
        {
            LatLng candidate = userCurrentLocation;
            int attempts = 0;
            while (isPositionTaken(candidate) && attempts < 100) {
                candidate = new LatLng(
                        candidate.latitude + (Math.random() - 0.5) * 0.0002,
                        candidate.longitude + (Math.random() - 0.5) * 0.0002
                );
                attempts++;
            }
            startingMarker = mMap.addMarker(new MarkerOptions()
                    .position(candidate)
                    .draggable(true)
                    .title("Itt vagy éppen!"));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startingMarker.getPosition(), 40));
            okIMG.setVisibility(View.VISIBLE);
            noIMG.setVisibility(View.VISIBLE);
        }

        //a "no" gombra láthatatlanok lesznek a gombok, újra meg kell nyomni a report gombot
        noIMG.setOnClickListener(v ->
        {
            okIMG.setVisibility(View.INVISIBLE);
            noIMG.setVisibility(View.INVISIBLE);
            if (startingMarker != null)
            {
                startingMarker.remove();
                startingMarker = null;
            }
            if (destinationMarker != null)
            {
                destinationMarker.remove();
                destinationMarker = null;
            }
        });


        okIMG.setOnClickListener(v ->
        {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_reporting_form, null);
            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.setCanceledOnTouchOutside(false);
            bottomSheetDialog.setDismissWithAnimation(false);
            if (startingMarker == null) return;
            setupAutoCompleteFragment(bottomSheetDialog);

            if (destinationMarker != null)
            {
                Geocoder geocoder = new Geocoder(this.getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        String city = addresses.get(0).getLocality();
                        autocompleteSupportFragment.setText(city);
                    } else {
                        autocompleteSupportFragment.setText("Destination not found");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    autocompleteSupportFragment.setText("Error getting destination");
                }
            }

            okIMG.setVisibility(View.INVISIBLE);
            noIMG.setVisibility(View.INVISIBLE);

            setupSpinnersForReportSheet(sheetView);

            EditText detailsEditText = sheetView.findViewById(R.id.et_problem_details);
            EditText delayEditText = sheetView.findViewById(R.id.et_delay_duration);
            delayEditText.setText("0");
            Button cancelButton = sheetView.findViewById(R.id.btn_cancel);
            Button submitButton = sheetView.findViewById(R.id.btn_submit);

            detailsEditText.setText(problemDetailsText);
            delayEditText.setText(String.valueOf(delayMinutes));
            spinnerTransport.setSelection(selectedTransport);
            spinnerProblem.setSelection(selectedProblem);

            bottomSheetDialog.setOnDismissListener(x ->
            {
                if (spinnerTransport != null && spinnerProblem != null && delayEditText != null && detailsEditText != null) {
                    selectedTransport = (int) spinnerTransport.getSelectedItemId();
                    selectedProblem = (int) spinnerProblem.getSelectedItemId();
                    problemDetailsText = detailsEditText.getText().toString();
                    try {
                        delayMinutes = Integer.parseInt(delayEditText.getText().toString());
                    } catch (Exception e) {
                        delayMinutes = 0;
                    }
                }
            });


            cancelButton.setOnClickListener(x -> {
                bottomSheetDialog.dismiss();
                if (startingMarker != null) {
                    startingMarker.remove();
                    startingMarker = null;
                }
                if (destinationMarker != null)
                {
                    destinationMarker.remove();
                    destinationMarker = null;
                }
                removeAutoFragment();
            });

            submitButton.setOnClickListener(x ->
            {
                // Get input data
                String meanOfTransport = spinnerTransport.getSelectedItem().toString();
                String problemType = spinnerProblem.getSelectedItem().toString();
                String problemDetails = detailsEditText.getText().toString();

                if (destinationMarker == null) {
                    Toast.makeText(this, "Kérem adja meg a végállomást!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int delay;
                try {
                    delay = Integer.parseInt(delayEditText.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(this, "Kérem adja meg mennyi percet késik. (0, ha nincs késés)", Toast.LENGTH_SHORT).show();
                    return;
                }


                Report report = new Report(
                        delay,
                        problemDetails,
                        destinationMarker.getPosition().latitude,
                        destinationMarker.getPosition().longitude,
                        meanOfTransport,
                        startingMarker.getPosition().latitude,
                        startingMarker.getPosition().longitude,
                        problemType,
                        FirebaseAuth.getInstance().getCurrentUser().getUid()
                );
                report.save(db);

                startingMarker.remove();
                startingMarker = null;
                destinationMarker.remove();
                destinationMarker = null;

                bottomSheetDialog.dismiss();
                removeAutoFragment();
            });

            bottomSheetDialog.show();
        });
    }
    private void removeAutoFragment()
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(autocompleteSupportFragment);
        fragmentTransaction.commit();
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

    private void setupAutoCompleteFragment(BottomSheetDialog bottomSheetDialog)
    {
        autocompleteSupportFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteSupportFragment.setTypeFilter(TypeFilter.CITIES);
        List<Address> address = null;
        try
        {
            address = geocoder.getFromLocation(userCurrentLocation.latitude, userCurrentLocation.longitude, 1);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        var country = address.get(0);

        autocompleteSupportFragment.setCountries(country.getCountryCode());
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
        ));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onError(@NonNull Status status)
            {
                Log.i("error", status.toString());
            }
            @Override
            public void onPlaceSelected(@NonNull Place place)
            {
                if (destinationMarker != null)
                {
                    destinationMarker.remove();
                    destinationMarker = null;
                }
                LatLng candidate = place.getLocation();
                int attempts = 0;
                while (isPositionTaken(candidate) && attempts < 100)
                {
                    candidate = new LatLng(
                            candidate.latitude + (Math.random() - 0.5) * 0.0002,
                            candidate.longitude + (Math.random() - 0.5) * 0.0002
                    );
                    attempts++;
                }

                bottomSheetDialog.dismiss();
                okIMG.setVisibility(View.VISIBLE);
                noIMG.setVisibility(View.VISIBLE);

                destinationMarker = mMap.addMarker(new MarkerOptions()
                        .position(candidate)
                        .title("Végállomás")
                        .draggable(true));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(candidate, 40));

                removeAutoFragment();
            }
        });
    }
    private boolean isPositionTaken(LatLng pos)
    {
        for (MarkerPair item : markerPairs)
        {
            if (areLatLngEqual(pos, item.startMarker.getPosition()) ||
                    areLatLngEqual(pos, item.endMarker.getPosition()))
            {
                return true;
            }
        }
        return false;
    }
    private boolean areLatLngEqual(LatLng pos1, LatLng pos2)
    {
        return Math.abs(pos1.latitude - pos2.latitude) < TOLERANCE &&
                Math.abs(pos1.longitude - pos2.longitude) < TOLERANCE;
    }

    private void fetchCurrentUserRole()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .whereEqualTo("email", user.getEmail())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                            String roleFromDb = userDoc.getString("role");
                            if (roleFromDb != null) {
                                currentUserRole = roleFromDb;
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FetchRole", "Hiba a szerep lekérésekor: " + e.getMessage());
                    });
        }
    }

}
