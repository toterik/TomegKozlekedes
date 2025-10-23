package szakdolgozat.tomegkozlekedesjelento;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocalDate;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
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
import com.google.type.DateTime;

import android.Manifest.permission;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


import szakdolgozat.tomegkozlekedesjelento.Model.Applicant;
import szakdolgozat.tomegkozlekedesjelento.Model.ApplicantMarkerInfo;
import szakdolgozat.tomegkozlekedesjelento.Model.CarReport;
import szakdolgozat.tomegkozlekedesjelento.Model.MarkerPair;
import szakdolgozat.tomegkozlekedesjelento.Model.Report;
import szakdolgozat.tomegkozlekedesjelento.databinding.ActivityMapsBinding;

public class MapsActivity extends MenuForAllActivity implements OnMapReadyCallback
{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int POINTS_FOR_CAR_REPORT_PER_USER = 5;
    private static final int POINTS_FOR_REPORT_PER_LIKE = 1;
    private Marker selectedMarker;
    private static final double TOLERANCE = 0.00005;
    Set<MarkerPair> markerPairs = new HashSet<>();
    private String currentUserRole = "";
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore db;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private AutocompleteSupportFragment autocompleteSupportFragmentCarReport;

    private ArrayList<Marker> reportmarkersList = new ArrayList<>();
    private ArrayList<Marker> carReportmarkersList = new ArrayList<>();
    private ArrayList<Marker> applicantMarkersList = new ArrayList<>();
    private static final Map<String, String> transportMap = new HashMap<>();
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
    private int startHour, startMinute;
    private int endHour, endMinute;
    private Handler locationHandler = new Handler();
    private Runnable locationUpdater;


    static
    {
        transportMap.put("busz", "busz");
        transportMap.put("bus", "busz");
        transportMap.put("vonat", "vonat");
        transportMap.put("train","vonat");
        transportMap.put("tram","villamos");
        transportMap.put("villamos","villamos");
        transportMap.put("troli","troli");
        transportMap.put("trolley","troli");
    }

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
        //displayAllMarkers();
        liveMarkerTracker();
        //zoom and rotation settings on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);


        mMap.setOnMarkerClickListener(marker ->
        {
            selectedMarker = marker;
            if(marker.getTag() != null)
            {
                if (marker.getTag().getClass() == Report.class)
                {
                    Report report = (Report) marker.getTag();
                    if (report != null)
                    {
                        openEditingBottomSheetDialog();
                    }
                } else if (marker.getTag().getClass() == CarReport.class)
                {
                    CarReport carReport = (CarReport) marker.getTag();
                    if (carReport != null)
                    {
                        openCarReportDetailsBottomSheet(carReport);
                    }
                } else if (marker.getTag().getClass() == ApplicantMarkerInfo.class)
                {
                    ApplicantMarkerInfo applicantMarkerInfo = (ApplicantMarkerInfo) marker.getTag();
                    if (applicantMarkerInfo != null)
                    {
                        openApplicantBottomSheet(applicantMarkerInfo.getApplicant(), applicantMarkerInfo.getCarReport());
                    }
                }
                return true;
            }
            return false;
        });
        checkAndStartCarReportTracking();
    }

    public void openApplicantBottomSheet(Applicant applicant, CarReport carReport)
    {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottomsheet_applicant, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tvName = sheetView.findViewById(R.id.tv_applicant_name);
        Button btnAccept = sheetView.findViewById(R.id.btn_accept);
        Button btnReject = sheetView.findViewById(R.id.btn_reject);

        // Firestore lekérés a user emailéhez
        FirebaseFirestore.getInstance().collection("Users")
                .document(applicant.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {
                    if (documentSnapshot.exists())
                    {
                        String email = documentSnapshot.getString("username");
                        tvName.setText(email);
                    } else
                    {
                        tvName.setText(getString(R.string.ismeretlen_felhasznalo));
                    }
                });

        bottomSheetDialog.show();
        // Elfogadás gomb
        btnAccept.setOnClickListener(v ->
        {
            FirebaseFirestore.getInstance().collection("carReports")
                    .document(carReport.getDocumentId())
                    .update(
                            "accepted", FieldValue.arrayUnion(applicant.getUid()),
                            "applicants", FieldValue.arrayRemove(applicant)
                    )
                    .addOnSuccessListener(unused ->
                    {
                        Toast.makeText(this, getString(R.string.elfogadva), Toast.LENGTH_SHORT).show();
                        removeApplicantMarkerLive(applicant);
                        bottomSheetDialog.dismiss();
                    });
        });

        // Elutasítás gomb
        btnReject.setOnClickListener(v ->
        {
            FirebaseFirestore.getInstance().collection("carReports")
                    .document(carReport.getDocumentId())
                    .update("applicants", FieldValue.arrayRemove(applicant))
                    .addOnSuccessListener(unused ->
                    {
                        carReport.getApplicants().remove(applicant);
                        Toast.makeText(this, getString(R.string.elutas_tva), Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        removeApplicantMarkerLive(applicant);
                    });
        });

    }

    private void openCarReportDetailsBottomSheet(CarReport carReport)
    {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_carreport_details, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tvDriver = sheetView.findViewById(R.id.tv_driver);
        TextView tvFromTo = sheetView.findViewById(R.id.tv_from_to);
        TextView tvSeats = sheetView.findViewById(R.id.tv_seats);
        TextView tvComment = sheetView.findViewById(R.id.tv_comment);
        Button btnJoin = sheetView.findViewById(R.id.btn_join);
        Button endCarReport = sheetView.findViewById(R.id.btn_endCarReport);

        String currentUid = "";
        if (user != null)
        {
            currentUid = user.getUid();
        }
        if (currentUid.equals(carReport.getUid()))
        {
            btnJoin.setVisibility(View.INVISIBLE);
            endCarReport.setOnClickListener(v ->
            {
                carReport.setActive(false);
                removeCarReportMarkerLive(carReport);
                addPointsForCarReport(carReport);
                db.collection("carReports").document(carReport.getDocumentId()).delete()
                        .addOnSuccessListener(aVoid -> Log.d("Delete_Report", "Document successfully deleted!"))
                        .addOnFailureListener(e -> Log.e("Delete_Report", "Error deleting document", e));
                bottomSheetDialog.dismiss();
                stopUpdatingLocation();
            });
        } else if (user != null)
        {
            btnJoin.setVisibility(View.VISIBLE);
            endCarReport.setVisibility(View.INVISIBLE);
        }
        else
        {
            btnJoin.setVisibility(View.INVISIBLE);
            endCarReport.setVisibility(View.INVISIBLE);
        }

        // Sofőr UID alapján lekérjük a felhasználónevet
        db.collection("Users").document(carReport.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {
                    if (documentSnapshot.exists())
                    {
                        String username = documentSnapshot.getString("username");
                        tvDriver.setText(tvDriver.getText()+ " " + username);
                    } else
                    {
                        tvDriver.setText(tvDriver.getText() +" "+ getString(R.string.ismeretlen));
                    }
                })
                .addOnFailureListener(e -> {
                    tvDriver.setText("Sofőr: hiba a lekéréskor");
                    Log.e("CarReport", "Hiba a sofőr lekérésekor: " + e.getMessage());
                });

        tvFromTo.setText(carReport.getFromCity() + " → " + carReport.getToCity());

        int bookedSeats = carReport.getAccepted().size() + 1;
        tvSeats.setText(getString(R.string.helyek) + bookedSeats + "/" + carReport.getSeatsAvailable());
        tvComment.setText(getString(R.string.megjegyz_s) + (carReport.getComment().isEmpty() ? "" : carReport.getComment()));

        if (bookedSeats >= carReport.getSeatsAvailable())
        {
            btnJoin.setEnabled(false);
            btnJoin.setText(getString(R.string.betelt));
        }

        btnJoin.setOnClickListener(v ->
        {
            var User = FirebaseAuth.getInstance().getCurrentUser();

            boolean alreadyApplied = carReport.getApplicants() != null &&
                    carReport.getApplicants().stream().anyMatch(app -> User.getUid().equals(app.getUid()));

            boolean alreadyAccepted = carReport.getAccepted() != null &&
                    carReport.getAccepted().contains(user.getUid());

            if (alreadyApplied || alreadyAccepted)
            {
                Toast.makeText(this, getString(R.string.toast_already_applied), Toast.LENGTH_SHORT).show();
                return;
            }

            Applicant applicant = new Applicant(user.getUid(), userCurrentLocation.latitude, userCurrentLocation.longitude);
            db.collection("carReports")
                    .document(carReport.getDocumentId())
                    .update("applicants", FieldValue.arrayUnion(applicant))
                    .addOnSuccessListener(aVoid ->
                    {
                        Toast.makeText(this, getString(R.string.toast_application_sent), Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                    {
                        Toast.makeText(this, getString(R.string.toast_error_occurred) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        bottomSheetDialog.show();
    }

    private void liveMarkerTracker()
    {
        LocalDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int nowMinutes = localDateTime.getHour() * 60 + localDateTime.getMinute();
        db.collection("reports")
                .whereLessThanOrEqualTo("start_minutes", nowMinutes)
                .whereGreaterThanOrEqualTo("end_minutes", nowMinutes)
                .addSnapshotListener((snapshots, e) ->
                {
                    if (e != null)
                    {
                        Log.w("TAG", "listen:error", e);
                        return;
                    }
                    for (DocumentChange dc : snapshots.getDocumentChanges())
                    {
                        Report report = dc.getDocument().toObject(Report.class);
                        report.setDocumentId(dc.getDocument().getId());
                        switch (dc.getType()) {
                            case ADDED:
                                addMarkersFromReport(report);
                                break;
                            case MODIFIED:
                                removeReportMarkerLive(report);
                                addMarkersFromReport(report);
                                break;
                            case REMOVED:
                                removeReportMarkerLive(report);
                                break;
                        }
                    }
                });

        db.collection("carReports")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("TAG", "listen:error", e);
                        return;
                    }


                    for (DocumentChange dc : snapshots.getDocumentChanges())
                    {
                        CarReport carReport = dc.getDocument().toObject(CarReport.class);
                        carReport.setDocumentId(dc.getDocument().getId());
                        switch (dc.getType())
                        {
                            case ADDED:
                                addMarkersFromCarReport(carReport);
                                break;
                            case MODIFIED:
                                removeCarReportMarkerLive(carReport);
                                addMarkersFromCarReport(carReport);
                                break;
                            case REMOVED:
                                removeCarReportMarkerLive(carReport);
                                break;
                        }
                    }
                });
    }

    private void updateCarReportLive(CarReport updatedCarReport) {
        String targetId = updatedCarReport.getDocumentId();

        for (Marker marker : carReportmarkersList) {
            Object tag = marker.getTag();
            if (tag instanceof CarReport) {
                CarReport existingReport = (CarReport) tag;

                if (targetId.equals(existingReport.getDocumentId())) {

                    LatLng newPos = new LatLng(
                            updatedCarReport.getStartingLatitude(),
                            updatedCarReport.getStartingLongitude()
                    );
                    marker.setPosition(newPos);
                    break;
                }
            }
        }
        if (user != null && updatedCarReport.getUid().equals(user.getUid()))
        {
            int applicantResId = getResources().getIdentifier("applicant", "drawable", getPackageName());

            for (Applicant applicant : updatedCarReport.getApplicants())
            {
                LatLng applicantLatLng = new LatLng(applicant.getStartingLatitude(), applicant.getStartingLongitude());
                if (!markerExists(applicantLatLng))
                {
                    Marker applicantMarker = mMap.addMarker(new MarkerOptions()
                        .position(applicantLatLng)
                        .icon(getIcon("applicant")));

                    if (applicantMarker != null)
                    {
                        applicantMarker.setTag(new ApplicantMarkerInfo(applicant,updatedCarReport));
                    }
                }
            }
        }
    }



    private void removeReportMarkerLive(Report report)
    {
        double starting_latitude = report.getStartingLatitude();
        double starting_longitude = report.getStartingLongitude();
        double destination_latitude = report.getDestinationLatitude();
        double destination_longitude = report.getDestinationLongitude();

        Iterator<Marker> iterator = reportmarkersList.iterator();
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

    private void removeApplicantMarkerLive(Applicant applicant)
    {
        double starting_latitude = applicant.getStartingLatitude();
        double starting_longitude = applicant.getStartingLongitude();


        Iterator<Marker> iterator = applicantMarkersList.iterator();
        while (iterator.hasNext())
        {
            Marker marker = iterator.next();
            LatLng markerPosition = marker.getPosition();

            if (areLatLngEqual(markerPosition, new LatLng(starting_latitude, starting_longitude)))
            {
                marker.remove();
                iterator.remove();
            }
        }
    }

    private void removeCarReportMarkerLive(CarReport carReport)
    {
        String targetId = carReport.getDocumentId();

        Iterator<Marker> iterator = carReportmarkersList.iterator();
        while (iterator.hasNext())
        {
            Marker marker = iterator.next();
            Object tag = marker.getTag();

            if (tag instanceof CarReport)
            {
                CarReport taggedReport = (CarReport) tag;
                if (targetId.equals(taggedReport.getDocumentId()))
                {
                    marker.remove();
                    iterator.remove();
                }
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
    private void checkAndStartCarReportTracking()
    {
        if (user == null) return;

        db.collection("carReports")
                .whereEqualTo("uid", user.getUid())
                .get()
                .addOnSuccessListener(querySnapshot ->
                {
                    if (!querySnapshot.isEmpty())
                    {
                        for (DocumentSnapshot doc : querySnapshot)
                        {
                            CarReport carReport = doc.toObject(CarReport.class);
                            if (carReport != null)
                            {
                                carReport.setDocumentId(doc.getId());
                                startUpdatingLocation(carReport);
                                break;
                            }
                        }
                    } else
                    {
                        Log.d("CarReport", "No active car reports for this user.");
                    }
                })
                .addOnFailureListener(e -> Log.e("CarReport", "Failed to check active car reports", e));
    }

    @SuppressLint("MissingPermission")
    private void startUpdatingLocation(CarReport carReport)
    {
        locationUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                fusedLocationProviderClient.getLastLocation()
                        .addOnCompleteListener(task ->
                        {
                            if (task.isSuccessful() && task.getResult() != null)
                            {
                                Location location = task.getResult();

                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                db.collection("carReports")
                                        .document(carReport.getDocumentId())
                                        .update("starting_latitude", location.getLatitude(),
                                                "starting_longitude", location.getLongitude())
                                        .addOnSuccessListener(aVoid ->
                                        {
                                            for (Marker marker : carReportmarkersList)
                                            {
                                                Object tag = marker.getTag();
                                                if (tag instanceof CarReport)
                                                {
                                                    CarReport cr = (CarReport) tag;
                                                    if (cr.getDocumentId().equals(carReport.getDocumentId()))
                                                    {
                                                        LatLng markerPos = marker.getPosition();
                                                        LatLng startPos = new LatLng(cr.getStartingLatitude(), cr.getStartingLongitude());

                                                        if (areLatLngEqual(markerPos, startPos))
                                                        {
                                                            cr.setStartingLatitude(location.getLatitude());
                                                            cr.setStartingLongitude(location.getLongitude());
                                                            marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        });

                            }
                        });
                locationHandler.postDelayed(this, 10000);
            }
        };

        locationHandler.post(locationUpdater);
    }


    private void stopUpdatingLocation() {
        if (locationUpdater != null) {
            locationHandler.removeCallbacks(locationUpdater);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.logout_item)
        {
            stopUpdatingLocation();
        }
        return super.onOptionsItemSelected(item);
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
                Toast.makeText(this, getString(R.string.toast_location_permission_required), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void startProblemReport(View view)
    {
        if (userIsLoggedIn) openReportSheetDialog();
        else
            Toast.makeText(this, getString(R.string.toast_login_required_for_report), Toast.LENGTH_SHORT).show();
    }

    public void displayAllMarkers() {
        LocalDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int nowMinutes = localDateTime.getHour() * 60 + localDateTime.getMinute();

        Task<QuerySnapshot> reportsTask = db.collection("reports")
                .whereLessThanOrEqualTo("start_minutes", nowMinutes)
                .whereGreaterThanOrEqualTo("end_minutes", nowMinutes)
                .get();

        Task<QuerySnapshot> carReportsTask = db.collection("carReports")
                .get();

        Tasks.whenAllSuccess(reportsTask, carReportsTask)
                .addOnSuccessListener(results -> {
                    QuerySnapshot reportsSnapshot = (QuerySnapshot) results.get(0);
                    QuerySnapshot carReportsSnapshot = (QuerySnapshot) results.get(1);

                    for (QueryDocumentSnapshot document : reportsSnapshot) {
                        Report currentReport = document.toObject(Report.class);
                        currentReport.setDocumentId(document.getId());
                        addMarkersFromReport(currentReport);
                    }

                    for (QueryDocumentSnapshot document : carReportsSnapshot) {
                        CarReport currentCarReport = document.toObject(CarReport.class);
                        currentCarReport.setDocumentId(document.getId());
                        addMarkersFromCarReport(currentCarReport);
                    }

                    liveMarkerTracker();
                })
                .addOnFailureListener(e -> Log.e("Marker_Fail", "Error fetching markers", e));
    }

    private void addMarkersFromCarReport(CarReport currentCarReport)
    {
        LatLng startLatLng = new LatLng(currentCarReport.getStartingLatitude(), currentCarReport.getStartingLongitude());
        LatLng endLatLng = new LatLng(currentCarReport.getDestinationLatitude(), currentCarReport.getDestinationLongitude());

        int resourceId = getResources().getIdentifier("car", "drawable", getPackageName());
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resourceId);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 100, 100, false);

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

        Marker startMarker = null;
        if (!markerExists(startLatLng))
        {
            startMarker = mMap.addMarker(new MarkerOptions()
                    .position(startLatLng)
                    .icon(icon));
        }

        Marker endMarker = null;
        if (!markerExists(endLatLng))
        {
            endMarker = mMap.addMarker(new MarkerOptions()
                    .position(endLatLng)
                    .icon(icon));
        }

        if (startMarker != null && endMarker != null)
        {
            startMarker.setTag(currentCarReport);
            endMarker.setTag(currentCarReport);

            carReportmarkersList.add(startMarker);
            carReportmarkersList.add(endMarker);
        }
        if (user != null && currentCarReport.getUid().equals(user.getUid()))
        {
            // Applicants hozzáadása a térképhez
            int applicantResId = getResources().getIdentifier("applicant", "drawable", getPackageName());
            Bitmap applicantBitmap = BitmapFactory.decodeResource(getResources(), applicantResId);
            Bitmap resizedApplicantBitmap = Bitmap.createScaledBitmap(applicantBitmap, 100, 100, false);
            BitmapDescriptor applicantIcon = BitmapDescriptorFactory.fromBitmap(resizedApplicantBitmap);

            for (Applicant applicant : currentCarReport.getApplicants())
            {
                LatLng applicantLatLng = new LatLng(applicant.getStartingLatitude(), applicant.getStartingLongitude());
                if (!markerExists(applicantLatLng))
                {
                    Marker applicantMarker = mMap.addMarker(new MarkerOptions()
                            .position(applicantLatLng)
                            .icon(applicantIcon));
                    if (applicantMarker != null)
                    {
                        applicantMarker.setTag(new ApplicantMarkerInfo(applicant, currentCarReport));
                        applicantMarkersList.add(applicantMarker);
                    }
                }
            }
        }

    }

    private boolean markerExists(LatLng position)
    {
        for (Marker m : reportmarkersList)
        {
            if (m.getPosition().equals(position))
            {
                return true;
            }
        }
        return false;
    }
    private final Map<String, BitmapDescriptor> iconCache = new HashMap<>();
    private BitmapDescriptor getIcon(String iconName) {
        if (iconCache.containsKey(iconName)) {
            return iconCache.get(iconName);
        }
        int resourceId = getResources().getIdentifier(iconName, "drawable", getPackageName());

        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 100, 100, false);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

        iconCache.put(iconName, icon);
        return icon;
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

        Marker startMarker = null;
        String iconName = transportMap.get(meanOfTransport);
        if (!markerExists(startLatLng))
        {
            startMarker = mMap.addMarker(new MarkerOptions()
                    .position(startLatLng)
                    .title(report.getMarkerTitle(true))
                    .icon(getIcon(iconName)));
        }

        Marker endMarker = null;
        if (!markerExists(endLatLng))
        {
            endMarker = mMap.addMarker(new MarkerOptions()
                    .position(endLatLng)
                    .title(report.getMarkerTitle(false))
                    .icon(getIcon(iconName)));
        }

        if (startMarker != null && endMarker != null)
        {
            startMarker.setTag(report);
            endMarker.setTag(report);

            reportmarkersList.add(startMarker);
            reportmarkersList.add(endMarker);

            markerPairs.add(new MarkerPair(startMarker, endMarker));
        }
        else if(startMarker != null)
        {
            startMarker.setTag(report);
            reportmarkersList.add(startMarker);
        }
        else if(endMarker != null)
        {
            endMarker.setTag(report);
            reportmarkersList.add(endMarker);
        }
    }

    public void openReportSheetDialog()
    {
        startHour = 0;
        startMinute = 0;

        endHour = 0;
        endMinute = 0;
        if (startingMarker == null)
        {
            LatLng candidate = userCurrentLocation;
            int attempts = 0;
            while (isPositionTaken(candidate) && attempts < 100)
            {
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

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startingMarker.getPosition(), 15));
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
                try
                {
                    List<Address> addresses = geocoder.getFromLocation(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, 1);
                    if (addresses != null && !addresses.isEmpty())
                    {
                        String city = addresses.get(0).getLocality();
                        autocompleteSupportFragment.setText(city);
                    } else
                    {
                        autocompleteSupportFragment.setText(getString(R.string.destination_not_found));
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                    autocompleteSupportFragment.setText(getString(R.string.toast_error_occurred));
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
            Button pickStartTime = sheetView.findViewById(R.id.btn_pick_start_time);
            Button pickEndTime = sheetView.findViewById(R.id.btn_pick_end_time);

            configureTimePickers(pickStartTime,pickEndTime);

            pickStartTime.setText(String.format("%02d:%02d", startHour, startMinute));
            pickEndTime.setText(String.format("%02d:%02d", endHour, endMinute));

            detailsEditText.setText(problemDetailsText);
            delayEditText.setText(String.valueOf(delayMinutes));
            spinnerTransport.setSelection(selectedTransport);
            spinnerProblem.setSelection(selectedProblem);

            bottomSheetDialog.setOnDismissListener(x ->
            {
                if (spinnerTransport != null && spinnerProblem != null && delayEditText != null && detailsEditText != null)
                {
                    selectedTransport = (int) spinnerTransport.getSelectedItemId();
                    selectedProblem = (int) spinnerProblem.getSelectedItemId();
                    problemDetailsText = detailsEditText.getText().toString();
                    try
                    {
                        delayMinutes = Integer.parseInt(delayEditText.getText().toString());
                    } catch (Exception e)
                    {
                        delayMinutes = 0;
                    }
                }
            });


            cancelButton.setOnClickListener(x ->
            {
                bottomSheetDialog.dismiss();
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
                removeAutoFragment(autocompleteSupportFragment);
            });

            submitButton.setOnClickListener(x -> {
                // Get input data
                String meanOfTransport = spinnerTransport.getSelectedItem().toString();
                String problemType = spinnerProblem.getSelectedItem().toString();
                String problemDetails = detailsEditText.getText().toString();

                if (destinationMarker == null) {
                    Toast.makeText(this, getString(R.string.toast_destination_required), Toast.LENGTH_SHORT).show();
                    return;
                }

                int delay;
                try {
                    delay = Integer.parseInt(delayEditText.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.toast_delay_required), Toast.LENGTH_SHORT).show();
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
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        startHour * 60 + startMinute,
                        endHour * 60 + endMinute
                );
                report.save(db);

                if (startingMarker != null) startingMarker.remove();
                if (destinationMarker != null) destinationMarker.remove();
                startingMarker = null;
                destinationMarker = null;

                bottomSheetDialog.dismiss();
                removeAutoFragment(autocompleteSupportFragment);

                Toast.makeText(this, getString(R.string.toast_report_submitted), Toast.LENGTH_SHORT).show();
            });


            bottomSheetDialog.show();
        });
    }

    private void configureTimePickers(Button pickStartTime, Button pickEndTime)
    {

        pickStartTime.setOnClickListener(v2 -> {
            new TimePickerFragment((hour, minute) -> {
                startHour = hour;
                startMinute = minute;
                pickStartTime.setText(String.format("%02d:%02d", hour, minute));
            }).show(getSupportFragmentManager(), "startTimePicker");
        });

        pickEndTime.setOnClickListener(v2 -> {
            new TimePickerFragment((hour, minute) -> {
                endHour = hour;
                endMinute = minute;
                pickEndTime.setText(String.format("%02d:%02d", hour, minute));
            }).show(getSupportFragmentManager(), "endTimePicker");
        });
    }

    private void removeAutoFragment(AutocompleteSupportFragment autocompleteSupportFragment)
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

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(candidate, 15));

                removeAutoFragment(autocompleteSupportFragment);
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
                    .addOnSuccessListener(querySnapshot ->
                    {
                        if (!querySnapshot.isEmpty())
                        {
                            DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                            String roleFromDb = userDoc.getString("role");
                            if (roleFromDb != null)
                            {
                                currentUserRole = roleFromDb;
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                    {
                        Log.e("FetchRole", "Hiba a szerep lekérésekor: " + e.getMessage());
                    });
        }
    }

    public void likeReport(View view)
    {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Report report = (Report) selectedMarker.getTag();
        if (currentUser == null)
        {
            Toast.makeText(this, getString(R.string.toast_login_required_for_like), Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference likeRef = db
                .collection("reports")
                .document(report.getDocumentId())
                .collection("likes")
                .document(userId);

        likeRef.get().addOnSuccessListener(documentSnapshot ->
        {
            if (documentSnapshot.exists())
            {
                likeRef.delete().addOnSuccessListener(aVoid ->
                {
                    Toast.makeText(this, getString(R.string.toast_like_removed), Toast.LENGTH_SHORT).show();
                });
                getLikeCount(report.getDocumentId(), currentBottomSheetView);
            } else
            {
                Map<String, Object> likeData = new HashMap<>();
                likeData.put("likedAt", FieldValue.serverTimestamp());

                likeRef.set(likeData).addOnSuccessListener(aVoid ->
                {
                    Toast.makeText(this, getString(R.string.toast_like_added), Toast.LENGTH_SHORT).show();
                });
                getLikeCount(report.getDocumentId(), currentBottomSheetView);
            }
        });
    }

    public void openEditingBottomSheetDialog()
    {
        startHour = 0;
        startMinute = 0;

        endHour = 0;
        endMinute = 0;
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
        Button pickStartTime = currentBottomSheetView.findViewById(R.id.btn_pick_start_time);
        Button pickEndTime = currentBottomSheetView.findViewById(R.id.btn_pick_end_time);

        configureTimePickers(pickStartTime,pickEndTime);

        Report report = (Report) selectedMarker.getTag();

        if (user == null || (!"admin".equals(currentUserRole) && !report.getUid().equals(user.getUid())))
        {
            spinnerTransport.setEnabled(false);
            spinnerProblem.setEnabled(false);
            detailsEditText.setEnabled(false);
            delayEditText.setEnabled(false);
            editButton.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            editStartMarker.setVisibility(View.GONE);
            editDestinationMarker.setVisibility(View.GONE);
            startingCityEditText.setEnabled(false);
            destinationCityEditText.setEnabled(false);
            pickStartTime.setEnabled(false);
            pickEndTime.setEnabled(false);
        }

        setupSpinnersForReportSheet(currentBottomSheetView);

        detailsEditText.setText(report.getDescription());
        delayEditText.setText(String.valueOf(report.getDelay()));
        startingCityEditText.setText(report.getCity(geocoder, report.getStartingLatitude(), report.getStartingLongitude()));
        destinationCityEditText.setText(report.getCity(geocoder, report.getDestinationLatitude(), report.getDestinationLongitude()));

        startMinute = report.getStartMinutes();
        endMinute = report.getEndMinutes();

        pickStartTime.setText(String.format("%02d:%02d", startMinute / 60, startMinute % 60));
        pickEndTime.setText(String.format("%02d:%02d", endMinute / 60, endMinute % 60));

        setSpinnerSelectionByValue(spinnerTransport, report.getMeanOfTransport());
        setSpinnerSelectionByValue(spinnerProblem, report.getType());

        editButton.setOnClickListener(v ->
        {
            editReport(report);
            bottomSheetDialog.dismiss();
        });

        // kezdőpont módosítása
        editStartMarker.setOnClickListener(v ->
        {
            bottomSheetDialog.dismiss();
            okIMG_editing.setVisibility(View.VISIBLE);
            cancelIMG_editing.setVisibility(View.VISIBLE);

            if (areLatLngEqual(new LatLng(report.getStartingLatitude(), report.getStartingLongitude()), selectedMarker.getPosition()))
            {
                //ha a kiválasztott marker az kezdő marker
                startingMarker = selectedMarker;
            } else
            {
                startingMarker = getPairedMarker(selectedMarker);
            }

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startingMarker.getPosition(), 11));

            startingMarker.setDraggable(true);

            okIMG_editing.setOnClickListener(v1 ->
            {
                startingCityEditText.setText(report.getCity(geocoder, startingMarker.getPosition().latitude, startingMarker.getPosition().longitude));
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

                startingMarker.setPosition(new LatLng(report.getStartingLatitude(), report.getStartingLongitude()));
            });
        });

        editDestinationMarker.setOnClickListener(v ->
        {
            bottomSheetDialog.dismiss();
            okIMG_editing.setVisibility(View.VISIBLE);
            cancelIMG_editing.setVisibility(View.VISIBLE);

            if (areLatLngEqual(new LatLng(report.getDestinationLatitude(), report.getDestinationLatitude()), selectedMarker.getPosition()))
            {
                //ha a kiválasztott marker az kezdő marker
                destinationMarker = selectedMarker;
            } else
            {
                destinationMarker = getPairedMarker(selectedMarker);
            }

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationMarker.getPosition(), 11));

            destinationMarker.setDraggable(true);

            okIMG_editing.setOnClickListener(v1 ->
            {
                destinationCityEditText.setText(report.getCity(geocoder, destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude));
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

                destinationMarker.setPosition(new LatLng(report.getDestinationLatitude(), report.getDestinationLongitude()));
            });
        });


        deleteButton.setOnClickListener(v ->
        {
            removePairMarkers(report);
            bottomSheetDialog.dismiss();
        });

        cancelButton.setOnClickListener(v ->
        {
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
        for (int i = 0; i < spinner.getCount(); i++)
        {
            if (spinner.getItemAtPosition(i).toString().equals(value))
            {
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
        try
        {
            delay = Integer.parseInt(delayText);
        } catch (NumberFormatException e)
        {
            Log.e("Edit_Report", "Hibás késés érték: " + delayText);
        }

        report.setMeanOfTransport(selectedTransport);
        report.setType(selectedProblemType);
        report.setDescription(details);
        report.setDelay(delay);
        report.setStartMinutes(startHour * 60 + startMinute);
        report.setEndMinutes(endHour * 60 + endMinute);

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
        if (docId != null && !docId.isEmpty())
        {
            db.collection("reports").document(docId)
                    .set(report)
                    .addOnSuccessListener(aVoid -> Log.d("Edit_Report", "Report sikeresen frissítve"))
                    .addOnFailureListener(e -> Log.e("Edit_Report", "Hiba történt a frissítés során", e));
        } else
        {
            Log.e("Edit_Report", "Nincs documentId beállítva, nem lehet frissíteni");
        }
    }

    private void removePairMarkers(Report report)
    {
        //a kiválasztott marker törlése
        Iterator<MarkerPair> iterator = this.markerPairs.iterator();
        while (iterator.hasNext())
        {
            MarkerPair item = iterator.next();
            if (selectedMarker.getId().equals(item.startMarker.getId()))
            {
                item.startMarker.remove();
            } else if (selectedMarker.getId().equals(item.endMarker.getId()))
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
            if (toRemove != null)
            {
                markerPairs.remove(toRemove);
            }
        }

        addPointsForReport(report);
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

    private void getLikeCount(String reportId, final View rootView)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reports")
                .document(reportId)
                .collection("likes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {
                    int likeCount = queryDocumentSnapshots.size();

                    TextView likeCountTextView = rootView.findViewById(R.id.tv_like_count);
                    likeCountTextView.setText(String.valueOf(likeCount));
                })
                .addOnFailureListener(e ->
                {
                    Log.e("LikeCount", "Hiba a lájkok lekérésekor: ", e);
                });
    }

    private void setupAutoCompleteFragmentCarReport(BottomSheetDialog bottomSheetDialog)
    {
        autocompleteSupportFragmentCarReport = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_car_report);
        autocompleteSupportFragmentCarReport.setTypeFilter(TypeFilter.ADDRESS);
        List<Address> address = null;
        try
        {
            address = geocoder.getFromLocation(userCurrentLocation.latitude, userCurrentLocation.longitude, 1);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        var country = address.get(0);

        autocompleteSupportFragmentCarReport.setCountries(country.getCountryCode());
        autocompleteSupportFragmentCarReport.setPlaceFields(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
        ));
        autocompleteSupportFragmentCarReport.setOnPlaceSelectedListener(new PlaceSelectionListener()
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

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(candidate, 15));

            }
        });
    }

    public void openCarReportSheetDialog(View view)
    {
        if(!userIsLoggedIn)
        {
            Toast.makeText(this, getString(R.string.toast_login_required_for_carpool), Toast.LENGTH_SHORT).show();
            return;
        }

        if (startingMarker == null)
        {
            LatLng candidate = userCurrentLocation;
            startingMarker = mMap.addMarker(new MarkerOptions()
                    .position(candidate)
                    .draggable(true)
                    .title("Indulás"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startingMarker.getPosition(), 15));
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_carpool_form, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);

        setupAutoCompleteFragmentCarReport(bottomSheetDialog);

        EditText fromCityEt = sheetView.findViewById(R.id.et_from_city);
        EditText seatsEt = sheetView.findViewById(R.id.et_seats);
        EditText commentEt = sheetView.findViewById(R.id.et_comment);

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
            removeAutoFragment(autocompleteSupportFragmentCarReport);
        });


        okIMG.setOnClickListener(v ->
        {
            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.setCanceledOnTouchOutside(false);
            bottomSheetDialog.setDismissWithAnimation(false);
            if (startingMarker == null) return;
            if (destinationMarker != null)
            {
                Geocoder geocoder = new Geocoder(this.getApplicationContext(), Locale.getDefault());
                try
                {
                    List<Address> addresses = geocoder.getFromLocation(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, 1);
                    if (addresses != null && !addresses.isEmpty())
                    {
                        String city = addresses.get(0).getLocality();
                        autocompleteSupportFragmentCarReport.setText(city);
                    } else
                    {
                        autocompleteSupportFragmentCarReport.setText(getString(R.string.destination_not_found));
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                    autocompleteSupportFragmentCarReport.setText(getString(R.string.toast_error_occurred));
                }
            }
            bottomSheetDialog.show();
            okIMG.setVisibility(View.INVISIBLE);
            cancelIMG.setVisibility(View.INVISIBLE);
        });


        if (startingMarker != null)
        {
            List<Address> Address = null;
            try
            {
                Address = geocoder.getFromLocation(startingMarker.getPosition().latitude, startingMarker.getPosition().longitude, 1);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            fromCityEt.setText(Address.get(0).getLocality());
        }


        Button cancelButton = sheetView.findViewById(R.id.btn_cancel);
        Button submitButton = sheetView.findViewById(R.id.btn_submit);

        cancelButton.setOnClickListener(v ->
        {
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
            bottomSheetDialog.dismiss();
            removeAutoFragment(autocompleteSupportFragmentCarReport);
        });

        submitButton.setOnClickListener(v ->
        {
            if (destinationMarker == null)
            {
                Toast.makeText(this, getString(R.string.toast_destination_missing), Toast.LENGTH_SHORT).show();
                return;
            }

            String toCity;
            if (destinationMarker != null)
            {
                List<Address> Address = null;
                try
                {
                    Address = geocoder.getFromLocation(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude, 1);
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                toCity = (Address.get(0).getLocality());
            } else
            {
                toCity = "";
            }
            String fromCity = fromCityEt.getText().toString();
            String seatsText = seatsEt.getText().toString();
            String comment = commentEt.getText().toString();

            if (fromCity.isEmpty() || seatsText.isEmpty())
            {
                Toast.makeText(this, getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            int seats = Integer.parseInt(seatsText);

            CarReport carReport = new CarReport(
                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    startingMarker.getPosition().latitude,
                    startingMarker.getPosition().longitude,
                    destinationMarker.getPosition().latitude,
                    destinationMarker.getPosition().longitude,
                    fromCity,
                    toCity,
                    seats,
                    comment,
                    new Date()
            );

            db.collection("carReports").add(carReport)
                    .addOnSuccessListener(doc ->
                    {
                        String generatedId = doc.getId();
                        doc.update("documentId", generatedId);
                        Toast.makeText(this, getString(R.string.toast_trip_posted), Toast.LENGTH_SHORT).show();
                        carReport.setDocumentId(generatedId);
                        startUpdatingLocation(carReport);
                    })
                    .addOnFailureListener(e ->
                    {
                        Toast.makeText(this, getString(R.string.toast_error_occurred) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });


            startingMarker.remove();
            destinationMarker.remove();
            startingMarker = null;
            destinationMarker = null;
            bottomSheetDialog.dismiss();
            removeAutoFragment(autocompleteSupportFragmentCarReport);
        });

        bottomSheetDialog.show();
    }

    private void addPointsForReport(Report report)
    {
        FirebaseFirestore.getInstance()
                .collection("reports")
                .document(report.getDocumentId())
                .collection("likes")
                .get()
                .addOnSuccessListener(likes ->
                {
                    int points = likes.size() * POINTS_FOR_REPORT_PER_LIKE;
                    if (points > 0)
                    {
                        db.collection("Users").document(report.getUid())
                                .update("points", FieldValue.increment(points));
                    }
                });
    }

    private void addPointsForCarReport(CarReport carReport)
    {
        int acceptedCount = carReport.getAccepted().size();
        if (acceptedCount > 0)
        {
            int points = acceptedCount * POINTS_FOR_CAR_REPORT_PER_USER;
            db.collection("Users").document(carReport.getUid())
                    .update("points", FieldValue.increment(points));
        }
    }

    public void focusApplicantOnMap(Applicant applicant) {
        if (mMap == null) return; // GoogleMap objektum

        LatLng position = new LatLng(applicant.getStartingLatitude(), applicant.getStartingLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));


        for (Marker marker : applicantMarkersList) {
            LatLng pos = marker.getPosition();
            if (pos.latitude == applicant.getStartingLatitude() && pos.longitude == applicant.getStartingLatitude()) {

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
                marker.showInfoWindow();
                break;
            }
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private final OnTimeSelectedListener listener;

        public interface OnTimeSelectedListener {
            void onTimeSelected(int hour, int minute);
        }

        public TimePickerFragment(OnTimeSelectedListener listener) {
            this.listener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (listener != null) listener.onTimeSelected(hourOfDay, minute);
        }
    }

}

