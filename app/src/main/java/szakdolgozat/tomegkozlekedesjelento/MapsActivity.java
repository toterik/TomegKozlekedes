package szakdolgozat.tomegkozlekedesjelento;

import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;


import szakdolgozat.tomegkozlekedesjelento.databinding.ActivityMapsBinding;

public class MapsActivity extends MenuForAllActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        boolean userIsLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        mMap = googleMap;

        LatLng hungary = new LatLng(47.1625, 19.5033);
        mMap.addMarker(new MarkerOptions().position(hungary).title("Marker in Hungary"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hungary));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point)
            {
                if (userIsLoggedIn)
                {
                    LatLng markerPoint = new LatLng(point.latitude,point.longitude);
                    mMap.addMarker(new MarkerOptions().position(markerPoint));
                }
                else
                {
                    Toast.makeText(MapsActivity.this, "You need to be Logged in to add marker!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}