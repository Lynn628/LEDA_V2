package com.seu.ldea.util;

/**
 *  Description:对URI中的斜线替换为空格，分开字母和数字，分开驼峰的字符
 * 6/5日新加功能，先查询此URI是否包含“www.w3.org"字段，若没有，则进行拆分、抽取
 * @param uri
 * @return
 */
public class URIUtil {
	
	 public static String processURI2(String uri){
		  //前两步处理主要为了解决BTC原始文件中的< 问题
		   uri = uri.replaceAll("<", " ");
		   uri = uri.replaceAll(">", " ");
		   uri = uri.replaceAll("/", " ");
		//   System.out.println(uri);
		   int length = uri.length();
		   boolean tag[] = new boolean[length];
		   //将所有的数字和字母之间加上空格,驼峰表达式拆开
		   String output = "";
		   for(int i = 0; i < length-1; i++){
			   if((Character.isLetter(uri.charAt(i))&& Character.isDigit(uri.charAt(i+1)))||
					   (Character.isLetter(uri.charAt(i+1))&& Character.isDigit(uri.charAt(i)))){
				   tag[i] = true;
			   }else if(uri.charAt(i) >= 'a' && uri.charAt(i) <= 'z'
					   && uri.charAt(i+1) >= 'A' && uri.charAt(i+1) <= 'Z'){
				   tag[i] =true;
			   }
		   }
		   //记录下一个字符段的起始位置
		    int former = 0;
		    for(int i = 0; i < length; i++){
		    	if(tag[i]){
		    		String subString = uri.substring(former, i+1);
		    		output += subString + " ";
		    		former = i+1;
		    	}
		    }
		    //将余下的字符串加上
		   output += uri.substring(former);
		//   System.out.println("URI after process " + output);
		   return output;
	    
		   
	   }
	 
	 /**
	  * 剔除URI中www.w3.org产生的干扰时间信息
	  * @param uri
	  * @return
	  */
   public static String processURI(String uri){
	   if(!uri.contains("www.w3.org")){
	   uri = uri.replaceAll("/", " ");
	//   System.out.println(uri);
	   int length = uri.length();
	   boolean tag[] = new boolean[length];
	   //将所有的数字和字母之间加上空格,驼峰表达式拆开
	   String output = "";
	   for(int i = 0; i < length-1; i++){
		   if((Character.isLetter(uri.charAt(i))&& Character.isDigit(uri.charAt(i+1)))||
				   (Character.isLetter(uri.charAt(i+1))&& Character.isDigit(uri.charAt(i)))){
			   tag[i] = true;
		   }else if(uri.charAt(i) >= 'a' && uri.charAt(i) <= 'z'
				   && uri.charAt(i+1) >= 'A' && uri.charAt(i+1) <= 'Z'){
			   tag[i] =true;
		   }
	   }
	   //记录下一个字符段的起始位置
	    int former = 0;
	    for(int i = 0; i < length; i++){
	    	if(tag[i]){
	    		String subString = uri.substring(former, i+1);
	    		output += subString + " ";
	    		former = i+1;
	    	}
	    }
	    //将余下的字符串加上
	   output += uri.substring(former);
	//   System.out.println("URI after process " + output);
	   return output;
    }
	   return uri;
   }
   
   public static void main(String[] args){
	   String aString =  "http://data.semanticweb.org/workshop/ocas/2011/paper/9";
	   processURI(aString);
   }
   }


