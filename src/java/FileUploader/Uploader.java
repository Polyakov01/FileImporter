/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileUploader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author PK
 */
public class Uploader 
{
    @Deprecated
    public String doUpload(FileItem item,String fullPath, String name)
    {
        String status = "OK"; 
        //String name = item.getName();       
        if (item.getSize() < (500 * 1024 * 1024)) 
        {
            Boolean success = (new File(fullPath)).mkdirs();
            if (success) 
            {
                System.out.println("Created Folder name: " + fullPath + ";");
            } 
            else 
            {
                System.out.println("Folder name: " + fullPath + ";");
            }

            File uploadedFile = new File(fullPath + "/" + name);
            if (uploadedFile.exists()) 
            {
                if(uploadedFile.delete())
                {
                    status = "OK"; 
                }
                else
                {
                   status = "Unable to delete old file"; 
                }
            }
            if (status.equals("OK"))
            {
                try 
                {
                    uploadedFile.createNewFile();
                    item.write(uploadedFile);
                    status = "OK";
                } catch (IOException ex) {
                    Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, null, ex);
                     status = ex.getMessage();
                } catch (Exception ex) {
                    Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, null, ex);
                    status = ex.getMessage();
                }
            }            
        }
        else
        {
           status = "Trying to write a large file.";                   
        } 
        return status;
    }
    
    public String doUpload(FileItem item,String fullPath, String name, boolean thumbIsNeed)
    {
        String status = "OK"; 
        //String name = item.getName();       
        if (item.getSize() < (500 * 1024 * 1024)) 
        {
            Boolean success = (new File(fullPath)).mkdirs();
            if (success) 
            {
                System.out.println("Created Folder name: " + fullPath + ";");
            } 
            else 
            {
                System.out.println("Folder name: " + fullPath + ";");
            }

            File uploadedFile = new File(fullPath + "/" + name);
            if (uploadedFile.exists()) 
            {
                if(uploadedFile.delete())
                {
                    status = "OK"; 
                }
                else
                {
                   status = "Unable to delete old file"; 
                }
            }
            if (status.equals("OK"))
            {
                try 
                {
                    uploadedFile.createNewFile();
                    item.write(uploadedFile);
                    status = "OK";
                } catch (IOException ex) {
                    Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, null, ex);
                     status = ex.getMessage();
                } catch (Exception ex) {
                    Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, null, ex);
                    status = ex.getMessage();
                }
            }            
        }
        else
        {
           status = "Trying to write a large file.";                   
        } 
        return status;
    }
    
    public boolean doClearFiles(String FullPath)
    {      
        Boolean result = true;
        File f3 = new File(FullPath);
        if (!f3.exists()){f3.mkdirs();}
        for(File currFile:f3.listFiles())
        {                
             if(currFile.delete())
             {
                 result = true;
             }
             else
             {
                 result = false;
                 break;
             }
                     
        }   
        return result;
    }
    
    public Boolean doBackup(String sourcePath, String sourceFile, String destinationPath, String destinationFile)
    {
        new File(sourcePath).mkdirs();
        new File(destinationPath).mkdirs();
        File f1 = new File(sourcePath.concat("/").concat(sourceFile));    
        File f2 = new File(destinationPath.concat("/").concat(destinationFile));                     
        if (f1.exists())
        {
            return  f1.renameTo(f2);          
        }
        else
        {
            Logger.getLogger("UPLOADER_SAPSAN").log(Level.SEVERE, "NO FILE FOR BACKUP!");
            return true;
        }
    }
}
