package com.seu.ldea.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class PreProcessRDF {
	/**
	 * 
	 * @param filePath
	 *            ��Ҫ������ļ�·��
	 * @param dstName
	 *            Ŀ���ļ���
	 * @return
	 * @throws IOException
	 */
	public static String PreProcessRDFFile(String filePath, String dstName) throws IOException {
		
		/*FileReader fileReader = new FileReader(new File(filePath));
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		int index = filePath.lastIndexOf(".");
		String fileType = filePath.substring(index);
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\PreProcessedFile\\"
				+ dstName + fileType;
		File dstFile = new File(path);
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dstFile));
		String line = "";
		try {
			while ((line = bufferedReader.readLine()) != null) {
				String newline = RDFLineProcess(line);
				bufferedWriter.write(newline);
				bufferedWriter.newLine();
			}*/
		InputStreamReader read = new InputStreamReader(new FileInputStream(filePath),"UTF-8");
		BufferedReader bufferedReader = new BufferedReader(read);
		int index = filePath.lastIndexOf(".");
		String fileType = filePath.substring(index);
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\PreProcessedFile\\"
				+ dstName + fileType;
		File dstFile = new File(path);
	    OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(dstFile),"UTF-8");
		BufferedWriter bufferedWriter = new BufferedWriter(write);
		String line = "";
		try {
			while ((line = bufferedReader.readLine()) != null) {
				String newline = RDFLineProcess(line);
				bufferedWriter.write(newline);
				bufferedWriter.newLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bufferedReader.close();
		bufferedWriter.close();
		return path;
	}

	public static String PreProcessRDFFile3(String filePath, String dstName) throws IOException {
		// Scanner scanner = new Scanner(System.in);
		// String filePath = scanner.nextLine();
		/*
		 * System.out.println("Input dst fileName"); String dstName =
		 * scanner.nextLine(); scanner.close();
		 */
		FileReader fileReader = new FileReader(new File(filePath));
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		int index = filePath.lastIndexOf(".");
		String fileType = filePath.substring(index);
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\DataSet\\BTChallenge2014\\Prepocessed\\"
				+ dstName + fileType;
		File dstFile = new File(path);
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dstFile));
		String line = "";
		try {
			while ((line = bufferedReader.readLine()) != null) {
				// String content = line.matches(regex)
				if (line.contains("|")) {
					System.out.println(line);
					line = line.replace("|", "-");
					System.out.println("***" + line);
					bufferedWriter.write(line);
					bufferedWriter.newLine();
				} else {
					bufferedWriter.write(line);
					bufferedWriter.newLine();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bufferedReader.close();
		bufferedWriter.close();
		return path;
	}


	/**
	 * �����ļ���ÿһ��Jena�޷�ʶ���URI�ַ� ����URI�а��������<
	 * 
	 * @param input
	 */
	public static String RDFLineProcess(String input) {
		// String arr[] = input.split("<");
		char[] inputSeq = input.toCharArray();
		int left = 0;
		int right = 0;
		for (int i = 0; i < inputSeq.length; i++) {
			char alpha = inputSeq[i];

			if (alpha == '|') {
				inputSeq[i] = '-';
			}
			// ����URI�еĶ����<
			// ���������ţ�left��һ
			if (alpha == '<') {
				left++;
				if (left > 1) {
					// ���������������ϵ�<���滻�ַ�
					inputSeq[i] = '-';
					left--;
				}
				// ���������ţ�left��һ
			} else if (alpha == '>') {
				left--;
			}
		}
		return String.valueOf(inputSeq);

	}

	// <http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=DESCRIBE+<http://dbpedia.org/resource/Berlin>
	// <http://dbpedia.org/page/Berlin> .
	// C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\BTChallenge2014\data0.nq
	// <http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=DESCRIBE+<http://dbpedia.org/resource/Berlin>
	public static void main(String[] args) throws IOException {
		/*
		 * Scanner scanner = new Scanner(System.in); String path =
		 * scanner.nextLine(); String dst = scanner.nextLine(); scanner.close();
		 * PreProcessRDFFile(path, dst);
		 */
		RDFLineProcess(new String(
				"<http://dbpedia.org/page/Berlin> <http://www.w3.org/1999/xhtml/vocab#alternate> <http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=DESCRIBE+<http://dbpedia.org/resource/Berlin> <http://dbpedia.org/page/Berlin>."));
		
		// String("<http://ec.europa.eu/eurostat/ramon/rdfdata/estat-legis/30900>
		// <http://purl.org/dc/terms/title> \"Commission Decision No 2002/750/EC of 10 September 2002 on the continued application of areal survey and remote sensing techniques to the agricultural statistics for 2002-2003\" <http://ec.europa.eu/eurostat/ramon/rdfdata/estat-legis.rdf> ."));
	}
}
