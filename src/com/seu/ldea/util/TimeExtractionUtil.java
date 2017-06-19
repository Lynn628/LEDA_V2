package com.seu.ldea.util;

import java.text.SimpleDateFormat;

/**
 * 6/15/2017 集中包含抽取时间信息的功能类
 * - 使用SUTime 或者 HeidelTime抽取时间
 * - 依据抽取出的时间所占百分比，给出时间是否有效
 * - 判断当前谓词宾语中所包含时间信息所占百分比，以判定此谓语是否固定衔接时间类型的宾语
 *
 * @author Lynn
 *
 */
public class TimeExtractionUtil {
   
	/**
	 * 判断在literal中时间信息所占比重是否能显示该句话主要表明时间
	 * @param setence
	 * @param percentage
	 * @return
	 */
	
	/*public regexTimeMatch(String input){
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy").parse(input);
	}
	*/
	public boolean isTimeInfoValidateInSentence(String setence, double percentage){
	
		return (Boolean) null;
	}
	
	
	
}
