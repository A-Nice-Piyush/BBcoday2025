package com.nice.avishkar.dao;

import com.nice.avishkar.model.CustomerRequests;
import com.nice.avishkar.model.Route;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ResourceInfoReader
{

    public static List<Route> getRoutes(Path path)
    {
        return readCSV(path.toString(), Route.class);
    }

    public static List<CustomerRequests> getCustomerRequests(Path path)
    {
        return readCSV(path.toString(), CustomerRequests.class);
    }

    private static <T> List<T> readCSV(String filePath, Class<T> beanClass) {
        try (Reader reader = new FileReader(filePath)) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType(beanClass)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
