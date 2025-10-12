package szakdolgozat.tomegkozlekedesjelento;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.annotation.Nullable;

import szakdolgozat.tomegkozlekedesjelento.Helper.LanguageManager;

public class MenuForAllActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstances)
    {
        super.onCreate(savedInstances);
        LanguageManager languageManager = new LanguageManager(this);
        languageManager.updateResource(languageManager.getLang());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //Apply Menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.GTFS_upload).setVisible(false);

        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            //If a User is logged in, the logout item is visible
            MenuItem item = menu.findItem(R.id.login_item);
            item.setVisible(false);
            MenuItem items = menu.findItem(R.id.logout_item);
            items.setVisible(true);

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .get()
                    .addOnSuccessListener(doc ->
                    {
                        if(doc.exists())
                        {
                            String role = doc.getString("role"); // vagy "rang" a te meződ neve
                            if("admin".equals(role))
                            {
                                menu.findItem(R.id.GTFS_upload).setVisible(true);
                            }
                        }
                    });
        }
        else
        {
            //If the user is not logged in, the login item is visible
            MenuItem loginItem = menu.findItem(R.id.login_item);
            loginItem.setVisible(true);
            MenuItem logOutitem = menu.findItem(R.id.logout_item);
            logOutitem.setVisible(false);
            MenuItem carReportItems = menu.findItem(R.id.car_reports_item);
            carReportItems.setVisible(false);
            MenuItem GTFSUploadItems = menu.findItem(R.id.GTFS_upload);
            GTFSUploadItems.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        LanguageManager languageManager = new LanguageManager(this);
        if (id == R.id.map_item)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.report_item)
        {
            Intent intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.car_reports_item)
        {
            Intent intent = new Intent(this, CarReportsActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.logout_item)
        {
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.login_item)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.registration_item)
        {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.leaderboard)
        {
            Intent intent = new Intent(this, LeaderboardActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.GTFS_upload)
        {
            Intent intent = new Intent(this, UploadGtfsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.lang_en)
        {
            languageManager.updateResource("en");
            recreate();
        } else if (id == R.id.lang_hu)
        {
            languageManager.updateResource("hu");
            recreate();
        }
        return false;
    }
}
