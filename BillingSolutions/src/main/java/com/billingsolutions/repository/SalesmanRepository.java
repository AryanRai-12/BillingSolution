package com.billingsolutions.repository;

import com.billingsolutions.model.Salesman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesmanRepository extends JpaRepository<Salesman, Integer> {
}

