package de.ozml.hsqldiffextract.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import de.ozml.hsqldiffextract.entity.Row;
import de.ozml.hsqldiffextract.entity.Table;

/**
 * Represents a {@link RowSource} which holds all row entities of a table.
 */
public class EagerRowSource implements RowSource {

	private Table table;
	private Map<String, Row> rows;

	public EagerRowSource(Table table, Map<String, Row> rows){
		this.table = table;
		this.rows = rows;
	}

	@Override
	public String getTableName() {
		return table.getName();
	}

	@Override
	public int count() {
		return rows.size();
	}

	@Override
	public boolean containsRow(String key) {
		return rows.containsKey(key);
	}

	@Override
	public Row getRow(String key) {
		return rows.get(key);
	}

	@Override
	public List<Row> getAllRows() {
		return getPart(0, count());
	}

	@Override
	public List<Row> getPart(int startIndex, int number) {
		List<Row> list = new ArrayList<>();
		List<String> indexList = RowUtil.sortIndexKeyList(rows.keySet());
		for(int i = startIndex; i < startIndex + number && i < indexList.size(); i++){
			list.add(rows.get(indexList.get(i)));
		}

		return list;
	}

	@Override
	public Map<String, Row> getPartMap(int startIndex, int number) {
		Map<String, Row> map = new HashMap<>();
		List<String> indexList = RowUtil.sortIndexKeyList(rows.keySet());
		for(int i = startIndex; i < startIndex + number && i < indexList.size(); i++){
			map.put(indexList.get(i), rows.get(indexList.get(i)));
		}

		return map;
	}

	@Override
	public List<Row> getRows(Collection<String> keys) {
		List<Row> list = new ArrayList<>();
		collectRows(keys, (key, row) -> list.add(row));

		return list;
	}

	@Override
	public Map<String, Row> getRowsMap(Collection<String> keys) {
		Map<String, Row> map = new HashMap<>();
		collectRows(keys, (key, row) -> map.put(key, row));

		return map;
	}

	/**
	 * Reads the rows from key list and passes them to the handler.
	 * @param keys
	 * @param handler
	 */
	private void collectRows(Collection<String> keys, BiConsumer<String, Row> handler){
		if(keys != null && !keys.isEmpty()){
			List<String> indexList = new ArrayList<>(keys);
			indexList.sort((RowUtil.indexKeyComparator()));
			for(int i = 0; i < indexList.size(); i++){
				Row row = rows.get(indexList.get(i));
				if(row != null){
					handler.accept(indexList.get(i), row);
				}
			}
		}
	}

}