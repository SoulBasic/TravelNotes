import com.fasterxml.jackson.databind.ObjectMapper;

import javax.print.attribute.standard.MediaSize;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class Spots extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html;charset=UTF-8");
        ServletContext svtContext = req.getServletContext();
        PrintWriter pw = resp.getWriter();        HttpSession ss = req.getSession(false);

/*        if (ss==null) {
            pw.print("{\"result\":\"1\",\"message\":\"您没有权限访问这个页面，请登录\",\"tripid\":\"0\"}");
            return;-+0
        }*/

        String spotName = req.getParameter("spotname");
        Connection con = (Connection) req.getServletContext().getAttribute("connection");
        PreparedStatement preStat = null;
        String url = "https://m.ctrip.com/restapi/h5api/globalsearch/search?action=online&source=globalonline&keyword="+ URLEncoder.encode(spotName, "UTF-8")+"&t="+System.currentTimeMillis();
        String json = GISWEBUtil.getHttps(url);
        ObjectMapper om = new ObjectMapper();
        SpotsData spotsDatas = (SpotsData) GISWEBUtil.jsonToObj(new SpotsData(), json);
        List<Spot> spots = spotsDatas.data;
        Spot temp  = null;
        int usefulIndex = 0;
        for(int i=0;i<spots.size();i++){
            temp = spots.get(i);
            if(temp.type.equals("ticket")||temp.type.equals("sight")){
                usefulIndex = i;
                break;
            }
        }
        temp = spots.get(usefulIndex);
        if(temp.imageUrl == null){
            temp.imageUrl = "./404ImgNotFound.PNG";
        }
        int count = 0;
        OtherSpot os = null;
        List<OtherSpot> list = new ArrayList<>();
        for(int j=0;j<spots.size();j++){
            if(j!= usefulIndex ){
                if(spots.get(j).type.equals("ticket")|| spots.get(j).type.equals("sight")){
                    os = new OtherSpot(spots.get(j).spotName,spots.get(j).spotID);
                    list.add(os);
                    count++;
                }


            }
            if(count>5)
                break;
        }
        temp.otherSpots = list;
        json = om.writeValueAsString(temp);
        GISWEBUtil.log("用户  请求获取了 " + spotName + " 景点的信息");
        pw.print(json);

    }
}
