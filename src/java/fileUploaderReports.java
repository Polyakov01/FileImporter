/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import DB.DBOperations;
import FileUploader.Uploader;
import classes.ReportParametrs;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import util.JDBCUtil;


/**
 *
 * @author Hrust
 */
@WebServlet(urlPatterns = {"/fileUploaderReports"})
public class fileUploaderReports extends HttpServlet 
{
    
    private static final long serialVersionUID = 1L;
     String fullPath =  "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\reports\\templates\\";   
     String backupPath =  "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\reports\\templates\\backup";   
   
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */  
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {            
        
        //request.setCharacterEncoding("UTF-8");	
       // response.setCharacterEncoding("UTF-8");       
       
            
        try 
        {
           boolean isMultipart = ServletFileUpload.isMultipartContent(request);
           int rep_code = Integer.parseInt(request.getParameter("code"));
           int use_code = Integer.parseInt(request.getParameter("use_code"));
           
           String oldComment = request.getParameter("comment");
           String comment = new String(oldComment.getBytes("iso-8859-1"), "UTF-8");         
           
           Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "{0}{1}{3}{4}", new Object[]{"REPORT_CODE: ",String.valueOf(rep_code),"; UseCode: ", String.valueOf(use_code)});          
           if (isMultipart) 
           {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List items = upload.parseRequest(request);
                Iterator iter = items.iterator();
                while (iter.hasNext()) 
                {
                    FileItem item = (FileItem) iter.next();                   
                    if (!item.isFormField()) 
                    {
                        response.setContentType("text/plain");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write("{\"code\":\""+request.getParameter("code")+"\",\"status\":\""+processUploadedFile(item,rep_code,use_code,comment) +"\" }");
                    }
                }
            }
        } 
        catch (Exception e) 
        {
           Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE,null , e);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException 
    {
        processRequest(req, resp);
    }         

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
   {       
       processRequest(request, response);
    }
    
    
            
    private String processUploadedFile(FileItem item, int rep_code,int use_code,String comment) throws Exception 
    {
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "REPORT_UPLOADER_BEGIN");
        ReportParametrs reportParametrs = getReportParametrs(rep_code);        
        Uploader upl = new Uploader();                    
        String sourceFileName = reportParametrs.getReportViewName().concat(".xlsm");
        String destinationFileName = reportParametrs.getReportViewName().concat("_").concat(String.valueOf(reportParametrs.getReportVersion())).concat(".xlsm");         
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "BACKUP_BEGIN");
        Boolean isBackup = upl.doBackup(fullPath,sourceFileName,backupPath, destinationFileName);                        
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, String.valueOf(isBackup));
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "UPLOAD_BEGIN");
        if (isBackup)
        {
            upl.doUpload(item, fullPath, sourceFileName);                               
            addReportsTemplate(rep_code, reportParametrs.getReportVersion(),use_code,comment,reportParametrs.getReportViewName());                   
            return "OK"; 
        }
        else
        {
            return "ERROR, BACKUP FAIL";
        }        
    }     
    
    public static ReportParametrs  getReportParametrs(int REP_CODE)
    {                       
        DBOperations dbo = new JDBCUtil().getDBOperations();                
        ReportParametrs result = new ReportParametrs();
        Connection conn = null;   
        try 
        {                   
            conn = dbo.getConnection();
            Statement stmt = conn.createStatement();           
            stmt.execute("select * from ST_REPORTS where REP_CODE = '".concat(String.valueOf(REP_CODE)).concat("'"));
            ResultSet rs = stmt.getResultSet();
            while (rs.next())
            {
               result.setReportViewName(rs.getString("REP_VIEW_NAME"));
               result.setReportVersion(rs.getInt("REP_VERSION"));
            }                               
        }    
        catch (SQLException ex) 
        {
           Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, null, ex);
        }
        finally
        {
            try {conn.close();} catch (SQLException ex) 
            {
               Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, null, ex);
            }             
        }
        return result;
    }
    
    public static String addReportsTemplate(int rep_code, int rep_version, int use_code, String comment, String view_name)
    {            
        int result = 0;
        DBOperations dbo = new JDBCUtil().getDBOperations();  
        result+= dbo.doUpdate("Update st_reports set REP_VERSION = ".concat(String.valueOf(rep_version+1)).concat(" where rep_code = ".concat(String.valueOf(rep_code))));
        result+= dbo.doInsert("WT_REPORT_HISTORY", 
                        new String [] {"REH_REP_CODE","REH_REPORT_VERSION", "REH_REPORT_UPLOAD_USER", "REH_REPORT_COMMENT","REH_REPORT_VIEW"},
                        new String [] {String.valueOf(rep_code),String.valueOf(rep_version),String.valueOf(use_code),comment,view_name});
        if (result>0){return "OK";}else{return "ERROR";}
    }
    
    
    
}
