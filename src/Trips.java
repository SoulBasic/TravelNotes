import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Trips extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");

        PrintWriter pw = resp.getWriter();
        HttpSession ss = req.getSession(false);
        if (ss==null) {
            pw.print("{\"result\":\"1\",\"message\":\"您没有权限访问这个页面，请登录\",\"tripid\":\"0\"}");
            return;
        }



        String userName = (String) ss.getAttribute("userName");
        int userID = (Integer) ss.getAttribute("userID");
        Connection con = (Connection) req.getServletContext().getAttribute("connection");
        PreparedStatement preStat = null;
        if(req.getParameter("tripindex") == null){
            try {
                preStat = con.prepareStatement("select * from trips where userID=?");
                preStat.setInt(1, userID);
                ResultSet rs = preStat.executeQuery();
                List<TripInfo> trips = new ArrayList<>();
                ObjectMapper om = null;
                String json = null;
                TripInfo trip = null;
                while (rs.next()) {
                    double longitude = rs.getDouble(4);
                    double latitude = rs.getDouble(5);
                    String note = rs.getString(3);
                    long tripTime = rs.getLong(6);
                    int pub = rs.getInt(7);
                    String location = rs.getString(8);
                    trip = new TripInfo(rs.getInt(1),userID,note,longitude,latitude,tripTime,pub,location,GISWEBUtil.getUserName(con,userID));
                    trips.add(trip);
                }
                rs.close();
                om = new ObjectMapper();
                json = om.writeValueAsString(trips);
                GISWEBUtil.log("用户 "+ss.getAttribute("userName")+" 请求获取了自己的所有行程");
                pw.print(json);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }finally {
                try {
                    preStat.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        else{
            try {
                int tripIndex = Integer.valueOf(req.getParameter("tripindex"));
                preStat = con.prepareStatement("select * from trips where userID=? and TripIndex=?");
                preStat.setInt(1, userID);
                preStat.setInt(2, tripIndex);
                ResultSet rs = preStat.executeQuery();
                ObjectMapper om = null;
                String json = null;
                TripInfo trip = null;
                if (rs.next()) {
                    double longitude = rs.getDouble(4);
                    double latitude = rs.getDouble(5);
                    String note = rs.getString(3);
                    long tripTime = rs.getLong(6);
                    int pub = rs.getInt(7);
                    String location = rs.getString(8);
                    trip = new TripInfo(tripIndex,userID,note,longitude,latitude,tripTime,pub,location,GISWEBUtil.getUserName(con, userID));
                    rs.close();
                }
                else {
                    pw.print("{\"result\":\"-1\",\"message\":\"请求不合法，行程不存在或不公开\"}");
                    if(rs!=null)
                        rs.close();
                    return;
                }

                om = new ObjectMapper();
                json = om.writeValueAsString(trip);
                GISWEBUtil.log("用户 "+ss.getAttribute("userName")+" 请求获取了 "+tripIndex+" 号行程的信息");
                pw.print(json);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }finally {
                try {
                    preStat.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
        }



    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter pw = resp.getWriter();
        ObjectMapper om = new ObjectMapper();
        HttpSession ss = req.getSession(false);
        if (ss ==null) {
            tripSubmitResponse tsRes = new tripSubmitResponse("1", "用户尚未登录",0);
            String json = om.writeValueAsString(tsRes);
            pw.print(json);
            return;
        }

        int userID = (Integer) ss.getAttribute("userID");
        String userName = (String)ss.getAttribute("userName") ;
        String temp = req.getParameter("longitude");
        double longitude = Double.parseDouble(temp);
        temp = req.getParameter("latitude");
        double latitude = Double.parseDouble(temp);
        temp = req.getParameter("triptime");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        long tripTime = 0;
        try {
            tripTime = sdf.parse(temp).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String note = req.getParameter("note");
        int pub = Integer.parseInt( req.getParameter("pub"));
        String location = req.getParameter("location");
        Connection con = (Connection) req.getServletContext().getAttribute("connection");
        boolean findID = false;
        PreparedStatement preStat = null;
        try {
            preStat = con.prepareStatement("select * from users where UserID=?");
            preStat.setInt(1, userID);
            ResultSet rs = preStat.executeQuery();
            if (rs.next())
                findID = true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                assert preStat != null;
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        int count = 0;
        try {
            preStat = con.prepareStatement("insert into trips(UserID,Note,Longitude,Latitude,TripTime,Pub,Location) values(?,?,?,?,?,?,?)");
            preStat.setInt(1, userID);
            preStat.setString(2, note);
            preStat.setDouble(3, longitude);
            preStat.setDouble(4, latitude);
            preStat.setLong(5, tripTime);
            preStat.setInt(6, pub);
            preStat.setString(7, location);
            count = preStat.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            try {
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        ResultSet rs = null;
        int tripIndex = 0;
        try {
            preStat = con.prepareStatement("select TripIndex from trips where Longitude=? and Latitude=? and TripTime=?");
            preStat.setDouble(1, longitude);
            preStat.setDouble(2, latitude);
            preStat.setLong(3, tripTime);
            rs =  preStat.executeQuery();
            if(rs.next()){
                tripIndex = rs.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            try {
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        GISWEBUtil.log("用户 "+ss.getAttribute("userName")+" 添加了一个新的行程");
        tripSubmitResponse tsRes = new tripSubmitResponse("0", "提交成功",tripIndex);
        String json = om.writeValueAsString(tsRes);
        pw.print(json);

    }


}

class tripSubmitResponse {
    public tripSubmitResponse(String result, String message, int tripIndex) {
        this.result = result;
        this.message = message;
        this.tripIndex = tripIndex;
    }

    public String result;
    public String message;
    public int tripIndex;
}


