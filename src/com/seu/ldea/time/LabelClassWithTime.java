package com.seu.ldea.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.seu.Idea.cluster.Dataset;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;

/**
 * ����ʵ���ʱ����Ϣ������class��ʱ���ǩ
 * 
 * @author Lynn ˼·��words���ҵ�type��id������id����ȡ��Ӧ��col��row�ļ����ҵ�����ʱ����Ϣ����Դ���������
 *         ����ͬһʱ�����������ʱ��
 */
public class LabelClassWithTime {
	// ÿ����Դ��id�Լ�Я����ʱ����Ϣ
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();
	// class id��Resource id ��ӳ��
	public static HashMap<Integer, ArrayList<ResourceInfo>> classTimeInfo = new HashMap<>();

	public static void getClassTimeInformation(String dir) throws IOException {

		String wordsFile = dir + "\\words";
		FileReader fileReader = new FileReader(wordsFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		// type��uri���
		int num = 0;
		while ((line = bufferedReader.readLine()) != null) {
			int index = line.indexOf(":");
			String subStr1 = line.substring(0, index);
			String subStr2 = line.substring(index + 1);
			if (subStr2.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				num = Integer.parseInt(subStr1);
				break;
			}
		}
		// �����ļ�
		String colFile = dir + "\\" + num + "-cols";
		// �����ļ�
		String rowFile = dir + "\\" + num + "-rows";
		FileReader fr1 = new FileReader(colFile);
		FileReader fr2 = new FileReader(rowFile);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		String bString1 = br1.readLine();
		String bString2 = br2.readLine();
		br1.close();
		br2.close();
		// �洢��
		String[] bArr1 = bString1.split(" ");
		// �洢Entity
		String[] bArr2 = bString2.split(" ");
		bufferedReader.close();
		// System.out.println("b1 -- " + bArr1.length + " " + "b2 -- " +
		// bArr2.length);
		System.out.println("resourceTimeInfoSize ---" + resourceTimeInfo.size());
		for (int i = 0; i < bArr1.length; i++) {
			System.out.println("entity id is " + bArr2[i] + " class id is " + bArr1[i]);
			int entityId = Integer.parseInt(bArr2[i]);
			int classId = Integer.parseInt(bArr1[i]);
			// ������ʱ���ǩ��Entity set�в����Ƿ��д�entity id��ʱ���ǩ
			ResourceInfo resourceInfo = resourceTimeInfo.get(entityId);
			if (classTimeInfo.containsKey(classId)) {
				/* System.out.println(resourceInfo.); */
				if (resourceInfo != null) {
					// ����Դ��Ϣ�������
					resourceInfo.setType(classId);
					classTimeInfo.get(classId).add(resourceInfo);
				}
			} else {
				ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
				if (resourceInfo != null) {
					list.add(resourceInfo);
					classTimeInfo.put(classId, list);
				} else {
					classTimeInfo.put(classId, list);
				}
			}
		}
		// �洢�ϲ����<class, <p1 , timeSpan1>, <p2, timeSpan2>>
		HashMap<Integer, ResourceInfo> classTimeLabels = new HashMap<>();
		for (Entry<Integer, ArrayList<ResourceInfo>> entry : classTimeInfo.entrySet()) {
			int classId = entry.getKey();
			// ��class����ʱ����Ϣ��ǩ
			ResourceInfo aclassInfo = new ResourceInfo(classId);
			HashMap<String, HashSet<TimeSpan>> classtimePair = new HashMap<>();
			Iterator<ResourceInfo> iterator = entry.getValue().iterator();
			// ������class���е�ʱ���ǩ
			while (iterator.hasNext()) {
				// ��class��¼������һ��ʱ���ǩ
				ResourceInfo resourceInfo = iterator.next();
				HashMap<String, HashSet<TimeSpan>> pTPairs = resourceInfo.getPredicateTimePair();
				// ������ʱ���ǩ������pt��
				for (Entry<String, HashSet<TimeSpan>> entry2 : pTPairs.entrySet()) {
					String property = entry2.getKey();
					HashSet<TimeSpan> resourceTimeSpans = entry2.getValue();
					if (classtimePair.containsKey(property)) {
						for (TimeSpan span : resourceTimeSpans) {
							classtimePair.get(property).add(span);
						}
						// classtimePair.get(property).add
					} else {
						classtimePair.put(entry2.getKey(), entry2.getValue());
					}
				}
			}
			aclassInfo.setTimeInfoPair(classtimePair);
			classTimeLabels.put(classId, aclassInfo);
		}
		String dstFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\labelclasstest1.txt";
		FileWriter fileWriter = new FileWriter(dstFile);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (Entry<Integer, ResourceInfo> entry : classTimeLabels.entrySet()) {
			bufferedWriter.write(entry.getKey() + " ");
			ResourceInfo classInfo = entry.getValue();
			bufferedWriter.write(classInfo.toString());
			bufferedWriter.newLine();
			bufferedWriter.flush();
		}
		bufferedWriter.close();
	}

	public static HashMap<Integer, ResourceInfo> getResourceTimeInfo() {
		HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();
		ResourceInfo reource1 = new ResourceInfo(1);
		HashMap<String, HashSet<TimeSpan>> pTPairs1 = new HashMap<>();
		HashSet<TimeSpan> span1 = new HashSet<>();
		span1.add(new TimeSpan("2011", "2011"));
		span1.add(new TimeSpan("2012", "2012"));
		pTPairs1.put("has", span1);
		HashSet<TimeSpan> span2 = new HashSet<>();
		span2.add(new TimeSpan("2013", "2013"));
		span2.add(new TimeSpan("2012", "2012"));
		pTPairs1.put("date", span2);
		reource1.setTimeInfoPair(pTPairs1);
		resourceTimeInfo.put(1, reource1);
		return resourceTimeInfo;
	}

	public static void main(String[] args) throws IOException {
		long t1 = System.currentTimeMillis();
		Dataset dataset = new Dataset("jdbc:virtuoso://localhost:1111", "http://LDEA/SWCC.org", "dba", "dba");
		resourceTimeInfo = LabelResourceWithTimeTest.timeExtraction(dataset, "SWCC-Class-ExtractionEstimation4",
				"C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
		System.out.println("resourceTimeInfoSize in Main ---" + resourceTimeInfo.size());
		getClassTimeInformation("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
		long t2 = System.currentTimeMillis();
		double timeCost = (t2 - t1) / 1000.0;
		System.out.println("End of main~~~~~~time cost " + timeCost + "s");
	}
}
