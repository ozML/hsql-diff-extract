package de.ozml.hsqldiffextract.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ozml.hsqldiffextract.entity.Column;
import de.ozml.hsqldiffextract.entity.ColumnType;
import de.ozml.hsqldiffextract.entity.Table;

/**
 * Provides methods to build {@link Table} instances out of sql create table
 * query definitions.
 */
public class TableParser {

	private static final String TABLE_PATTERN = "^CREATE (MEMORY )?TABLE (PUBLIC\\.)?(\\w+)\\s?\\((.*)\\)$";
	private static final String COLUMN_PATTERN = "^\\s*(\\w+)\\s+.*,?$";

	private static final String UNIQUE_INDEX_PATTERN = "^CREATE UNIQUE INDEX\\s?(\\w+) ON (PUBLIC\\.)?(\\w+)\\s?\\((\\w+)\\)$";

	private static final String PRIMARY_KEY_CONSTRAINT_PATTERN = "^(CONSTRAINT\\s+(\\w+)\\s+)?PRIMARY KEY\\s?\\((\\w+\\s*(,\\s*\\w+\\s*)*)\\)$";
	private static final String FOREIGN_KEY_CONSTRAINT_PATTERN = "^(CONSTRAINT\\s+(\\w+)\\s+)?FOREIGN KEY\\s?\\((\\w+\\s*(,\\s*\\w+\\s*)*)\\)\\s?REFERENCES PUBLIC\\.(\\w+)\\s?\\((\\w+\\s*(,\\s*\\w+\\s*)*)\\)$";
	private static final String UNIQUE_CONSTRAINT_PATTERN = "^(CONSTRAINT\\s+(\\w+)\\s+)?UNIQUE\\s?\\((\\w+\\s*(,\\s*\\w+\\s*)*)\\)$";

	private static final int TABLE_GROUP_NAME = 3;
	private static final int TABLE_GROUP_CONTENT = 4;
	private static final int UNIQUE_INDEX_GROUP_TABLE = 3;
	private static final int UNIQUE_INDEX_GROUP_COLUMN = 4;
	private static final int COLUMN_GROUP_NAME = 1;
	private static final int PRIMARY_KEY_CONSTRAINT_GROUP_COLUMNS = 3;
	private static final int UNIQUE_CONSTRAINT_GROUP_COLUMNS = 3;

	/**
	 * Builds a map of tables from the definitions contained in the
	 * specified sql file.
	 * @param path path to sql file
	 * @return
	 */
	public static List<Table> readTablesFromFile(String path) {
		List<Table> tables = new ArrayList<>();
		Map<String, Set<String>> uniqueIndices = new HashMap<>();

		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();

			// Build table list
			while(line != null){
				if(isTableDefinition(line)){
					Table table = extractTable(line);
					if(table != null){
						tables.add(table);
					}
				}
				else if(isUniqueIndex(line)){
					Pattern pattern = Pattern.compile(UNIQUE_INDEX_PATTERN);
					Matcher matcher = pattern.matcher(line);
					if(matcher.find()){
						String tableNameKey = matcher.group(UNIQUE_INDEX_GROUP_TABLE);
						String columnNameKey = matcher.group(UNIQUE_INDEX_GROUP_COLUMN);

						if(!uniqueIndices.containsKey(tableNameKey)){
							uniqueIndices.put(tableNameKey, new HashSet<>());
						}

						Set<String> tableUniqueIndices = uniqueIndices.get(tableNameKey);
						tableUniqueIndices.add(columnNameKey);
					}
				}

				line = reader.readLine();
			}

			// Set unique property of columns from unique index list
			if(!tables.isEmpty() && !uniqueIndices.isEmpty()){
				for (Table table : tables) {
					Set<String> tableUniqueIndices = uniqueIndices.get(table.getName());
					if(tableUniqueIndices != null){
						for (Column column : table.getColumns()) {
							if(tableUniqueIndices.contains(column.getName())){
								column.setUnique(true);
							}
						}
					}
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			try{if(reader != null) reader.close();} catch(Exception e){}
		}

		return tables;
	}

	private static boolean isTableDefinition(String s){
		return s.matches(TABLE_PATTERN);
	}
	
	private static boolean isColumnDefinition(String s){
		return s.matches(COLUMN_PATTERN);
	}

	private static boolean isPrimaryKeyConstraint(String s){
		return s.matches(PRIMARY_KEY_CONSTRAINT_PATTERN);
	}

	private static boolean isUniqueConstraint(String s){
		return s.matches(UNIQUE_CONSTRAINT_PATTERN);
	}

	private static boolean isForeignKeyConstraint(String s){
		return s.matches(FOREIGN_KEY_CONSTRAINT_PATTERN);
	}

	private static boolean isUniqueIndex(String s){
		return s.matches(UNIQUE_INDEX_PATTERN);
	}

	/**
	 * Builds a table from the table definition string.
	 * @param def sql table definition
	 * @return
	 */
	private static Table extractTable(String def){
		Table table = null;
		Pattern pattern = Pattern.compile(TABLE_PATTERN);
		Matcher matcher = pattern.matcher(def);
		if(matcher.find()){
			String tableName = matcher.group(TABLE_GROUP_NAME);
			String columnDefs = matcher.group(TABLE_GROUP_CONTENT);
			List<Column> tableColumns = extractColumns(columnDefs);

			 table = new Table(tableName, tableColumns.toArray(new Column[tableColumns.size()]));
		}

		return table;
	}

	/**
	 * Builds Columns from the columns definition string.
	 * @param defs sql column definition list
	 * @return
	 */
	private static List<Column> extractColumns(String defs){
		List<Column> columns = new ArrayList<>();
		String[] defParts = splitColumnsDef(defs);
		for(int i = 0; i < defParts.length; i++){
			String def = defParts[i].trim();

			if(def.isEmpty()){
				continue;
			}

			if(isPrimaryKeyConstraint(def)){
				setColumnPrimaryFromConstraint(def, columns);
			} else if(isUniqueConstraint(def)){
				setColumnUniqueFromConstraint(def, columns);
			} else if(isForeignKeyConstraint(def)){
				// TODO implement foreign key?
			} else if(isColumnDefinition(def)){
				addColumn(def, columns);
			}
		}

		return columns;
	}

	/**
	 * Splits the column definition string to an array of strings. The string is split by ','
	 * as token, preserving those encapsulated by curved brackets like in constraint definitions.
	 * @param defs sql column definition list
	 * @return
	 */
	private static String[] splitColumnsDef(String defs){
		List<String> columns = new ArrayList<>();

		int index = defs.indexOf(",");
		while(index != -1){
			// opening\ closing bracket before index
			int obb = defs.indexOf("(");
			int cbb = defs.indexOf(")");
			boolean isEnclosedBefore =  obb != -1 && cbb != -1 && obb < index && (cbb < index && cbb < obb || cbb > index);

			// closing\ opening bracket after index
			int cba = defs.indexOf(")", index + 1);
			int oba = defs.indexOf("(", index + 1);
			boolean isEnclosedAfter = cba != -1 && oba != -1 && oba > cba;
	
			if(!(isEnclosedBefore && isEnclosedAfter)){
				columns.add(defs.substring(0, index));
				defs = defs.substring(index + 1, defs.length());

				index = defs.indexOf(",");
			}
			else{
				index = defs.indexOf(",", index + 1);
			}
		}

		if(!defs.isEmpty()){
			columns.add(defs);
		}

		return columns.toArray(new String[columns.size()]);
	}

	/**
	 * Initializes a new column from the specified definition string and 
	 * adds it to the columns list.
	 * @param def sql column definition
	 * @param columns list of columns
	 */
	private static void addColumn(String def, List<Column> columns){
		Pattern pattern = Pattern.compile(COLUMN_PATTERN);
		Matcher matcher = pattern.matcher(def);
		if(matcher.find()){
			String columnName = matcher.group(COLUMN_GROUP_NAME);
			ColumnType type = extractType(def);
			boolean isPrimaryKey = def.contains("PRIMARY KEY");
			boolean isUnique = isPrimaryKey || def.contains("UNIQUE");
			boolean isNullable = !def.contains("NOT NULL");

			Column column = new Column(columnName, type, isPrimaryKey, isUnique, isNullable);
			columns.add(column);
		}
	}

	/**
	 * Iterates through the list of columns and marks all which are contained in
	 * the specified definition as primary keys and additionally as unique.
	 * @param def primary key constraint definition
	 * @param columns list of columns
	 */
	private static void setColumnPrimaryFromConstraint(String def, List<Column> columns){
		Pattern pattern = Pattern.compile(PRIMARY_KEY_CONSTRAINT_PATTERN);
		Matcher matcher = pattern.matcher(def);
		if(matcher.find()){
			String primaryKeys = matcher.group(PRIMARY_KEY_CONSTRAINT_GROUP_COLUMNS);
			String keyValuePattern = "(" + String.join("|", primaryKeys.split("\\s*,\\s*")) + ")";
			for (Column column : columns) {
				if(column.getName().matches(keyValuePattern)){
					column.setPrimaryKey(true);
					column.setUnique(true);
				}
			}
		}
	}

	/**
	 * Iterates through the list of columns and marks all which are contained in
	 * the specified definition as unique.
	 * @param def unique constraint definition
	 * @param columns list of columns
	 */
	private static void setColumnUniqueFromConstraint(String def, List<Column> columns){
		Pattern pattern = Pattern.compile(UNIQUE_CONSTRAINT_PATTERN);
		Matcher matcher = pattern.matcher(def);
		if(matcher.find()){
			String uniques = matcher.group(UNIQUE_CONSTRAINT_GROUP_COLUMNS);
			String uniqueValuePattern = "(" + String.join("|", uniques.split("\\s*,\\s*")) + ")";
			for (Column column : columns) {
				if(column.getName().matches(uniqueValuePattern)){
					column.setUnique(true);
				}
			}
		}
	}

	/**
	 *  Returns the fitting {@link ColumnType} from the specified sql type.
	 * @param type sql type
	 * @return
	 */
	private static ColumnType extractType(String type){
		if(type.matches(".*(TINYINT|SMALLINT|INTEGER|BIGINT).*")){
			return ColumnType.Numeric;
		} else if(type.matches(".*(NUMERIC|DECIMAL|DOUBLE).*")){
			return ColumnType.Decimal;
		} else if(type.matches(".*(BIT|BITVARYING).*")){
			return ColumnType.Bit;
		} else if(type.matches(".*(CHAR|VARCHAR|CLOB).*")){
			return ColumnType.Character;
		} else if(type.matches(".*(BOOLEAN).*")){
			return ColumnType.Boolean;
		} else if(type.matches(".*(DATE|TIME|TIMESTAMP).*")){
			return ColumnType.Date;
		} else if(type.matches(".*(BINARY|VARBINARY|BLOB).*")){
			return ColumnType.Binary;
		}

		return null;
	}

}