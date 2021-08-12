package de.ozml.hsqldiffextract.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.ozml.hsqldiffextract.entity.Row;

/**
 * The class represents a repo of the rows for the contained table and provides multiple
 * methods to get access to them. The key which is refered in the method declarations is 
 * composed of the primary column names, seperated each by a colon.
 */
public interface RowSource {

	/**
	 * Returns the name of the associated table.
	 * @return
	 */
	public String getTableName();

	/**
	 * Returns the count of row entries.
	 * @return
	 */
	public int count();

	/**
	 * Checks if the row with the specified key exists.
	 * @param key
	 * @return
	 */
	public boolean containsRow(String key);

	/**
	 * Returns the row with the specified key. The order must correspond
	 * to the order in the table definition.
	 * @param key
	 * @return
	 */
	public Row getRow(String key);

	/**
	 * Returns all rows sorted by the keys. 
	 * @return
	 */
	public List<Row> getAllRows();

	/**
	 * Returns a limited part of the rows.The index declaration applies to an internal list, 
	 * which is sorted by key values.
	 * @param startIndex
	 * @param number
	 * @return
	 */
	public List<Row> getPart(int startIndex, int number);

	/**
	 * Returns a limited part of the rows as a map. The index declaration applies to an internal list, 
	 * which is sorted by key values.
	 * @param startIndex
	 * @param number
	 * @return
	 */
	public Map<String, Row> getPartMap(int startIndex, int number);

	/**
	 * Returns the rows with the specified keys. The result list is sorted by key the values.
	 * @param key
	 * @return
	 */
	public List<Row> getRows(Collection<String> keys);

	/**
	 * Returns the rows with the specified keys as a map.
	 * @param key
	 * @return
	 */
	public Map<String, Row> getRowsMap(Collection<String> keys);

}