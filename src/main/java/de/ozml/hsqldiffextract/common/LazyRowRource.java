package de.ozml.hsqldiffextract.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.ozml.hsqldiffextract.entity.Row;
import de.ozml.hsqldiffextract.entity.Table;
import de.ozml.hsqldiffextract.parser.RowParser;

/**
 * Represents a {@link RowSource} which holds just the line numbers of all row
 * entities of a table. The actual row data is not loaded until needed.
 */
public class LazyRowRource implements RowSource {

	private Table table;
	private String dataFilePath;
	private Map<String, Integer> lineNumberMap;

	public LazyRowRource(Table table, String dataFilePath, Map<String, Integer> lineNumberMap) {
		this.table = table;
		this.dataFilePath = dataFilePath;
		this.lineNumberMap = lineNumberMap;
	}

	@Override
	public String getTableName() {
		return table.getName();
	}

	@Override
	public int count() {
		return lineNumberMap.size();
	}

	@Override
	public boolean containsRow(String key) {
		return lineNumberMap.containsKey(key);
	}

	@Override
	public Row getRow(String key) {
		Row row = null;
		Integer lineNumber = lineNumberMap.get(key);
		if(lineNumber != null){
			String line = readLineFromDataFile(lineNumber);
			row = RowParser.extractRow(table, line);
		}

		return row;
	}

	@Override
	public List<Row> getAllRows() {
		return getPart(0, count());
	}

	@Override
	public List<Row> getPart(int startIndex, int number) {
		List<Row> list = new ArrayList<>();
		int endIndex = startIndex + number <= count() ? startIndex + number : count();
		List<Entry<String, Integer>> entryList = sortedEntryList().subList(startIndex, endIndex);
		collectRows(entryList, row -> list.add(row));
		list.sort((o1, o2) -> RowUtil.compareIndexKeys(o1, o2));

		return list;
	}

	@Override
	public Map<String, Row> getPartMap(int startIndex, int number) {
		Map<String, Row> map = new HashMap<>();
		int endIndex = startIndex + number <= count() ? startIndex + number : count();
		List<Entry<String, Integer>> entryList = sortedEntryList().subList(startIndex, endIndex);
		collectRows(entryList, row -> map.put(RowUtil.genIndexKey(row.getPrimaryKey()), row));

		return map;
	}

	@Override
	public List<Row> getRows(Collection<String> keys) {
		List<Row> list = new ArrayList<>();
		List<Entry<String, Integer>> entryList = filteredEntryList(keys);
		collectRows(entryList, row -> list.add(row));
		list.sort((o1, o2) -> RowUtil.compareIndexKeys(o1, o2));

		return list;
	}

	/**
	 * Returns the rows with the specified keys as a map.
	 * @param key
	 * @return
	 */
	public Map<String, Row> getRowsMap(Collection<String> keys) {
		Map<String, Row> map = new HashMap<>();
		List<Entry<String, Integer>> entryList = filteredEntryList(keys);
		collectRows(entryList, row -> map.put(RowUtil.genIndexKey(row.getPrimaryKey()), row));

		return map;
	}

	/**
	 * Reads the rows from line numbers and passes them to the handler. Each entry within the
	 * specified list is composed an row index key string and the line number in the sql source
	 * file.
	 * @param entryList
	 * @param handler
	 */
	private void collectRows(List<Entry<String, Integer>> entryList, Consumer<Row> handler){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(dataFilePath));
			entryList.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
			int currentLine = 0;
			for (int i = 0; i < entryList.size(); i++) {
				int lineNumber = entryList.get(i).getValue();
				int step = lineNumber - currentLine;
				if(step > 1){
					skipLines(reader, step);
				}
				currentLine += step;

				String line = reader.readLine();
				Row row =  RowParser.extractRow(table, line);
				if (row != null) {
					handler.accept(row);
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			try{if(reader != null) reader.close();} catch(Exception e){}
		}
	}

	/**
	 * Returns a sorted line list.
	 * @return
	 */
	private List<Entry<String, Integer>> sortedEntryList() {
		return lineNumberMap.entrySet()
			.stream()
			.sorted((o1, o2) -> RowUtil.compareIndexKeys(o1.getKey(), o2.getKey()))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Returns a list of entries corresponding to the specified keys.
	 * @param keys
	 * @return
	 */
	private List<Entry<String, Integer>> filteredEntryList(Collection<String> keys) {
		List<String> keyList = new ArrayList<>(keys);
		return lineNumberMap.entrySet()
			.stream()
			.filter(entry -> keyList.contains(entry.getKey()))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Reads the line with the line number from the sql data file.
	 * @param lineNumber
	 * @return
	 */
	private String readLineFromDataFile(int lineNumber) {
		try (Stream<String> fileStream = Files.lines(Paths.get(dataFilePath))) {
			return fileStream.skip(lineNumber).findFirst().get();
		} catch (Exception e) {}

		return null;
	}

	/**
	 * Skips the specified line number from the given reader.
	 * @param reader
	 * @param number
	 * @throws IOException
	 */
	private void skipLines(BufferedReader reader, int number) throws IOException {
		while(number-- > 0){
			reader.readLine();
		}
	}

}