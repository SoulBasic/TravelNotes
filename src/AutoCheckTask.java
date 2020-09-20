import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.TimerTask;

public class AutoCheckTask extends TimerTask {

    public Connection con;
    public ServletContext servletContext;
    public String host;
    public String userName;
    public String passWord;

    public AutoCheckTask(Connection con, ServletContext servletContext, String host, String userName, String passWord) {
        this.con = con;
        this.servletContext = servletContext;
        this.host = host;
        this.userName = userName;
        this.passWord = passWord;
    }


    @Override
    public void run() {
        try {
            Connection conn = (Connection) servletContext.getAttribute("connection");
            PreparedStatement st = conn.prepareStatement("update time set lasttime=? where name='jwl'");
            st.setString(1, String.valueOf(System.currentTimeMillis()));
            st.executeUpdate();
            servletContext.setAttribute("connection", conn);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }
}
