package com.seu.ldea.commonutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

//��rescal embedding���д����ļ�ӳ����Integer�ķ�Χ֮��
public class BigDecimalUtil {
	static final int rangeBegin = 0;
	static final int rangeEnd = 1;
	
	 /**
     * ��һ��embedding�ļ���������ļ�ȫ�����滯
     * @param dirname
     * @throws IOException
     */
    public static void normalizeDirFile(String dirname) throws IOException{
    	//�����embedding�ļ����ڵ��ļ���
    	String path1 = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\toBeNormalized\\" + dirname;
    	
    	/**�ȶ�ȡԭembedding�ļ������ļ����**/
    	File file = new File(path1);
    	if(file.isDirectory()){
    		String[] filePathList = file.list();
    		for(int i = 0; i < filePathList.length; i++){
    			File fileItem = new File(path1 + "\\" + filePathList[i]);
    			BigDecimalUtil.mappingBigdecimalToRange(0, 1, fileItem.getAbsolutePath(), fileItem.getName());
    		
    			if (!fileItem.isDirectory()) {
    				  System.out.println("path=" + fileItem.getPath());
                      System.out.println("absolutepath="
                                      + fileItem.getAbsolutePath());
                      System.out.println("name=" + fileItem.getName());
				}
    		}
    	}
        
    }
	
	
	
	//�ҵ�bigdecimal�ļ��������Сֵ
    public static BigDecimal[] changeBigDecimalToInteger(String inputFilePath) throws IOException{
         int lineNum = 0;
    	FileReader fileReader = new FileReader(inputFilePath);
    	BufferedReader bufferedReader = new BufferedReader(fileReader);
    	String line = ""; 
    	BigDecimal max;
    	BigDecimal min;
    	BigDecimal[] result = new BigDecimal[2];
    	   //ȡ����һ�У���
    	   String firstLine = bufferedReader.readLine();
    	//   System.out.println("first line " + firstLine);
    	   String[] firstBigDecimalArrStr = firstLine.split(" ");
    	   max = new BigDecimal(firstBigDecimalArrStr[0]);
    	   min = new BigDecimal(firstBigDecimalArrStr[0]);
    	   System.out.println("Initial max & min " + firstBigDecimalArrStr[0]);
    	   for(int i = 0; i < firstBigDecimalArrStr.length; i++){
    		   BigDecimal currentNum = new BigDecimal(firstBigDecimalArrStr[i]);
    		   if(currentNum.compareTo(max) > 0)
    			   max = currentNum;   
    		   if(currentNum.compareTo(min) < 0)
    			   min = currentNum;  
    	   }
    	   System.out.println("firstLine max & min " + max + " " + min);
    	   while((line = bufferedReader.readLine()) != null){
    		   lineNum++;
    		   System.out.println(lineNum + "**");
             String[] bigDecimalLine = line.split(" ");
    		// System.out.println(line);
    	    for(int i = 0; i < bigDecimalLine.length; i++){
    	    	BigDecimal currentNum = new BigDecimal(bigDecimalLine[i]);
    	    	if(currentNum.compareTo(min) < 0)
    	    		min = currentNum;
    	        if(currentNum.compareTo(max) > 0)
    	        	max = currentNum;
    	   }
    	}
    	   System.out.println("global min is " + min.toString()  + "global max is " + max.toString());
    	   bufferedReader.close();
    	   result[0] = min;
    	   result[1] = max;
    	   //result�ĵ�һλ���ȫ����С���ڶ�λ���ȫ�����
    	   return result;
    }
	
    
    /**
     *  ��bigdecimal������ӳ�䵽һ��С����  O ��ʾoriginal�� N��ʾnew
     *   Nx = (Nmax - Nmin)/(Omax - Omin)*(Ox - Omin) + Nmin
     * @param begin
     * @param end
     * @param inputEmbeddingPath
     * @param dstFileName
     * @throws IOException
     */
    public static void mappingBigdecimalToRange(int begin, int end,String inputEmbeddingPath, String dstFileName) throws IOException{
        int lineNum = 0;
    	BigDecimal[] minAndMax = changeBigDecimalToInteger(inputEmbeddingPath); 
    	BigDecimal min = minAndMax[0];
    	BigDecimal max = minAndMax[1];
    	System.out.println(min + " " + max);
    	FileReader fileReader = new FileReader(inputEmbeddingPath);
    	BufferedReader bufferedReader = new BufferedReader(fileReader);
    	String dstFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\Normailzed-" + dstFileName;
    	FileWriter fileWriter = new FileWriter(dstFilePath);
    	BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
    	String line = "";
    	while((line = bufferedReader.readLine()) != null){
    		lineNum++;
    		System.out.println(lineNum + "***");
    		double newNumArr;
    		String[] bigDecimalArrStr = line.split(" ");
    		//System.out.println("length is " + bigDecimalArrStr.length);
    		for(int i = 0; i < bigDecimalArrStr.length; i++){
    			BigDecimal currentBigDecimal = new BigDecimal(bigDecimalArrStr[i]);
    			newNumArr = (end - begin)*(currentBigDecimal.subtract(min)).divide(max.subtract(min), 15, BigDecimal.ROUND_HALF_UP).doubleValue() + begin;
    		 //   System.out.println(newNumArr);
    			bufferedWriter.write(newNumArr + " "); 
    		}
    	    bufferedWriter.newLine();	
    	}
    	bufferedReader.close();
    	bufferedWriter.close();
    	
    }
    
    public static void main(String[] args) throws IOException{
    	long t1 = System.currentTimeMillis();
    	/*String path = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\www-2010-complete-latent30-lambda0.embeddings.txt";
    	//BigDecimal[] minAndMax = changeBigDecimalToInteger(path);
    	mappingBigdecimalToRange(rangeBegin, rangeEnd, path, "Normalizedwww-2010-complete-latent30-lambda0");*/
    	
    	//String dir = "Jamendo";
    	String dir = "DBLP";
    	normalizeDirFile(dir);
    	long t2 = System.currentTimeMillis();
    	System.out.println( " time cost " + (t2 - t1)/1000 + " s");
    	
    }
}
