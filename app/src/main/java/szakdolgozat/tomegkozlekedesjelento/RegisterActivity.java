package szakdolgozat.tomegkozlekedesjelento;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity
{
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void regisztracio(View view)
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

                mAuth.signInWithEmailAndPassword(email,password);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
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