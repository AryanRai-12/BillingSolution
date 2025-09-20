package com.billingsolutions.config;

import com.billingsolutions.model.Salesman;
import com.billingsolutions.repository.SalesmanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SalesmanRepository salesmanRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize some sample salesmen if none exist
        if (salesmanRepository.count() == 0) {
            Salesman salesman1 = new Salesman();
            salesman1.setName("John Smith");
            salesmanRepository.save(salesman1);

            Salesman salesman2 = new Salesman();
            salesman2.setName("Jane Doe");
            salesmanRepository.save(salesman2);

            Salesman salesman3 = new Salesman();
            salesman3.setName("Mike Johnson");
            salesmanRepository.save(salesman3);
        }
    }
}

