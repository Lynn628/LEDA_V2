package com.seu.ldea.virtuoso;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

public class virtuosoTest {
	
   public static void main(String[] args){
	  // System.out.println("Begin");
	 String url = "jdbc:virtuoso://localhost:1111";
	 VirtGraph graph = new VirtGraph("http://LDEA/SWCC.org", url, "dba", "dba");
	 
	 Query sparql = QueryFactory.create("select ?s ?p ?o where {?s ?p ?o} limit 10");
       //System.out.println("test1");
	 VirtuosoQueryExecution virtuosoQueryExecution = VirtuosoQueryExecutionFactory.create(sparql,graph);
	// System.out.println("test2");
	 ResultSet resultSet = virtuosoQueryExecution.execSelect();
	 while(resultSet.hasNext()){
		// System.out.println("test3");
		 QuerySolution result = resultSet.nextSolution();
		 RDFNode sNode = result.get("s");
		 RDFNode pNode = result.get("p");
		 RDFNode oNode = result.get("o");
		 System.out.println( sNode + " " + sNode.isResource() + " ; " + pNode + " ; " + oNode + " " + oNode.isResource());		 
	    System.out.println("----");
	 }
	  
   }
}
