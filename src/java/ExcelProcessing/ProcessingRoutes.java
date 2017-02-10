/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExcelProcessing;


import DB.DBOperations;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import util.JDBCUtil;

/**
 *
 * @author PK
 */
public class ProcessingRoutes 
{
    public static String doProcessRoutes(File f1)
    {       
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "BEGIN PROCESS ROUTES");
        String importStatus = "OK";
        String fileName = f1.getAbsolutePath();
        Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, fileName);
        String fileNameWOPath = f1.getName();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DBOperations dbo = new JDBCUtil().getDBOperations();
        Document doc = null;
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();                
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new File( fileName));  
        }
        catch (IOException | ParserConfigurationException | SAXException ex)
        {
            importStatus = "Ошибка: "+ex.getMessage();
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, null, ex);
        } 
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (doc != null)
        {                                  
            String sqlFields = "";
            String sqlValues = "";
            boolean getValuesIsOk;
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.INFO, "Processing routes getXML");
            StringBuilder sb = new StringBuilder();
            try
            {
                NodeList list = doc.getChildNodes(); 
                for (int i = 0; i < list.item(0).getChildNodes().getLength(); i++) 
                {                           
                    Node childNode = list.item(0).getChildNodes().item(i);          
                    if (childNode.getNodeName().equalsIgnoreCase("VISIT_PLAN"))
                    {
                        getValuesIsOk = true;
                        for (int j = 0;j< childNode.getChildNodes().getLength();j++ )
                        {
                            if (childNode.getChildNodes().item(j) instanceof Element)
                            { 
                                try
                                {
                                    Element e = (Element) childNode.getChildNodes().item(j);
                                    sqlFields = sqlFields.concat(e.getNodeName().replaceAll("'", "").replaceAll("\"",""))
                                                         .concat(",");
                                    if(e.getFirstChild() != null)
                                    {
                                        sqlValues = sqlValues.concat("'")
                                                             .concat(e.getFirstChild().getNodeValue().replaceAll("'", "").replaceAll("\"",""))
                                                             .concat("',");              
                                    }
                                    else
                                    {
                                        sqlValues= sqlValues.concat("'    ',");
                                    }
                                }
                                catch(DOMException ex)
                                {
                                    getValuesIsOk = false;
                                    Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE,null, ex);
                                }
                            }
                        }
                        if(getValuesIsOk && sqlFields.length() > 3 && sqlValues.length() > 3 )
                        {
                            sb.append("insert into TMP_IMPORT_ROUTES(IMR_FILENAME,")
                                    .append(sqlFields.substring(0,sqlFields.length()-1))
                                    .append(") values ('")
                                    .append(fileNameWOPath)
                                    .append("',")
                                    .append(sqlValues.substring(0,sqlValues.length()-2))
                                    .append("')\n");
                        }
                        sqlFields = "";
                        sqlValues = "";
                    }            
                }
                dbo.doBatchUpdate(sb.toString());                
            }
            catch(Exception ex)
            {
                importStatus = "Ошибка БД"+ex.getMessage();
                Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE,null, ex);                
            }           
        }         
        return importStatus;
    }
}
