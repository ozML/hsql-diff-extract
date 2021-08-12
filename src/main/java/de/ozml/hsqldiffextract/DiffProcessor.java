package de.ozml.hsqldiffextract;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;

import de.ozml.hsqldiffextract.entity.Row;
import de.ozml.hsqldiffextract.common.Controlable;
import de.ozml.hsqldiffextract.common.RowSource;
import de.ozml.hsqldiffextract.common.RowUtil;
import de.ozml.hsqldiffextract.common.Tuple;

/**
 * The class offers methods to determine changes between table row state.
 */
public class DiffProcessor implements Controlable {

	private static final int STATE_READY = 0;
	private static final int STATE_CHECK_CREATED = 1;
	private static final int STATE_CHECK_UPDATED = 2;
	private static final int STATE_CHECK_DELETED = 3;
	private static final int STATE_FINISHED = 4;

	private static final int PART_SIZE = 5000;

	private String tableName;
	private String outputDir;
	private BufferedWriter writer;
	private Queue<Tuple<Row, Row>> queue;

	public DiffProcessor(String tableName, String outputDir){
		this.tableName = tableName;
		this.outputDir = outputDir;
	}

	/**
	 * Return the name of the contained table.
	 * @return
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Return the output directory path.
	 * @return
	 */
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
	public void process(RowSource originalRows, RowSource changedRows){
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
	private void checkCreated(RowSource originalRows, RowSource changedRows)
			throws IOException {
		for(int i = 0; i < changedRows.count(); i += PART_SIZE){
			for (Row cRow : changedRows.getPart(i, PART_SIZE)) {
				if(!originalRows.containsRow(RowUtil.genIndexKey(cRow.getPrimaryKey()))){
					openWriter().write(buildInsertQueryString(cRow));
					openWriter().newLine();
				}
			}
		}
	}

	/**
	 * Determines updated rows and prints a corresponding sql line to the output.
	 * @param originalRows
	 * @param changedRows
	 * @throws IOException
	 */
	private void checkUpdated(RowSource originalRows, RowSource changedRows)
			throws IOException {
		for(int i = 0; i < originalRows.count(); i += PART_SIZE){
			Map<String, Row> oPartMap = originalRows.getPartMap(i, PART_SIZE);
			Map<String, Row> cPartMap = changedRows.getRowsMap(oPartMap.keySet());
			
			for (Row oRow : oPartMap.values()) {
				String key = RowUtil.genIndexKey(oRow.getPrimaryKey());
				if(!cPartMap.containsKey(key)){
					continue;
				}
	
				Row cRow = cPartMap.get(key);
				if(cRow != null && !oRow.equals(cRow)){
					openWriter().write(buildUpdateQueryString(oRow, cRow));
					openWriter().newLine();
				}
			}
		}
	}

	/**
	 * Determines deleted rows and prints a corresponding sql line to the output.
	 * @param originalRows
	 * @param changedRows
	 * @throws IOException
	 */
	private void checkDeleted(RowSource originalRows, RowSource changedRows)
			throws IOException {
		for(int i = 0; i < originalRows.count(); i += PART_SIZE){
			for (Row oRow : originalRows.getPart(i, PART_SIZE)) {
				if(!changedRows.containsRow(RowUtil.genIndexKey(oRow.getPrimaryKey()))){
					openWriter().write(buildDeleteQueryString(oRow));
					openWriter().newLine();
				}
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
		} catch(Exception e){}
	}

	private void 

	@Override
	public void start() {
		

	}

	@Override
	public void stop() {
		

	}

	@Override
	public void pause() {
		

	}

	private class DiffIterator {

		private RowSource source1;
		private RowSource source2;
		private TriFunction<RowSource, RowSource, Integer> t;
		

	}

}