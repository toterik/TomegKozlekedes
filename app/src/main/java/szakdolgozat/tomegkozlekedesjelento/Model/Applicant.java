package szakdolgozat.tomegkozlekedesjelento.Model;

import com.google.firebase.firestore.PropertyName;

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
}
