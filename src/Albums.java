import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Albums extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter pw = resp.getWriter();
        HttpSession ss = req.getSession(false);
        if (ss==null) {
            pw.print("{\"result\":\"1\",\"message\":\"您没有权限访问这个页面，请登录\"}");
            return;
        }
        if(req.getParameter("tripindex") == null){
            pw.print("{\"result\":\"-1\",\"message\":\"请求不合法，已被服务器拒绝\"}");
            return;
        }
        int userID = (Integer)ss.getAttribute("userID");
        int tripIndex = Integer.parseInt(req.getParameter("tripindex"));
        Connection con = (Connection) req.getServletContext().getAttribute("connection");
        PreparedStatement preStat = null;
        ResultSet rs = null;
        try {
            preStat = con.prepareStatement("select UserID from trips where TripIndex=?");
            preStat.setInt(1,tripIndex);
            rs =preStat.executeQuery();
            if(rs.next()){
                if(userID != rs.getInt(1)){
                    pw.print("{\"result\":\"1\",\"message\":\"该行程不属于您，您无权限操作\"");
                    return;
                }
            }
            else{
                pw.print("{\"result\":\"-1\",\"message\":\"请求不合法，行程不存在\"}");
                return;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {

            try {
                rs.close();
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        ObjectMapper om = new ObjectMapper();
        String json = "";
        List<String> photoFileNames = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        try {
            preStat = con.prepareStatement("select * from photos where TripIndex=?");
            preStat.setInt(1, tripIndex);
            rs = preStat.executeQuery();
            while (rs.next()) {
                if (rs.getInt(5) == 1) {
                    photoFileNames.add(rs.getString(3));
                    notes.add(rs.getString(4));
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //String[] fileNames = new String[]{"1.jpg","2.jpg","3,jpg","4.jpg"};
        int photoSize = photoFileNames.size();
        String noteString = "[";
        for (int i = 0; i < photoSize; i++) {
            noteString = noteString + "{fileName:\"" + String.valueOf(i + 1) + ".jpg\",title:\"\",desc:\"" + notes.get(i) + "\"},";
        }
        noteString = noteString.substring(0,noteString.length() - 1);
        noteString = noteString+"]";


        //String[] fileNames = new String[];
        String pathString = "";
        for (int j = 0; j < photoSize; j++) {
            String[] s = photoFileNames.get(j).split("/");
            pathString = pathString + "function(e,t,n){e.exports=n.p+\"" + s[2] + "\"},";
        }
        if (photoSize < 16) {
            for (int k = 0; k < 16 - photoSize; k++) {
                pathString = pathString + "function(e,t,n){e.exports=n.p+\"\"},";
            }
        }


        String DBpath = this.getServletContext().getRealPath("./");
        String path = this.getServletContext().getRealPath("/albums/" + tripIndex);
        GISWEBUtil.log("DBPATH="+DBpath+" path="+path);

        File dirFile = new File(path);
        if (dirFile.exists()) {
            GISWEBUtil a = new GISWEBUtil();
            a.deleteDir(dirFile);
            dirFile.mkdirs();
        } else {
            dirFile.mkdirs();
        }
        ResourceBundle rb = ResourceBundle.getBundle("dbcfg");
        GISWEBUtil.xcopy(rb.getString("root"), path);
        for (int l = 0; l < photoSize; l++) {
            String fileName = DBpath + photoFileNames.get(l);
            String[] s = fileName.split("/");
            fileName = path + photoFileNames.get(l);
            String[] s1 = fileName.split("/");

            GISWEBUtil.cpFile(DBpath + "images/" + s[9], path + "/assets/" + s1[10]);
            GISWEBUtil.changeSize(420,280,path + "/assets/" + s1[10]);
            GISWEBUtil.log("src="+DBpath + "images/" + s[9]+" tg="+path + "/assets/" + s1[10]);

        }


        FileInputStream fileInputStream = new FileInputStream(rb.getString("part1"));
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);


        FileOutputStream fileOutputStream = new FileOutputStream(path + "/assets/app.js");
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        char[] temp = new char[1024];
        int len = 0;
        while ((len = bufferedReader.read(temp)) != -1) {
            bufferedWriter.write(temp, 0, len);
        }
        bufferedWriter.flush();


        bufferedWriter.write(noteString);
        bufferedWriter.flush();


        bufferedReader.close();
        inputStreamReader.close();
        fileInputStream.close();
        fileInputStream = new FileInputStream(rb.getString("part2"));
        inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        bufferedReader = new BufferedReader(inputStreamReader);
        while ((len = bufferedReader.read(temp)) != -1) {
            bufferedWriter.write(temp, 0, len);
        }
        bufferedWriter.flush();

        bufferedWriter.write(pathString);
        bufferedWriter.flush();

        bufferedReader.close();
        inputStreamReader.close();
        fileInputStream.close();
        fileInputStream = new FileInputStream(rb.getString("part3"));
        inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        bufferedReader = new BufferedReader(inputStreamReader);
        while ((len = bufferedReader.read(temp)) != -1) {
            bufferedWriter.write(temp, 0, len);
        }
        bufferedWriter.flush();
        bufferedReader.close();
        inputStreamReader.close();
        fileInputStream.close();
        bufferedWriter.close();
        outputStreamWriter.close();
        fileOutputStream.close();

        GISWEBUtil.log("用户 " + (String)ss.getAttribute("userName") + " 为"+tripIndex+"号行程添加了音乐相册");
        resp.sendRedirect("/albums/"+tripIndex+"/MusicAlbum.html");


    }
}
