/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ExcelProcessing.StatusLog;
import ExcelProcessing.Processing;
import FileUploader.Uploader;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



/**
 *
 * @author Hrust
 */
@WebServlet(urlPatterns = {"/fileUploaderShipment"})
public class fileUploaderShipment extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    //private StatusLog statusLog; 
    public boolean isStopped = false;
     final String FULL_PATH =  "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\upload\\fileUploadShipment";   
     final String BACKUP_PATH = "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\upload\\backupFileShipment";
   
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
   /* 
    private static DiskFileItemFactory newDiskFileItemFactory(ServletContext context,
			File repository) {
		FileCleaningTracker fileCleaningTracker
			= FileCleanerCleanup.getFileCleaningTracker(context);
		DiskFileItemFactory factory
			= new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD,
					repository);
		factory.setFileCleaningTracker(fileCleaningTracker);
		return factory;
	}
    */
    /*private static ProgressListener getProgressListener (final String id, final HttpSession sess) {
		ProgressListener progressListener = new ProgressListener(){
			public void update(long pBytesRead, long pContentLength, int pItems) {
				// put progress into session
				sess.setAttribute(id, ((double)pBytesRead / (double)pContentLength) * 100);
                           // sess.setAttribute(id, 50);
			}
		};
		return progressListener;
	}
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
        
    }
           
   /* private StringBuilder generateLogRespose()
    {
           StringBuilder sb = new StringBuilder("");
           sb.append(statusLog.getJSONStringWithCurPosition());     
        return sb;        
    }
    */
    /*
    private boolean doClearFiles()
    {      
            // boolean result = false;
            File f3 = new File(FULL_PATH);
            for(File currFile:f3.listFiles())
            {                
                currFile.delete();
            }                        
        return true;        
    }
    */
    /*
    private boolean doFileArchive()
    {
      
        boolean result = false;
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Calendar cal = Calendar.getInstance();
            String dt = dateFormat.format(cal.getTime());
            System.out.println(BACKUP_PATH+"\\"+dt+"\\OK");
            File f2 = null;
            f2 = new File(BACKUP_PATH+"\\"+dt+"\\OK");    
            f2.mkdirs();
            f2 = new File(BACKUP_PATH+"\\"+dt+"\\ERROR");    
            f2.mkdirs();
            ArrayList<String> fileList = new ArrayList<>();
            try
            {
                fileList =  statusLog.getOkFiles();
            }
            catch (Exception ex)
            {
                System.out.println(ex.toString());
            }
            Iterator files = fileList.iterator();
            while(files.hasNext())
            {
              String curfileName = files.next().toString();
              File f = new File(FULL_PATH.concat("\\").concat(curfileName));
              File f1 = new File(BACKUP_PATH+"\\"+dt+"\\OK"+"\\"+curfileName);
              result = f.renameTo(f1);
            }
            File f3 = new File(FULL_PATH);
            for(File currFile:f3.listFiles())
            {
                File f1 = new File(BACKUP_PATH+"\\"+dt+"\\ERROR"+"\\"+currFile.getName());
                currFile.renameTo(f1);
            }                        
        return result;        
    }
    */        

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
        catch (FileUploadException | IOException e) 
        {
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, null, e);
        }
    }
            

    private String processUploadedFile(FileItem item)  
    {
        Uploader upl = new Uploader();
        upl.doClearFiles(FULL_PATH);
        String status = upl.doUpload(item, FULL_PATH,item.getName());
        if (status.equals("OK"))
        {
            status = processImportFile(FULL_PATH.concat("//").concat(item.getName()));
        }
        return status.replaceAll("\"", "").replaceAll("'", ""); 
    }
                           
    private String processImportFile(String fileName)
    {  
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "SHIPMENT BEGIN" );                 
        try
        {
           XSSFWorkbook workbook = new XSSFWorkbook(fileName);
           return Processing.DoShipmentProcess(workbook).replaceAll("\"", "").replaceAll("'", "");
        }
        catch (Exception xlsException)
        {
           return "Ошибка открытия файла: ".concat(xlsException.getMessage()).replaceAll("\"", "").replaceAll("'", "");
        }
    }               
}
