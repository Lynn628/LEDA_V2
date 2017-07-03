package com.seu.ldea.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
/**
 * 时间实体类，具有时间信息的实体
 * @author Lynn
 *
 */
public class ResourceInfo {
	// 主语Id,外键，主键
	Integer reourceId;
	// 主语对应的宾语个数
	Integer totalObjNum;
	// 主语对应的包含时间信息的宾语个数
	Integer timeObjNum;
	//资源的类型
	Integer type;
	
	public void setType(Integer type) {
		this.type = type;
	}
	
	public Integer getType() {
		return type;
	}
	// 存储时间信息的<pId,<t1, t2>>对
	HashMap<String, HashSet<TimeSpan>> timeInfoPair = new HashMap<>();
	public ResourceInfo(Integer rId) {
		this.reourceId = rId;
		// TODO Auto-generated constructor stub
	}

	public HashMap<String, HashSet<TimeSpan>> getPredicateTimePair() {
		return timeInfoPair;
	}
   
	public void setTimeInfoPair(HashMap<String, HashSet<TimeSpan>> timeInfoPair) {
		// TODO Auto-generated method stub
		this.timeInfoPair = timeInfoPair;
	}
	public String toString() {
		Iterator<Entry<String, HashSet<TimeSpan>>> iter = timeInfoPair.entrySet().iterator();
		String spanStr = "";
		System.out.println("The time predicate # on this Resource -----" + timeInfoPair.entrySet().size());
		while (iter.hasNext()) {
			Entry<String, HashSet<TimeSpan>> entry = iter.next();
			String pre = (String) entry.getKey();
			HashSet<TimeSpan> spanSet = (HashSet<TimeSpan>) entry.getValue();
			spanStr += "< " + pre + ", ";
			for (TimeSpan span : spanSet) {
				spanStr += span.toString() + "; ";
			}
			spanStr += "> ";
		}
		return spanStr;
	}

	public static void main(String[] args) {

	}

	
	

}
