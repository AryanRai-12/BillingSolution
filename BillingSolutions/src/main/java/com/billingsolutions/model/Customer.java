package com.billingsolutions.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "customers")
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@JsonIgnore
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;
	
	@NotBlank(message = "Counter name is required.")
	@Column(nullable=false)
	private String CounterName;
	
	@NotBlank(message = "Customer name is required.")
	@Column(nullable = false)
	private String name;

	private String address;
	
	@Size(min = 10, max = 10,message = "Phone number must be exactly 10 digits.")
	private String phone;

	@Pattern(regexp = "^$|^.{15}$", message = "GST number must be either empty or exactly 15 characters long.")
	@Column(length = 15)
	private String gst;

	@Email(message = "Please enter a valid email address.")
	private String email;

	@NotNull(message = "Credit limit cannot be empty.")
	@Column(precision = 18, scale = 2)
	private BigDecimal creditLimit = BigDecimal.ZERO;

	@NotNull(message = "Due amount cannot be empty.")
	@Column(precision = 18, scale = 2)
	private BigDecimal due = BigDecimal.ZERO;

	private String landmark;

	private String houseAddress;

	@NotBlank(message = "Party code is required.")
	@Column(unique = true)
	private String partyCode;
	
	
	private String customerGroup;

	public Customer() {}
	
	

	public Business getBusiness() {
		return business;
	}



	public void setBusiness(Business business) {
		this.business = business;
	}



	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getGst() { return gst; }
	public void setGst(String gst) { this.gst = gst; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public BigDecimal getCreditLimit() { return creditLimit; }
	public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
	public BigDecimal getDue() { return due; }
	public void setDue(BigDecimal due) { this.due = due; }
	public String getLandmark() { return landmark; }
	public void setLandmark(String landmark) { this.landmark = landmark; }
	public String getHouseAddress() { return houseAddress; }
	public void setHouseAddress(String houseAddress) { this.houseAddress = houseAddress; }
	public String getPartyCode() { return partyCode; }
	public void setPartyCode(String partyCode) { this.partyCode = partyCode; }

	public String getCounterName() {
		return CounterName;
	}

	public void setCounterName(String counterName) {
		CounterName = counterName;
	}

	public String getCustomerGroup() {
		return customerGroup;
	}

	public void setCustomerGroup(String group) {
		customerGroup = group;
	}
	
} 