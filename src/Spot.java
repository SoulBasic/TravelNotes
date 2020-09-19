import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Spot {
    @JsonProperty("id")
    public String spotID;

    @JsonProperty("word")
    public String spotName;

    public String eName;

    public String type;

    @JsonProperty("lat")
    public Double latitude;

    @JsonProperty("lon")
    public Double longitude;

    public String districtName;

    public String price;

    @JsonProperty("alias")
    public String brief;

    @JsonProperty("commentScore")
    public String spotScore;

    public String address;

    public String imageUrl;

    public List<OtherSpot> otherSpots;

    @Override
    public String toString() {
        return "Spot{" +
                "spotID='" + spotID + '\'' +
                ", spotName='" + spotName + '\'' +
                ", eName='" + eName + '\'' +
                ", type='" + type + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", districtName='" + districtName + '\'' +
                ", price='" + price + '\'' +
                ", brief='" + brief + '\'' +
                ", spotScore='" + spotScore + '\'' +
                ", address='" + address + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
