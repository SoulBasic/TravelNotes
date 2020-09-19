import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class Photos extends HttpServlet {
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
        boolean selfTrip = false;
        boolean tripPublic = false;
        int userID = (Integer)ss.getAttribute("userID");
        int tripIndex = Integer.parseInt(req.getParameter("tripindex"));
        Connection con = (Connection) req.getServletContext().getAttribute("connection");
        PreparedStatement preStat = null;
        ResultSet rs = null;
        ObjectMapper om = new ObjectMapper();
        String json ="";
        try {
            preStat =con.prepareStatement("select UserID,Pub from trips where TripIndex=?");
            preStat.setInt(1, tripIndex);
            rs = preStat.executeQuery();
            if(!rs.next()){//请求的行程不存在
                pw.print("{\"result\":\"-1\",\"message\":\"请求不合法，行程不存在\"}");
                return;
            }

            if(userID == rs.getInt(1))
                selfTrip =true;
            else{
                if(rs.getInt(2) == 1)
                    tripPublic = true;
            }
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
        //开始select图片表中的数据

        try {
            preStat = con.prepareStatement("select * from photos where Tripindex=?");
            preStat.setInt(1, tripIndex);
            rs = preStat.executeQuery();
            List<Photo> photos = new ArrayList<>();
            if(selfTrip){
                while (rs.next()){
                    Photo p = new Photo(rs.getInt(1),rs.getInt(2),rs.getString(3), rs.getString(4),rs.getInt(5));
                    photos.add(p);
                }
                om = new ObjectMapper();
                json = om.writeValueAsString(photos);
                GISWEBUtil.log("用户 "+ss.getAttribute("userName")+" 访问了 "+tripIndex+" 号行程的照片");
                pw.print(json);
            }//请求自己的行程
            else{
                if(tripPublic){
                    while(rs.next()){
                        if(rs.getInt(5) == 1){
                            Photo p = new Photo(rs.getInt(1),rs.getInt(2),rs.getString(3), rs.getString(4),rs.getInt(5));
                            photos.add(p);
                        }
                    }
                    om = new ObjectMapper();
                    json = om.writeValueAsString(photos);
                    GISWEBUtil.log("用户 "+ss.getAttribute("userName")+" 访问了 "+tripIndex+" 号行程的照片");
                    pw.print(json);
                }//他人的公开行程
                else
                    pw.print("{\"result\":\"-1\",\"message\":\"请求不合法，行程不公开\"}");//他人的私人行程
            }//请求他人的行程
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            try {
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        HttpSession ss = request.getSession(false);
        if (ss==null) {
            pw.print("{\"result\":\"1\",\"message\":\"您没有权限访问这个页面，请登录\"");
            return;
        }
        // 判断请求是否为multipart请求
        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new RuntimeException("当前请求不支持文件上传");
        }
        OutputStream os = null;
        InputStream is = null;
        int userID = (Integer) ss.getAttribute("userID");
        Connection con = (Connection) request.getServletContext().getAttribute("connection");
        PreparedStatement preStat = null;

        try {
            // 创建一个FileItem工厂
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // 设置使用临时文件的边界值，大于该值，上传文件会先保存在临时文件中，否则，上传文件将直接写入到内存。
            // 单位：字节。本例设置边界值为1M
            factory.setSizeThreshold(1024 * 1024);

            // 设置临时文件
            String tempPath = this.getServletContext().getRealPath("/temp");
            File temp = new File(tempPath);
            factory.setRepository(temp);

            // 创建文件上传核心组件
            ServletFileUpload upload = new ServletFileUpload(factory);

            // 设置每一个item的头部字符编码，其可以解决文件名的中文乱码问题
            upload.setHeaderEncoding("UTF-8");

            // 设置单个上传文件的最大边界值为2M
            upload.setFileSizeMax(1024 * 1024 * 5);

            // 设置一次上传所有文件的总和最大值为5M（对于上传多个文件时起作用）
            upload.setSizeMax(1024 * 1024 * 20);

            // 解析请求，获取到所有的item
            List<FileItem> items = upload.parseRequest(request);
            // 遍历items
            int tripIndex = 0;
            String note = "";
            int pub = 0;
            for (FileItem item : items) {
                if (item.isFormField()) {   // 若item为普通表单项
                    String fieldName = item.getFieldName();           // 获取表单项名称
                    if (fieldName.equals("tripindex")) {
                        String ti = item.getString("UTF-8");      // 获取表单项的值
                        tripIndex = Integer.parseInt(ti);
                    } else if (fieldName.equals("note"))
                        note = item.getString("UTF-8");
                    else if(fieldName.equals("pub"))
                        pub = Integer.parseInt(item.getString("UTF-8"));

                } else {                   // 若item为文件表单项
                    String fileName = item.getName();    // 获取上传文件原始名称
                    fileName = System.currentTimeMillis() + fileName;
                    // 获取输入流，其中有上传文件的内容
                    is = item.getInputStream();
                    // 获取文件保存在服务器的路径
                    String path = this.getServletContext().getRealPath("/images");


                    // 若该目录不存在，则创建这个目录
                    File dirFile = new File(path);
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }

                    // 创建目标文件，将来用于保存上传文件
                    File descFile = new File(path, fileName);
                    if (tripIndex <= 0) {
                        pw.print("{\"result\":\"-1\",\"message\":\"请求不合法，已被服务器拒绝\"");
                        return;
                    }
                    preStat = con.prepareStatement("select UserID from trips where TripIndex=?");
                    preStat.setInt(1,tripIndex);
                    ResultSet rs =preStat.executeQuery();
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

                    rs.close();
                    preStat.close();

                    preStat = con.prepareStatement("insert into photos(TripIndex,UserID,PhotoFileName,Note,Pub) values(?,?,?,?,?)");
                    preStat.setInt(1, tripIndex);
                    preStat.setInt(2, userID);
                    preStat.setString(3, "/images/" +fileName);
                    preStat.setString(4, note);
                    preStat.setInt(5, pub);
                    int count = preStat.executeUpdate();
                    if (count != 1) {
                        pw.print("{\"result\":\"-1\",\"message\":\"请求不合法，已被服务器拒绝\"");
                        System.out.println("insert语句count不为1，为" + count);
                        return;
                    }

                    // 创建文件输出流
                    os = new FileOutputStream(descFile);
                    // 将输入流中的数据写入到输出流中
                    int len = -1;
                    byte[] buf = new byte[1024];
                    while ((len = is.read(buf)) != -1) {
                        os.write(buf, 0, len);
                    }
                    GISWEBUtil.log("用户 "+ss.getAttribute("userName")+" 提交图片");
                    pw.print("{\"result\":\"0\",\"message\":\"提交成功\"}");
                    // 删除临时文件
                    item.delete();
                }
            }


        } catch (FileUploadException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            // 关闭流
            os.close();
            is.close();
            try {
                preStat.close();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }


        }


    }

}

class Photo{

    public int tripIndex;
    public int userID;
    public String photoUrl;
    public String note;
    public int pub;

    public Photo(int tripIndex, int userID, String photoUrl, String note, int pub) {
        this.tripIndex = tripIndex;
        this.userID = userID;
        this.photoUrl = photoUrl;
        this.note = note;
        this.pub = pub;
    }
}
