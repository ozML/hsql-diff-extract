package de.ozml.hsqldiffextract.util;

import java.util.Arrays;

import de.ozml.hsqldiffextract.entity.Row;

public class RowUtil {

	private RowUtil(){}

	public static boolean isPrimaryKeyEqual(Row row1, Row row2){
		return Arrays.equals(row1.getPrimaryKeyValue(), row2.getPrimaryKeyValue());
	}

	public static String generateIndexKey(String[] columnData){
		return String.join(",", columnData);
	}

}