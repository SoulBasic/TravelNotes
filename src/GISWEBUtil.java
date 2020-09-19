import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.bcel.internal.generic.RET;

import javax.imageio.ImageIO;
import javax.net.ssl.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

public class GISWEBUtil {
    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    //执行预编译sql查询语句
    public static ResultSet preparedSQLQuery(Connection con, String sql, Object[] parameters) {
        PreparedStatement preStat = null;
        ResultSet rs = null;
        try {
            preStat = con.prepareStatement(sql);
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] instanceof String) {
                    preStat.setString(i + 1, (String) parameters[i]);
                    break;
                } else if (parameters[i] instanceof Integer) {
                    preStat.setInt(i + 1, (Integer) parameters[i]);
                    break;
                } else if (parameters[i] instanceof Long) {
                    preStat.setLong(i + 1, (Long) parameters[i]);
                    break;
                } else if (parameters[i] instanceof Double) {
                    preStat.setDouble(i + 1, (Double) parameters[i]);
                    break;
                }

            }
            rs = preStat.executeQuery();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return rs;
    }

    //执行预编译sql更新语句
    public static int preparedSQLUpdate(Connection con, String sql, Object[] parameters) {
        PreparedStatement preStat = null;
        int count = 0;
        try {
            preStat = con.prepareStatement(sql);
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] instanceof String) {
                    preStat.setString(i + 1, (String) parameters[i]);
                    break;
                } else if (parameters[i] instanceof Integer) {
                    preStat.setInt(i + 1, (Integer) parameters[i]);
                    break;
                } else if (parameters[i] instanceof Long) {
                    preStat.setLong(i + 1, (Long) parameters[i]);
                    break;
                } else if (parameters[i] instanceof Double) {
                    preStat.setDouble(i + 1, (Double) parameters[i]);
                    break;
                }

            }
            count = preStat.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                preStat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return count;
    }


/*    public static Connection getConnection(ServletContext svtContext){
        HashMap<Connection,Boolean> cons = (HashMap<Connection,Boolean>) svtContext.getAttribute("connection");
        Iterator it = cons.keySet().iterator();
        Connection con = null;
        while(it.hasNext()){
            con = (Connection) it.next();
            if(cons.get(con)){
                cons.put(con, false);
                break;
            }
        }
        return con;
    }

    public static void freeConnection(ServletContext svtContext,Connection con){
        HashMap<Connection,Boolean> cons = (HashMap<Connection,Boolean>) svtContext.getAttribute("connection");
        cons.put(con,true);
    }*/

    public static void initConnection(ServletContext svtContext){

            //初始化数据库连接
            ResourceBundle rb = ResourceBundle.getBundle("dbcfg");
            String host = rb.getString("host");
            String userName = rb.getString("username");
            String passWord = rb.getString("password");
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(host, userName, passWord);
                svtContext.setAttribute("connection", con);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
    }

    public static int getUserID(Connection con, String userName) {
        Object[] objs = new Object[1];
        objs[0] = userName;
        ResultSet rs = GISWEBUtil.preparedSQLQuery(con, "select UserID from users where UserName=?", objs);
        try {
            if (rs.next()) {
                return rs.getInt(1);
            } else
                return 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public static String getUserName(Connection con, int userID) {
        PreparedStatement preStat = null;
        ResultSet rs = null;
        try {
            preStat = con.prepareStatement("select UserName from users where UserID=?");
            preStat.setInt(1, userID);
            rs = preStat.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else
                return "";
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return "";
    }

    public static void log(String logString) {
        SimpleDateFormat sdf = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss,SSS]");
        Date date = new Date();
        System.out.println(sdf.format(date) + " " + logString);
        FileWriter out = null;
        ResourceBundle rb = ResourceBundle.getBundle("dbcfg");
        try {
            out = new FileWriter(rb.getString("log"), true);
            out.write(sdf.format(date) + " " + logString + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public static String URLDecoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }

    public static String getHttps(String hsUrl) {

        try {
            URL url;
            url = new URL(hsUrl);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            X509TrustManager xtm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                    // TODO Auto-generated method stub

                }
            };

            TrustManager[] tm = {xtm};

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tm, null);

            con.setSSLSocketFactory(ctx.getSocketFactory());
            con.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });

            InputStream inStream = con.getInputStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] b = outStream.toByteArray();//网页的二进制数据
            outStream.close();
            inStream.close();
            String rtn = new String(b, "utf-8");
            return rtn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String postHttps(String url, String content, boolean isJSON) {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()},
                    new java.security.SecureRandom());

            URL console = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
            conn.setDoOutput(true);
            conn.addRequestProperty("connection", "Keep-Alive");
            if (isJSON)
                conn.addRequestProperty("content-type", "application/json;charset=UTF-8");
//        conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
            conn.connect();
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.write(content.getBytes("utf-8"));
            // 刷新、关闭
            out.flush();
            out.close();
            InputStream is = conn.getInputStream();
            if (is != null) {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                is.close();
                if (outStream != null) {
                    return new String(outStream.toByteArray(), "utf-8");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getHttp(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }


    public static String postHttp(String url, String param) {
//    public static String sendPost(String url, Map<String, String> map) {
//        String param = getKeyVAlueSting(map);    //如果是参数是String 就不需要再转换成name1=value1&name2=value2 的形式
        System.out.println(param);
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("content-type", "charset=UTF-8");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 1.获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 2.中文有乱码的需要将PrintWriter改为如下
            // out=new OutputStreamWriter(conn.getOutputStream(),"UTF-8")
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static Object jsonToObj(Object obj,String jsonStr) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return obj = mapper.readValue(jsonStr, obj.getClass());
    }

    public void deleteDir(File file){
        if(file.exists()){
            if(file.isFile()){
                file.delete();
            }else if(file.isDirectory()){
                File files[] = file.listFiles();
                for(int i=0;i<files.length;i++){
                    this.deleteDir(files[i]);
                }
            }
            file.delete();
        }else{
            System.out.println("所删除的文件不存在！"+'\n');
        }
    }

    // 递归方法
    public static void copyFile(File file, File file2) {
        // 当找到目录时，创建目录
        if (file.isDirectory()) {
            file2.mkdir();
            System.out.println("创建目录"+file2);
            File[] files = file.listFiles();
            for (File file3 : files) {
                // 递归
                copyFile(file3, new File(file2, file3.getName()));
            }
            //当找到文件时
        } else if (file.isFile()) {
            File file3 = new File(file2.getAbsolutePath());
            try {
                file3.createNewFile();
                System.out.println("创建文件"+file3);
                copyDatas(file.getAbsolutePath(), file3.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 复制文件数据的方法
    public static void copyDatas(String filePath, String filePath1) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            // 字节流
            fis = new FileInputStream(filePath);
            fos = new FileOutputStream(filePath1);
            byte[] buffer = new byte[1024];
            while (true) {
                int temp = fis.read(buffer, 0, buffer.length);
                if (temp == -1) {
                    break;
                } else {
                    fos.write(buffer, 0, temp);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            try {
                fis.close();
                fos.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public static void xcopy(String srcPath, String desPath){
        File srcFile = new File(srcPath);
        File desFile = new File(desPath);
        copyFile(srcFile, desFile);
    }

    public static boolean changeSize(int newWidth, int newHeight, String path) {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(path));

            //字节流转图片对象
            Image bi = ImageIO.read(in);
            //构建图片流
            BufferedImage tag = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            //绘制改变尺寸后的图
            tag.getGraphics().drawImage(bi, 0, 0, newWidth, newHeight, null);
            //输出流
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));
            //JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            //encoder.encode(tag);
            ImageIO.write(tag, "jpg", out);
            in.close();
            out.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public static void describe(String sort1, String sort2){

    }
    public static void cpFile(String src, String tg){
        // 输入流 --> 从一个目标读取数据
        // 输出流 --> 向一个目标写入数据
        File resource = new File(src);
        File target = new File(tg);
        long start = System.currentTimeMillis();

        // 文件输入流并进行缓冲
        FileInputStream inputStream = null;
        BufferedInputStream bufferedInputStream=null;
        BufferedOutputStream bufferedOutputStream= null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(resource);
            bufferedInputStream = new BufferedInputStream(inputStream);

            // 文件输出流并进行缓冲
            outputStream= new FileOutputStream(target);
            bufferedOutputStream = new BufferedOutputStream(outputStream);

            // 缓冲数组
            // 大文件 可将 1024 * 2 改大一些，但是 并不是越大就越快
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes)) != -1) {
                bufferedOutputStream.write(bytes, 0, len);
            }
            // 刷新输出缓冲流
            bufferedOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭流
            try {
                if(bufferedInputStream!=null)
                    bufferedInputStream.close();
                if(bufferedOutputStream!=null)
                    bufferedOutputStream.close();
                if(inputStream!=null)
                    inputStream.close();
                if(outputStream!=null)
                    outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        long end = System.currentTimeMillis();

        System.out.println("耗时：" + (end - start)  + " ms");

    }

    public static void writeFile(InputStreamReader inputStreamReader,OutputStreamWriter outputStreamWriter){
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {

            bufferedReader = new BufferedReader(inputStreamReader);
            // 文件输出流并进行缓冲
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            // 缓冲数组
            // 大文件 可将 1024 * 2 改大一些，但是 并不是越大就越快
           // char[] temp = new char[1024];
            //int len = 0;
            String temp = "";
            while ((temp = bufferedReader.readLine()) != null) {
                bufferedWriter.write(temp);
                bufferedWriter.write("\n");
            }
            // 刷新输出缓冲流
            bufferedWriter.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}

