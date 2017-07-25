package com.seu.ldea.test;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.specifics.DirectedSpecifics;



public class JGraptTest {
   public static void main(String[] args){
	  //Graph<Integer, DefaultEdge> testGraph = createGraph();
	   System.out.println(1/(2 * 1.0));
	 /* Set<DefaultEdge> edgeSet =   testGraph.edgesOf(1);
	  for(DefaultEdge edge: edgeSet){
		 System.out.println(testGraph.getEdgeSource(edge));
		  System.out.println(edge.toString());
	  }*/
	  
     /* PageRank<Integer, DefaultEdge> gPageRank = new PageRank<>(testGraph);
      Map<Integer, Double> rank = gPageRank.getScores();
      for(Entry<Integer, Double> item : rank.entrySet()){
    	  System.out.println(item.getKey() + "----> " + item.getValue());
   }*/
   }

   public static Graph<Integer, DefaultEdge> createGraph(){
	   AbstractBaseGraph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		   graph.addVertex(1);
		   graph.addVertex(2);
		   graph.addVertex(3);
		   graph.addVertex(4);
		   
	       graph.addEdge(1, 2);
		   graph.addEdge(3, 2);
		   graph.addEdge(4, 1);
		   DirectedSpecifics<Integer, DefaultEdge> specificGraph = new DirectedSpecifics<Integer, DefaultEdge>(graph);  
		   System.out.println(specificGraph.degreeOf(1));
		   return graph;
      }
   }

