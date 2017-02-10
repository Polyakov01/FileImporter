/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExcelProcessing;

import DB.DBOperations;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import static util.JDBCOperations.getImportDifference;
import static util.JDBCOperations.getRegionCode;
import util.JDBCUtil;

/**
 *
 * @author PK
 */
public class ProcessingDistributor 
{
    private boolean isStopped = false;    
    public void doStopProcess()
    {
       isStopped = true; 
    }
    public Boolean doProcessDistributor(HSSFWorkbook workbook,StatusLog statusLog, String fileName, String time) throws IOException
    {
        DBOperations dbo = new JDBCUtil().getDBOperations();
        int user_count = 0;
        Boolean isFail = false;
        int current_reg_code = -1;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        NumberFormat formatter = new DecimalFormat("#0.00",symbols); 
        HSSFSheet sh = (HSSFSheet) workbook.getSheet("TDSheet");                        
         int appendRowCount = 0;
         int fRow = 0;
         int fCol = 0;
         int dateRow = 0;
         int dateCol = 0;
         StringBuilder sb = new StringBuilder();
         sb.append("");
         StringBuilder sbHierarchy = new StringBuilder();        
         statusLog.addLog("Обработка началась","SUCCESS");
         Iterator itr = sh.rowIterator();                          
////////////////////////opredelenie nachalnyh ZNACHENII
         try
         {
                while (itr.hasNext())
                {
                   Row row =  (Row) itr.next();
                   sh.setRowGroupCollapsed(row.getRowNum(), false);
                   Cell cell = row.getCell(1,Row.CREATE_NULL_AS_BLANK);
                   if ("Номенклатура".equals(cell.getStringCellValue()))
                   {
                      // statusLog.addLog("Найдена первая ячейка R" + String.valueOf(cell.getRowIndex())+"C" +String.valueOf(cell.getColumnIndex()));
                       fRow = cell.getRowIndex();
                       fCol = cell.getColumnIndex();
                       dateRow = fRow-2;
                       dateCol = fCol+3;
                       break;
                   }
                }
                //opredelenie maksimalnogo vlojeniya
                int maxIndent = 0;                         
                for (int i = fRow;i<sh.getLastRowNum();i++)
                {
                   Row row =  sh.getRow(i);            
                   Cell cell = row.getCell(fCol+1,Row.CREATE_NULL_AS_BLANK);
                   if (Cell.CELL_TYPE_STRING == cell.getCellType())
                   {
                       if (cell.getStringCellValue().startsWith("RB") || cell.getStringCellValue().startsWith("BP"))
                       {
                           if (row.getCell(fCol,Row.CREATE_NULL_AS_BLANK).getCellStyle().getIndention()> maxIndent)
                           {                   
                               maxIndent = row.getCell(fCol,Row.CREATE_NULL_AS_BLANK).getCellStyle().getIndention();           
                           }             
                       }
                   }
                }
         }
         catch(Exception fValException)
         {
             statusLog.addLog("Ошибка поиска начальных значений: '"+fValException.getMessage()+"'","ERROR");
             statusLog.setGlobalStatus("PROCESSERROR");
             isFail = true;
             return isFail;
         }
///////////////////////////////////////////////////////////                
         String[] values = new String[15];
         for (int i=0;i<15;i++)
         {
             values[i] = "";
         }
         int k=0;
         String isNode = "0";
         int lastSalPos = 0;
         int lastSalIndent = 1;
///////////////////////////////////osnovnoi perebor
         for (int i = 1;i<sh.getLastRowNum();i++)
         {  
             //System.out.println(isStopped);
             if (isStopped){ System.out.println("DO BREAK"); break;}
             try
             {
                Row basicRow = sh.getRow(fRow+i);                              
                //
                if (basicRow.getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellStyle().getIndention() == 0)
                {
                   // opredelenie itoga i okonchanie cikla
                   if (cellValueToString(basicRow.getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).equalsIgnoreCase("Итог"))
                   {
                       basicRow.getCell(sh.getRow(dateRow).getLastCellNum()-1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellType(1);
                       basicRow.getCell(sh.getRow(dateRow).getLastCellNum()-2,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellType(1);
                       statusLog.setFileSum(basicRow.getCell(sh.getRow(dateRow).getLastCellNum()-1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());                               
                       statusLog.setFileProdCount(cellValueToString(basicRow.getCell(sh.getRow(dateRow).getLastCellNum()-2,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                        //statusLog.addLog("Итог по файлу KZT: " + basicRow.getCell(sh.getRow(dateRow).getLastCellNum()-1,Row.CREATE_NULL_AS_BLANK).getStringCellValue());                                                              
                       statusLog.addLog("Итог по файлу базовая единица: " + cellValueToString(basicRow.getCell(sh.getRow(dateRow).getLastCellNum()-2,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)),"DEFAULT");
                       statusLog.addLog("Анализ файла "+fileName+" завершен","SUCCESS");
                       break;
                   }
                   else
                   {
                       String cur_use_name = cellValueToString(basicRow.getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                       if (cur_use_name.isEmpty()){cur_use_name = "Офис";}
                       statusLog.addLog(cur_use_name+ "     Итого "+ String.valueOf(basicRow.getCell(sh.getRow(dateRow).getLastCellNum()-1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue()),"DEFAULT");
                       values[0] = cur_use_name;                                            
                       values[9] = formatter.format(basicRow.getCell(sh.getRow(dateRow).getLastCellNum()-1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue()); //SUM_BY_USER                       
                       values[10] = formatter.format(basicRow.getCell(sh.getRow(dateRow).getLastCellNum()-2,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue()); //COUNT_BY_USER
                       user_count++;
                   }
                }

                   try
                   {                            
                                String SAL_ID = "";
                              //  try
                              //  {
                                    SAL_ID = cellValueToString(sh.getRow(fRow+i).getCell(fCol+2,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                              //  }
                              //  catch (Exception salExcept)
                              //  {
                                 //   SAL_ID = String.valueOf(sh.getRow(fRow+i).getCell(fCol+2,Row.CREATE_NULL_AS_BLANK).getNumericCellValue());
                              //  }

                            if (SAL_ID.length()>0)
                            {                                                                                                                                                                    
                                if (isNode.equals("0") && !(cellValueToString(sh.getRow(fRow+i+1).getCell(fCol+1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).startsWith("RB") || cellValueToString(sh.getRow(fRow+i+1).getCell(fCol+1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).startsWith("BP")))
                                {
                                    k++;
                                }

                                if (cellValueToString(sh.getRow(fRow+i+1).getCell(fCol+1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).startsWith("RB") || cellValueToString(sh.getRow(fRow+i+1).getCell(fCol+1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).startsWith("BP"))
                                {
                                    isNode = "0";                                            
                                }
                                else
                                {
                                    isNode = "1";
                                }

                                String ish_name = cellValueToString(sh.getRow(fRow+i).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));                                        
                                ish_name = ish_name.replaceAll("'", "");
                                ish_name = ish_name.replaceAll(";", "");
                                ish_name = ish_name.replaceAll(String.valueOf('"'), "");

                                sbHierarchy.append("insert into tmp_import_salepoint_hierarchy (ISH_SAL_ID,ISH_NAME,ISH_POSITION,ISH_IS_NODE,ISH_FILENAME, ISH_PARENT_CODE,ISH_INDENT) values (");
                                sbHierarchy.append("'").append(SAL_ID).append("',");
                                sbHierarchy.append("'").append(ish_name).append("',");
                                sbHierarchy.append("'").append(String.valueOf(i)).append("',");
                                sbHierarchy.append("'").append(isNode).append("',");
                                sbHierarchy.append("'").append(fileName).append("',");
                                sbHierarchy.append("'").append(String.valueOf(lastSalPos)).append("',");
                                sbHierarchy.append("'").append(String.valueOf(sh.getRow(fRow+i).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellStyle().getIndention())).append("');");                            

                                if (sh.getRow(fRow+i).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellStyle().getIndention() < lastSalIndent)
                                {
                                    lastSalIndent =  sh.getRow(fRow+i).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellStyle().getIndention();
                                    lastSalPos = i;
                                }
                            }
                    }
                    catch (Exception inEx)
                    {
                        statusLog.addLog(inEx.getMessage()+"___"+String.valueOf(fRow+i),"ERROR");
                    }


                if (cellValueToString(basicRow.getCell(fCol+1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).startsWith("RB") || cellValueToString(basicRow.getCell(fCol+1,Row.CREATE_NULL_AS_BLANK)).startsWith("BP"))
                {
                    int j = 0;
                    int chanelPosition = 0;
                    String channel = "";
                    int currIndent = sh.getRow(fRow+i).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellStyle().getIndention();
                    while(sh.getRow(fRow+i-j).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellStyle().getIndention()>=currIndent)
                    {     
                        j++;
                    }

                   // statusLog.addLog("Поиск канала");                                                         
                     values[1] = String.valueOf(fRow+i); // local_id
                     values[2] = cellValueToString(sh.getRow(fRow+i-j).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)); //imd_sal_name
                     values[3] = cellValueToString(sh.getRow(fRow+i-j).getCell(fCol+2,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)); //imd_sal_id
                     values[4] = cellValueToString(sh.getRow(fRow+i).getCell(fCol,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));//IMD_PRO_NAME
                     values[5] = cellValueToString(sh.getRow(fRow+i).getCell(fCol+1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));//IMD_PRO_ID
                     values[6] = cellValueToString(sh.getRow(fRow+i-j).getCell(fCol+1,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));//IMD_SAL_HOUSE

                     if (current_reg_code == -1 && values[3].length()>3)
                     {                         
                         statusLog.addLog("ОПРЕДЕЛЕНИЕ РЕГИОНА "+ values[3].substring(0, 3),"DEFAULT");
                         current_reg_code = getRegionCode(values[3].substring(0, 3));
                         statusLog.addLog("КОД РЕГИОНА "+ String.valueOf(current_reg_code),"DEFAULT");
                         statusLog.setFileRegion(String.valueOf(current_reg_code));
                     }

                    String parentDate = "";                    
                    int dataType = 0;
                    double sum = 0;
                    double count = 0;
                   // колонки с датой
                    for (int n=dateCol;n<sh.getRow(dateRow).getLastCellNum()-2;n++ )
                    {
                       // k++;                                                                  
                        double value = 0;
                        if (Cell.CELL_TYPE_NUMERIC == sh.getRow(fRow+i).getCell(n,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType())
                        {
                            value = sh.getRow(fRow+i).getCell(n,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue();        
                        }

                        if (sh.getRow(dateRow).getCell(n,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().length()>0)
                        {                           
                            parentDate = sh.getRow(dateRow).getCell(n,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                            dataType = 1; // "Количество";
                            sum = 0;
                            count = value;                                                        
                            if(n==dateCol)
                            {
                                statusLog.setFirstDate(parentDate);
                            }
                            if (n ==sh.getRow(dateRow).getLastCellNum()-4)
                            {
                                statusLog.setLastDate(parentDate);
                            }                            
                        }
                        else
                        {
                            dataType = 2; // "Сумма";
                            sum = value;
                        }    

                        for (int e = 0; e<values.length ;e++)
                        {
                            values [e] = values[e].replaceAll("'", "");
                            values [e] = values[e].replaceAll(";", "");
                            values [e] = values[e].replaceAll(String.valueOf('"'), "");
                        }

                        if ((sum != 0 || count != 0)&& dataType == 2)
                        {                            
                            sb.append("insert into tmp_import_data (IMD_EXPEDITOR,IMD_LOCAL_ID,IMD_SAL_NAME,IMD_SAL_ID,IMD_SAL_HOUSE,IMD_CHANNEL_NAME,IMD_CHANNEL_CODE,IMD_BDN_CODE,IMD_PRO_NAME,IMD_PRO_ID,IMD_VIS_DATE,IMD_TYPE, IMD_SUM_BY_USER, IMD_COUNT_BY_USER,IMD_FILE_NAME ,IMD_VALUE,IMD_SUM,IMD_IMPORT_CODE) values ");
                            sb.append("('").append(values[0]).append("'").append(",");
                            sb.append("'").append(values[1]).append("'").append(",");
                            sb.append("'").append(values[2]).append("'").append(",");
                            sb.append("'").append(values[3]).append("'").append(",");
                            sb.append("'").append(values[6]).append("'").append(",");
                            sb.append("'").append(values[7]).append("'").append(",");
                            sb.append("'").append(values[8]).append("'").append(",");
                            sb.append("'").append(String.valueOf(current_reg_code)).append("'").append(",");
                            sb.append("'").append(values[4]).append("'").append(",");
                            sb.append("'").append(values[5]).append("'").append(",");
                            sb.append("'").append(parentDate).append("'").append(",");
                            sb.append("'").append(dataType).append("'").append(",");
                            sb.append("'").append(values[9]).append("'").append(",");
                            sb.append("'").append(values[10]).append("'").append(",");
                            sb.append("'").append(fileName).append("'").append(",");
                            sb.append("'").append(formatter.format(count)).append("'").append(",");
                            sb.append("'").append(formatter.format(sum)).append("','").append(time).append("'") .append(");").append("\n");
                            appendRowCount++;

                            if (appendRowCount> 50000)
                            {
                                dbo.doBatchUpdate(sb.toString());                                        
                                appendRowCount = 0;
                                sb = null;
                                sb = new StringBuilder();

                                dbo.doBatchUpdate(sbHierarchy.toString());
                                sbHierarchy = null;
                                sbHierarchy = new StringBuilder();
                            } 
                            // SBRAQSYVAEM CHTOBY PROPUSKAT PUSTYE STOLBCY
                            dataType = 0;
                            count = 0;
                            sum = 0;
                        }                                               
                    }
                   }              
             }
             catch(Exception e)
             {
                System.out.println("EXCEPTION ON ROW"+ String.valueOf(i)+" "+ e.toString());
                statusLog.addLog("Ошибка на строке: "+ String.valueOf(i)+", "+ e.toString(),"ERROR");
             }
        }                                              
        try
        {                    
            if(!isStopped)
            {
                statusLog.setUserCount(user_count);
                statusLog.addLog("Вставка данных","DEFAULT");
                dbo.doBatchUpdate(sbHierarchy.toString());    
                statusLog.addLog("Иерархия вставлена","SUCCESS");
                dbo.doBatchUpdate(sb.toString());  
                statusLog.addLog("Основные данные вставлены","SUCCESS");
                statusLog.addLog("Проверка на вставку","DEFAULT");
                String importDifference = getImportDifference(fileName);
                if (importDifference.length() == 0)
                {
                    statusLog.addLog("Вставка завершена успешно","SUCCESS");
                    statusLog.setStatus("FILEEND");
                    System.out.println("FILEOK");
                }
                else
                {
                    statusLog.addLog("Вставка завершена с ошибками: ".concat(importDifference),"ERROR"); 
                    statusLog.setStatus("FILEERROR");
                    System.out.println("FILEERROR");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Ошибка:"+e.getMessage());
            statusLog.addLog("Ошибка:"+e.getMessage(),"ERROR");
            statusLog.setGlobalStatus("PROCESSERROR");
            isFail  = true;
        }
        workbook.close();
        return isFail;
    }
    
    private String cellValueToString(Cell _cell)
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
