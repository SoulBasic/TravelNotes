import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

public class SpotsComments extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html;charset=UTF-8");
        ServletContext svtContext = req.getServletContext();
        PrintWriter pw = resp.getWriter();
        HttpSession ss = req.getSession(false);
        long checkTime =System.currentTimeMillis() - (long)svtContext.getAttribute("lastRequestTime");
        if(checkTime  <=5000){
            pw.print("{\"result\":\"-1\",\"message\":\"两次访问间隔必须超过5秒\"}");
            GISWEBUtil.log("用户请求获取了景点的评论,但因为时间过短被拒绝");
            return;
        }
        req.getServletContext().setAttribute("lastRequestTime", System.currentTimeMillis());

/*        if (ss==null) {
            pw.print("{\"result\":\"1\",\"message\":\"您没有权限访问这个页面，请登录\",\"tripid\":\"0\"}");
            return;
        }*/
        String spotName = req.getParameter("spotname");
        String spotID = req.getParameter("spotid");
        Connection con = (Connection) req.getServletContext().getAttribute("connection");
        String spotInfoUrl = "https://sec-m.ctrip.com/restapi/soa2/12530/json/viewCommentList?_fxpcqlniredt=09031091111074115997";
        String spotInfoUrlPiggy = "https://travelsearch.fliggy.com/index.htm?searchType=product&keyword=";
        String spotInfoUrlTn = "https://www.tuniu.com/web-mall/item/";
        String postData= "{\"pageid\":290510,\"viewid\":"+spotID+",\"tagid\":0,\"pagenum\":1,\"pagesize\":10,\"contentType\":\"json\",\"head\":{\"appid\":\"100013776\",\"cid\":\"09031091111074115997\",\"ctok\":\"\",\"cver\":\"1.0\",\"lang\":\"01\",\"sid\":\"8888\",\"syscode\":\"09\",\"auth\":\"\",\"extension\":[{\"name\":\"protocal\",\"value\":\"https\"}]},\"ver\":\"7.10.3.0319180000\"}";
        String commentsSource = GISWEBUtil.postHttps(spotInfoUrl, postData,true);
        GISWEBUtil.describe(spotInfoUrlPiggy,spotInfoUrlTn);
        GISWEBUtil.log("用户  请求获取了 " + spotName + " 景点的评论 ID="+spotID);
        pw.print(commentsSource);

    }




}

