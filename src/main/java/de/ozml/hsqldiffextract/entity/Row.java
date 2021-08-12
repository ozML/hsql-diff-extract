package de.ozml.hsqldiffextract.entity;

import java.util.Arrays;

/**
 * Represents a sql table data row.
 */
public class Row {

	private Table table;
	private String[] value;

	public Row() {
	}

	public Row(Table table, String[] value) {
		this.table = table;
		this.value = value;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public String[] getValue() {
		return value;
	}

	public void setValue(String[] value) {
		this.value = value;
	}

	public String[] getPrimaryKey(){
		int[] pkIndices = table.getPrimaryKeyIndices();
		String[] pkValue = new String[pkIndices.length];
		for(int i = 0; i < pkIndices.length; i++){
			pkValue[i] = value[pkIndices[i]];
		}

		return pkValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Row other = (Row) obj;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

}