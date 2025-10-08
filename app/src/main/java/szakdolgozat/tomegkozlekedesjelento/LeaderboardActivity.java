package szakdolgozat.tomegkozlekedesjelento;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import szakdolgozat.tomegkozlekedesjelento.Model.User;
import szakdolgozat.tomegkozlekedesjelento.RecycleViewAdapter.UserAdapter;

public class LeaderboardActivity extends MenuForAllActivity {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private EditText searchBox;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");

    private List<String> globalTop3Ordered = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        searchBox = findViewById(R.id.searchBox);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadTopUsers();

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadTopUsers() {
        usersRef.orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    globalTop3Ordered.clear();
                    int idx = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User u = doc.toObject(User.class);
                        u.setId(doc.getId());
                        u.setRank(idx);
                        userList.add(u);
                        if (idx < 3) {
                            globalTop3Ordered.add(doc.getId());
                        }
                        idx++;
                    }
                    adapter.setGlobalTop3Ordered(globalTop3Ordered);
                    adapter.setFilteredList(new ArrayList<>(userList));
                });
    }
    private void filterList(String query) {
        List<User> filtered = new ArrayList<>();
        for (User u : userList) {
            if (u.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(u);
            }
        }
        adapter.setFilteredList(filtered);
    }
}

