package com.seu.ldea.util;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.dbcp2.BasicDataSource;

public class MySqlUtil {
	public static BasicDataSource pool = DBPool.initDataSource();
	// private static Connection conn;
	private static int rNum = 0;
	private static int pNum = 1;
	public static Connection conn = null;

	public static void initDB() {
		try {
			conn = pool.getConnection();

		} catch (Exception e) {
			// TODO: handle exception

			e.printStackTrace();
		}
	}

	/**
	 *  向相应表中插入键值对
	 * @param input
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static boolean insertMap(String input, String type) throws IOException, SQLException {
		PreparedStatement pstmt = null;
		int status = 0;
		// Connection conn = pool.getConnection();
		if (type.equals("resource") || type.equals("predicate")) {
			try {
				switch (type) {
				case "resource":
					pstmt = conn.prepareStatement("insert into resource_id values(?, ?)");
					pstmt.setInt(1, rNum);
					pstmt.setString(2, input);
					break;
				case "predicate":
					pstmt = conn.prepareStatement("insert into property_id values(?, ?)");
					pstmt.setInt(1, pNum);
					// System.out.println("pNum " + input + pNum );
					// System.out.println(input);
					pstmt.setString(2, input);
				default:
					break;
				}
				status = pstmt.executeUpdate();
				pstmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block

			}
		}
		if (status == 1 && type.equals("resource")) {
			// 将Entity写入entity-id文件中
			FileWriter fileWriter = new FileWriter("D:\\rescalInputFile\\Dbpedia\\entity-ids", true);
			fileWriter.write(input + "\n");
			fileWriter.close();
			rNum++;
			// System.out.println("rNum " + rNum);
			return true;
		} else if (status == 1 && type.equals("predicate")) {
			FileWriter fileWriter = new FileWriter("D:\\rescalInputFile\\Dbpedia\\words", true);
			fileWriter.write(input + "\n");
			fileWriter.close();
			pNum++;
			return true;
		}
		return false;

	}

	/*// 将记录插入triple表中
	public static void insertTriple(String subject, String predicate, String object) throws SQLException {
		// Connection conn = pool.getConnection();
		// Statement stmt = null;
		// 判断并获取对应resource或者predicate的Id
		int subId = getId(subject, "resource");
		int preId = getId(predicate, "predicate");
		int objId = getId(object, "resource");
		System.out.println(subId + "  " + preId + "  " + objId);
		if (!(subId == -1 || preId == -1 || objId == -1)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(
						"insert into triple(subId, preId, objId) values(" + subId + "," + preId + "," + objId + ")");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}*/

	/**
	 *  将记录插入triple表中，同时将三元组对应值写入col row文件中
	 * @param subject
	 * @param predicate
	 * @param object
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void insertTriple2(String subject, String predicate, String object) throws IOException, SQLException {
		// Connection conn = pool.getConnection();
		String colFileName, rowFileName;
		// Statement stmt = null;
		// 判断并获取对应resource或者predicate的Id
		int subId = getId(subject, "resource");
		int preId = getId(predicate, "predicate");
		int objId = getId(object, "resource");
		System.out.println(subId + "  " + preId + "  " + objId);
		if (!(subId == -1 || preId == -1 || objId == -1)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(
						"insert into triple(subId, preId, objId) values(" + subId + "," + preId + "," + objId + ")");
				stmt.close();
				// 将三元组写入对应的编号的文档当中
				colFileName = "D:\\rescalInputFile\\Dbpedia\\" + preId + "-cols";
				rowFileName = "D:\\rescalInputFile\\Dbpedia\\" + preId + "-rows";
				FileWriter fw1 = new FileWriter(colFileName, true);
				FileWriter fw2 = new FileWriter(rowFileName, true);
				fw1.write(subId + " ");
				fw2.write(objId + " ");
				fw1.close();
				fw2.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private static int getId(String input, String type) throws SQLException {
		PreparedStatement pstmt = null;
		try {
			switch (type) {
			case "resource":
				pstmt = conn.prepareStatement("select * from resource_id where resource=?");
				break;
			case "predicate":
				pstmt = conn.prepareStatement("select * from property_id where property=?");
				break;
			default:
				break;
			}
			pstmt.setString(1, input);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	// 清除表
	public static void clearTable(String tableName) throws SQLException {
		// Connection conn = pool.getConnection();
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("truncate table " + tableName);
			//stmt.executeUpdate("truncate table property_id");
			//stmt.executeUpdate("truncate table triple");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 关闭数据库
	public static void closeDB() throws SQLException {
		Connection conn = pool.getConnection();
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *  获取使用的最多的 top k 个Predicate,将top k的predicate的序列映射写入文件
	 * @param k
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<Integer> getTopPredicate(int k) throws IOException {
		int i = 0;
		HashMap<Integer, Integer> topPreMap = new HashMap<>();
		// 存储前k个predicate
		ArrayList<Integer> topPreList = new ArrayList<>();
		ResultSet resultSet = null;
		File outFile = new File("D://rescalInputFile//predicateMap");
		FileWriter fw = new FileWriter(outFile);
		BufferedWriter bufferedWriter = new BufferedWriter(fw);
		
		try (Statement stmt = conn.createStatement()) {
			resultSet = stmt.executeQuery("select preId,count(*) from triple group by preId order by count(*) desc");
			while (resultSet.next() && i < k) {
				topPreMap.put(resultSet.getInt(1), resultSet.getInt(2));
				topPreList.add(resultSet.getInt(1));
				bufferedWriter.write(i + 1 + "   " + resultSet.getInt(1));
				bufferedWriter.newLine();
				i++;
				System.out.println("i num is " + i);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
				bufferedWriter.flush();
				bufferedWriter.close();
				return topPreList;
	}

	/**
	 *  写top k predicate组成的col row 文件
	 * @param pList
	 * @throws IOException
	 */
	public static void makeSubInputFile(ArrayList<Integer> pList) throws IOException {
		String colFileName = null;
		String rowFileName = null;
		int num = 0;
		try {
			PreparedStatement pstmt = conn.prepareStatement("select * from triple where preId = ?");
			//id 标识第几个predicate
			for (int id = 1; id <= pList.size(); id++) {
				pstmt.setInt(1, pList.get(id-1));
				ResultSet resultSet = pstmt.executeQuery();
				colFileName = "D:\\rescalInputFile\\Dbpedia_Sub\\" + id + "-cols";
				rowFileName = "D:\\rescalInputFile\\Dbpedia_Sub\\" + id + "-rows";
				FileWriter fw1 = new FileWriter(colFileName, true);
				FileWriter fw2 = new FileWriter(rowFileName, true);
				while (resultSet.next()) {
					//判断是已存在此subId objId，不存在则写入数据库表中
					int subId = judgeResource(resultSet.getInt(2));
					int objId = judgeResource(resultSet.getInt(3));
					
					if(subId == -1){
						insertNewResourceMap(resultSet.getInt(2), num);
						subId = num;
						num++;
					}
					if(objId == -1){
						insertNewResourceMap(resultSet.getInt(3), num);
						objId = num;
						num++;
					}		
					fw1.write(subId + " ");
					fw2.write(objId + " ");
					
				}
				fw1.close();
				fw2.close();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("end of make file");
	}

	/**
	 * 判断数据表中是否存在此资源,若存在则返回id
	 * @param originId
	 * @return
	 */
	public static int judgeResource(int originId){
		ResultSet resultSet = null;
		try(PreparedStatement pstmt = conn.prepareStatement("select * from sub_resource_map where originId = ?")){
			pstmt.setInt(1, originId);
		    resultSet = pstmt.executeQuery();
		    if(resultSet.next())
				return resultSet.getInt(2);
		}catch (Exception e) {
			// TODO: handle exception
		}
           //没有此资源返回-1		
			return -1;
		}
		
	public static void insertNewResourceMap(int originId, int newId){
		try(PreparedStatement pstmt = conn.prepareStatement("insert into sub_resource_map values(?, ?)")) {
		    pstmt.setInt(1, originId);
			pstmt.setInt(2, newId);
			pstmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*public static void restoreResourceTable() throws IOException, SQLException{
		FileReader fileReader = new FileReader(new File("D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\Dbpedia\\entity-ids"));
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String data = null;
		int num = 0;
		conn.setAutoCommit(false);
		PreparedStatement preparedStatement = conn.prepareStatement("insert into resource_id values(?, ?)");
		String alt = null;
		while( (alt= bufferedReader.readLine()) != null){
			System.out.println(alt);
			System.out.println(num);
			    data = new String(alt.getBytes("latin1"));
				preparedStatement.setInt(1, num);
				preparedStatement.setString(2, data);
				num++;
				//preparedStatement.execute();
				preparedStatement.addBatch();
			
		}
		bufferedReader.close();
		preparedStatement.executeBatch();
	    conn.commit();
		conn.close();
	}*/
	
	public static void main(String[] args) throws IOException, SQLException {
		initDB();
		//restoreResourceTable();
		ArrayList<Integer > inputList = getTopPredicate(60);
		makeSubInputFile(inputList);
		
		// initDB();
		/*
		 * int id = getId("http://dbpedia.org/ontology/AdultActor", "resource");
		 * System.out.println(id); id =
		 * getId("http://www.w3.org/2000/01/rdf-schema#subClassOf",
		 * "predicate"); System.out.println(id);
		 */
		// clearTable();
		// getEntityId();
		// }
		// clearTable("rescaldb.resource_id");
		closeDB();

	}
}


