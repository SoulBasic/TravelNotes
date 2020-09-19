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

public class ResetPassword extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        String userName = req.getParameter("username");
        String phoneNumber = req.getParameter("phonenum");
        String oldPassword = req.getParameter("oldpassword");
        String newPassword = req.getParameter("newpassword");
        PrintWriter wt = resp.getWriter();
        ObjectMapper om = new ObjectMapper();
        PreparedStatement preStat = null;
        Connection con =  (Connection) req.getServletContext().getAttribute("connection");
        ResultSet result = null;
        boolean matched = false;
        int count = 0;
        try {
            preStat =con.prepareStatement("select UserID from users where UserName=? and PassWord=? and PhoneNumber=?");
            preStat.setString(1, userName);
            preStat.setString(2, oldPassword);
            preStat.setString(3, phoneNumber);
            result = preStat.executeQuery();
            if(result.next())
                matched = true;
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            try {
                assert preStat != null;
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        if(!matched){
            resetResponse regRes = new resetResponse("1","修改密码失败，原密码或手机号码不匹配");
            String json = om.writeValueAsString(regRes);
            wt.print(json);
            return;
        }
        try {
            preStat = con.prepareStatement("update users set PassWord=? where UserName=?");
            preStat.setString(1, newPassword);
            preStat.setString(2, userName);
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
        GISWEBUtil.log("用户 "+userName+" 修改了密码");
        resetResponse regRes = new resetResponse("0","修改密码成功");
        String json = om.writeValueAsString(regRes);
        wt.print(json);
       // resp.sendRedirect("/login.html");
    }
}

class resetResponse{
    public resetResponse(String result, String message) {
        this.result = result;
        this.message = message;
    }
    public String result;
    public String message;
}
