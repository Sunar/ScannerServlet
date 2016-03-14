package ru.mi.chimcent;

import org.json.JSONObject;
import org.json.JSONException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: Avia-Kos
 * Date: 14.01.2016
 * Time: 12:02:31
 * To change this template use File | Settings | File Templates.
 */
public class ScannerServlet extends HttpServlet {
    final static String MSSQL_DB = "jdbc:jtds:sqlserver://10.10.10.112/trs";
    final static String MSSQL_LOGIN = "sa";
    final static String MSSQL_PASS = "128pxt8E";
        public void init() {
    }

    public void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {

        /*DataInputStream in = new DataInputStream(
                (InputStream)request.getInputStream());
        request.
        String db = in.readUTF();
        String user = in.readUTF();
        String pwd = in.readUTF();

        String message ="jdbc:mysql://localhost:3306/"+db+","+user+","+pwd;
        try {

            connect(db.toLowerCase().trim(),user.toLowerCase().trim(),
                    pwd.toLowerCase().trim());

            message += "100 ok";

        } catch (Throwable t) {
            message += "200 " + t.toString();
        }
        response.setContentType("text/plain");
        response.setContentLength(message.length());
        PrintWriter out = response.getWriter();
        out.println(message);
        in.close();
        out.close();
        out.flush();*/
    }

    public void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String barcode = request.getParameter("barcode");
        String addrBarcode = request.getParameter("address");
        JSONObject res = new JSONObject();
        if(barcode == null) {
            try {
                res.put("name", "unknown");
                res.put("cell", "-");
                res.put("code", "-");
            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            out.print(res);
            out.flush();
            return;
        }

        Connection p = null;
        try {
            p = connect();
            Statement st = p.createStatement();
            if(!addrBarcode.equals("")){

            }


            ResultSet rs = st.executeQuery("select b.DESCR as addr, a.DESCR as name, b.SP5227 as addr_code from SC30 a, SC1020 b where SP667 = '" + barcode + "' and a.SP1007 = b.ID");
            if(rs.next()){
                if(!addrBarcode.equals("")&&!addrBarcode.equals(rs.getString("addr_code"))){
                    Statement st2 = p.createStatement();
                    int i = st2.executeUpdate("update SC30 set SP1007 = (select ID from SC1020 where SP5227 = '" + addrBarcode + "') where SP667 = '" + barcode + "'");
                    if(i > 0){
                        st2 = p.createStatement();
                        try {
                            ResultSet rs2 = st2.executeQuery("select b.DESCR as addr from SC30 a, SC1020 b where SP667 = '" + barcode + "' and a.SP1007 = b.ID");
                            if(rs2.next())
                            res.put("changed", "yes: " + rs2.getString("addr"));
                            //BufferedWriter outWriter = null;
                            try
                                {
                                    //FileWriter fstream = new FileWriter("out.txt", true); //true tells to append data.
                                    //outWriter = new BufferedWriter(fstream);
                                    //out.write("" + rs.getString("name") + " " + barcode + " " + rs2.getString("addr") + "\n");
                                    File outputFile = new File(getServletContext().getRealPath("/") + "changes.txt");
                                    FileWriter fout = new FileWriter(outputFile, true);
                                    String date = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(Calendar.getInstance().getTime());
                                    fout.write(""+ date + " " + rs.getString("name").trim() + " " + rs.getString("addr").trim() + " " + rs2.getString("addr").trim() + System.lineSeparator());
                                    fout.close();
                                }
                                catch (IOException e)
                                {
                                    System.err.println("Error: " + e.getMessage());
                                }
                        }
                        catch (SQLException e){
                            res.put("changed", "no, error");
                        }

                    }
                }
                //address = rs.getString("addr");
                res.put("name", rs.getString("name"));
                res.put("cell", rs.getString("addr"));
                res.put("code", rs.getString("addr_code"));
                out.print(res);
                out.flush();
                p.close();
                return;
            }
            else{
                res.put("name", "not found");
                res.put("cell", "-");
                res.put("code", "-");
                out.print(res);
                out.flush();
                p.close();
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                p.close();
            } catch (SQLException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                res.put("name", "error");
                res.put("cell", "-");
                res.put("code", "-");
                out.print(res);
                out.flush();
                return;
            } catch (JSONException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
    /* This method connects to MSSQL database*/
    private Connection connect() throws Exception {
        Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
        return DriverManager.getConnection(MSSQL_DB, MSSQL_LOGIN, MSSQL_PASS);
    }
}
