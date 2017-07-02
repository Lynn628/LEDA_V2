package com.seu.ldea.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 将时间字符串规范化表示，并以date形式返回，以便进行大小比较
 * @author Lynn
 *
 */
public class TimeUtil {
	
    public static Date formatTime(String dateStr) throws ParseException{
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
    		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    		Date date = dateFormat.parse(dateStr);
    		System.out.println(date);
    		return date;
    	}else{
    		String[] strings = dateStr.split("-");
    		int length =strings.length;
    		DateFormat dateFormat = new SimpleDateFormat();
    		if(!strings[0].equals("XX")){
    			//设置年的范围
    			System.out.println("String[0]-- " + strings[0]);
    			System.out.println();
    			int year = Integer.parseInt(strings[0]);
    			if(year > 1900 && year < 2017)
    		      dateFormat = new SimpleDateFormat("yyyy");
    			else return null;
    		}
    		
    		if((length >= 2) && !strings[1].equals("XX")){
    			System.out.println("String[1]-- " + strings[1]);
    			System.out.println();
    			int month = Integer.parseInt(strings[1]);
    			if(month >= 0 && month <= 12)
    			dateFormat = new SimpleDateFormat("yyyy-MM");
    			else 
    				return null;
    		}
    		if((length >= 3) && !strings[2].equals("XX")){
    			System.out.println("String[2]-- " + strings[2]);
    			System.out.println();
    			int day = Integer.parseInt(strings[2]);
    			if(day >= 0 && day <= 31)
    			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    			else return null;
    		}
    		System.out.println(dateFormat.parse(dateStr));
            return dateFormat.parse(dateStr);
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
    
  //2003-10-21T16:00:00+0000
    public static void main(String[] args) throws ParseException{
    	String dateStr = "2013-XX-XX";
    	String str1 = "2011-03-31T10:30:00+0530";
    	//int index = dateStr.indexOf("+");
		//dateStr = dateStr.substring(0, index);
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy");
        Date date1 = dateFormat1.parse(dateStr);
        System.out.println(date1);
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		/*Date date = dateFormat.parse(dateStr);
		System.out.println(date);*/
    	String str2 = "2013-06-05";
    	/*formatTime(str1);*/
    	formatTime(str1);
    }
}
