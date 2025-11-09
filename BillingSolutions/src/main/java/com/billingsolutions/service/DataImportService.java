package com.billingsolutions.service;

import com.billingsolutions.model.Customer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataImportService {

    private static final Logger logger = LoggerFactory.getLogger(DataImportService.class);
    private final CustomerService customerService;

    public DataImportService(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Imports customers from a local CSV file. This is transactional, so if any record fails,
     * the entire import is rolled back to prevent partial data insertion.
     * @param filePath The full path to the CSV file on the server.
     */
    @Transactional
    public void importCustomersFromCsvFile(String filePath) {
        try (
            BufferedReader fileReader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));
            CSVParser csvParser = new CSVParser(fileReader,
                // Use the actual headers from your Parties.csv file
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())
        ) {
            
            List<Customer> customersToSave = new ArrayList<>();
            logger.info("Starting CSV import from file: {}", filePath);

            for (CSVRecord csvRecord : csvParser) {
                Customer customer = new Customer();
                
                String name = csvRecord.get("Name");
                customer.setName(name);
                customer.setCounterName(name); // Using Name for CounterName as well

                customer.setAddress(csvRecord.get("Address"));
                customer.setGst(csvRecord.get("GST"));
                customer.setEmail(csvRecord.get("E-Mail"));

                // Clean and validate the phone number to ensure it's 10 digits
                String phone = csvRecord.get("Mobile No").replaceAll("[^\\d]", "");
                if (phone.length() >= 10) {
                    customer.setPhone(phone.substring(0, 10)); // Truncate to 10 digits
                } else {
                    logger.warn("Skipping invalid or short phone number for customer '{}'", name);
                }

                // Safely parse the "Closing Balance" to set the due amount
                try {
                    String closingBalanceStr = csvRecord.get("Closing Balance");
                    customer.setDue(new BigDecimal(closingBalanceStr.isEmpty() ? "0" : closingBalanceStr));
                } catch (NumberFormatException e) {
                    logger.error("Could not parse closing balance for customer '{}'. Setting due to 0.", name, e);
                    customer.setDue(BigDecimal.ZERO);
                }
                
                customersToSave.add(customer);
            }
            
            // Save all the new customers using the existing CustomerService
            for (Customer customer : customersToSave) {
                // This will automatically generate party codes and assign the business!
                customerService.save(customer);
            }
            logger.info("✅ Successfully imported and saved {} customers.", customersToSave.size());

        } catch (Exception e) {
            logger.error("❌ Failed to parse CSV file and import customers.", e);
            // Re-throw as a runtime exception to trigger the transactional rollback
            throw new RuntimeException("Failed to import customers from CSV: " + e.getMessage());
        }
    }
}