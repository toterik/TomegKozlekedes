package szakdolgozat.tomegkozlekedesjelento;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MenuForAllActivity extends AppCompatActivity
{

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //Apply Menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            //If a User is logged in, the logout item is visible
            MenuItem item = menu.findItem(R.id.login_item);
            item.setVisible(false);
            MenuItem items = menu.findItem(R.id.logout_item);
            items.setVisible(true);
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
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.map_item)
        {
            //The Map item is selected, the Map activity opens
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.report_item)
        {
            //The Report item is selected, the Report activity opens
            Intent intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.car_reports_item)
        {
            //The Report item is selected, the Report activity opens
            Intent intent = new Intent(this, CarReportsActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.logout_item)
        {
            //The Logout item is selected, the user is logged out and the activity refreshes
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.login_item)
        {
            //The Login item is selected, the Login activity opens
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.registration_item)
        {
            //The Registration item is selected, the Registration activity opens
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.leaderboard)
        {
            //The leaderboard item is selected, the leaderboard activity opens
            Intent intent = new Intent(this, LeaderboardActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
