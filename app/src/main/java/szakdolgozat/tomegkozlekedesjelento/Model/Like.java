public class Like {
    private int count;
    private String uid;
    private String reportId;
    private String likeId;

    public Like() {
    }

    public Like(int count, String uid, String reportId, String likeId) {
        this.count = count;
        this.uid = uid;
        this.reportId = reportId;
        this.likeId = likeId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getLikeId() {
        return likeId;
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
    }
}
