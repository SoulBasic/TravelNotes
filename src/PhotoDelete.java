import com.fasterxml.jackson.databind.ObjectMapper;

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

public class PhotoDelete extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter pw = resp.getWriter();
        ObjectMapper om = new ObjectMapper();
        HttpSession ss = req.getSession(false);
        if (ss == null) {
            pw.print("{\"result\":\"1\",\"message\":\"您没有权限访问这个页面，请登录\"");
            return;
        }
        PreparedStatement preStat = null;
        Connection con = (Connection) req.getServletContext().getAttribute("connection");
        int userID = (Integer) ss.getAttribute("userID");
        String photoUrl = GISWEBUtil.URLDecoderString(req.getParameter("photourl"));
        System.out.println(photoUrl);
        ResultSet rs = null;
        try {
            preStat = con.prepareStatement("select UserID from photos where PhotoFileName=?");
            preStat.setString(1, photoUrl);
            rs = preStat.executeQuery();
            if (rs.next()) {
                if (userID != rs.getInt(1)) {
                    pw.print("{\"result\":\"1\",\"message\":\"该照片不属于您，您无权限操作\"");
                    return;
                }
            } else {
                pw.print("{\"result\":\"-1\",\"message\":\"请求不合法，照片不存在\"}");
                return;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                if(rs!=null)
                    rs.close();
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
        int count =0;
        try {
            preStat = con.prepareStatement("delete from photos where PhotoFileName=?");
            preStat.setString(1, photoUrl);
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
        if(count!=1){
            pw.print("{\"result\":\"-1\",\"message\":\"请求不合法，照片不存在\"}");
            return;
        }
        GISWEBUtil.log("用户 "+ss.getAttribute("userName")+" 删除了照片 "+photoUrl);
        photoDeleteResponse psRes = new photoDeleteResponse("0", "删除成功");
        String json = om.writeValueAsString(psRes);
        pw.print(json);

    }
}

class photoDeleteResponse {
    public photoDeleteResponse(String result, String message) {
        this.result = result;
        this.message = message;
    }
    public String result;
    public String message;
}
