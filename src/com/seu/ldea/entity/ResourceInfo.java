package com.seu.ldea.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
/**
 * ʱ��ʵ���࣬����ʱ����Ϣ��ʵ��
 * @author Lynn
 *
 */
public class ResourceInfo {
	// ����Id,���������
	Integer reourceId;
	// �����Ӧ�ı������
	Integer totalObjNum;
	// �����Ӧ�İ���ʱ����Ϣ�ı������
	Integer timeObjNum;
	//��Դ������
	Integer type;
	
	public void setType(Integer type) {
		this.type = type;
	}
	
	public Integer getType() {
		return type;
	}
	// �洢ʱ����Ϣ��<pId,<t1, t2>>��
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
