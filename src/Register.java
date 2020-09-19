import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Register extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        String userName = req.getParameter("username");
        String passWord = req.getParameter("password");
        String phoneNumber = req.getParameter("phonenum");
        PrintWriter wt = resp.getWriter();
        if(userName.length()<=2 || passWord.length() <6 || phoneNumber.length()!=11){
            wt.print("{\"result\":\"-1\",\"userid\":\"0\"}");
            return; }
        ObjectMapper om = new ObjectMapper();
        String userID = "";
        PreparedStatement preStat = null;
        Connection con =  (Connection) req.getServletContext().getAttribute("connection");
        int count = 0;
        try {
            preStat = con.prepareStatement("insert into users(UserName,PassWord,PhoneNumber) value(?,?,?)");
            preStat.setString(1, userName);
            preStat.setString(2, passWord);
            preStat.setString(3, phoneNumber);
            count = preStat.executeUpdate();
            System.out.println(count);

        }catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException a){
            registerResponse regRes = new registerResponse("1","注册失败，用户名已存在", userID);
            String json = om.writeValueAsString(regRes);
            wt.print(json);
            return;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            try {
                assert preStat != null;
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        try {
            preStat = con.prepareStatement("select UserID from users where UserName=?");
            preStat.setString(1, userName);
            ResultSet rs = preStat.executeQuery();
            if(rs.next())
                userID = rs.getString(1);
            else
                userID = "0";
        } catch (SQLException throwables) {
            throwables.printStackTrace();

        }finally {
            try {
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        GISWEBUtil.log("用户 "+userName+" 注册成为了新会员");
        registerResponse regRes = new registerResponse("0","注册成功",userID);
        String json = om.writeValueAsString(regRes);
        wt.print(json);
        //resp.sendRedirect("/index.html");
    }
}

class registerResponse{
    public registerResponse(String result, String message, String userid) {
        this.result = result;
        this.message = message;
        this.userid = userid;
    }
    public String result;
    public String message;
    public String userid;
}
