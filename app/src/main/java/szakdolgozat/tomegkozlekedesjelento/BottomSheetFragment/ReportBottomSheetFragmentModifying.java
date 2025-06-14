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
        View view = inflater.inflate(R.layout.bottom_sheet_report_modify_report, container, false);

        transportType = view.findViewById(R.id.spinner_transport);
        problemType = view.findViewById(R.id.spinner_problem_type);

        detailsEditText = view.findViewById(R.id.et_problem_details);
        delayEditText = view.findViewById(R.id.et_delay_duration);
        startingCityEditText = view.findViewById(R.id.startingCity);
        destinationCityEditText = view.findViewById(R.id.destinationCity);
        Button editButton = view.findViewById(R.id.btn_edit);
        Button deleteButton = view.findViewById(R.id.btn_delete);
        Button cancelButton = view.findViewById(R.id.btn_cancel);
        Button editStartMarker = view.findViewById(R.id.btn_set_start_location);
        Button editDestinationMarker = view.findViewById(R.id.btn_set_end_location);

        MapsActivity activity = (MapsActivity) getActivity();
        if (activity != null)
        {
             okIMG = activity.findViewById(R.id.img_ok);
             noIMG = activity.findViewById(R.id.img_no);
        }

        if (user == null || (!"admin".equals(role) && !report.getUid().equals(user.getUid())))
        {
            transportType.setEnabled(false);
            problemType.setEnabled(false);
            detailsEditText.setEnabled(false);
            delayEditText.setEnabled(false);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            startingCityEditText.setEnabled(false);
            destinationCityEditText.setEnabled(false);
        }

        ArrayAdapter<CharSequence> adapterTransport = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.means_of_transport,
                android.R.layout.simple_spinner_item
        );
        adapterTransport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportType.setAdapter(adapterTransport);

        ArrayAdapter<CharSequence> adapterProblem = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.type_of_problem,
                android.R.layout.simple_spinner_item
        );
        adapterProblem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        problemType.setAdapter(adapterProblem);

        detailsEditText.setText(report.getDescription());
        delayEditText.setText(String.valueOf(report.getDelay()));
        startingCityEditText.setText(getCityNameFromLatLng(new LatLng(report.getStartingLatitude(),report.getStartingLongitude())));
        destinationCityEditText.setText(getCityNameFromLatLng(new LatLng(report.getDestinationLatitude(),report.getDestinationLongitude())));

        setSpinnerSelectionByValue(transportType, report.getMeanOfTransport());
        setSpinnerSelectionByValue(problemType, report.getType());

        editButton.setOnClickListener(v -> {
            editReport();
            dismiss();
        });
        editStartMarker.setOnClickListener(v ->
        {
            editStartMarker();
            dismiss();
        });

        editDestinationMarker.setOnClickListener(v ->
        {
            editDestinationMarker();
            dismiss();
        });



        deleteButton.setOnClickListener(v -> {
            removePairMarkers();
            dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        return view;
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

    private void editReport()
    {
        String selectedTransport = transportType.getSelectedItem().toString();
        String selectedProblemType = problemType.getSelectedItem().toString();
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
            report.setStartingLatitude(startingMarker.getPosition().longitude);
            report.setStartingLongitude(startingMarker.getPosition().latitude);
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

    private void removePairMarkers()
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


    private void setSpinnerSelectionByValue(Spinner spinner, String value)
    {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

}

