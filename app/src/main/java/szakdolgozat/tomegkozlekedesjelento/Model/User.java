package szakdolgozat.tomegkozlekedesjelento.Model;

import com.google.firebase.firestore.Exclude;

public class User {
    private String id;
    private String email;
    private String username;
    private String role;
    private Long points;
    @Exclude
    private int rank;


    public User() {
    }

    public User(String email, String username, String role, Long points) {
        this.email = email;
        this.username = username;
        this.role = role;
        this.points = points;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
    @Exclude
    public int getRank() { return rank; }
    @Exclude
    public void setRank(int rank) { this.rank = rank; }
    public Long getPoints() {
        return points != null ? points : 0L; // ha nincs pont, 0-t ad vissza
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}


