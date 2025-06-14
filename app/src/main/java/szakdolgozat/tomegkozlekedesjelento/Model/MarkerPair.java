package szakdolgozat.tomegkozlekedesjelento.Model;

import com.google.android.gms.maps.model.Marker;

public class MarkerPair {
    public Marker startMarker;
    public Marker endMarker;

    public MarkerPair(Marker start, Marker end) {
        this.startMarker = start;
        this.endMarker = end;
    }
}