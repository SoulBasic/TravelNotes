import com.fasterxml.jackson.databind.ObjectMapper;
import sun.java2d.pipe.SpanIterator;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginCheckFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse  resp =(HttpServletResponse)servletResponse;
        HttpSession ss = req.getSession(false);
        if (ss == null) {
            servletResponse.setContentType("text/html;charset=UTF-8");
            PrintWriter pw = servletResponse.getWriter();
            pw.print("{\"result\":\"1\",\"message\":\"您没有权限访问这个页面，请登录\"}");
            return;
        }
        int userID = (Integer) ss.getAttribute("userID");
        Connection con = (Connection) servletRequest.getServletContext().getAttribute("connection");
        String uri = req.getRequestURI();
        //uri = GISWEBUtil.URLDecoderString(uri);
        PreparedStatement preStat = null;
        ResultSet rs = null;
        boolean check = false;
        try {
            preStat = con.prepareStatement("select UserID,Pub from photos where PhotoFileName=?");
            preStat.setString(1, uri);
            rs = preStat.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == userID || rs.getInt(2) == 1)
                    check = true;
            }
            else{
                resp.setStatus(404);
                return;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            try {
                if(rs!=null)
                    rs.close();
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (!check) {
            GISWEBUtil.log("用户 " + ss.getAttribute("userName") + " 尝试非法访问图片资源" + uri);
            servletResponse.setContentType("text/html;charset=UTF-8");
            PrintWriter pw = servletResponse.getWriter();
            pw.print("{\"result\":\"1\",\"message\":\"您没有权限访问这个页面\"}");
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

}

