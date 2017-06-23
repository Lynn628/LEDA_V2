package com.seu.Idea.cluster;

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
	 * º∆À„Õº÷–µ„µƒPageRank
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
	 * “¿æ›score÷µ…˝–Ú≈≈–Úentry
	 * 
	 * @param socreMap
	 * @throws IOException
	 */
	public static ArrayList<Entry<Integer, Double>> sortedRank(Map<Integer, Double> socreMap) throws IOException {
		ArrayList<Entry<Integer, Double>> entryList = new ArrayList<>(socreMap.entrySet());
		System.out.println("whether list is empty " + entryList.isEmpty());
		Collections.sort(entryList, new Comparator<Entry<Integer, Double>>() {
			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				// Ωµ–Ú≈≈–Ú
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		for (Entry<Integer, Double> item : entryList) {
			System.out.println("vertex " + item.getKey() + "---> " + item.getValue());
			//bufferedWriter.write("vertex " + item.getKey() + "---> " + item.getValue());
			//bufferedWriter.newLine();
		}
		return entryList;
	}
	
	
	public static  ArrayList<Entry<Integer, Double>> getSortedDegree(Graph<Integer, DefaultEdge> ingraph) throws IOException{
		graph = ingraph;
		return sortedRank(calcPageRank(graph));
	}
}
