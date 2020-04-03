package de.ozml.hsqldiffextract.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ozml.hsqldiffextract.entity.Row;
import de.ozml.hsqldiffextract.entity.Table;
import de.ozml.hsqldiffextract.util.RowUtil;

/**
 * Provides methods to extract {@link Row} instances associated with a {@link Table}
 * from sql insert query definitions.
 */
public class RowParser {

	/**
	 * Builds a list of rows for the given table from the data definitions 
	 * contained in the specified sql file. 
	 * @param table target table
	 * @param path path to sql file
	 * @return
	 */
	public static Map<String, Row> readRowsFromTable(Table table, String path){
		Map<String, Row> rows = new HashMap<>();

		String pattern = buildMatchPattern(table);
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();

			// Build row list
			while(line != null){
				if(line.matches(pattern)){
					Row row = extractRow(table, line, pattern);
					if(row != null){
						rows.put(RowUtil.generateIndexKey(row.getPrimaryKeyValue()), row);
					}
				}
				line = reader.readLine();
			}
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			try{if(reader != null) reader.close();} catch(Exception e){}
		}

		return rows;
	}

	/**
	 * Builds a row from the data definition string.
	 * @param table target table
	 * @param def sql data definition
	 * @param rowPattern data match pattern
	 * @return
	 */
	private static Row extractRow(Table table, String def, String rowPattern){
		Row row = null;
		String[] values = new String[table.getColumns().length];
		Pattern pattern = Pattern.compile(rowPattern);
		Matcher matcher = pattern.matcher(def);
		if(matcher.find()){
			String rowData = matcher.group(1);
			String[] rowDataParts = splitRowData(rowData);
			if(rowDataParts.length == values.length){
				for(int i = 0; i < rowDataParts.length; i++){
					values[i] = rowDataParts[i];
				}

				row = new Row(table, values);
			}
		}

		return row;
	}

	/**
	 * Returns a data match pattern for the specified table.
	 * @param table target table
	 * @return
	 */
	private static String buildMatchPattern(Table table){
		return "INSERT INTO " + table.getName() + " VALUES\\s*\\((.*)\\)";
	}

	/**
	 * Splits the data list string to an array of strings. The string is split by ','
	 * as token, preserving those encapsulated within the single quotes of sql text.
	 * @param data sql row data list
	 * @return
	 */
	private static String[] splitRowData(String data){
		List<String> rowData = new ArrayList<>();
		boolean splitLock = false;
		for(int i = 0; i < data.length(); i++){
			char currChar = data.charAt(i);
			if(currChar == ',' && !splitLock){
				rowData.add(data.substring(0, i));
				data = data.substring(i + 1, data.length());
				i = -1;
			} else if(currChar == '\''){
				splitLock = !splitLock;
			}
		}

		if(!data.isEmpty()){
			rowData.add(data);
		}

		return rowData.toArray(new String[rowData.size()]);
	}

}