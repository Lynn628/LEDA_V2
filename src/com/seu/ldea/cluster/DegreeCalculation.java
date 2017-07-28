package com.seu.ldea.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultEdge;

public class DegreeCalculation {
	public static Graph<Integer, DefaultEdge> graph;

	/**
	 * 计算图中点的PageRank
	 * 
	 * @param graph
	 * @return
	 */
	public static Map<Integer, Double> calcPageRank(Graph<Integer, DefaultEdge> graph) {
		PageRank<Integer, DefaultEdge> pageRank = new PageRank<>(graph);
		for (Entry<Integer, Double> item : pageRank.getScores().entrySet()) {
			// System.out.println("vertex " + item.getKey() + "---> " + "score "
			// + item.getValue());
		}
		return pageRank.getScores();
	}

	/**
	 * 依据score值升序排序entry
	 * 
	 * @param socreMap
	 * @throws IOException
	 */
	public static ArrayList<Entry<Integer, Double>> sortedRank(Map<Integer, Double> socreMap) throws IOException {
		ArrayList<Entry<Integer, Double>> entryList = new ArrayList<>(socreMap.entrySet());
		// System.out.println("whether list is empty " + entryList.isEmpty());
		Collections.sort(entryList, new Comparator<Entry<Integer, Double>>() {
			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				// 降序排序
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		/*
		 * System.out.
		 * println("********************Page rank **********************"); for
		 * (Entry<Integer, Double> item : entryList) {
		 * System.out.println("vertex " + item.getKey() + "---> " +
		 * item.getValue()); //bufferedWriter.write("vertex " + item.getKey() +
		 * "---> " + item.getValue()); //bufferedWriter.newLine(); }
		 */
		return entryList;
	}

	public static ArrayList<Entry<Integer, Double>> getSortedDegree(Graph<Integer, DefaultEdge> ingraph, int type)
			throws IOException {
		graph = ingraph;
		// 如果type为1，则调用pagerank， 获取排好序的page rank list
		if (type == 1) {
			return sortedRank(calcPageRank(graph));
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		long t1 = System.currentTimeMillis();
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\LinkedMDB2";
		sortedRank(calcPageRank(GraphUtil.buildGraph(path)));
		long t2 = System.currentTimeMillis();
		System.out.println("Time cost -----> " + (t2 - t1) / 1000 + " s");

	}
}
