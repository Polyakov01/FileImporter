package ExcelProcessing;


import util.FileStatus;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.JDBCOperations;
import util.FileStatus;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Hrust
 */
public class StatusLog 
{
    private String curStatus = "";
    private String globalStatus = "";
    // private String[] fileList;
    private String identifier = "";
    private String fileName = "";
    private final ArrayList<LogString> logList = new ArrayList<>();
    private int lastPos = 0;
    private boolean isFDateSet = false;
    private boolean isLDateSet = false;
    private FileStatus fileStatus;

    private class LogString
    {
        String logText;
        String logStatus;
        public LogString(String _logText,String _logStatus)
        {
            logText = _logText;
            logStatus = _logStatus;
        }
    }
    
    /**
     * 
     * @param _identifier описание параметра
     * @param _fileCount 
     */
    public StatusLog(String _identifier,int _fileCount)
    {             
        identifier = _identifier;
        curStatus = "begin"; 
        globalStatus = "begin";
        fileStatus = new FileStatus();        
        JDBCOperations.doClearLogData();
        JDBCOperations.doInsert("AT_IMPORT_STATUS",(new String[] {"IMS_ID","IMS_FILE_COUNT","IMS_STATUS"}), (new String[] {_identifier,String.valueOf(_fileCount),"START"}));
    }
    
    public void finishStep ()
    {        
        Connection conn = null;   
        try 
        {                   
            conn = JDBCOperations.DB().getConnection();
            Statement stmt = conn.createStatement();           
            stmt.executeUpdate("UPDATE AT_IMPORT_STATUS SET ILH_STATUS ='FINISH' where ILH_ID = '"+identifier+"'");            
        }    
        catch (SQLException ex) 
        {
           Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try {conn.close();} catch (SQLException ex) 
            {
                Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
            }             
        }   
    }
    
   public ArrayList<String> getOkFiles()
   {
       ArrayList<String> of = new ArrayList<>();
        of = fileStatus.getOkFiles();                   
       return of;
   }
    
    public void fileRegister(String _fileName)
    {
        fileName  = _fileName;        
        isFDateSet = false;
        isLDateSet = false;
        fileStatus.addFile(_fileName);
        JDBCOperations.doInsert("AT_IMPORT_LOG_HEADER", (new String[] {"ILH_ID","ILH_FILE_NAME"}), (new String[] {identifier,_fileName}));
    }
        
    
    public void setStatus (String _status)
    {        
        curStatus = _status;
        fileStatus.getFileParams(fileName).setFileLoadStatus(curStatus);
         addLog(_status,"STATUS");
        JDBCOperations.doInsert("AT_IMPORT_LOG", (new String[] {"IML_STATUS","IML_ID","IML_FILE_NAME"}), (new String[] {_status,identifier,fileName}));                 
        JDBCOperations.setDBStatus("ILH_STATUS", _status, identifier, fileName);                       
    }
    
    
    public void setGlobalStatus (String _status)
    {        
        globalStatus = _status;
        addLog(_status,"STATUS");
        //logList.add(_status);
    }
    
    public void setUserCount (int _userCount)
    {       
        addLog("Обнаружено пользователей:"+ String.valueOf(_userCount),"DEAULT"); 
        fileStatus.getFileParams(fileName).setFileUserCount(String.valueOf(_userCount));
        JDBCOperations.setDBStatus("ILH_USERS_COUNT", String.valueOf(_userCount), identifier, fileName);                                   
    }
    
    public void setFileSum (String _fileSum)
    {       
        fileStatus.getFileParams(fileName).setFileSum(_fileSum);
        JDBCOperations.setDBStatus("ILH_FILE_SUM", _fileSum, identifier, fileName);                
    }
    
    public void setFileProdCount (String _fileProdCount)
    {       
        fileStatus.getFileParams(fileName).setFileProdCount(_fileProdCount);
        JDBCOperations.setDBStatus("ILH_FILE_PROD_COUNT", _fileProdCount, identifier, fileName);        
    }
     
    public void setFileRegion (String _fileRegion)
    {       
        fileStatus.getFileParams(fileName).setFileRegion(_fileRegion);
        JDBCOperations.setDBStatus("ILH_REGION", _fileRegion, identifier, fileName);               
    }
    
    public void setFirstDate (String _firstDate)
    {       
       if(!isFDateSet)
       {
           fileStatus.getFileParams(fileName).setStartDate(_firstDate);
           JDBCOperations.setDBStatus("ILH_STARTDATE", _firstDate, identifier, fileName);              
           isFDateSet = true;
       }
    }
    
    public void setLastDate (String _lastDate)
    {       
       if(!isLDateSet)
       {
           fileStatus.getFileParams(fileName).setFinishDate(_lastDate);
           JDBCOperations.setDBStatus("ILH_FINISHDATE", _lastDate, identifier, fileName);                         
           isLDateSet = true;
       }
    }
    
    public void addLog(String _logText, String _logType)
    {
        //LogString logString = new LogString(_logText,_logText);
        logList.add(new LogString(_logText,_logType));
        JDBCOperations.doInsert("AT_IMPORT_LOG",(new String[] {"IML_DESC","IML_ID","IML_FILE_NAME"}), (new String[] {_logText,identifier,fileName}));        
    }

    public String getJSONStringWithCurPosition()
    {
        if (globalStatus.equals("DOCOMPLEXIMPORT"))
         {
                getDBFileStatus();
         }
        
        String logString = "";
        Iterator itr = logList.listIterator(lastPos);
        while (itr.hasNext())
        {
            LogString ls = (LogString)itr.next();
            logString = logString.concat(",{\"text\":\"").concat(ls.logText).concat("\",\"type\":\"").concat(ls.logStatus).concat("\"}");
            //logString = logString.concat(",\"").concat(itr.next().toString()).concat("\"");
        }
        lastPos = logList.size();
        logString = logString.replaceFirst(",", "");
        
        String strJSON = "{\"progress\" : {\""
                            .concat(identifier)
                            .concat("\":{\"")
                            .concat("status\" : \"")
                            .concat(globalStatus)
                            .concat("\",\"log\":[")
                            .concat(logString)
                            .concat("]}}, \"fileLog\":"+fileStatus.getJSONFileArray()+" }");   
        
         /*if (globalStatus.equals("DOCOMPLEXIMPORT"))
         {
            if(JDBCOperations.checkFinish())
            {
               globalStatus = "PROCESSEND";
               getDBFileStatus();
            }          
         }
         */        
        return strJSON;
    }         
    
    public void getDBFileStatus()
    {
        Connection conn = null;   
            try 
            {                   
                conn = JDBCOperations.DB().getConnection();
                Statement stmt = conn.createStatement();                      
                ResultSet rs = stmt.executeQuery("select * from VIEW_IMP_AT_IMPORT_LOG_HEADER");            
                while (rs.next())
                {
                    fileStatus.getFileParams(rs.getString("ILH_FILE_NAME")).setFileLoadStatus(rs.getString("ILH_LOAD_STATUS")); 
                    fileStatus.getFileParams(rs.getString("ILH_FILE_NAME")).setFileProgress(rs.getString("ILH_STATUS")); 
                    fileStatus.getFileParams(rs.getString("ILH_FILE_NAME")).setFileRegion(rs.getString("BDN_NAME")); 
                    fileStatus.getFileParams(rs.getString("ILH_FILE_NAME")).setFileSum(rs.getString("ILH_FILE_SUM")); 
                    fileStatus.getFileParams(rs.getString("ILH_FILE_NAME")).setFileProdCount(rs.getString("ILH_FILE_PROD_COUNT")); 
                    fileStatus.getFileParams(rs.getString("ILH_FILE_NAME")).setFileUserCount(rs.getString("ILH_USERS_COUNT")); 
                    fileStatus.getFileParams(rs.getString("ILH_FILE_NAME")).setStartDate(rs.getString("ILH_STARTDATE")); 
                    fileStatus.getFileParams(rs.getString("ILH_FILE_NAME")).setFinishDate(rs.getString("ILH_FINISHDATE")); 
                }
            }    
            catch (SQLException ex) 
            {
               Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally
            {
                try {conn.close();} catch (SQLException ex) 
                {
                    Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
                }             
            }
    }              
}
