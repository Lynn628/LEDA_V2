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
	// �洢ʱ����Ϣ��<pId,<t1, t2>>��
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
	// �ڲ��࣬ʱ��ʵ��ʱ����Ϣ��ֵ��
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
