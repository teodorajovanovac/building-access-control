package com.buildingaccess.util;

import java.util.List;

/** BOM za ispravan prikaz č/ć/š/ž/đ, separator ';' jer Excel na srpskim podešavanjima ne prepoznaje zarez. */
public final class CsvUtil {

    private static final char BOM = '\uFEFF';
    private static final char SEPARATOR = ';';

    private CsvUtil() {
    }

    public static String toCsv(List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(BOM);
        sb.append(joinRow(headers));
        for (List<String> row : rows) {
            sb.append(joinRow(row));
        }
        return sb.toString();
    }

    private static String joinRow(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(SEPARATOR);
            }
            sb.append(escape(values.get(i)));
        }
        sb.append("\r\n");
        return sb.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(SEPARATOR) >= 0 || value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
