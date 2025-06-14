package szakdolgozat.tomegkozlekedesjelento;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends MenuForAllActivity
{
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setSupportActionBar(toolbar);
    }

    public void registrationByEmailAndPassword(View view)
    {
        EditText emailET = findViewById(R.id.email);
        EditText passwordET = findViewById(R.id.password);
        EditText passwordagainET = findViewById(R.id.passwordagain);

        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordagain = passwordagainET.getText().toString();

        mAuth = FirebaseAuth.getInstance();

        if(email.isEmpty() || password.isEmpty() || passwordagain.isEmpty())
        {
            Toast.makeText(this, "Minden mezőt ki kell tölteni!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!passwordagain.equals(password))
        {
            Toast.makeText(this, "A két jelszó nem egyezik!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task ->
        {
            if (task.isSuccessful())
            {
                mAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void unused)
                    {
                        Map<String, Object> userData = new HashMap<>();
                        DocumentReference documentReference = db.collection("Users").document(mAuth.getCurrentUser().getUid());
                        userData.put("email", email);
                        userData.put("role", "user");
                        documentReference.set(userData);
                        Toast.makeText(RegistrationActivity.this, "Sikeres regisztráció!\n A megerősítő emailt elküldtük!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d("RegistrationActivity", "Hiba: Email nem lett elküldve! "+e.getMessage());
                    }
                });
            } else
            {
                Exception e = task.getException();
                if (e instanceof FirebaseAuthUserCollisionException)
                {
                    Toast.makeText(this, "Ezzel az emaillel már regisztáltak!", Toast.LENGTH_SHORT).show();

                } else if (e instanceof FirebaseAuthWeakPasswordException)
                {
                    Toast.makeText(this, "A jelszó túl gyenge!", Toast.LENGTH_SHORT).show();

                } else if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    Toast.makeText(this, "Nem megfelelő email!", Toast.LENGTH_SHORT).show();

                } else
                {
                    Toast.makeText(this, "Valami hiba történt!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}