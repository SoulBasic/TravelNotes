import javax.servlet.*;
import java.io.IOException;
import java.sql.Connection;

public class AllFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ServletContext svtContext = servletRequest.getServletContext();
        Connection con = (Connection)svtContext.getAttribute("connection");

        if(con == null){
            GISWEBUtil.initConnection(svtContext);
            GISWEBUtil.log("检查connection不可用，已重新连接");
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
