//package com.billingsolutions.runner;
//
//import com.billingsolutions.service.DataImportService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//
//@Profile("import-data")
//@Component
//public class DataImportRunner implements CommandLineRunner {
//
//    private static final Logger logger = LoggerFactory.getLogger(DataImportRunner.class);
//    
//    // 👇 MODIFIED: Inject AuthenticationManager
//    private final AuthenticationManager authenticationManager;
//    private final DataImportService dataImportService;
//
//    public DataImportRunner(DataImportService dataImportService, AuthenticationManager authenticationManager) {
//        this.dataImportService = dataImportService;
//        this.authenticationManager = authenticationManager;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        // ❗ IMPORTANT: Set the user credentials and file path here
//        String usernameToImportAs = "Aryan"; // <--- CHANGE THIS
//        String passwordToImportAs = "12345678"; // <--- CHANGE THIS
//        String filePath = "C:\\Users\\Aryan\\Downloads\\Parties.csv";
//
//        logger.info("--- Data Import Runner is ACTIVE ---");
//
//        if (!Files.exists(Paths.get(filePath))) {
//            logger.error("--- ❌ Could not find the import file at the specified path: {} ---", filePath);
//            return; // Stop if file doesn't exist
//        }
//
//        try {
//            // Step 1: "Log in" the specified user programmatically
//            logger.info("Attempting to authenticate as user '{}' for the import process.", usernameToImportAs);
//            Authentication authenticationRequest =
//                UsernamePasswordAuthenticationToken.unauthenticated(usernameToImportAs, passwordToImportAs);
//            Authentication authenticationResponse =
//                this.authenticationManager.authenticate(authenticationRequest);
//            SecurityContextHolder.getContext().setAuthentication(authenticationResponse);
//            logger.info("Successfully authenticated. Starting data import...");
//
//            // Step 2: Run the import. It will now use the authenticated user's context.
//            dataImportService.importCustomersFromCsvFile(filePath);
//            
//            logger.info("--- ✅ Data import process finished successfully. ---");
//
//        } catch (Exception e) {
//            logger.error("--- ❌ An error occurred during the data import process. The transaction has been rolled back. ---", e);
//        } finally {
//            // Step 3: Clear the security context to "log out" the user. This is crucial.
//            SecurityContextHolder.clearContext();
//            logger.info("Security context cleared. Import task complete.");
//        }
//    }
//}