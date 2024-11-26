package szakdolgozat.tomegkozlekedesjelento;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

import szakdolgozat.tomegkozlekedesjelento.databinding.ActivityMapsBinding;

public class MapsActivity extends MenuForAllActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db =  FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean userIsLoggedIn = user != null;
        mMap = googleMap;
        LatLng hungary = new LatLng(47.1625, 19.5033);
        mMap.addMarker(new MarkerOptions().position(hungary).title("Marker in Hungary"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hungary,10));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
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
}
