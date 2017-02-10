/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExcelProcessing;

import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import DB.DBOperations;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import util.JDBCUtil;

/**
 *
 * @author PK
 */
public final class Processing 
{
    //private static final String  _JNDINAME = "JNDI_SAPSAN";
    public static String DoTargetProcess (XSSFWorkbook workbook) throws NamingException, IOException            
    {
        DBOperations dbo = new JDBCUtil().getDBOperations();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.'); 
        String importStatus = "OK";
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "GetSheet" );
        XSSFSheet sh = (XSSFSheet) workbook.getSheet("IMS");     
        int fRow = 2;
        int fCol = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("set dateformat dmy;");                    
        try
        {
            if  (sh != null)
            {
                Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "GetName");
                String sopName = cellValueToString(sh.getRow(0).getCell(fCol+1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                String monthNumber = cellValueToString(sh.getRow(0).getCell(fCol+3,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                String yearNumber = cellValueToString(sh.getRow(0).getCell(fCol+5,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));  
                Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, sopName);
                Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, monthNumber);
                Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, yearNumber);  
                if (
                    "SOP".equalsIgnoreCase(cellValueToString(sh.getRow(0).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))                    
                    && "MONTH".equalsIgnoreCase(cellValueToString(sh.getRow(0).getCell(fCol+2,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))                   
                    && "YEAR".equalsIgnoreCase(cellValueToString(sh.getRow(0).getCell(fCol+4,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))                                                                                      
                    && sopName.length()>0 
                    && monthNumber.length()>0 
                    && monthNumber.length()<3 
                    && yearNumber.length() == 4
                    )
                {
                    Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "DoTargetProcess BeginIterator");
                    for (int i = fRow;i<sh.getLastRowNum();i++)
                    {                            
                       String skuName = cellValueToString(sh.getRow(i).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                       int j = 1;
                       while (!cellValueToString(sh.getRow(1).getCell(fCol+j,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).contains("KZ SOP") && j < 100)
                       {
                           String cityName = cellValueToString(sh.getRow(1).getCell(fCol+j,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                           String value = cellValueToString(sh.getRow(i).getCell(fCol+j,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                           sb.append("insert into TMP_IMPORT_TARGET (IMT_CITY_NAME,IMT_SOP_NAME,IMT_MONTH_NUMBER,IMT_YEAR_NUMBER,IMT_SKU_NAME,IMT_VALUE) values ('").append(cityName).append("','").append(sopName).append("','").append(monthNumber).append("','").append(yearNumber).append("','").append(skuName).append("','").append(value).append("');");
                           j++;
                       }                      
                    }                    
                }
                else
                {
                    Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "File error incorret columns");
                    importStatus = "Ошибка обработки файла, неверные колонки";
                }
            }
            else
            {
                importStatus = "Лист с именем IMS не найден";
            }
        }
        catch(Exception fValException)
        {
            importStatus = "Ошибка обработки значений: "+ fValException.getMessage();
        }

        try
        {         
            dbo.doUpdate("truncate table TMP_IMPORT_TARGET;");
            dbo.doBatchUpdate(sb.toString());  
            dbo.doUpdate("exec IMP_TARGET_IMPORT");
            String difference = getImportDifference();
            if (difference.length() != 0)
            {   
                importStatus = difference;
            }
            
        }
        catch (Exception e)
        {
            importStatus = "Ошибка БД"+e.getMessage();
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE,null, e);
        }
        workbook.close();
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, importStatus);
        return importStatus;
    }
    
    public static String DoShipmentProcess(XSSFWorkbook workbook) throws IOException 
    {
        DBOperations dbo = new JDBCUtil().getDBOperations();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        String importStatus = "OK"; 
        XSSFSheet sh = (XSSFSheet) workbook.getSheet("Sheet1");                        
        int fRow = 0;
        int fCol = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("set dateformat dmy;");
        String materialDocument;
        String postingDate;
        String customer;
        String material;
        String materialDescription;
        String quantity;
        String amount;                 
        try
        {
                if ("Material Document".equalsIgnoreCase(cellValueToString(sh.getRow(0).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))
                    && "Posting Date".equalsIgnoreCase(cellValueToString(sh.getRow(0).getCell(fCol+1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))
                    && "Customer".equalsIgnoreCase(cellValueToString(sh.getRow(0).getCell(fCol+2,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))
                    && "Material".equalsIgnoreCase(cellValueToString(sh.getRow(0).getCell(fCol+3,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))
                    && "Material Description".equalsIgnoreCase(cellValueToString(sh.getRow(0).getCell(fCol+4,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))                                    
                    && "Quantity".equalsIgnoreCase(cellValueToString(sh.getRow(0).getCell(fCol+5,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))                                    
                    && "Amount in LC".equalsIgnoreCase(cellValueToString(sh.getRow(0).getCell(fCol+6,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))                                    
                   )
                {
                    Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "DoShipmentProcess FILEOK");
                    fRow = 1;
                    fCol = 0;
                    for (int i = fRow;i<sh.getLastRowNum();i++)
                    {
                        Row row =  sh.getRow(i);                                  
                        materialDocument = cellValueToString(row.getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                        postingDate = cellValueToString(row.getCell(fCol+1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                        customer = cellValueToString(row.getCell(fCol+2,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                        material = cellValueToString(row.getCell(fCol+3,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                        materialDescription = cellValueToString(row.getCell(fCol+4,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                        quantity = cellValueToString(row.getCell(fCol+5,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                        amount = cellValueToString(row.getCell(fCol+6,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                        sb.append("insert into tmp_import_shipment (IMS_MATERIAL_DOCUMENT,IMS_POSTING_DATE,IMS_CUSTOMER,IMS_MATERIAL,IMS_MATERIAL_DESCRIPTION,IMS_QUANTITY,IMS_AMOUNT_IN_LC) values ('").append(materialDocument).append("','").append(postingDate).append("','").append(customer).append("','").append(material).append("','").append(materialDescription).append("','").append(quantity).append("','").append(amount).append("');");                                                                                                         
                    }
                }
                else
                {
                    importStatus = "Ошибка обработки файла, неверные колонки";
                }
        }
        catch(Exception fValException)
        {  
             importStatus = "Ошибка обработки значений: "+ fValException.getMessage();
        }

        try
        {                    
            dbo.doUpdate("truncate table tmp_import_shipment;");
            dbo.doBatchUpdate(sb.toString());  
            dbo.doUpdate("exec IMP_SHIPMENT_IMPORT");            
        }
        catch (Exception e)
        {
            importStatus = "Ошибка БД"+e.getMessage();
        }
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO,importStatus);
        workbook.close();
        return importStatus;
}
    
    public static String getImportDifference()
    {                       
        DBOperations dbo = new JDBCUtil().getDBOperations();
        ArrayList<String> missingPositions = new ArrayList<>();
        dbo.doQueryString("select MISSING_POSITION from IMP_TARGET_SKU_CHECK ", "MISSING_POSITION");
        String result = "";
        int i = 0;
        for (String item:missingPositions)
        {
              result = result.concat("<br>").concat(item);
              result = result.concat("");
              i++;
        }
        if (i <3 )
        {
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO,"getImportDifference ALL OK");
            result = "";
        }          
        return result;

    }
    
    
    
    private static String cellValueToString(Cell _cell)
    {
        String result = "ERROR WHEN GET VALUE";
        switch(_cell.getCellType()) 
        {
            case Cell.CELL_TYPE_STRING:
                result = _cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(_cell)) 
                {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    result =  dateFormat.format(_cell.getDateCellValue());
                } else 
                {
                     _cell.setCellType(Cell.CELL_TYPE_STRING);
                    result = _cell.getStringCellValue();
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                result =  String.valueOf(_cell.getBooleanCellValue());
                break;
            default:
                result = "";
            break;
        }
        
        return result;
    }
}
