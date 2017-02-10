/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ExcelProcessing.StatusLog;
import ExcelProcessing.ProcessingDistributor;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import java.util.logging.Logger;
import java.util.logging.Level;
import util.JDBCOperations;
//import static util.JDBCOperations.DoComplexImport;


/**
 *
 * @author Hrust
 */
@WebServlet(urlPatterns = {"/fileUploader"})
public class fileUploader extends HttpServlet 
{    
    private static final long serialVersionUID = 1L;
    private static final String LOG_NAME  = "SAPSAN_UPLOAD";
    private StatusLog statusLog;
    private String time;
    String  testString = "";   
    public boolean isStopped = false;
    private ProcessingDistributor dp;
    private final String FULL_PATH =  "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\upload\\fileUpload";   
    private final String BACKUP_PATH = "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\upload\\backupFile";
    private final String GET_REGISTER = "register";
    private final String GET_STOP = "stop";
    private final String GET_ARCHIVE = "doArchive";
    private final String GET_LOG_REQUEST = "log_request";            
    private final String GET_OPERATION_CLEAR = "clear";
    private final String GET_CHECK_FINISH = "checkFinish";   
   
    
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
    
    private boolean registerNewImport(String _timeID)
    {
        if (JDBCOperations.checkFinish())
        {
            System.out.println("PROCESS START");
            isStopped = false;
            time = _timeID;        
            File f = new File(FULL_PATH); 
            statusLog = new StatusLog(time,f.listFiles().length);  
            try
            {            
                    statusLog.addLog("Запущен процесс обработки ID: ".concat(_timeID) ,"DEFAULT");
                    dp = new ProcessingDistributor();
                    processImportFile();               
            }
            catch(Exception ex)
            {
                System.out.println("GLOBAL ERROR: "+ex.getMessage()+" \n TRACE: "+ ex.getStackTrace().toString());
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
       if (statusLog != null)
       {
            sb.append(statusLog.getJSONStringWithCurPosition());     
       }
       else
       {
           sb.append( "{\"progress\" : {\""
                        .concat(time)
                        .concat("\":{\"")
                        .concat("status\" : \"")
                        .concat("")
                        .concat("\",\"log\":[")
                        .concat("")
                        .concat("]}}}")); 
       }
        return sb;        
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
        boolean result = false;
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
                        String fileProcessResult = processUploadedFile(item);
                        response.setContentType("text/plain");
                        response.setCharacterEncoding("UTF-8"); 
                        response.getWriter().write(getResponseBody(request.getParameter("file_id"),fileProcessResult));                         
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String getResponseBody (String _file_id, String _status )
    {        
        return "{\"id\":\""+_file_id+"\",\"status\":\""+_status +"\" }";
    }
    
    private boolean checkFolderPathForExists (String _folderPath)
    {
        Boolean success = (new File(_folderPath)).mkdirs();
        if (success) 
        {
            System.out.println("Created Folder name: " + _folderPath + ";");
        } 
        else 
        {
            System.out.println("Folder name: " + _folderPath + ";");
        }
        return success;
    }
            

    private String processUploadedFile(FileItem item) throws Exception 
    {
        String name = item.getName();
        long sizeInBytes = item.getSize();       
        String result;
        boolean writeToFile = true;
        if (sizeInBytes > (500 * 1024 * 1024)){ writeToFile = false;}
        if (writeToFile) 
        {
            checkFolderPathForExists(FULL_PATH);            
            File uploadedFile = new File(FULL_PATH + "/" + name);
            if (!uploadedFile.exists()) 
            {
                uploadedFile.createNewFile();
            }            
            item.write(uploadedFile);
            result =  "true";    
        } 
        else 
        {
            result = "Trying to write a large file.";
            Logger.getLogger("SAPSAN: FileUpload").log(Level.WARNING, "Trying to write a large file.");
        } 
        
        return result;
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
            HSSFWorkbook workbook;
            try
            {
               NPOIFSFileSystem fs = new NPOIFSFileSystem(new File(destination));
               workbook = new HSSFWorkbook(fs.getRoot(), true);           
               Logger.getLogger(LOG_NAME).log(Level.SEVERE, "TEST");
               isFail = dp.doProcessDistributor(workbook, statusLog,f1.getName(),time);               
               workbook.close();
               fs.close();
               statusLog.setStatus("Обрабатывается");
            }
            catch (IOException xlsException)
            {
                statusLog.addLog("Ошибка открытия файла:"+xlsException.getMessage(),"ERROR");
                Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, xlsException);
            }
        }
//zavershenie vstavki             
         if (!isFail && !isStopped)
         {                
            statusLog.setGlobalStatus("DOCOMPLEXIMPORT");            
            String importResult = JDBCOperations.DoComplexImport();                                              
            System.out.println("ZAKONCHILI");
            statusLog.addLog(importResult,"DEFAULT"); 
            statusLog.getDBFileStatus();
            doFileArchive(); 
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
