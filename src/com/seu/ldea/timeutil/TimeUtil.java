package com.seu.ldea.timeutil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * 将时间字符串规范化表示，并以date形式返回，以便进行大小比较
 * @author Lynn
 *
 */
public class TimeUtil {
	
    public static Date formatTime(String dateStr) {
    System.out.println("datStr -- " + dateStr);
    	//如果是日期格式 
    if(isValidDate(dateStr)){
    	//2012-05-31T10:30:00
    	if(dateStr.contains("T")){
    		//2011-03-31T10:30:00+0530
    		if(dateStr.contains("+")){
    		int index = dateStr.indexOf("+");
    		dateStr = dateStr.substring(0, index);
    		}
    		System.out.println("dateStr2-- " + dateStr);
    		try{
    		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    		Date date = dateFormat.parse(dateStr);
    		System.out.println(date);
    		return date;
    		}catch (ParseException e) {
    			return null;
				// TODO: handle exception
			}
    	}else{
    		String[] strings = dateStr.split("-");
    		int length =strings.length;
    		DateFormat dateFormat = new SimpleDateFormat();
    		if(!strings[0].contains("X") && !strings[0].equals("")){
    			System.out.println("String[0]-- " + strings[0]);
    			System.out.println();
    			int year = Integer.parseInt(strings[0]);
    			//设置年的范围
    			if(year > 1900 && year < 2017)
    		      dateFormat = new SimpleDateFormat("yyyy");
    			else 
    				return null;
    		}
    		Pattern pattern = Pattern.compile("[0-9]*");
    		
    		//if((length >= 2) && !strings[1].equals("XX")){
    		if(length >= 2 && !strings[1].contains("X")){
    			Matcher matcher = pattern.matcher(strings[1]);
    			if(matcher.matches()){
    			System.out.println("String[1]-- " + strings[1]);
    			System.out.println();
    			int month = Integer.parseInt(strings[1]);
    			//设置月的范围
    			if(month >= 0 && month <= 12)
    			dateFormat = new SimpleDateFormat("yyyy-MM");
    			else 
    				return null;
    			}else return null;
    		}
    		
    		//if((length >= 3) && !strings[2].equals("XX")){
    		if(length >= 3 && !strings[2].contains("X")){
    			Matcher matcher2 = pattern.matcher(strings[2]);
    			//日期非数值
    			if(matcher2.matches()){
    		System.out.println("String[2]-- " + strings[2]);
    			System.out.println();
    			int day = Integer.parseInt(strings[2]);
    			if(day >= 0 && day <= 31)
    			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    			else
    				return null;
    		 }else 
    			 return null;
    		}
    		try{
    		  System.out.println(dateFormat.parse(dateStr));
              return dateFormat.parse(dateStr);
            }catch (ParseException e) {
            	return null;
				// TODO: handle exception
			}
    	}
      }else{//非日期格式
    		return null;
    	}
    }
    
    public static boolean isValidDate(String str){
    	boolean convertSuccess = true;
    	
    	SimpleDateFormat format = new SimpleDateFormat("yyyy");
    	try{
    		format.parse(str);
    	}catch (ParseException e) {
    		convertSuccess = false;
			// TODO: handle exception
		}
    	return convertSuccess;
    }
    
    /**
     * 判断是否是有效的时间日期字符串
     * @param str
     * @return
     */
    public static boolean isValidDateStr(String str){
    	boolean convertSuccess = true;
    	SimpleDateFormat format1 = new SimpleDateFormat("yyyy");
    	SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    	
    	if(str.contains("T")){
    		//2011-03-31T10:30:00+0530
    		if(str.contains("+")){
    		int index = str.indexOf("+");
    		str = str.substring(0, index);
    		}
    	}else{
    	try{
    		format1.parse(str);
    	}catch (ParseException e) {
    		convertSuccess = false;
			// TODO: handle exception
		}
    	}
    	return convertSuccess;
    }
    
  //2003-10-21T16:00:00+0000
    public static void main(String[] args) throws ParseException{
    	String dateStr = "-99999";
    	String str1 = "2011-03-31T10:30:00+0530";
    	//int index = dateStr.indexOf("+");
		//dateStr = dateStr.substring(0, index);
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy");
        Date date1 = dateFormat1.parse(dateStr);
        System.out.println(date1);
   /* 	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date = dateFormat.parse(dateStr);
		System.out.println(date);
    	String str2 = "2013-06-05";
    	formatTime(str1);
    	//formatTime(str1);
        String day = "20XX";
        DateFormat format = new SimpleDateFormat("yyyy");
        Date dates = format.parse( day);
        System.out.println("***" + dates);
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
    	System.out.println("day of 1000 " + format2.parse(str2));*/
    }
}
