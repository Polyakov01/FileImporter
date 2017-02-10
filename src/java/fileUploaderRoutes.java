/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import DB.DBOperations;
import ExcelProcessing.ProcessingRoutes;
import FileUploader.Uploader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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
@WebServlet(urlPatterns = {"/fileUploaderRoutes"})
public class fileUploaderRoutes extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private String time = "";    
    private String uploadStatus;
    private String errorMessage;
    String  testString = "";   
    public boolean isStopped = false;
    private final String FULL_PATH =  "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\upload\\fileUploadRoutes";   
    private final String GET_OPERATION_CLEAR = "clear";
    private final String GET_CHECK_FINISH = "checkFinish";
    private final String GET_REGISTER = "register";
    
    
    // String backupPath = "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\upload\\backupFileShipment";
   
    
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
            throws ServletException, IOException {
            
        System.out.println("Async Servlet with thread: " + Thread.currentThread().toString());        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {            
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet fileUploader</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("Файл обработан");
            out.println("</body>");
            out.println("</html>");
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
        switch(req.getParameter("operation"))
        {
            case GET_OPERATION_CLEAR:
                doClearFiles();
                resp.getWriter().println("{\"operation\": \"clear\", \"status\": \"ok\"}");
            break;
            case GET_CHECK_FINISH:
                Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "UPLOAD STATUS:".concat(uploadStatus));
                resp.getWriter().println("{\"operation\": \"checkFinish\", \"status\": \""+uploadStatus+"\",\"message\":\"".concat(errorMessage).concat("\", \"time\": \""+new Date().getTime()+"\"}"));
            break;
            case GET_REGISTER:
                doRegister(req.getParameter("time"));
                resp.getWriter().println("{\"operation\": \"register\", \"status\": \"ok\"}");
                uploadStatus = processImportFile();
            break;
        }      
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
       request.setCharacterEncoding("UTF-8");	
       response.setCharacterEncoding("UTF-8");
       
       try 
       {
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
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
                        response.getWriter().write("{\"id\":\""+request.getParameter("file_id")+"\",\"status\":\""+processUploadedFile(item) +"\" }");
                    }
                }
            }
        } 
        catch (Exception e) 
        {
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE,null,e);
        }
    }
            
    private String processUploadedFile(FileItem item) throws Exception 
    {
        Uploader upl = new Uploader();        
        String status = upl.doUpload(item, FULL_PATH,item.getName());
        if (!status.equals("OK"))
        {
            errorMessage = status;
        }
        return status.replaceAll("\"", "").replaceAll("'", "");              
    }
                           
    
    
    private String processImportFile() 
    {  
        String importStatus = "OK";
        File fileList = new File(FULL_PATH);
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "ROUTE_IMPORT");
        for(File f1:fileList.listFiles())
        {
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, f1.getAbsolutePath());
            importStatus = ProcessingRoutes.doProcessRoutes(f1);
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "PROCESSING OK");
        }        
        if (importStatus.equals("OK"))
        {
            try
            {
                Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "EXEC IMP_ROUTES_IMPORT");
                DBOperations dbo = new JDBCUtil().getDBOperations();         
                dbo.doUpdate("exec IMP_ROUTES_IMPORT;");
                Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "IMPORT ROUTE COMPLETE");
            }
            catch(Exception ex)
            {
                Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, null, ex);
            }
        }
        return importStatus.replaceAll("\"", "").replaceAll("'", "");
    }
    
    private boolean doClearFiles()
    {      
        uploadStatus = "clearFiles";
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "CURRENT_LOAD");
        Uploader upl = new Uploader();
        upl.doClearFiles(FULL_PATH);
        DBOperations dbo = new JDBCUtil().getDBOperations();
        dbo.doUpdate("truncate table TMP_IMPORT_ROUTES;");
        return true;        
    } 
    
    private boolean doRegister(String _time)
    {                      
        if (time==null){time = "";}
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "NEW_LOAD:".concat(_time));
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "CURRENT_LOAD".concat(time));
        if (!_time.equalsIgnoreCase(time))
        {
            errorMessage = "";
            time = _time;
            uploadStatus = "inProgress";                                      
        }                   
        return true;        
    }        
}
