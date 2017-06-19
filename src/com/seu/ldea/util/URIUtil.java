package com.seu.ldea.util;

/**
 *  Description:��URI�е�б���滻Ϊ�ո񣬷ֿ���ĸ�����֣��ֿ��շ���ַ�
 * 6/5���¼ӹ��ܣ��Ȳ�ѯ��URI�Ƿ������www.w3.org"�ֶΣ���û�У�����в�֡���ȡ
 * @param uri
 * @return
 */
public class URIUtil {
	
	 public static String processURI2(String uri){
		  //ǰ����������ҪΪ�˽��BTCԭʼ�ļ��е�< ����
		   uri = uri.replaceAll("<", " ");
		   uri = uri.replaceAll(">", " ");
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
				   tag[i] =true;
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
	 
	 /**
	  * �޳�URI��www.w3.org�����ĸ���ʱ����Ϣ
	  * @param uri
	  * @return
	  */
   public static String processURI(String uri){
	   if(!uri.contains("www.w3.org")){
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
			   tag[i] =true;
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
	   return uri;
   }
   
   public static void main(String[] args){
	   String aString =  "http://data.semanticweb.org/workshop/ocas/2011/paper/9";
	   processURI(aString);
   }
   }


