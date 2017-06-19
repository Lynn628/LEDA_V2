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
	// 存储时间信息的<pId,<t1, t2>>对
	//HashMap<Integer, HashSet<TimeSpan>> timeInfoPair = new HashMap<>();
	HashMap<String, HashSet<TimeSpan>> timeInfoPair = new HashMap<>();
	public ResourceInfo(Integer rId) {
		this.reourceId = rId;
		// TODO Auto-generated constructor stub
	}

	/*public HashMap<Integer, HashSet<TimeSpan>> getTimeInfoPair() {
		return timeInfoPair;
	}*/
	public HashMap<String, HashSet<TimeSpan>> getTimeInfoPair() {
		return timeInfoPair;
	}

	/*public String toString() {
		Iterator<Entry<Integer, HashSet<TimeSpan>>> iter = timeInfoPair.entrySet().iterator();
		String spanStr = "";
		while (iter.hasNext()) {
			Entry<Integer, HashSet<TimeSpan>> entry = iter.next();
			Integer pId = (Integer) entry.getKey();
			HashSet<TimeSpan> spanSet = (HashSet<TimeSpan>) entry.getValue();
			spanStr = "< " + pId + ", ";
			for (TimeSpan span : spanSet) {
				spanStr += span.toString() + "; ";
			}
		}
		return spanStr;
	}*/
	public String toString() {
		Iterator<Entry<String, HashSet<TimeSpan>>> iter = timeInfoPair.entrySet().iterator();
		String spanStr = "";
		while (iter.hasNext()) {
			Entry<String, HashSet<TimeSpan>> entry = iter.next();
			String pId = (String) entry.getKey();
			HashSet<TimeSpan> spanSet = (HashSet<TimeSpan>) entry.getValue();
			spanStr = "< " + pId + ", ";
			for (TimeSpan span : spanSet) {
				spanStr += span.toString() + "; ";
			}
		}
		return spanStr + ">";
	}
	// 内部类，时间实体时间信息键值对
	class TimeLabel {
		String predicate;
		String begin;
		String end;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "<" + predicate + ", <" + begin + "," + end + ">>";
		}
	}

	public static void main(String[] args) {

	}

}
