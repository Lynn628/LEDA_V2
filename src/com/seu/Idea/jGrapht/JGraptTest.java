package com.seu.Idea.jGrapht;

import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;



public class JGraptTest {
   public static void main(String[] args){
	  Graph<Integer, DefaultEdge> testGraph = createGraph();
      PageRank<Integer, DefaultEdge> gPageRank = new PageRank<>(testGraph);
      Map<Integer, Double> rank = gPageRank.getScores();
      for(Entry<Integer, Double> item : rank.entrySet()){
    	  System.out.println(item.getKey() + "----> " + item.getValue());
   }
   }

   public static Graph<Integer, DefaultEdge> createGraph(){
	   Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
	     
		   graph.addVertex(1);
		   graph.addVertex(2);
		   graph.addVertex(3);
	       graph.addEdge(1, 2);
		   graph.addEdge(3, 2);
		   return graph;
   }
   }

