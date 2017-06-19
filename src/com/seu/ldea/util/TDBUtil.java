package com.seu.ldea.util;


import java.io.IOException;
import java.sql.SQLException;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;



/**
 * 创建TDB数据库，将链接数据存入数据库中，获取Model，读取Model中的三元组进行解析
 * 
 * @author Lynn
 *
 */
public class TDBUtil {
	public static Dataset ds = TDBFactory.createDataset("E:\\DBpediaTDB");// 建立了一个test的TDB，如果存储test的TDB，则表示使用这个TDB
	public static Model model = null;
	/**
	 * 初始化TDB数据库，向数据库装载链接数据文件
	 */
	public static void initTDB() {
		model = ds.getDefaultModel();// 这里使用TDB的默认Model
		model.read("E:\\DataSet\\Dbpedia2016\\mappingbased_objects_en.ttl", "TTL");// 读取RDF文件到指定的model里面
		// model.commit();//这里类似于数据库的commint，意思是把现在的操作立刻提交
		//model.close();// 结束使用的时候，一定要对Model和Dataset进行关闭

	}
    
	public static void initTDB(String filePath, String fileType ){
		model = ds.getDefaultModel();
		model.read(filePath, fileType);// 读取RDF文件到指定的model里面
		// model.commit();//这里类似于数据库的commint，意思是把现在的操作立刻提交
		model.close();// 结束使用的时候，一定要对Model和Dataset进行关闭
	}
	
	public static void searchTDB() throws IOException, SQLException {
		// 初始化连接池
		//DBUtil2.initDB();
		// DBUtil2.clearTable();
		 ds.begin(ReadWrite.READ) ;
		 String qs1 = "SELECT * where { ?s ?p <http://dbpedia.org/resource/Prize_Ampère> }" ;

		 try(QueryExecution qExec = QueryExecutionFactory.create(qs1, ds)) {
		     ResultSet rs = qExec.execSelect() ;
		     ResultSetFormatter.out(rs) ;
		 }
		//writeToDB();

	}
	
	public static Model getTDBModel(){
		/*if(model == null){
			initTDB();
			System.out.println("inital tdb and get Model");
			return model;
		}else{
			System.out.println("directly get Model");
			return model;
		}*/
		return ds.getDefaultModel();
	}
	
	
}
