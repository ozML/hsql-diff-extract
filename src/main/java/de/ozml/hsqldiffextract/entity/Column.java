package de.ozml.hsqldiffextract.entity;

/**
 * Represents a sql table column.
 */
public class Column {
	
	private String name;
	private ColumnType type;
	private boolean isPrimaryKey;
	private boolean isUnique;
	private boolean isNullable;

	public Column(){}

	public Column(String name, ColumnType type, boolean isPrimaryKey, boolean isUnique, boolean isNullable){
		this.name = name;
		this.type = type;
		this.isPrimaryKey = isPrimaryKey;
		this.isUnique = isUnique;
		this.isNullable = isNullable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ColumnType getType() {
		return type;
	}

	public void setType(ColumnType type) {
		this.type = type;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public void setPrimaryKey(boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public boolean isNullable() {
		return isNullable;
	}

	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}

	@Override
	public String toString() {
		return "Column " + name + " (type=" + type + ", isPrimaryKey=" + isPrimaryKey + ", isUnique=" + isUnique 
				+ ", isNullable=" + isNullable + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isNullable ? 1231 : 1237);
		result = prime * result + (isPrimaryKey ? 1231 : 1237);
		result = prime * result + (isUnique ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Column other = (Column) obj;
		if (isNullable != other.isNullable)
			return false;
		if (isPrimaryKey != other.isPrimaryKey)
			return false;
		if (isUnique != other.isUnique)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}