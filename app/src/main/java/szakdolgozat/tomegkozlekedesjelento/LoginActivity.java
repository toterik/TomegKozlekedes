package szakdolgozat.tomegkozlekedesjelento;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("");
        }

    }

    public void cancel(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void login(View view)
    {
        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (email.isEmpty() || password.isEmpty())
        {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task ->
        {
            if (task.isSuccessful())
            {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user.isEmailVerified())
                {
                    Toast.makeText(LoginActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "This account is blocked!", Toast.LENGTH_SHORT).show();

                    FirebaseAuth.getInstance().signOut();
                }

            } else {
                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void registration(View view)
    {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }
}