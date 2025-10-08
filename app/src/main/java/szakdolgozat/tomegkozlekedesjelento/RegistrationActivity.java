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

public class RegistrationActivity extends MenuForAllActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void registrationByEmailAndPassword(View view) {
        EditText usernameET = findViewById(R.id.username);
        EditText emailET = findViewById(R.id.email);
        EditText passwordET = findViewById(R.id.password);
        EditText passwordagainET = findViewById(R.id.passwordagain);

        String username = usernameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString();
        String passwordagain = passwordagainET.getText().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordagain.isEmpty()) {
            Toast.makeText(this, "Minden mezőt ki kell tölteni!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordagain)) {
            Toast.makeText(this, "A két jelszó nem egyezik!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ellenőrizzük, hogy a username már létezik-e
        db.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Ez a felhasználónév már foglalt!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Username szabad, létrehozzuk a felhasználót
                        createFirebaseUser(email, password, username);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt a felhasználónév ellenőrzése során!", Toast.LENGTH_SHORT).show();
                    Log.e("RegistrationActivity", "Username check error: " + e.getMessage());
                });
    }
    private void createFirebaseUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnSuccessListener(unused -> {
                                    String uid = mAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = db.collection("Users").document(uid);

                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("email", email);
                                    userData.put("username", username);
                                    userData.put("role", "user");

                                    documentReference.set(userData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Sikeres regisztráció!\nA megerősítő emailt elküldtük!", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("RegistrationActivity", "Firestore hiba: " + e.getMessage());
                                                Toast.makeText(this, "A felhasználó mentése sikertelen!", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("RegistrationActivity", "Email nem lett elküldve: " + e.getMessage());
                                    Toast.makeText(this, "Email nem lett elküldve!", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Ezzel az emaillel már regisztáltak!", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(this, "A jelszó túl gyenge!", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Nem megfelelő email!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Valami hiba történt!", Toast.LENGTH_SHORT).show();
                            Log.e("RegistrationActivity", "Hiba: ", e);
                        }
                    }
                });
    }

}
