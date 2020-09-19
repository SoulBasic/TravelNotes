import java.util.ArrayList;
public class CommentSource{
    public data data;
}
class data{
    public int cmtscore;
    public comments[] comments;
}
class comments {
    public String uid;
    public String userImage;
    public String title;
    public String content;
    public String date;
    public int score;
    public ArrayList<String> bimgs;
}
