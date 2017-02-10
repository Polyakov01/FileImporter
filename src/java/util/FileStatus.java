package util;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * * @author Hrust
 */
public class FileStatus
{
    public class Params
    {
        private String fileName = "";
        private String fileRegion = "";
        private String fileUserCount = "";
        private String fileSum = "";
        private String fileProdCount = "";
        private String fileProgress = "";
        private int filePos = 0;

        public String getFileProdCount() {
            return fileProdCount;
        }

        public void setFileProdCount(String fileProdCount) {
            this.fileProdCount = fileProdCount;
        }

        
        
        public int getFilePos() {
            return filePos;
        }

        public String getFileProgress() {
            return fileProgress;
        }

        public void setFileProgress(String fileProgress) {
            this.fileProgress = fileProgress;
        }

        public void setFilePos(int filePos) {
            this.filePos = filePos;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void setFileRegion(String fileRegion) {
            this.fileRegion = fileRegion;
        }

        public void setFileUserCount(String fileUserCount) {
            this.fileUserCount = fileUserCount;
        }

        public void setFileSum(String fileSum) {
            this.fileSum = fileSum;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public void setFinishDate(String finishDate) {
            this.finishDate = finishDate;
        }

        public void setFileLoadStatus(String fileLoadStatus) {
            this.fileLoadStatus = fileLoadStatus;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileRegion() {
            return fileRegion;
        }

        public String getFileUserCount() {
            return fileUserCount;
        }

        public String getFileSum() {
            return fileSum;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getFinishDate() {
            return finishDate;
        }

        public String getFileLoadStatus() {
            return fileLoadStatus;
        }
        private String startDate = "";
        private String finishDate = "";
        private String fileLoadStatus = "";
        
        public Params (String _fileName)
        {
            fileName = _fileName;
        }      
        
        public void  SetParams (String _fileRegion,String _fileUserCount,String _fileSum,String _startDate,String _finishDate,String _fileLoadStatus)
        {            
            fileRegion = _fileRegion;
            fileUserCount = _fileUserCount;
            fileSum = _fileSum;
            startDate = _startDate;
            finishDate = _finishDate;
            fileLoadStatus =_fileLoadStatus;
        }       
    }
       
   private final HashMap<Integer,Params> fileList;
   public FileStatus ()
   {
       fileList = new HashMap<>();
   }
   
   public ArrayList<String> getOkFiles()
   {
       ArrayList<String> fileListOK =  new ArrayList<>();
       try
        {                   
            Iterator names = fileList.keySet().iterator();
            while (names.hasNext())
            {
               int currPos = (Integer)names.next();
               if ((fileList.get(currPos).getFileLoadStatus().equals("OK")))
               {
                   fileListOK.add(fileList.get(currPos).getFileName());
               }
            }
        }
        catch(Exception ex)
        {
                   System.out.println(ex.getMessage());
        }
        return fileListOK;               
   }
   
   public void addFile (String _fileName)
   {
       fileList.put(fileList.size(), new Params(_fileName)); 
      // System.out.println("_fileName"+_fileName);
   }
   
   public Params getFileParams (String _fileName)
   {
       Iterator names = fileList.keySet().iterator();
       boolean isFound = false;
       int findPos = -1;      
       while(names.hasNext() && !isFound)
       { 
           int currPos = (Integer)names.next();
           if (_fileName.equals(fileList.get(currPos).getFileName()))
           {
               isFound = true;
               findPos = currPos;
           }
       }
       return fileList.get(findPos);
   }
   
   
   public String getJSONFileArray()
   {
       String result = "[";
       
       Iterator names = fileList.keySet().iterator();
       while(names.hasNext())
       {        
           int key =  (Integer)names.next();
        //   System.out.println(key);
           //System.out.println(key);
           result = result.concat("{\"fileName\":".concat("\"").concat(fileList.get(key).getFileName()).concat("\"").concat(","));
           result = result.concat("\"fileRegion\":".concat("\"").concat(fileList.get(key).getFileRegion()).concat("\"").concat(","));
           result = result.concat("\"filePos\":".concat("\"").concat(String.valueOf(key)).concat("\"").concat(","));
           result = result.concat("\"fileSum\":".concat("\"").concat(fileList.get(key).getFileSum()).concat("\"").concat(","));
           result = result.concat("\"fileProdCount\":".concat("\"").concat(fileList.get(key).getFileProdCount()).concat("\"").concat(","));
           result = result.concat("\"fileStatus\":".concat("\"").concat(fileList.get(key).getFileLoadStatus()).concat("\"").concat(","));
           result = result.concat("\"fileProg\":".concat("\"").concat(fileList.get(key).getFileProgress()).concat("\"").concat(","));
           result = result.concat("\"fileUserCount\":".concat("\"").concat(fileList.get(key).getFileUserCount()).concat("\"").concat(","));
           result = result.concat("\"fileFd\":".concat("\"").concat(fileList.get(key).getStartDate()).concat("\"").concat(","));
           result = result.concat("\"fileLd\":".concat("\"").concat(fileList.get(key).getFinishDate()).concat("\"").concat("},"));
       }
       result = result.substring(0, result.length()-1);
       result = result.concat("]");
      // System.out.println(result);
       return result;
   }
   
    
}
