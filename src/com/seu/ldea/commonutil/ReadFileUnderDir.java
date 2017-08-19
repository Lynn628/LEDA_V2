package com.seu.ldea.commonutil;
/**
 * 读取某个目录下面所有文件的文件名
 */
import java.io.File;
import java.util.ArrayList;

public class ReadFileUnderDir {
 private static ArrayList<String> filePathList = new ArrayList<>();
  //后期可以加一个判断文件是不是rdf结尾的，如果是rdf结尾则加进ArrayList进行读取
  public static ArrayList<String> readDir(String dirPath){
	  /*try{*/
		  File file = new File(dirPath);
		  if(!file.isDirectory()){
			  filePathList.add(file.getPath().replaceAll("\\\\","/"));			  
		  }else if(file.isDirectory()){
			  String[] fileList = file.list();
			  for(int i=0; i<fileList.length; i++){
				  File readfile = new File(dirPath);  
				  if (!readfile.isDirectory()) {  
	                    filePathList.add(readfile.getPath());  
	                } else if (readfile.isDirectory()) {  
	                    readDir(dirPath + "\\" + fileList[i]);        	 
			    }
			  }
		  }
	
	 return filePathList;
  }
  //判断文件是否存在
  public static Boolean filePathValidation(String filePath) {
    File file = new File(filePath);
    if (!file.exists()) {
      System.out.println("Wrong path or no such file.");
    }
    return file.exists();
  }
  
/* public static void main(String[] args){
	  String path = "C:/Users/Lynn/Desktop/Academic/LinkedDataProject/DataSet/SWCC/conferences";
	  Iterator<String> iter = readDir(path).iterator();
	  int i = 0;
	  while(iter.hasNext()){
		  System.out.println(iter.next() + "\n");
		  i++;
	  }
	  System.out.println("\n" + i);
  }*/
}
