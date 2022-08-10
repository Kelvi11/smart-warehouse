package com.kelvin.smartwarehouse.utils;

import com.kelvin.smartwarehouse.model.Order;
import com.kelvin.smartwarehouse.model.enums.OrderStatus;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class XlsxUtils {

    public static void writeXlsx(File file, List<Order> orders) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Orders");

        createHeaderRow(sheet);

        for (int i = 0; i < orders.size(); i++) {
            createDataRow(sheet, orders.get(i), i + 1);
        }

        FileOutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        workbook.close();
    }

    private static void createHeaderRow(Sheet sheet) {
        String headerNames[] = {
                "uuid",
                "submittedDate",
                "deadlineDate",
                "status"
        };

        Row header = sheet.createRow(0);
        for (int i = 0; i < headerNames.length; i++){
            Cell headerCell = header.createCell(i);
            headerCell.setCellValue(headerNames[i]);
        }
    }

    private static void createDataRow(Sheet sheet, Order order, int index) {
        Row row = sheet.createRow(index);

        createUuidCell(row, 0, order.getUuid());

        createSubmittedDateCell(row, 1, order.getSubmittedDate());
        createDeadlineDateCell(row, 2, order.getDeadlineDate());

        createOrderStatusCell(row, 3, order.getStatus());
    }

    private static void createUuidCell(Row row, int i, String order) {
        Cell cell = row.createCell(i);
        cell.setCellValue(order);
    }

    private static void createSubmittedDateCell(Row row, int i, LocalDate submittedDate) {
        Cell cell = row.createCell(i);
        cell.setCellValue(submittedDate);
    }

    private static void createDeadlineDateCell(Row row, int i, LocalDate deadlineDate) {
        Cell cell = row.createCell(i);
        cell.setCellValue(deadlineDate);
    }

    private static void createOrderStatusCell(Row row, int i, OrderStatus status) {
        Cell cell = row.createCell(i);
        cell.setCellValue(status.name());
    }

    public static String extractExelFileContentAsString(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            List<String[]> allData = collectRowsInList(sheet);

            collectListToString(stringBuilder, allData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static List<String[]> collectRowsInList(Sheet sheet) {
        List<String[]> allData = new ArrayList<>();
        for (Row row : sheet) {
            String[] rowAsStringArray = new String[row.getLastCellNum()];
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case STRING:
                        rowAsStringArray[cell.getColumnIndex()] = cell.getStringCellValue();
                        break;
                    case NUMERIC:
                        rowAsStringArray[cell.getColumnIndex()] = String.valueOf(cell.getNumericCellValue());
                        break;
                    case BOOLEAN:
                        rowAsStringArray[cell.getColumnIndex()] = String.valueOf(cell.getBooleanCellValue());
                        break;
                }
            }
            allData.add(rowAsStringArray);
        }

        return allData;
    }

    private static void collectListToString(StringBuilder stringBuilder, List<String[]> allData) {
        for (String[] line : allData) {
            stringBuilder.append(String.join(",", line));
            stringBuilder.append(System.lineSeparator());
        }
    }
}
