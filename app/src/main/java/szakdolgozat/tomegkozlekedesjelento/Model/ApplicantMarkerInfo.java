package szakdolgozat.tomegkozlekedesjelento.Model;

public class ApplicantMarkerInfo {
    private Applicant applicant;
    private CarReport carReport;

    public ApplicantMarkerInfo(Applicant applicant, CarReport carReport) {
        this.applicant = applicant;
        this.carReport = carReport;
    }

    public Applicant getApplicant() { return applicant; }
    public CarReport getCarReport() { return carReport; }
}
