package com.billingsolutions.repository.projection;

import java.math.BigDecimal;

public interface CustomerProfit {
	Long getCustomerId();
	String getCustomerName();
	BigDecimal getProfit();
} 