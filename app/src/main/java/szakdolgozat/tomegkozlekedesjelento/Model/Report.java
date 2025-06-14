package szakdolgozat.tomegkozlekedesjelento.Model;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import szakdolgozat.tomegkozlekedesjelento.MapsActivity;

public class Report implements Serializable
{
    private int delay;
    private String description;
    private double destinationLatitude;
    private double destinationLongitude;
    private String meanOfTransport;
    private double startingLatitude;
    private double startingLongitude;
    private String type;
    private String uid;
    private String documentId;

    public Report()
    {}
    public Report(int delay,
                  String description,
                  double destinationLatitude,
                  double destinationLongitude,
                  String meanOfTransport,
                  double startingLatitude,
                  double startingLongitude,
                  String type,
                  String uid)
    {
        this.delay = delay;
        this.description = description;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.meanOfTransport = meanOfTransport;
        this.startingLatitude = startingLatitude;
        this.startingLongitude = startingLongitude;
        this.type = type;
        this.uid = uid;
    }

    public String getMarkerTitle(boolean isStarting)
    {
        return isStarting ? "Starting Marker" : "Destination Marker";
    }
    public String getMarkerSnippet(boolean isStarting, Geocoder geocoder)
    {
        String cityName = isStarting
                ? getCity(geocoder, startingLatitude, startingLongitude)
                : getCity(geocoder, destinationLatitude, destinationLongitude);

        return "Type: " + type + "\n" +
                "Transport: " + meanOfTransport + "\n" +
                "Description: " + description + "\n" +
                "Delay: " + delay + " minutes\n" +
                (isStarting ? "Starting city: " : "Destination: ") + cityName;
    }
    //gets the city's name from the location
    public String getCity(Geocoder geocoder, double Latitude, double longitude)
    {

        List<Address> Address = null;
        try {
            Address = geocoder.getFromLocation(Latitude, longitude, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Address.get(0).getLocality();
    }

    @PropertyName("delay")
    public int getDelay() {
        return delay;
    }

    public String getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(String documentId)
    {
        this.documentId = documentId;
    }

    @PropertyName("delay")
    public void setDelay(int delay) {
        this.delay = delay;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("destination_latitude")
    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    @PropertyName("destination_latitude")
    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    @PropertyName("destination_longitude")
    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    @PropertyName("destination_longitude")
    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    @PropertyName("mean_of_transport")
    public String getMeanOfTransport() {
        return meanOfTransport;
    }

    @PropertyName("mean_of_transport")
    public void setMeanOfTransport(String meanOfTransport) {
        this.meanOfTransport = meanOfTransport;
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

    @PropertyName("type")
    public String getType() {
        return type;
    }

    @PropertyName("type")
    public void setType(String type) {
        this.type = type;
    }

    @PropertyName("uid")
    public String getUid() {
        return uid;
    }

    @PropertyName("uid")
    public void setUid(String uid) {
        this.uid = uid;
    }


    @NonNull
    @Override
    public String toString()
    {
        return "Report{" +
                "delay=" + delay +
                ", description='" + description + '\'' +
                ", destinationLatitude=" + destinationLatitude +
                ", destinationLongitude=" + destinationLongitude +
                ", meanOfTransport='" + meanOfTransport + '\'' +
                ", startingLatitude=" + startingLatitude +
                ", startingLongitude=" + startingLongitude +
                ", type='" + type + '\'' +
                ", uid='" + uid + '\'' +
                ", documentId='" + documentId + '\'' +
                '}';
    }

    public void save(FirebaseFirestore db) {
        String newDocId = db.collection("reports").document().getId();
        this.documentId = newDocId;
        db.collection("/reports/").document(newDocId).set(this)
                .addOnSuccessListener(aVoid -> Log.d("Save_Report", "Report saved with ID: " + newDocId))
                .addOnFailureListener(e -> Log.e("Save_Report", "Error saving report", e));
    }
}
