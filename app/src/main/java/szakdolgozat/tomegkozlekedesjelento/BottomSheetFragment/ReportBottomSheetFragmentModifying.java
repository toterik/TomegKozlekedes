package szakdolgozat.tomegkozlekedesjelento.BottomSheetFragment;

import static szakdolgozat.tomegkozlekedesjelento.MapsActivity.userCurrentLocation;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import szakdolgozat.tomegkozlekedesjelento.MapsActivity;
import szakdolgozat.tomegkozlekedesjelento.Model.MarkerPair;
import szakdolgozat.tomegkozlekedesjelento.Model.Report;
import szakdolgozat.tomegkozlekedesjelento.R;

public class ReportBottomSheetFragmentModifying extends BottomSheetDialogFragment
{

    private Report report;
    private Marker selectedMarker;
    private Set<MarkerPair> markerPairs = new HashSet<>();
    private FirebaseFirestore db;
    private Spinner transportType;
    private Spinner problemType;
    private EditText detailsEditText;
    private EditText delayEditText;
    private EditText startingCityEditText;
    private EditText destinationCityEditText;
    private String role;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private ImageView okIMG;
    private ImageView noIMG;
    private Marker startingMarker;
    private Marker destinationMarker;



    public static ReportBottomSheetFragmentModifying newInstance(Report report, Marker marker, Set<MarkerPair> markerPairs, String role) {
        ReportBottomSheetFragmentModifying fragment = new ReportBottomSheetFragmentModifying();
        Bundle args = new Bundle();
        args.putSerializable("report", report);
        args.putString("role", role);
        fragment.setArguments(args);
        fragment.setSelectedMarker(markerPairs);
        fragment.setSelectedMarker(marker);

        return fragment;
    }



    private void setSelectedMarker(Marker marker)
    {
        this.selectedMarker = marker;
    }
    private void setSelectedMarker(Set<MarkerPair>  markerPairs)
    {
        this.markerPairs = markerPairs;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null)
        {
            report = (Report) getArguments().getSerializable("report");
            role = getArguments().getString("role");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {

        return null;
    }

    private void editStartMarker()
    {
        startingMarker = getMarkerPair().startMarker;
        startingMarker.setDraggable(true);
        okIMG.setVisibility(View.VISIBLE);
        noIMG.setVisibility(View.VISIBLE);
    }

    private void editDestinationMarker()
    {
        destinationMarker = getMarkerPair().endMarker;
        destinationMarker.setDraggable(true);
        okIMG.setVisibility(View.VISIBLE);
        noIMG.setVisibility(View.VISIBLE);
    }




    private MarkerPair getMarkerPair()
    {
        Iterator<MarkerPair> iterator = this.markerPairs.iterator();
        while (iterator.hasNext())
        {
            MarkerPair item = iterator.next();
            if (selectedMarker.getId().equals(item.startMarker.getId()))
            {
                return item;
            } else if (selectedMarker.getId().equals(item.endMarker.getId()))
            {
                return item;
            }
        }
        return null;
    }



    private boolean isStartingMarker(Marker selected)
    {
        for (MarkerPair pair : markerPairs)
        {
            if (pair.startMarker.getId().equals(selected.getId()))
            {
                return true;
            }
            if (pair.endMarker.getId().equals(selected.getId()))
            {
                return false;
            }
        }
        return false;
    }
    private String getCityNameFromLatLng(LatLng position)
    {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String city = addresses.get(0).getLocality();
                return city;
            } else {
                return "Destination not found";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error getting destination";
        }
    }




}

