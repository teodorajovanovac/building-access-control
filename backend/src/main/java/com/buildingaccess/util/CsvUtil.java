package com.buildingaccess.util;

import java.util.List;

/** Prost CSV izvoz (bez spoljne biblioteke) - BOM na pocetku da Excel ispravno prikaze c/c/s/z/dj. */
public final class CsvUtil {

    private static final char BOM = '\uFEFF';

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
                sb.append(',');
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
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
