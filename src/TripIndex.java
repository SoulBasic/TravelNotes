import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripIndex extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        HttpSession ss = req.getSession(false);
        ObjectMapper om = new ObjectMapper();
        PrintWriter pw = resp.getWriter();
        if (ss ==null) {
            pw.print("{\"result\":\"1\",\"message\":\"请登录\"");
            return;
        }
        Connection con = (Connection)req.getServletContext().getAttribute("connection");
        PreparedStatement preStat = null;
        ResultSet rs =null;
        List<TripInfo> trips = new ArrayList<>();
        int userID = (Integer) ss.getAttribute("userID");
        try {
            preStat = con.prepareStatement("select * from trips where Pub=1 and UserID!=?");
            preStat.setInt(1, userID);
            rs = preStat.executeQuery();

            while(rs.next()){
                    TripInfo trip = new TripInfo(
                            rs.getInt(1),
                            rs.getInt(2),
                            rs.getString(3),
                            rs.getDouble(4),
                            rs.getDouble(5),
                            rs.getLong(6),
                            rs.getInt(7),
                            rs.getString(8),
                            GISWEBUtil.getUserName(con,rs.getInt(2)));
                    trips.add(trip);
                }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            try {
                preStat.close();
                if(rs!=null)
                    rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        String json = om.writeValueAsString(trips);
        GISWEBUtil.log("用户 "+ss.getAttribute("userName")+" 请求获取了他人的公共行程");
        pw.print(json);

    }
}
