package com.seu.ldea.commonutil;
/**
 * ��ȡĳ��Ŀ¼���������ļ����ļ���
 */
import java.io.File;
import java.util.ArrayList;

public class ReadFileUnderDir {
 private static ArrayList<String> filePathList = new ArrayList<>();
  //���ڿ��Լ�һ���ж��ļ��ǲ���rdf��β�ģ������rdf��β��ӽ�ArrayList���ж�ȡ
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
  //�ж��ļ��Ƿ����
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
