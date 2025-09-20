package szakdolgozat.tomegkozlekedesjelento.Model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarReport {
    private String uid;
    private double startingLatitude;
    private double startingLongitude;
    private double destinationLatitude;
    private double destinationLongitude;
    private String fromCity;
    private String toCity;
    private int seatsAvailable;
    private List<Applicant> applicants = new ArrayList<>();
    private List<String> accepted;
    private String comment;
    private Date startingDate;
    private boolean active;

    @Exclude
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
                     String comment,
                     Date startingDate) {
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
        this.startingDate = startingDate;
        this.active = true;
    }

    @PropertyName("starting_date")
    public Date getStartingDate() {
        return startingDate;
    }
    @PropertyName("starting_date")
    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }
    @PropertyName("active")
    public boolean isActive()
    {
        return active;
    }
    @PropertyName("active")
    public void setActive(boolean active)
    {
        this.active = active;
    }

    @PropertyName("uid")
    public String getUid() {
        return uid;
    }
    @PropertyName("uid")
    public void setUid(String uid) {
        this.uid = uid;
    }

    @PropertyName("comment")
    public String getComment() {
        return comment;
    }
    @PropertyName("comment")
    public void setComment(String comment) {
        this.comment = comment;
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

    @PropertyName("from_city")
    public String getFromCity() {
        return fromCity;
    }
    @PropertyName("from_city")
    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    @PropertyName("to_city")
    public String getToCity() {
        return toCity;
    }
    @PropertyName("to_city")
    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    @PropertyName("seats_available")
    public int getSeatsAvailable() {
        return seatsAvailable;
    }
    @PropertyName("seats_available")
    public void setSeatsAvailable(int seatsAvailable) {
        this.seatsAvailable = seatsAvailable;
    }

    @PropertyName("applicants")
    public List<Applicant> getApplicants() {
        return applicants;
    }
    @PropertyName("applicants")
    public void setApplicants(List<Applicant> applicants) {
        this.applicants = applicants;
    }

    @PropertyName("accepted")
    public List<String> getAccepted() {
        return accepted;
    }
    @PropertyName("accepted")
    public void setAccepted(List<String> accepted) {
        this.accepted = accepted;
    }

    @DocumentId
    public String getDocumentId() { return documentId; }

    @DocumentId
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}
