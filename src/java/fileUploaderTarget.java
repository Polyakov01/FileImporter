/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ExcelProcessing.Processing;
import FileUploader.Uploader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



/**
 *
 * @author Hrust
 */
@WebServlet(urlPatterns = {"/fileUploaderTarget"})
public class fileUploaderTarget extends HttpServlet {
    
   // private SvcMain_Service service;
   // @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/195.82.7.166_1080/svcMain.wsdl")
    private static final long serialVersionUID = 1L;     
    String  testString = "";   
    public boolean isStopped = false;   
    String fullPath =  "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\upload\\fileUploadTarget";   
    String backupPath = "D:\\jboss\\standalone\\deployments\\ROOT.war\\sapsan\\upload\\backupFileTarget";
   
    
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
        
    }
              
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws java.io.UnsupportedEncodingException
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, UnsupportedEncodingException 
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
                        String status = processUploadedFile(item);
                        System.out.println(status);
                        response.getWriter().write("{\"id\":\""+request.getParameter("file_id")+"\",\"status\":\""+ status+"\" }");
                    }
                }
            }
        } 
        catch (Exception e) 
        {
             Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, null, e);
        }
    }
            

    private String processUploadedFile(FileItem item) throws Exception 
    {  
        Uploader upl = new Uploader();
        String status = upl.doUpload(item, fullPath, item.getName());
        if (status.equals("OK"))
        {
            status = processImportFile(fullPath.concat("//").concat(item.getName()));
        }
        return status.replaceAll("\"", "").replaceAll("'", ""); 
    }
                           
    private String processImportFile(String fileName)
    {  
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "TARGET BEGIN" );
        try
        {
           XSSFWorkbook workbook = new XSSFWorkbook(fileName);    
           return Processing.DoTargetProcess(workbook).replaceAll("\"", "").replaceAll("'", "");
        }
        catch (IOException | NamingException xlsException)
        {
           return "Ошибка открытия файла: ".concat(xlsException.getMessage()).replaceAll("\"", "").replaceAll("'", "");
        }                   
    }
}
