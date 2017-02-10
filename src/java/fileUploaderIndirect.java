/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ExcelProcessing.ProcessingDistributor;
import ExcelProcessing.ProcessingIndirect;
import ExcelProcessing.StatusLog;
import FileUploader.Uploader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import util.JDBCOperations;
import static util.JDBCOperations.DoComplexImportIndirect;



/**
 *
 * @author Hrust
 */
@WebServlet(urlPatterns = {"/fileUploaderIndirect"})
public class fileUploaderIndirect extends HttpServlet {
    
   // private SvcMain_Service service;
   // @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/195.82.7.166_1080/svcMain.wsdl")
   
    //private services.SvcMain_Service service;

    private static final long serialVersionUID = 1L;    
    private StatusLog statusLog;
    private String time;
    //private int user_count = 0;
    //private int current_reg_code = -1;
    private ProcessingIndirect dp;
    public boolean isStopped = false;
    private final String FULL_PATH =  "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\upload\\Indirect\\fileUpload";   
    private final String BACKUP_PATH = "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\upload\\Indirect\\backupFile";
    private final String GET_REGISTER = "register";
    private final String GET_STOP = "stop";
    private final String GET_ARCHIVE = "doArchive";
    private final String GET_LOG_REQUEST = "log_request";            
    private final String GET_OPERATION_CLEAR = "clear";
    private final String GET_CHECK_FINISH = "checkFinish"; 
    private final String LOG_NAME  = "SAPSAN_UPLOAD";
   
    
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
            //out.println("<h1>Servlet fileUploader at " + request.getContextPath() + "</h1>");
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
	req.setCharacterEncoding("UTF-8");	
        resp.setCharacterEncoding("UTF-8");
        switch(req.getParameter("operation"))
        {
            case GET_REGISTER:
                registerNewImport(req.getParameter("time"));
            break;
            case GET_STOP:
                resp.getWriter().println("{\"operation\": \"".concat(req.getParameter("operation")).concat("\", \"status\": \"").concat(doStop()).concat("\"}"));
            break;
            case GET_CHECK_FINISH:
                resp.getWriter().println("{\"operation\": \"".concat(req.getParameter("operation")).concat("\", \"status\": \"").concat(doCheckFinish()).concat("\"}"));
            break;
            case GET_OPERATION_CLEAR:
                resp.getWriter().println("{\"operation\": \"".concat(req.getParameter("operation")).concat("\", \"status\": \"").concat(doClearFiles()).concat("\"}"));       
            break;
            case GET_ARCHIVE:
                resp.getWriter().println("{\"operation\": \"".concat(req.getParameter("operation")).concat("\", \"status\": \"").concat(doFileArchive()).concat("\"}"));                
            break;
            case GET_LOG_REQUEST:  
                resp.getWriter().println(generateLogRespose().toString());
            break;
        }     
    }
    
    private String doStop()
    {
        isStopped = true; 
        if (dp!=null){dp.doStopProcess();}
        JDBCOperations.doStopProcess();
        statusLog.addLog("Операция отменена","WARNING");  
        return "ok";
    }

    private String doCheckFinish()
    {
        if (JDBCOperations.checkFinish())   {return "ok";}               
        else                                {return "busy";}               
    }
    
     private String doClearFiles()
    {      
        String result = "";
        Uploader upl = new Uploader();
        if (upl.doClearFiles(FULL_PATH))
        {
            result = "ok";
        }
        else
        {
            result = "The files can not be deleted";
        }
        return result;        
    }
    
    private String doFileArchive()
    {      
        Uploader upl = new Uploader();
        boolean result = true;
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Calendar cal = Calendar.getInstance();
        String dt = dateFormat.format(cal.getTime());
        ArrayList<String> fileList =  statusLog.getOkFiles();

        Iterator files = fileList.iterator();
        while(files.hasNext())
        {
            String curfileName = files.next().toString();
            if(!upl.doBackup(FULL_PATH, curfileName, BACKUP_PATH.concat("\\").concat(dt).concat("\\OK").concat("\\"), curfileName))
            {
                result = false; 
                break;
            }
        }
        File f3 = new File(FULL_PATH);
        for(File currFile:f3.listFiles())
        {
            upl.doBackup(FULL_PATH, currFile.getName(), BACKUP_PATH.concat("\\").concat(dt).concat("\\ERROR").concat("\\"), currFile.getName()); 
        }      
        if (result)
        {
            return "archiveComplete";
        }
        else
        {
            return "archiveError";  
        }
    }
    
    private boolean registerNewImport(String _timeID)
    {
        if (JDBCOperations.checkFinish())
        {
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "PROCESS START"); 
            isStopped = false;
            time = _timeID;        
            File f = new File(FULL_PATH); 
            statusLog = new StatusLog(time,f.listFiles().length);  
            try
            {            
                    statusLog.addLog("Заущен процесс обработки","DEFAULT");
                    dp = new ProcessingIndirect();
                    processImportFile();               
            }
            catch(Exception ex)
            {                
                Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE,null, ex); 
                statusLog.addLog("GLOBAL ERROR: "+ex.getMessage(),"ERROR");
                JDBCOperations.doFinishProcess();
                statusLog.setGlobalStatus("PROCESSERROR");                
            }
         }
        return true;        
    }
    
    private StringBuilder generateLogRespose()
    {
           StringBuilder sb = new StringBuilder("");
           sb.append(statusLog.getJSONStringWithCurPosition());     
        return sb;        
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
       
               try {
            // Check that we have a file upload request
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);

           // fileId = request.getParameter("file_id");
           // fileName = request.getParameter("file_name");
            //System.out.println("fileId: " + fileId + "; fileName: " + fileName);
            
            if (isMultipart) {
                // Create a factory for disk-based file items
                FileItemFactory factory = new DiskFileItemFactory();

// Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);

// Parse the request
                List /* FileItem */ items = upload.parseRequest(request);

// Process the uploaded items
                Iterator iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();

                    if (!item.isFormField()) 
                    {
                        response.setContentType("text/plain");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write("{\"id\":\""+request.getParameter("file_id")+"\",\"status\":\""+processUploadedFile(item) +"\" }");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String processUploadedFile(FileItem item) throws Exception 
    {
        Uploader upl = new Uploader();
        if (upl.doUpload(item, FULL_PATH, item.getName()).equals("OK"))
        {
            return "true";
        }
        else
        {
            return "false";
        }
    }
                           
    private void processImportFile() throws Exception 
    {  
        Boolean isFail = false;
        String destination = "";
        File f = new File(FULL_PATH);
        statusLog.addLog("Поиск книги","DEFAULT");
        statusLog.addLog("Запущена обработка :".concat(String.valueOf(f.listFiles().length)).concat(" файлов"),"DEFAULT");
        for(File f1:f.listFiles())
        {  
            if (isStopped){break;}
            statusLog.fileRegister(f1.getName());
            destination = f1.getAbsolutePath();
            statusLog.addLog("NEW_FILE","DEFAULT");
            statusLog.addLog("Открыт файл:"+f1.getName(),"DEFAULT");
            HSSFWorkbook workbook = null;
            NPOIFSFileSystem fs = null;
            try
            {
               fs = new NPOIFSFileSystem(new File(destination));
               workbook = new HSSFWorkbook(fs.getRoot(), true);                          
               isFail = dp.doProcessIndirect(workbook, statusLog,f1.getName(),time);               
               workbook.close();
               fs.close();
               statusLog.setStatus("Обрабатывается");
            }
            catch (IOException xlsException)
            {
                statusLog.addLog("Ошибка открытия файла:"+xlsException.getMessage(),"ERROR");
                Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, xlsException);
            }
            finally
            {
                try
                {
                    workbook.close();
                    fs.close();
                }
                catch(Exception e)
                {
                  // statusLog.addLog("Ошибка открытия файла:"+e.getMessage(),"ERROR");
                  //  Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, e); 
                }
            }
        }
//zavershenie vstavki             
        if (!isFail && !isStopped)
        {   
            statusLog.setGlobalStatus("DOCOMPLEXIMPORT");
            String importResult = DoComplexImportIndirect();                                    
            statusLog.addLog(importResult,"DEFAULT"); 
            statusLog.getDBFileStatus();
            statusLog.addLog(doFileArchive(),"DEFAULT"); 
            if (importResult.equals("OK"))
            {
                statusLog.setGlobalStatus("PROCESSEND");
            }
            else
            {
                statusLog.setGlobalStatus("PROCESSERROR");
            }
        }
        if (isStopped)
        {            
             statusLog.setGlobalStatus("PROCESSSTOPED");
        }
        JDBCOperations.doFinishProcess(); 
    }
    
    
    
    
}
