package de.ozml.hsqldiffextract.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a sql table.
 */
public class Table {

	private String name;
	private Column[] columns;

	public Table() {
	}

	public Table(String name, Column[] columns) {
		this.name = name;
		this.columns = columns;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Column[] getColumns() {
		return columns;
	}

	public void setColumns(Column[] columns) {
		this.columns = columns;
	}

	private String listColumns(){
		if(columns != null && columns.length > 0){
			return String.join(", ", Arrays.stream(columns).map(Object::toString).toArray(String[]::new));
		} else {
			return "";
		}
	}

	public String[] getColumnNames(){
		String[] names = new String[columns.length];
		for(int i = 0; i < columns.length; i++){
			names[i] = columns[i].getName();
		}

		return names;
	}

	public Column[] getPrimaryKey(){
		List<Column> primaryKeys = new ArrayList<>();
		for(int i = 0; i < columns.length; i++){
			if(columns[i].isPrimaryKey()){
				primaryKeys.add(columns[i]);
			}
		}

		return primaryKeys.toArray(new Column[primaryKeys.size()]);
	}

	public int[] getPrimaryKeyIndices(){
		List<Integer> indices = new ArrayList<>();
		for(int i = 0; i < columns.length; i++){
			if(columns[i].isPrimaryKey()){
				indices.add(i);
			}
		}

		return indices.stream().mapToInt(i->i).toArray();
	}

	@Override
	public String toString() {
		return "Table " + name + " (" + listColumns() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(columns);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Table other = (Table) obj;
		if (!Arrays.equals(columns, other.columns))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	

}