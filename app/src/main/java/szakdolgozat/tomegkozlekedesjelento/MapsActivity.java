package szakdolgozat.tomegkozlekedesjelento;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.Transliterator;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.Manifest.permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


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
    private PlacesClient placesClient;
    private FirebaseUser user;
    private Geocoder geocoder;
    private ImageView okIMG;
    private ImageView cancelIMG;
    private ImageView okIMG_editing;
    private ImageView cancelIMG_editing;
    private int selectedTransport = 0;
    private int selectedProblem = 0;
    private String problemDetailsText = "";
    private int delayMinutes = 0;
    private View currentBottomSheetView;

    EditText detailsEditText;
    EditText delayEditText;
    EditText startingCityEditText;
    EditText destinationCityEditText;


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
        cancelIMG = findViewById(R.id.img_cancel);
        okIMG.setVisibility(View.INVISIBLE);
        cancelIMG.setVisibility(View.INVISIBLE);
        okIMG_editing = findViewById(R.id.img_ok_editing);
        cancelIMG_editing = findViewById(R.id.img_cancel_editing);
        okIMG_editing.setVisibility(View.INVISIBLE);
        cancelIMG_editing.setVisibility(View.INVISIBLE);
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
        placesClient = Places.createClient(this);
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
                openEditingBottomSheetDialog();
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

        String meanOfTransport = report.getMeanOfTransport().toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ö", "o")
                .replace("ő", "o")
                .replace("ú", "u")
                .replace("ü", "u")
                .replace("ű", "u");

        int resourceId = getResources().getIdentifier(meanOfTransport, "drawable", getPackageName());
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resourceId);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 100, 100, false);

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

        Marker startMarker = null;
        if (!markerExists(startLatLng))
        {
            startMarker = mMap.addMarker(new MarkerOptions()
                    .position(startLatLng)
                    .title(report.getMarkerTitle(true))
                    .icon(icon)
                    .snippet(report.getMarkerSnippet(true, geocoder)));
        }

        Marker endMarker = null;
        if (!markerExists(endLatLng))
        {
            endMarker = mMap.addMarker(new MarkerOptions()
                    .position(endLatLng)
                    .title(report.getMarkerTitle(false))
                    .icon(icon)
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

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startingMarker.getPosition(), 20));
            okIMG.setVisibility(View.VISIBLE);
            cancelIMG.setVisibility(View.VISIBLE);
        }

        //a "no" gombra láthatatlanok lesznek a gombok, újra meg kell nyomni a report gombot
        cancelIMG.setOnClickListener(v ->
        {
            okIMG.setVisibility(View.INVISIBLE);
            cancelIMG.setVisibility(View.INVISIBLE);
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
            cancelIMG.setVisibility(View.INVISIBLE);

            setupSpinnersForReportSheet(sheetView);

            detailsEditText = sheetView.findViewById(R.id.et_problem_details);
            delayEditText = sheetView.findViewById(R.id.et_delay_duration);
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
        spinnerTransport = (Spinner) sheetView.findViewById(R.id.spinner_transport);
        ArrayAdapter<CharSequence> adapterTransport = ArrayAdapter.createFromResource(
                this,
                R.array.means_of_transport,
                android.R.layout.simple_spinner_item
        );

        adapterTransport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransport.setAdapter(adapterTransport);
        spinnerProblem = (Spinner) sheetView.findViewById(R.id.spinner_problem_type);
        ArrayAdapter<CharSequence> adapterProblem = ArrayAdapter.createFromResource(
                this,
                R.array.type_of_problem,
                android.R.layout.simple_spinner_item
        );
        adapterProblem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
                cancelIMG.setVisibility(View.VISIBLE);

                destinationMarker = mMap.addMarker(new MarkerOptions()
                        .position(candidate)
                        .title("Végállomás")
                        .draggable(true));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(candidate, 20));

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

    public void likeReport(View view)
    {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Report report = (Report) selectedMarker.getTag();
        if (currentUser == null) {
            Toast.makeText(this, "Be kell jelentkezned a like-hoz!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference likeRef = db
                .collection("reports")
                .document(report.getDocumentId())
                .collection("likes")
                .document(userId);

        likeRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists())
            {
                likeRef.delete().addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Like visszavonva", Toast.LENGTH_SHORT).show();
                });
                getLikeCount(report.getDocumentId(),currentBottomSheetView);
            } else
            {
                Map<String, Object> likeData = new HashMap<>();
                likeData.put("likedAt", FieldValue.serverTimestamp());

                likeRef.set(likeData).addOnSuccessListener(aVoid ->
                {
                    Toast.makeText(this, "Like hozzáadva", Toast.LENGTH_SHORT).show();
                });
                getLikeCount(report.getDocumentId(),currentBottomSheetView);
            }
        });
    }

    public void openEditingBottomSheetDialog()
    {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        currentBottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_report_modify_report, null);
        bottomSheetDialog.setContentView(currentBottomSheetView);


        spinnerTransport = currentBottomSheetView.findViewById(R.id.spinner_transport);
        spinnerProblem = currentBottomSheetView.findViewById(R.id.spinner_problem_type);

        detailsEditText = currentBottomSheetView.findViewById(R.id.et_problem_details);
        delayEditText = currentBottomSheetView.findViewById(R.id.et_delay_duration);
        startingCityEditText = currentBottomSheetView.findViewById(R.id.startingCity);
        destinationCityEditText = currentBottomSheetView.findViewById(R.id.destinationCity);
        Button editButton = currentBottomSheetView.findViewById(R.id.btn_edit);
        Button deleteButton = currentBottomSheetView.findViewById(R.id.btn_delete);
        Button cancelButton = currentBottomSheetView.findViewById(R.id.btn_cancel);
        Button editStartMarker = currentBottomSheetView.findViewById(R.id.btn_set_start_location);
        Button editDestinationMarker = currentBottomSheetView.findViewById(R.id.btn_set_end_location);

        Report report = (Report)selectedMarker.getTag();

        if (user == null || (!"admin".equals(currentUserRole) && !report.getUid().equals(user.getUid())))
        {
            spinnerTransport.setEnabled(false);
            spinnerProblem.setEnabled(false);
            detailsEditText.setEnabled(false);
            delayEditText.setEnabled(false);
            editButton.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            startingCityEditText.setVisibility(View.INVISIBLE);
            destinationCityEditText.setVisibility(View.INVISIBLE);
        }

        setupSpinnersForReportSheet(currentBottomSheetView);

        detailsEditText.setText(report.getDescription());
        delayEditText.setText(String.valueOf(report.getDelay()));
        startingCityEditText.setText(report.getCity(geocoder,report.getStartingLatitude(),report.getStartingLongitude()));
        destinationCityEditText.setText(report.getCity(geocoder,report.getDestinationLatitude(),report.getDestinationLongitude()));

        setSpinnerSelectionByValue(spinnerTransport, report.getMeanOfTransport());
        setSpinnerSelectionByValue(spinnerProblem, report.getType());

        editButton.setOnClickListener(v -> {
            editReport(report);
            bottomSheetDialog.dismiss();
        });

        // kezdőpont módosítása
        editStartMarker.setOnClickListener(v ->
        {
            bottomSheetDialog.dismiss();
            okIMG_editing.setVisibility(View.VISIBLE);
            cancelIMG_editing.setVisibility(View.VISIBLE);

            if(areLatLngEqual(new LatLng(report.getStartingLatitude(),report.getStartingLongitude()),selectedMarker.getPosition()))
            {
                //ha a kiválasztott marker az kezdő marker
                startingMarker = selectedMarker;
            }
            else
            {
                startingMarker = getPairedMarker(selectedMarker);
            }

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startingMarker.getPosition(), 11));

            startingMarker.setDraggable(true);

            okIMG_editing.setOnClickListener(v1 -> {
                startingCityEditText.setText(report.getCity(geocoder,startingMarker.getPosition().latitude,startingMarker.getPosition().longitude));
                okIMG_editing.setVisibility(View.INVISIBLE);
                cancelIMG_editing.setVisibility(View.INVISIBLE);
                report.setStartingLatitude(startingMarker.getPosition().latitude);
                report.setStartingLongitude(startingMarker.getPosition().longitude);
                bottomSheetDialog.show();
            });
            cancelIMG_editing.setOnClickListener(v1 ->
            {
                bottomSheetDialog.show();
                okIMG_editing.setVisibility(View.INVISIBLE);
                cancelIMG_editing.setVisibility(View.INVISIBLE);

                startingMarker.setPosition(new LatLng(report.getStartingLatitude(),report.getStartingLongitude()));
            });
        });

        editDestinationMarker.setOnClickListener(v ->
        {
            bottomSheetDialog.dismiss();
            okIMG_editing.setVisibility(View.VISIBLE);
            cancelIMG_editing.setVisibility(View.VISIBLE);

            if(areLatLngEqual(new LatLng(report.getDestinationLatitude(),report.getDestinationLatitude()),selectedMarker.getPosition()))
            {
                //ha a kiválasztott marker az kezdő marker
                destinationMarker = selectedMarker;
            }
            else
            {
                destinationMarker = getPairedMarker(selectedMarker);
            }

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationMarker.getPosition(), 11));

            destinationMarker.setDraggable(true);

            okIMG_editing.setOnClickListener(v1 -> {
                destinationCityEditText.setText(report.getCity(geocoder,destinationMarker.getPosition().latitude,destinationMarker.getPosition().longitude));
                okIMG_editing.setVisibility(View.INVISIBLE);
                cancelIMG_editing.setVisibility(View.INVISIBLE);

                report.setDestinationLatitude(destinationMarker.getPosition().latitude);
                report.setDestinationLongitude(destinationMarker.getPosition().longitude);

                bottomSheetDialog.show();
            });
            cancelIMG_editing.setOnClickListener(v1 ->
            {
                bottomSheetDialog.show();
                okIMG_editing.setVisibility(View.INVISIBLE);
                cancelIMG_editing.setVisibility(View.INVISIBLE);

                destinationMarker.setPosition(new LatLng(report.getDestinationLatitude(),report.getDestinationLongitude()));
            });
        });


        deleteButton.setOnClickListener(v -> {
            removePairMarkers(report);
            bottomSheetDialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        if (report != null && report.getDocumentId() != null)
        {
            getLikeCount(report.getDocumentId(), currentBottomSheetView);
        }
        bottomSheetDialog.show();
    }

    private void setSpinnerSelectionByValue(Spinner spinner, String value)
    {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
    private void editReport(Report report)
    {
        String selectedTransport = spinnerTransport.getSelectedItem().toString();
        String selectedProblemType = spinnerProblem.getSelectedItem().toString();
        String details = detailsEditText.getText().toString();
        String delayText = delayEditText.getText().toString();

        int delay = 0;
        try {
            delay = Integer.parseInt(delayText);
        } catch (NumberFormatException e) {
            Log.e("Edit_Report", "Hibás késés érték: " + delayText);
        }

        report.setMeanOfTransport(selectedTransport);
        report.setType(selectedProblemType);
        report.setDescription(details);
        report.setDelay(delay);
        if (destinationMarker != null)
        {
            report.setDestinationLongitude(destinationMarker.getPosition().longitude);
            report.setDestinationLatitude(destinationMarker.getPosition().latitude);
        }
        if (startingMarker != null)
        {
            report.setStartingLatitude(startingMarker.getPosition().latitude);
            report.setStartingLongitude(startingMarker.getPosition().longitude);
        }

        String docId = report.getDocumentId();
        if (docId != null && !docId.isEmpty()) {
            db.collection("reports").document(docId)
                    .set(report)
                    .addOnSuccessListener(aVoid -> Log.d("Edit_Report", "Report sikeresen frissítve"))
                    .addOnFailureListener(e -> Log.e("Edit_Report", "Hiba történt a frissítés során", e));
        } else {
            Log.e("Edit_Report", "Nincs documentId beállítva, nem lehet frissíteni");
        }
    }
    private void removePairMarkers(Report report)
    {
        //a kiválasztott marker törlése
        Iterator<MarkerPair> iterator = this.markerPairs.iterator();
        while (iterator.hasNext()) {
            MarkerPair item = iterator.next();
            if (selectedMarker.getId().equals(item.startMarker.getId()))
            {
                item.startMarker.remove();
            }
            else if(selectedMarker.getId().equals(item.endMarker.getId()))
            {
                item.endMarker.remove();
            }
        }

        //marker párjának törlése
        Marker pairMarker = getPairedMarker(selectedMarker);
        selectedMarker.remove();

        //marker lista párok törlése
        if (pairMarker != null)
        {
            pairMarker.remove();

            MarkerPair toRemove = null;
            for (MarkerPair pair : markerPairs)
            {
                if ((pair.startMarker.equals(selectedMarker) && pair.endMarker.equals(pairMarker)) ||
                        (pair.startMarker.equals(pairMarker) && pair.endMarker.equals(selectedMarker)))
                {
                    toRemove = pair;
                    break;
                }
            }
            if (toRemove != null) {
                markerPairs.remove(toRemove);
            }
        }

        //report törlése adatbázisban
        db.collection("reports").document(report.getDocumentId()).delete()
                .addOnSuccessListener(aVoid -> Log.d("Delete_Report", "Document successfully deleted!"))
                .addOnFailureListener(e -> Log.e("Delete_Report", "Error deleting document", e));

    }

    private Marker getPairedMarker(Marker selected)
    {
        for (MarkerPair pair : markerPairs)
        {
            if (pair.startMarker.getId().equals(selected.getId()))
            {
                return pair.endMarker;
            }
            if (pair.endMarker.getId().equals(selected.getId()))
            {
                return pair.startMarker;
            }
        }
        return null;
    }
    private void getLikeCount(String reportId, final View rootView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reports")
                .document(reportId)
                .collection("likes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int likeCount = queryDocumentSnapshots.size();

                    TextView likeCountTextView = rootView.findViewById(R.id.tv_like_count);
                    likeCountTextView.setText(String.valueOf(likeCount));
                })
                .addOnFailureListener(e -> {
                    Log.e("LikeCount", "Hiba a lájkok lekérésekor: ", e);
                });
    }
}
