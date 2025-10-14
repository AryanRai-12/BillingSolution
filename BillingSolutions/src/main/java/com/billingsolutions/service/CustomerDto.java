package com.billingsolutions.service;

//Create this in a new 'dto' package, e.g., com.billingsolutions.dto


public class CustomerDto {
 private Long id;
 private String name;
 private Long customerGroupId;

 // Default constructor is important
 public CustomerDto() {}

 // A helpful constructor to make mapping easier
 public CustomerDto(Long id, String name, Long customerGroupId) {
     this.id = id;
     this.name = name;
     this.customerGroupId = customerGroupId;
 }

 // Getters and Setters
 public Long getId() {
     return id;
 }

 public void setId(Long id) {
     this.id = id;
 }

 public String getName() {
     return name;
 }

 public void setName(String name) {
     this.name = name;
 }

 public Long getCustomerGroupId() {
     return customerGroupId;
 }

 public void setCustomerGroupId(Long customerGroupId) {
     this.customerGroupId = customerGroupId;
 }
}