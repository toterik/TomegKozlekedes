package szakdolgozat.tomegkozlekedesjelento.Model;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class CarReport
{
    private String uid;
    private double startingLatitude;
    private double startingLongitude;
    private double destinationLatitude;
    private double destinationLongitude;
    private String fromCity;
    private String toCity;
    private int seatsAvailable;
    private List<String> applicants;
    private List<String> accepted;
    private String comment;

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    private String documentId;

    public CarReport() {}
    public CarReport(String uid,
                     double startingLatitude,
                     double startingLongitude,
                     double destinationLatitude,
                     double destinationLongitude,
                     String fromCity,
                     String toCity,
                     int seatsAvailable,
                     String comment) {
        this.uid = uid;
        this.startingLatitude = startingLatitude;
        this.startingLongitude = startingLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.seatsAvailable = seatsAvailable;
        this.applicants = new ArrayList<>();
        this.accepted = new ArrayList<>();
        this.comment = comment;
    }
    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public double getStartingLatitude()
    {
        return startingLatitude;
    }

    public void setStartingLatitude(double startingLatitude)
    {
        this.startingLatitude = startingLatitude;
    }

    public double getStartingLongitude()
    {
        return startingLongitude;
    }

    public void setStartingLongitude(double startingLongitude)
    {
        this.startingLongitude = startingLongitude;
    }

    public double getDestinationLatitude()
    {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude)
    {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude()
    {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude)
    {
        this.destinationLongitude = destinationLongitude;
    }

    public String getFromCity()
    {
        return fromCity;
    }

    public void setFromCity(String fromCity)
    {
        this.fromCity = fromCity;
    }

    public String getToCity()
    {
        return toCity;
    }

    public void setToCity(String toCity)
    {
        this.toCity = toCity;
    }

    public int getSeatsAvailable()
    {
        return seatsAvailable;
    }

    public void setSeatsAvailable(int seatsAvailable)
    {
        this.seatsAvailable = seatsAvailable;
    }

    public List<String> getApplicants()
    {
        return applicants;
    }

    public void setApplicants(List<String> applicants)
    {
        this.applicants = applicants;
    }

    public List<String> getAccepted()
    {
        return accepted;
    }

    public void setAccepted(List<String> accepted)
    {
        this.accepted = accepted;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}
