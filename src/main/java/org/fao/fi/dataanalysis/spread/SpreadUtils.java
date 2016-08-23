package org.fao.fi.dataanalysis.spread;

import java.io.FileWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gcube.contentmanagement.lexicalmatcher.utils.AnalysisLogger;
import org.gcube.dataanalysis.ecoengine.utils.DatabaseFactory;
import org.hibernate.SessionFactory;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * Some utils for SPREAD
 * (implemented while implementing SPREAD, they might be moved to
 *  other classes such as gCube DatabaseUtils)
 * 
 * @author Emmanuel Blondel
 *
 */
public final class SpreadUtils {

	public static void createLocalFileFromRemoteTable(String filePath,
			String tablename, String delimiter, boolean hasHeader,
			String username, String password, String databaseurl)
			throws Exception {

		Connection conn = DatabaseFactory.getDBConnection(
				"org.postgresql.Driver", username, password, databaseurl);
		CopyManager copyManager = new CopyManager((BaseConnection) conn);
		FileWriter fw = new FileWriter(filePath);
		String hasHeaderS = (hasHeader)? "CSV HEADER" : "CSV";
		copyManager.copyOut(
				String.format(
				"COPY %s TO STDOUT WITH DELIMITER '%s' NULL AS '' %s",
				tablename, delimiter, hasHeaderS), fw);
		conn.close();
		fw.close();
	}
	
	public static Map<String,String> getSchemaDescription(String tablename, SessionFactory dbconnection){
		List<Object> fieldNames = DatabaseFactory.executeSQLQuery(
			String.format("select column_name from information_schema.columns "+
						  "where table_name = '%s'", tablename),
			dbconnection);
		
		List<Object> fieldTypes = DatabaseFactory.executeSQLQuery(
				String.format("select data_type from information_schema.columns "+
							  "where table_name = '%s'", tablename),
				dbconnection);
		
		Map<String,String> output = null;
		if(fieldNames.size() > 0){
			output = new HashMap<String,String>();
		}
		if(output != null){
			for(int i=0; i<fieldNames.size();i++){
				AnalysisLogger.getLogger().debug(((String) fieldNames.get(i)) +" | "+((String) fieldTypes.get(i)));
				output.put((String) fieldNames.get(i), (String) fieldTypes.get(i));
			}
		}
		return output;
	}
	
	public static String unquote(String str) {
		int length = str == null ? -1 : str.length();
		if (str == null || length == 0)
			return str;
		if (length > 1 && str.charAt(0) == '\"'
				&& str.charAt(length - 1) == '\"') {
			str = str.substring(1, length - 1);
		}
		return str;
	}

}
