package de.ozml.hsqldiffextract;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import de.ozml.hsqldiffextract.entity.Row;
import de.ozml.hsqldiffextract.util.RowUtil;

/**
 * The class offers methods to determine changes between table row state.
 */
public class DiffProcessor {

	private String tableName;
	private String outputDir;
	private BufferedWriter writer;

	public DiffProcessor(String tableName, String outputDir){
		this.tableName = tableName;
		this.outputDir = outputDir;
	}

	public String getTableName() {
		return tableName;
	}

	public String getOutputDir() {
		return outputDir;
	}

	/**
	 * Determines new created, updated and deleted rows between the two states given in the specified maps of table
	 * rows. Corresponding sql statements are written to a automatically generated output file within the output 
	 * directory. The name of the file corresponds to the table name passed to the constructor.
	 * @param originalRows
	 * @param changedRows
	 */
	public void process(Map<String, Row> originalRows, Map<String, Row> changedRows){
		try{
			// Check for new created entries
			checkCreated(originalRows, changedRows);

			// Check for updated entries
			checkUpdated(originalRows, changedRows);

			// Check for deleted entries
			checkDeleted(originalRows, changedRows);
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			closeWriter();
		}
	}

	/**
	 * Determines new rows and prints a corresponding sql line to the output.
	 * @param originalRows
	 * @param changedRows
	 * @throws IOException
	 */
	private void checkCreated(Map<String, Row> originalRows, Map<String, Row> changedRows)
			throws IOException {
		for (Row cRow : changedRows.values()) {
			if(!originalRows.containsKey(RowUtil.generateIndexKey(cRow.getPrimaryKeyValue()))){
				openWriter().write(buildInsertQueryString(cRow));
				openWriter().newLine();
			}
		}
	}

	/**
	 * Determines updated rows and prints a corresponding sql line to the output.
	 * @param originalRows
	 * @param changedRows
	 * @throws IOException
	 */
	private void checkUpdated(Map<String, Row> originalRows, Map<String, Row> changedRows)
			throws IOException {
		for (Row oRow : originalRows.values()) {
			Row cRow = changedRows.get(RowUtil.generateIndexKey(oRow.getPrimaryKeyValue()));

			if(cRow != null && !oRow.equals(cRow)){
				openWriter().write(buildUpdateQueryString(oRow, cRow));
				openWriter().newLine();
			}
		}
	}

	/**
	 * Determines deleted rows and prints a corresponding sql line to the output.
	 * @param originalRows
	 * @param changedRows
	 * @throws IOException
	 */
	private void checkDeleted(Map<String, Row> originalRows, Map<String, Row> changedRows)
			throws IOException {
		for (Row oRow : originalRows.values()) {
			Row cRow = changedRows.get(RowUtil.generateIndexKey(oRow.getPrimaryKeyValue()));

			if(cRow == null){
				openWriter().write(buildDeleteQueryString(oRow));
				openWriter().newLine();
			}
		}
	}

	/**
	 * Builds a sql insert query string for the row.
	 * @param row
	 * @return
	 */
	private static String buildInsertQueryString(Row row){
		String query = "INSERT INTO " + row.getTable().getName();
		query += " (" + String.join(", ", row.getTable().getColumnNames())+ ")";
		query += " VALUES (" + String.join(", ", row.getValue()) + ");";

		return query;
	}

	/**
	 * Builds a sql update query string corresponding to the differing between
	 * columns of the two specified rows.
	 * @param originalRow
	 * @param changedRow
	 * @return
	 */
	private static String buildUpdateQueryString(Row originalRow, Row changedRow){
		String[] columnNames = changedRow.getTable().getColumnNames();

		String query = "UPDATE " + changedRow.getTable().getName() + " SET";

		String updateList = "";
		for(int i = 0; i < columnNames.length; i++){
			if(!originalRow.getValue()[i].equals(changedRow.getValue()[i])){
				updateList += (updateList.isEmpty() ? " " : ", ") + columnNames[i] + "=" + changedRow.getValue()[i];

			}
		}
		query += updateList + " WHERE";

		int[] pkIndices = changedRow.getTable().getPrimaryKeyIndices();
		for(int i = 0; i < pkIndices.length; i++){
			query += (i == 0 ? " " : " AND ") + columnNames[pkIndices[i]] + " = " + changedRow.getValue()[i];
		}
		query += ";";

		return  query;
	}

	/**
	 * Builds a sql delete query string for the row.
	 * @param row
	 * @return
	 */
	private static String buildDeleteQueryString(Row row){
		String[] columnNames = row.getTable().getColumnNames();

		String query = "DELETE FROM " + row.getTable().getName() + " WHERE";

		int[] pkIndices = row.getTable().getPrimaryKeyIndices();
		for(int i = 0; i < pkIndices.length; i++){
			query += (i == 0 ? " " : " AND ") + columnNames[pkIndices[i]] + " = " + row.getValue()[i];
		}
		query += ";";

		return query;
	}

	/**
	 * Opens the inner writer if not already existent and returns the instance.
	 * @return
	 * @throws IOException
	 */
	private BufferedWriter openWriter() throws IOException {
		if(writer == null){
			writer = new BufferedWriter(new FileWriter(outputDir + "\\" + tableName + ".txt"));
		}
		return writer;
	}

	/**
	 * Closes the inner writer instance if open and existent.
	 */
	private void closeWriter(){
		try{
			if(writer != null){ 
				writer.close();
			}
		} catch(Exception e){

		}
	}

}