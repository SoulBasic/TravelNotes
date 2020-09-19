import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class GListener implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //初始化数据库连接
        sce.getServletContext().setAttribute("lastRequestTime", System.currentTimeMillis());
        ResourceBundle rb = ResourceBundle.getBundle("dbcfg");
        String host = rb.getString("host");
        String userName = rb.getString("username");
        String passWord = rb.getString("password");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(host, userName, passWord);
            ServletContext svtcontext = sce.getServletContext();
            svtcontext.setAttribute("connection", con);
            Timer timer = new Timer();
            System.out.println("服务器开启");
            AutoCheckTask autoCheckTask = new AutoCheckTask(con,sce.getServletContext(),host,userName,passWord);
            timer.schedule(autoCheckTask, 10000, 600000);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }



    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //销毁数据库连接
        ServletContext svtcontext = sce.getServletContext();
        Connection con = (Connection) svtcontext.getAttribute("connection");
        try {
            con.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
