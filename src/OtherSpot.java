
public class OtherSpot {
    public String word;
    public String id;

    public OtherSpot(String word, String id) {
        this.word = word;
        this.id = id;
    }

    @Override
    public String toString() {
        return "OtherSpot{" +
                "word='" + word + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
