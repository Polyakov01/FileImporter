package util;


import classes.ReportParametrs;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebParam;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;


public class JDBCOperations
{
    /*
     * Create xml string - fastest, but may have encoding issues
     */                 
   public static DataSource DB() 
     {
        DataSource ds = null;
        try 
        {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:");
            ds = (DataSource) envContext.lookup("datasource/JNDI_SAPSAN");
        } 
        catch (NamingException ex) 
        {
            Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ds;
    }
          
  
   public static String adjrec_LargeTable(
                                           @WebParam(name = "QUERY") String QUERY,
                                           @WebParam(name = "use_code") int use_code
                                          )
    {                
       
       // System.out.println("qryrec_table | BASIC QUERY: " + QUERY);
        long timeStart = new java.util.Date().getTime();
        String result = "";                
        String [] queryes = QUERY.split(";");
        Connection conn = null;   
        String currQuery = "";
        try 
        {                   
            conn = JDBCOperations.DB().getConnection();
            Statement st = conn.createStatement();
            Statement stmt = conn.createStatement(); 
            conn.setAutoCommit(false);
            int i = 0;                                         
            for (String query : queryes) 
            { 
                currQuery = query;
                stmt.addBatch(query);
                //stmt.execute(query);
                //System.out.println("QUERY:"+query);
                 //stmt.addBatch(query.concat(";"));                          
                 //if  (i%1000 == 0)
                // {
                  //  System.out.println(query.concat(";"));
                   // stmt.executeBatch();
                   // conn.commit();
                   // stmt.clearBatch();
                 //}
                 //i++;
             }   
            //stmt.executeQuery(result)
            stmt.executeBatch();            
            conn.commit();            
            try
            {
               // result = JDBCOperations.toJSON(stmt.getResultSet());
            }
            catch (Exception resException)
            {
                result = resException.getMessage();
            }
        }    
        catch (SQLException ex) 
        {
            System.out.println("PASTE_ERROR"+ex.getMessage());
            System.out.println("ERROR_STRING:"+currQuery);
            result = ex.getMessage();
            Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try {conn.close();} catch (SQLException ex) 
            {
                Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
            }             
        }
        return result;
    }
     
   public  static void doInsert(String _tableName,String[] _fields, String[] _values)
   {
       String sqlExpression = "insert into ";
       sqlExpression = sqlExpression.concat(_tableName).concat(" (");
       
       for(String field:_fields)
       {
           sqlExpression = sqlExpression.concat(field).concat(",");
       }
       
       sqlExpression = sqlExpression.substring(0, sqlExpression.length()-1).concat(") values ( ");
       
       for(String value:_values)
       {
           sqlExpression = sqlExpression.concat("'").concat(value).concat("',");
       }
       
        sqlExpression = sqlExpression.substring(0, sqlExpression.length()-1).concat(")");
        
        //System.out.println(sqlExpression);
       
       Connection conn = null;   
        try 
        {                   
            conn = JDBCOperations.DB().getConnection();
            Statement stmt = conn.createStatement();           
            stmt.executeUpdate(sqlExpression);            
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
   
   public static boolean setDBStatus(String _fieldName, String _value, String _identifier, String _fileName)
    {
        boolean result = false;
        Connection conn = null;   
        try 
        {                   
            conn = JDBCOperations.DB().getConnection();
            Statement stmt = conn.createStatement();                      
            stmt.executeUpdate("UPDATE AT_IMPORT_LOG_HEADER SET "+_fieldName+" ='"+_value+"' where ILH_ID = '"+_identifier+"' and ILH_FILE_NAME = '"+_fileName+"'");            
            result = true;
        }    
        catch (SQLException ex) 
        {
            result = false;
           Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try {conn.close();} catch (SQLException ex) 
            {
                Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
            }             
        }   
            return result;
    }   
   
    public static boolean doStopProcess()
    {
        Connection conn = null;   
        try 
        {                   
            conn = DB().getConnection();
            Statement stmt = conn.createStatement();           
            stmt.executeUpdate("UPDATE AT_IMPORT_STATUS set IMS_STATUS = 'STOP'");                        
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
        return true;
    }
    
    public static boolean doFinishProcess()
    {
        Connection conn = null;   
        try 
        {                   
            conn = DB().getConnection();
            Statement stmt = conn.createStatement();           
            stmt.execute("UPDATE AT_IMPORT_STATUS set IMS_STATUS = 'FINISH'");                        
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
        return true;
    }
   
   public static boolean doClearLogData()
    {
        Connection conn = null;   
        try 
        {                   
            conn = DB().getConnection();
            Statement stmt = conn.createStatement();  
            stmt.executeUpdate("exec IMP_DATA_CLEAR;");                        
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
        return true;
    } 
   
   
   
   public static boolean checkFinish()
    {
        boolean result = false;
        Connection conn = null;   
            try 
            {                   
                conn = JDBCOperations.DB().getConnection();
                Statement stmt = conn.createStatement();                      
                ResultSet rs = stmt.executeQuery("select count(*) as RESULT from AT_IMPORT_STATUS WHERE IMS_STATUS = 'START'");            
                while (rs.next())
                {
                    if (rs.getInt("RESULT")>0)
                    {
                        result = false;
                    }
                    else
                    {
                        result = true;
                    }
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
            return result;
    }              
     
   public static String DoComplexImport()
    {            
        String result = "";                
        Connection conn = null;   
        String currQuery = "";
        try 
        {                   
            conn = JDBCOperations.DB().getConnection();                       
            CallableStatement storedProc = conn.prepareCall("Exec IMP_COMPLEX_IMPORT");
            storedProc.execute();
            result = "OK";
        }    
        catch (SQLException ex) 
        {
            System.out.println("PASTE_ERROR: "+ex.getMessage()+" ON QUERY: "+currQuery);
            result = "ERROR: "+ ex.getMessage()+" ON QUERY: "+currQuery ;
        }
        finally
        {
            try {conn.close();} catch (SQLException ex) 
            {
                Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
            }             
        }
        return result;
    }
   
   public static String DoComplexImportIndirect()
    {            
        String result = "";                
        Connection conn = null;   
        String currQuery = "";
        try 
        {                   
            conn = JDBCOperations.DB().getConnection();                       
            CallableStatement storedProc = conn.prepareCall("Exec IMP_COMPLEX_IMPORT_INDIRECT");
            storedProc.execute();
            result = "OK";
        }    
        catch (SQLException ex) 
        {
            System.out.println("PASTE_ERROR: "+ex.getMessage()+" ON QUERY: "+currQuery);
            result = "ERROR: "+ ex.getMessage()+" ON QUERY: "+currQuery ;
        }
        finally
        {
            try {conn.close();} catch (SQLException ex) 
            {
                Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
            }             
        }
        return result;
    }
      

    public static int getRegionCode( String sal_id_pref)
    {                       
        int result = -1;                
        Connection conn = null;   
        try 
        {                   
            conn = JDBCOperations.DB().getConnection();
            Statement stmt = conn.createStatement();           
            stmt.execute("select BDN_CODE from VIEW_ST_BDD_NAME where BDN_ENGNAME = '".concat(sal_id_pref).concat("'"));
            ResultSet rs = stmt.getResultSet();
            while (rs.next())
            {
               result = rs.getInt("BDN_CODE");
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
        return result;
    }
    
    public static ReportParametrs  getReportParametrs(int REP_CODE)
    {                       
        ReportParametrs result = new ReportParametrs();
        Connection conn = null;   

        try 
        {                   
            conn = JDBCOperations.DB().getConnection();
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
        String result = "";                
        Connection conn = null;   
        String currQuery = "";
        try 
        {                   
            conn = JDBCOperations.DB().getConnection();                       
            CallableStatement query = conn.prepareCall("Update st_reports set REP_VERSION = ".concat(String.valueOf(rep_version+1)).concat(" where rep_code = ".concat(String.valueOf(rep_code))));
            query.execute();
            PreparedStatement stm = conn.prepareStatement("INSERT INTO WT_REPORT_HISTORY (REH_REP_CODE,REH_REPORT_VERSION, REH_REPORT_UPLOAD_USER, REH_REPORT_COMMENT,REH_REPORT_VIEW)VALUES ( ?,?,?,?,?)");
            stm.setInt(1, rep_code);
            stm.setInt(2, rep_version);
            stm.setInt(3, use_code);
            stm.setString(4, comment);
            stm.setString(5, view_name);
            stm.executeQuery();                                        
            result = "OK";
        }    
        catch (SQLException ex) 
        {
            System.out.println("PASTE_ERROR: "+ex.getMessage()+" ON QUERY: "+currQuery);
            result = "ERROR: "+ ex.getMessage()+" ON QUERY: "+currQuery ;
        }
        finally
        {
            try {conn.close();} catch (SQLException ex) 
            {
                Logger.getLogger(JDBCOperations.class.getName()).log(Level.SEVERE, null, ex);
            }             
        }
        return result;
    }
    
    public static String getImportDifference(String _fileName)
    {                       
        String result = "";                
        Connection conn = null;   
        try 
        {                   
            conn = JDBCOperations.DB().getConnection();
            Statement stmt = conn.createStatement();           
            stmt.execute("EXEC IMP_BDN_REPAIR;");
            stmt.execute("select *  from IMP_CHECK_DIFFERENCE WHERE IMD_FILE_NAME = '"+_fileName+"'" );
            ResultSet rs = stmt.getResultSet();
            while (rs.next())
            {
               result = result.concat(rs.getString("IMD_EXPEDITOR").concat(" с разницей ").concat(rs.getString("IMD_DIFF")));
               result = result.concat(",");
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
        return result;
    }
    
    public static String getImportDifferenceIndirect(String _fileName)
    {                       
        String result = "";                
        Connection conn = null;   
        try 
        {                   
            conn = JDBCOperations.DB().getConnection();
            Statement stmt = conn.createStatement();           
            stmt.execute("EXEC IMP_BDN_REPAIR_INDIRECT;");
            stmt.execute("select *  from IMP_CHECK_DIFFERENCE_INDIRECT WHERE IMD_FILE_NAME = '"+_fileName+"'" );
            ResultSet rs = stmt.getResultSet();
            while (rs.next())
            {
               result = result.concat(rs.getString("IMD_EXPEDITOR").concat(" с разницей ").concat(rs.getString("IMD_DIFF")));
               result = result.concat(",");
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
        return result;
    }
}
