package com.seu.ldea.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *  Description:��URI�е�б���滻Ϊ�ո񣬷ֿ���ĸ�����֣��ֿ��շ���ַ�
 * 6/5���¼ӹ��ܣ��Ȳ�ѯ��URI�Ƿ������www.w3.org"�ֶΣ���û�У�����в�֡���ȡ
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
		 //��һ���ַ����ֽ��
		if(matcher.find()){
			return false;
		}
		return true;
	 }
	 
	 /**
	  * �޳�URI��www.w3.org�����ĸ���ʱ����Ϣ
	  * @param uri
	  * @return
	  */
   public static String processURI(String uri){
	 
	   uri = uri.replaceAll("/", " ");
	//   System.out.println(uri);
	   int length = uri.length();
	   boolean tag[] = new boolean[length];
	   //�����е����ֺ���ĸ֮����Ͽո�,�շ���ʽ��
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
	   //��¼��һ���ַ��ε���ʼλ��
	    int former = 0;
	    for(int i = 0; i < length; i++){
	    	if(tag[i]){
	    		String subString = uri.substring(former, i+1);
	    		output += subString + " ";
	    		former = i+1;
	    	}
	    }
	    //�����µ��ַ�������
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


