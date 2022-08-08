package com.kelvin.smartwarehouse.utils;

import com.kelvin.smartwarehouse.model.Order;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class CsvUtils {

    public static void writeCsv(Writer out, List<Order> orders) throws IOException {

        String header[] = {
                "uuid",
                "submittedDate",
                "deadlineDate",
                "status"
        };

        CSVWriter csv = new CSVWriter(out);
        csv.writeNext(header, false);

        for (Order order : orders) {
            String[] cols = {
                    order.getUuid(),
                    order.getSubmittedDate().toString(),
                    order.getDeadlineDate().toString(),
                    order.getStatus().name()
            };
            csv.writeNext(cols, false);
        }
        csv.close();
    }

    public static String extractCsvFileContentAsString(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileReader filereader = new FileReader(file);
            CSVReader csvReader = new CSVReaderBuilder(filereader).build();

            List<String[]> allData = csvReader.readAll();

            collectListToString(stringBuilder, allData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static void collectListToString(StringBuilder stringBuilder, List<String[]> allData) {
        for (String[] line : allData) {
            stringBuilder.append(String.join(",", line));
            stringBuilder.append(System.lineSeparator());
        }
    }
}
