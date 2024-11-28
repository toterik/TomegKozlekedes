package szakdolgozat.tomegkozlekedesjelento;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Get database instance
        db =  FirebaseFirestore.getInstance();

        //set the menu toolbar
        Toolbar menuToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(menuToolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //hide report menu until user clicks on map
        reportToolbar = findViewById(R.id.report_toolbar);
        reportToolbar.setVisibility(View.INVISIBLE);

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

        //addReport();
        //addMarkerToDatabaseOnMapClick();
    }
    public void showReportToolbar(View view)
    {
        if (!this.reportToolbarShowing)
        {
            reportToolbar.setVisibility(View.VISIBLE);
        }
        else
        {
            reportToolbar.setVisibility(View.INVISIBLE);
        }
        this.reportToolbarShowing = !this.reportToolbarShowing;
    }


    public void addMarkerOnClick()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean userIsLoggedIn = user != null;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng point)
            {
                if (userIsLoggedIn)
                {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,10));   
                    markersList.add(mMap.addMarker(new MarkerOptions().position(point)));
                }
                else
                {
                    Toast.makeText(MapsActivity.this, "You need to be logged in to add marker!", Toast.LENGTH_SHORT).show();
                }
            }
        });
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


    public void addMarkerToDatabaseOnMapClick()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean userIsLoggedIn = user != null;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng point)
            {
                if (userIsLoggedIn)
                {
                    Map<String, Object> data = new HashMap<>();
                    data.put("latitude", point.latitude);
                    data.put("longitude", point.longitude);
                    data.put("uid", user.getUid());

                    db.collection("markers")
                            .add(data)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                            {
                                @Override
                                public void onSuccess(DocumentReference documentReference)
                                {
                                    Toast.makeText(MapsActivity.this, "Successfully added Marker!", Toast.LENGTH_SHORT).show();
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(point.latitude,point.longitude)));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(MapsActivity.this, "Error adding marker!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else
                {
                    Toast.makeText(MapsActivity.this, "You need to be logged in to add marker!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void reportLate(View view)
    {
        addMarkerOnClick();
    }

    public void reportFull(View view)
    {
        addMarkerOnClick();
    }

    public void reportSubstitute(View view)
    {
        addMarkerOnClick();
    }

    public void reportOther(View view)
    {
        addMarkerOnClick();
    }
}
