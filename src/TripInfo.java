import java.text.SimpleDateFormat;
import java.util.Date;

public class TripInfo {

    public int tripIndex;
    public int userID;
    public String note;
    public double longitude;
    public double latitude;
    public String tripTime;
    public int pub;
    public String location;
    public String userName;

    public TripInfo(int tripIndex, int userID, String note, double longitude, double latitude, long tripTime, int pub, String location, String userName) {
        this.tripIndex = tripIndex;
        this.userID = userID;
        this.longitude = longitude;
        this.latitude = latitude;
        this.note = note;
        this.pub = pub;
        this.location = location;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        Date temp1 = new Date(tripTime);
        this.tripTime = sdf.format(temp1);
        this.userName = userName;
    }
}