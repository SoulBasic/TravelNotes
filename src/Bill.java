import java.text.SimpleDateFormat;
import java.util.Date;

public class Bill {
    public int billIndex;
    public String userName;
    public double cost;
    public String purpose;
    public String billLocation;
    public String billTime;


    public Bill(int billIndex, String userName, double cost, String purpose, String billLocation, long billTime) {
        this.billIndex = billIndex;
        this.userName = userName;
        this.cost = cost;
        this.purpose = purpose;
        this.billLocation = billLocation;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        Date temp1 = new Date(billTime);
        this.billTime = sdf.format(temp1);
    }
}
