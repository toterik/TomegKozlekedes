package szakdolgozat.tomegkozlekedesjelento.Model;

import android.location.Address;
import android.location.Geocoder;

import com.google.firebase.firestore.PropertyName;

import java.io.IOException;
import java.util.List;

public class Applicant
{
    private double startingLatitude;
    private double startingLongitude;
    private String uid;
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public Applicant()
    {
    }

    public Applicant(String uid,double startingLatitude, double startingLongitude)
    {
        this.uid = uid;
        this.startingLatitude = startingLatitude;
        this.startingLongitude = startingLongitude;
    }

    @PropertyName("starting_latitude")
    public double getStartingLatitude() {
        return startingLatitude;
    }
    @PropertyName("starting_latitude")
    public void setStartingLatitude(double startingLatitude) {
        this.startingLatitude = startingLatitude;
    }

    @PropertyName("starting_longitude")
    public double getStartingLongitude() {
        return startingLongitude;
    }
    @PropertyName("starting_longitude")
    public void setStartingLongitude(double startingLongitude) {
        this.startingLongitude = startingLongitude;
    }
    public String getAddress(Geocoder geocoder) {
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(startingLatitude, startingLongitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return "Ismeretlen cím";
        }

        if (addresses != null && !addresses.isEmpty()) {
            Address address = addresses.get(0);
            String city = address.getLocality();          // város
            String street = address.getThoroughfare();    // utca
            String number = address.getSubThoroughfare(); // házszám, ha van

            String fullAddress = "";
            if (street != null) fullAddress += street;
            if (number != null) fullAddress += " " + number;
            if (city != null) fullAddress += ", " + city;

            return fullAddress.isEmpty() ? "Ismeretlen cím" : fullAddress;
        } else {
            return "Ismeretlen cím";
        }
    }

}
