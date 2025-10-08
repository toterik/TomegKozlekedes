package szakdolgozat.tomegkozlekedesjelento.RecycleViewAdapter;

import android.content.Context;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import szakdolgozat.tomegkozlekedesjelento.MapsActivity;
import szakdolgozat.tomegkozlekedesjelento.Model.Applicant;
import szakdolgozat.tomegkozlekedesjelento.Model.CarReport;
import szakdolgozat.tomegkozlekedesjelento.R;

public class ApplicantsAdapter extends RecyclerView.Adapter<ApplicantsAdapter.ViewHolder> {

    private Context context;
    private CarReport carReport;
    private List<Applicant> applicants;

    public ApplicantsAdapter(Context context, CarReport carReport) {
        this.context = context;
        this.carReport = carReport;
        this.applicants = carReport.getApplicants();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.applicant_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Applicant applicant = applicants.get(position);

        // Email lekérése Firestore-ból
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(applicant.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        holder.applicantName.setText(doc.getString("username"));
                    } else {
                        holder.applicantName.setText("Ismeretlen");
                    }
                });

        // Elfogadás gomb
        holder.btnAccept.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("carReports")
                    .document(carReport.getDocumentId())
                    .update("accepted", FieldValue.arrayUnion(applicant.getUid()),
                            "applicants", FieldValue.arrayRemove(applicant))
                    .addOnSuccessListener(unused -> {
                        applicants.remove(applicant);
                        notifyItemRemoved(holder.getAdapterPosition());
                    });
        });


        // Elutasítás gomb
        holder.btnReject.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("carReports")
                    .document(carReport.getDocumentId())
                    .update("applicants", FieldValue.arrayRemove(applicant))
                    .addOnSuccessListener(unused -> {
                        applicants.remove(applicant);
                        notifyItemRemoved(holder.getAdapterPosition());
                    });
        });
        TextView tvApplicantFrom = holder.itemView.findViewById(R.id.tv_applicant_from);
        Geocoder geocoder = new Geocoder(context);
        String city = "Ismeretlen";
        try {
            city = applicant.getAddress(geocoder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tvApplicantFrom.setText(city);
    }

    @Override
    public int getItemCount() {
        return applicants.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView applicantName;
        Button btnAccept, btnReject;

        ViewHolder(View itemView) {
            super(itemView);
            applicantName = itemView.findViewById(R.id.tv_applicant_name);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}
