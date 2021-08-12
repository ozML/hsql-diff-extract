package de.ozml.hsqldiffextract.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import de.ozml.hsqldiffextract.entity.Row;

public class RowUtil {

	private RowUtil(){}

	/**
	 * Checks whether two column entities equals in the primary key data.
	 * @param row1
	 * @param row2
	 * @return
	 */
	public static boolean isPrimaryKeyEqual(Row row1, Row row2){
		return Arrays.equals(row1.getPrimaryKey(), row2.getPrimaryKey());
	}

	/**
	 * Generates an index key string from the column data array. The array consists of a rows
	 * primary key columns data which are as a result concatenated with a colon.
	 * @param columnData
	 * @return
	 */
	public static String genIndexKey(String[] columnData){
		return String.join(",", columnData);
	}

	/**
	 * Compares the indexKeys of the two rows.
	 * @param row1
	 * @param row2
	 * @return
	 */
	public static int compareIndexKeys(Row row1, Row row2){
		return compareIndexKeys(genIndexKey(row1.getPrimaryKey()), genIndexKey(row2.getPrimaryKey()));
	}

	/**
	 * Compares the two indexKeys.
	 * @param indexKey1
	 * @param indexKey2
	 * @return
	 */
	public static int compareIndexKeys(String indexKey1, String indexKey2){
		return indexKey1.compareTo(indexKey2);
	}

	/**
	 * Returns a comparator which sorts index key strings in
	 * ascending order.
	 * @return
	 */
	public static Comparator<String> indexKeyComparator(){
		return (o1, o2) -> compareIndexKeys(o1, o2);
	}

	/**
	 * Returns a sorted index list.
	 * @return
	 */
	public static List<String> sortIndexKeyList(Collection<String> indexCollection) {
		List<String> list = new ArrayList<>(indexCollection);
		list.sort(RowUtil.indexKeyComparator());

		return list;
	}

}