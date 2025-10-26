package szakdolgozat.tomegkozlekedesjelento;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.IOException;
import java.util.List;

import szakdolgozat.tomegkozlekedesjelento.Helper.GTFSParser;
import szakdolgozat.tomegkozlekedesjelento.Helper.ZipUtils;
import szakdolgozat.tomegkozlekedesjelento.Model.Report;

public class UploadGtfsActivity extends MenuForAllActivity {

    private Uri selectedBusFileUri;
    private Uri selectedTrainFileUri;
    private TextView selectedBusFileText;
    private TextView selectedTrainFileText;
    private FirebaseFirestore db;

    // 🚍 Busz fájlválasztó
    private final ActivityResultLauncher<Intent> busFilePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedBusFileUri = result.getData().getData();
                            if (selectedBusFileUri != null) {
                                selectedBusFileText.setText(selectedBusFileUri.getLastPathSegment());
                            }
                        }
                    });

    // 🚆 Vonat fájlválasztó
    private final ActivityResultLauncher<Intent> trainFilePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedTrainFileUri = result.getData().getData();
                            if (selectedTrainFileUri != null) {
                                selectedTrainFileText.setText(selectedTrainFileUri.getLastPathSegment());
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_gtfs);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();

        // 🔹 View-ok összekötése
        selectedBusFileText = findViewById(R.id.selectedBusFileText);
        selectedTrainFileText = findViewById(R.id.selectedTrainFileText);

        Button selectBusZipButton = findViewById(R.id.selectBusZipButton);
        Button uploadBusButton = findViewById(R.id.uploadBusButton);
        Button selectTrainZipButton = findViewById(R.id.selectTrainZipButton);
        Button uploadTrainButton = findViewById(R.id.uploadTrainButton);

        // 🚍 Busz menetrend
        selectBusZipButton.setOnClickListener(v -> openFilePicker(busFilePickerLauncher));
        uploadBusButton.setOnClickListener(v -> {
            if (selectedBusFileUri == null) {
                Toast.makeText(this, "Válassz ki egy busz ZIP fájlt!", Toast.LENGTH_SHORT).show();
                return;
            }
            processGtfsFile(selectedBusFileUri, "busz");
        });

        // 🚆 Vonat menetrend
        selectTrainZipButton.setOnClickListener(v -> openFilePicker(trainFilePickerLauncher));
        uploadTrainButton.setOnClickListener(v -> {
            if (selectedTrainFileUri == null) {
                Toast.makeText(this, "Válassz ki egy vonat ZIP fájlt!", Toast.LENGTH_SHORT).show();
                return;
            }
            processGtfsFile(selectedTrainFileUri, "vonat");
        });
    }

    private void openFilePicker(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        launcher.launch(Intent.createChooser(intent, "Válassz GTFS ZIP fájlt"));
    }

    private void processGtfsFile(Uri zipUri, String type) {
        try {
            File extractedDir = ZipUtils.unzipToCache(this, zipUri);
            File stops = new File(extractedDir, "stops.txt");
            File stopTimes = new File(extractedDir, "stop_times.txt");

            db.collection("reports")
                    .whereEqualTo("uid", "system_" + type)
                    .get()
                    .addOnSuccessListener(query -> {
                        // törli a korábbi adatokat
                        for (var doc : query.getDocuments())
                        {
                            doc.getReference().delete();
                        }

                        try {
                            List<Report> newReports = GTFSParser.parseReports(
                                    stops.getAbsolutePath(),
                                    stopTimes.getAbsolutePath(),
                                    type
                            );

                            for (Report report : newReports)
                            {
                                report.save(db);
                            }

                            Toast.makeText(this, type + " menetrend feltöltése sikeres!", Toast.LENGTH_LONG).show();

                        } catch (IOException e)
                        {
                            Toast.makeText(this, "Hiba a GTFS feldolgozásakor: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (IOException e) {
            Toast.makeText(this, "Nem sikerült kicsomagolni a ZIP-et: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
