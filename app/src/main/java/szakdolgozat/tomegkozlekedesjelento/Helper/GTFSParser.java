package szakdolgozat.tomegkozlekedesjelento.Helper;

import java.time.ZoneId;
import java.util.ArrayList;
import com.google.firebase.Timestamp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import szakdolgozat.tomegkozlekedesjelento.Model.Report;

public class GTFSParser {

    /**
     * Fő belépési pont: feldolgozza a GTFS fájlokat, és visszaadja a Report-ok listáját.
     */
    public static List<Report> parseReports(String stopsPath, String stopTimesPath, String meansOfTransport) throws IOException {
        Map<String, Stop> stops = readStops(stopsPath);
        Map<String, List<StopTime>> stopTimesByTrip = readStopTimes(stopTimesPath);

        List<Report> reports = new ArrayList<>();

        for (Map.Entry<String, List<StopTime>> entry : stopTimesByTrip.entrySet()) {
            List<StopTime> times = entry.getValue();
            if (times.isEmpty()) continue;

            // Rendezés a stop_sequence szerint
            times.sort(Comparator.comparingInt(t -> t.stopSequence));

            StopTime start = times.get(0);
            StopTime end = times.get(times.size() - 1);

            Stop startStop = stops.get(start.stopId);
            Stop endStop = stops.get(end.stopId);
            if (startStop == null || endStop == null) continue;

            LocalDateTime startDateTime = parseGtfsTime(start.arrivalTime);
            LocalDateTime endDateTime = parseGtfsTime(end.departureTime);

            Report report = new Report(
                    0,
                    "Automatikusan importált menetrend",
                    endStop.lat,
                    endStop.lon,
                    meansOfTransport,
                    startStop.lat,
                    startStop.lon,
                    "schedule",
                    "system_"+meansOfTransport,
                    startDateTime.getHour() * 60 + startDateTime.getMinute(),
                    endDateTime.getHour() * 60 + endDateTime.getMinute(),
                    "",
                    true
            );

            reports.add(report);
        }

        return reports;
    }

    // ------------------------------------------------------------------------

    /** Egy GTFS "stops.txt" beolvasása */
    private static Map<String, Stop> readStops(String filePath) throws IOException {
        Map<String, Stop> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine(); // pl. "stop_id","stop_name",...
            String[] headers = headerLine.replace("\"", "").split(",");
            if (headerLine == null) return map;

            int stopIdIdx = getColumnIndex(headers, "stop_id");
            int stopLatIdx = getColumnIndex(headers, "stop_lat");
            int stopLonIdx = getColumnIndex(headers, "stop_lon");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = splitCsv(line);
                if (cols.length <= Math.max(stopIdIdx, Math.max(stopLatIdx, stopLonIdx))) continue;

                String id = cols[stopIdIdx];
                double lat = Double.parseDouble(cols[stopLatIdx]);
                double lon = Double.parseDouble(cols[stopLonIdx]);
                map.put(id, new Stop(id, lat, lon));
            }
        }
        return map;
    }

    /** Egy GTFS "stop_times.txt" beolvasása */
    private static Map<String, List<StopTime>> readStopTimes(String filePath) throws IOException {
        Map<String, StopTime> firstStops = new HashMap<>();
        Map<String, StopTime> lastStops = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return Collections.emptyMap();

            String[] headers = headerLine.replace("\"", "").split(",");
            int tripIdIdx = getColumnIndex(headers, "trip_id");
            int stopIdIdx = getColumnIndex(headers, "stop_id");
            int arrivalIdx = getColumnIndex(headers, "arrival_time");
            int departIdx = getColumnIndex(headers, "departure_time");
            int seqIdx = getColumnIndex(headers, "stop_sequence");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = splitCsv(line);
                if (cols.length <= Math.max(seqIdx, departIdx)) continue;

                String tripId = cols[tripIdIdx];
                String stopId = cols[stopIdIdx];
                String arrival = cols[arrivalIdx];
                String depart = cols[departIdx];
                int seq = Integer.parseInt(cols[seqIdx]);

                StopTime current = new StopTime(tripId, stopId, arrival, depart, seq);

                // Első megálló
                StopTime first = firstStops.get(tripId);
                if (first == null || seq < first.stopSequence) {
                    firstStops.put(tripId, current);
                }

                // Utolsó megálló
                StopTime last = lastStops.get(tripId);
                if (last == null || seq > last.stopSequence)
                {
                    lastStops.put(tripId, current);
                }
            }
        }

        // Egyesítjük az első és utolsó megállót listába
        Map<String, List<StopTime>> result = new HashMap<>();
        for (String tripId : firstStops.keySet()) {
            result.put(tripId, Arrays.asList(firstStops.get(tripId), lastStops.get(tripId)));
        }

        return result;
    }

    // ------------------------------------------------------------------------

    /** GTFS 25:13:00 formátum konvertálása LocalDateTime-re */
    private static LocalDateTime parseGtfsTime(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]) % 24;
            int dayOffset = Integer.parseInt(parts[0]) / 24;
            int min = Integer.parseInt(parts[1]);
            int sec = Integer.parseInt(parts[2]);
            return LocalDateTime.now().plusDays(dayOffset).withHour(hour).withMinute(min).withSecond(sec);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    /** CSV sort darabol, figyelve az idézőjelekre */
    private static String[] splitCsv(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }

    private static int getColumnIndex(String[] headers, String columnName) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }


    // ------------------------------------------------------------------------
    // Segéd osztályok a GTFS feldolgozáshoz

    private static class Stop {
        String id;
        double lat, lon;
        Stop(String id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private static class StopTime {
        String tripId, stopId, arrivalTime, departureTime;
        int stopSequence;
        StopTime(String tripId, String stopId, String arrivalTime, String departureTime, int stopSequence) {
            this.tripId = tripId;
            this.stopId = stopId;
            this.arrivalTime = arrivalTime;
            this.departureTime = departureTime;
            this.stopSequence = stopSequence;
        }
    }
}