import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.TimerTask;

public class AutoCheckTask extends TimerTask {

    public Connection con;
    public ServletContext servletContext;
    public AutoCheckTask(Connection con, ServletContext servletContext) {
        this.con = con;
        this.servletContext = servletContext;
    }

    @Override
    public void run() {
        try {
            System.out.println("服务器自刷新mysql连接");
            con.close();
            ResourceBundle rb = ResourceBundle.getBundle("dbcfg");
            String host = rb.getString("host");
            String userName = rb.getString("username");
            String passWord = rb.getString("password");
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(host, userName, passWord);
            servletContext.setAttribute("connection", con);
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }


    }
}
