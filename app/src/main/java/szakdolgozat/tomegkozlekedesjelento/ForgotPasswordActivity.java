package szakdolgozat.tomegkozlekedesjelento;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void ResendPassword(View view)
    {
        EditText editText = findViewById(R.id.editTextTextEmailAddress);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailAddress = editText.getText().toString();

        if (!emailAddress.isEmpty())
        {
            auth.sendPasswordResetEmail(emailAddress).addOnSuccessListener(new OnSuccessListener<Void>()
            {
                @Override
                public void onSuccess(Void unused)
                {
                    Toast.makeText(ForgotPasswordActivity.this, "Email has been sent!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(ForgotPasswordActivity.this, "Wrong email!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            Toast.makeText(ForgotPasswordActivity.this, "Email is empty! Try again!", Toast.LENGTH_SHORT).show();
        }
    }
}