import com.fasterxml.jackson.databind.ObjectMapper;
import sun.plugin2.gluegen.runtime.CPU;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        HttpSession ss = req.getSession(false);
        PrintWriter pw = resp.getWriter();
        if (ss == null) {
            pw.print("{\"result\":\"1\",\"message\":\"请登录\"}");
            return;
        }
        String affirm = req.getParameter("affirm");
        if (affirm == null || Integer.parseInt(affirm) != 1) {
            pw.print("{\"result\":\"-1\",\"message\":\"请求不合法，已被服务器拒绝\"}");
            return;
        }
        GISWEBUtil.log("用户 " + ss.getAttribute("userName") + " 请求注销成功");
        ss.invalidate();
        pw.print("{\"result\":\"0\",\"message\":\"注销成功\"}");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin","*");
        resp.setContentType("text/html;charset=UTF-8");
        String userName = req.getParameter("username");
        String passWord = req.getParameter("password");
        PrintWriter wt = resp.getWriter();
        ObjectMapper om = new ObjectMapper();
        boolean matched = false;
        ServletContext svtContext = req.getServletContext();
        Connection con = (Connection) svtContext.getAttribute("connection");
        PreparedStatement preStat = null;
        ResultSet rs = null;
        try {
            preStat = con.prepareStatement("select UserID from users where UserName=? and PassWord=?");
            preStat.setString(1, userName);
            preStat.setString(2, passWord);
            rs = preStat.executeQuery();
            if (rs.next())
                matched = true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (matched) {
            GISWEBUtil.log("用户 " + userName + " 登录成功");
            Cookie cookieUserName = new Cookie("userName",userName);
            resp.addCookie(cookieUserName);
            loginResponse loginRes = new loginResponse("0", userName + "，欢迎回来");
            HttpSession ss = req.getSession();
            ss.setAttribute("userName", userName);
            int userID = 0;
            try {
                preStat = con.prepareStatement("select UserID from users where UserName=?");
                preStat.setString(1, userName);
                rs = preStat.executeQuery();
                userID = rs.next() ? rs.getInt(1) : 0;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                try {
                    if(rs!=null)
                        rs.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            ss.setAttribute("userID", userID);
            String json = om.writeValueAsString(loginRes);
            wt.print(json);
            //resp.sendRedirect("/index.html");
            //resp.sendRedirect("/GISWEB/index.html");
        } else {
            GISWEBUtil.log("用户 " + userName + " 登录失败");
            loginResponse loginRes = new loginResponse("1", "用户名或密码错误");
            String json = om.writeValueAsString(loginRes);
            wt.print(json);
        }
    }
}

class loginResponse {
    public loginResponse(String result, String message) {
        this.result = result;
        this.message = message;
    }

    public String result;
    public String message;
}