import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javafx.image.BytePixelSetter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
import java.util.List;

public class Bills extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter pw = resp.getWriter();
        HttpSession ss = req.getSession(false);
        if (ss == null) {
            pw.print("{\"result\":\"1\",\"message\":\"您没有权限访问这个页面，请登录\"}");
            return;
        }
        String userName = (String) ss.getAttribute("userName");
        Connection con = (Connection) req.getServletContext().getAttribute("connection");
        PreparedStatement preStat = null;



        String temp = req.getParameter("starttime");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        long startTime = 0;

        try {
            if(temp != null && !temp.equals("年月日"))
                startTime = sdf.parse(temp).getTime();
        } catch (ParseException e) {
            pw.print("{\"result\":\"-1\",\"message\":\"参数不合法，请检查参数\"}");
            return;
        }
        temp = req.getParameter("endtime");
        long endTime = 9999999999999L;
        try {
            if(temp != null && !temp.equals("年月日"))
                endTime = sdf.parse(temp).getTime();
        } catch (ParseException e) {
            pw.print("{\"result\":\"-1\",\"message\":\"参数不合法，请检查参数\"}");
            return;
        }

        double minCost = 0;
        if(req.getParameter("mincost") == null || req.getParameter("mincost").equals(""))
            minCost = 0;
        else{
            minCost = Double.parseDouble(req.getParameter("mincost"));
        }


        double maxCost = 0;
        if(req.getParameter("maxcost") == null || req.getParameter("maxcost").equals(""))
            maxCost =999999999;
        else{
            maxCost = Double.parseDouble(req.getParameter("maxcost"));
        }
        String sql = "select * from bills where UserName=? and Cost>=? and Cost<=? and TripTime>=? and TripTime<=? ";
        boolean haveCostType = false;
        String purpose = req.getParameter("purpose");
        if(purpose != null && !purpose.equals("")){
            sql = sql +"and Purpose=?";
            haveCostType = true;
        }

        try {
            preStat = con.prepareStatement(sql);
            preStat.setString(1, userName);
            preStat.setDouble(2, minCost);
            preStat.setDouble(3, maxCost);
            preStat.setDouble(4, startTime);
            preStat.setDouble(5, endTime);
            if(haveCostType)
                preStat.setString(6, purpose);

            ResultSet rs = preStat.executeQuery();
            List<Bill> bills = new ArrayList<>();
            ObjectMapper om = null;
            String json = null;
            Bill bill = null;
            while (rs.next()) {
                bill = new Bill(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4), rs.getString(5), rs.getLong(6));
                bills.add(bill);
            }
            rs.close();
            om = new ObjectMapper();
            json = om.writeValueAsString(bills);
            GISWEBUtil.log("用户 " + ss.getAttribute("userName") + " 请求获取账单");
            pw.print(json);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
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
        if (ss == null) {
            pw.print("{\"result\":\"1\",\"message\":\"您没有权限访问这个页面，请登录\"}");
            return;
        }
        String userName = (String)ss.getAttribute("userName");
        String location = req.getParameter("location");
        String temp = req.getParameter("billtime");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        long billTime = 0;
        try {
            billTime = sdf.parse(temp).getTime();
        } catch (ParseException e) {
            pw.print("{\"result\":\"-1\",\"message\":\"参数不合法，请检查参数\"}");
            return;
        }

        double cost = Double.parseDouble(req.getParameter("cost"));
        String purpose = req.getParameter("purpose");
        Connection con = (Connection) req.getServletContext().getAttribute("connection");
        PreparedStatement preStat = null;
        ResultSet rs = null;

        int count = 0;
        try {
            preStat = con.prepareStatement("insert into bills(UserName,Cost,Purpose,TripLocation,TripTime) values(?,?,?,?,?)");
            preStat.setString(1, userName);
            preStat.setDouble(2, cost);
            preStat.setString(3, purpose);
            preStat.setString(4, location);
            preStat.setLong(5, billTime);
            count = preStat.executeUpdate();
            if (count != 1) {
                pw.print("{\"result\":\"-1\",\"message\":\"服务器未知错误，请联系管理员\"}");
                return;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
        GISWEBUtil.log("用户 " + ss.getAttribute("userName") + " 添加了一个新的账单");
        billSubmitResponse bsRes = new billSubmitResponse("0", "提交成功");
        String json = om.writeValueAsString(bsRes);
        pw.print(json);


    }
}

class billSubmitResponse {
    public billSubmitResponse(String result, String message) {
        this.result = result;
        this.message = message;
    }

    public String result;
    public String message;
}

