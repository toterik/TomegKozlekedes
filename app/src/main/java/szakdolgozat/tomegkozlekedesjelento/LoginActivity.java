package szakdolgozat.tomegkozlekedesjelento;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends MenuForAllActivity
{
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Marker startingMarker;
    private Marker destinationMarker;
    protected void onCreate(Bundle savedInstanceState)
    {
        firebaseAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button resendButton = findViewById(R.id.resendVerificationEmail);
        resendButton.setVisibility(View.INVISIBLE);
    }

    public void forgotPasswordActivity(View view)
    {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
    public void login(View view)
    {
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();


        if (email.isEmpty() || password.isEmpty())
        {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task ->
        {
            if (task.isSuccessful())
            {
                user = firebaseAuth.getCurrentUser();
                if (user.isEmailVerified())
                {
                    Toast.makeText(LoginActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "This account is not verified!", Toast.LENGTH_SHORT).show();
                    Button resendButton = findViewById(R.id.resendVerificationEmail);
                    resendButton.setVisibility(View.VISIBLE);

                    FirebaseAuth.getInstance().signOut();
                }
            }
            else
            {
                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void registrationActivity(View view)
    {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }
    public void resendVerificationEmail(View view)
    {
        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void unused)
            {
                Toast.makeText(LoginActivity.this, "A megerősítő emailt elküldtük!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.d("LoginActivity", "Hiba: Email nem lett elküldve! "+e.getMessage());
            }
        });
    }
}