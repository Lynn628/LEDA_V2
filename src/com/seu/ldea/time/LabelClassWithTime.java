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
 * 汇总实体的时间信息，构建class的时间标签
 * 
 * @author Lynn 思路，words中找到type的id，依据id，读取对应的col和row文件，找到具有时间信息的资源所属的类别，
 *         汇总同一时间属性下面的时间
 */
public class LabelClassWithTime {
	// 每个资源的id以及携带的时间信息
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();
	// class id和Resource id 的映射
	public static HashMap<Integer, ArrayList<ResourceInfo>> classTimeInfo = new HashMap<>();

	public static void getClassTimeInformation(String dir) throws IOException {

		String wordsFile = dir + "\\words";
		FileReader fileReader = new FileReader(wordsFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		// type的uri编号
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
		// 宾语文件
		String colFile = dir + "\\" + num + "-cols";
		// 主语文件
		String rowFile = dir + "\\" + num + "-rows";
		FileReader fr1 = new FileReader(colFile);
		FileReader fr2 = new FileReader(rowFile);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		String bString1 = br1.readLine();
		String bString2 = br2.readLine();
		br1.close();
		br2.close();
		// 存储类
		String[] bArr1 = bString1.split(" ");
		// 存储Entity
		String[] bArr2 = bString2.split(" ");
		bufferedReader.close();
		// System.out.println("b1 -- " + bArr1.length + " " + "b2 -- " +
		// bArr2.length);
		System.out.println("resourceTimeInfoSize ---" + resourceTimeInfo.size());
		for (int i = 0; i < bArr1.length; i++) {
			System.out.println("entity id is " + bArr2[i] + " class id is " + bArr1[i]);
			int entityId = Integer.parseInt(bArr2[i]);
			int classId = Integer.parseInt(bArr1[i]);
			// 在贴有时间标签的Entity set中查找是否有此entity id的时间标签
			ResourceInfo resourceInfo = resourceTimeInfo.get(entityId);
			if (classTimeInfo.containsKey(classId)) {
				/* System.out.println(resourceInfo.); */
				if (resourceInfo != null) {
					// 给资源信息标上类别
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
		// 存储合并后的<class, <p1 , timeSpan1>, <p2, timeSpan2>>
		HashMap<Integer, ResourceInfo> classTimeLabels = new HashMap<>();
		for (Entry<Integer, ArrayList<ResourceInfo>> entry : classTimeInfo.entrySet()) {
			int classId = entry.getKey();
			// 给class创建时间信息标签
			ResourceInfo aclassInfo = new ResourceInfo(classId);
			HashMap<String, HashSet<TimeSpan>> classtimePair = new HashMap<>();
			Iterator<ResourceInfo> iterator = entry.getValue().iterator();
			// 遍历此class所有的时间标签
			while (iterator.hasNext()) {
				// 此class收录的其中一个时间标签
				ResourceInfo resourceInfo = iterator.next();
				HashMap<String, HashSet<TimeSpan>> pTPairs = resourceInfo.getPredicateTimePair();
				// 遍历此时间标签的所有pt对
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
