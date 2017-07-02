package com.seu.ldea.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *  Description:对URI中的斜线替换为空格，分开字母和数字，分开驼峰的字符
 * 6/5日新加功能，先查询此URI是否包含“www.w3.org"字段，若没有，则进行拆分、抽取
 * @param uri
 * @return
 */
public class URIUtil {
	
	 public static boolean judgeURI(String uri){
		 uri = uri.replaceAll("/", " ");
		// System.out.println(uri);
		 if(uri.contains("www.w3.org")){
			 return false;
		 }
		 String regex = "[0-9]+[a-z]+";
		 Pattern pattern = Pattern.compile(regex);
		 Matcher matcher = pattern.matcher(uri);
		 //是一串字符数字结合
		if(matcher.find()){
			return false;
		}
		return true;
	 }
	 
	 /**
	  * 剔除URI中www.w3.org产生的干扰时间信息
	  * @param uri
	  * @return
	  */
   public static String processURI(String uri){
	 
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
			   tag[i] = true;
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
   
   public static void main(String[] args){
	   String aString =  "http://data.semanticweb.org/workshop/ocas/2011/paper/9";
	   String bString = "http://www.freebase.com/view/guid/9202a8c04000641f800000000b946bc9";
	   String cString = "http://data.semanticweb.org/conference/dh/2010/abstracts/paper/ab-589";
	  // processURI(aString);
	 /*  String regex = "[0-9]+[a-z]+";
		 Pattern pattern = Pattern.compile(regex);
		 Matcher matcher = pattern.matcher("1b");
		
		 System.out.println(matcher.find());*/
	   System.out.println("aString " + judgeURI(aString));
	   System.out.println("bString " + judgeURI(bString));
       System.out.println("cString " + judgeURI(cString));	 
      // System.out.println(matcher.matches());
   }
   }


