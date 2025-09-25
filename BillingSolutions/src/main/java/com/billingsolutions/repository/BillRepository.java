package com.billingsolutions.repository;

import com.billingsolutions.model.Bill;
import com.billingsolutions.model.User;
import com.billingsolutions.repository.projection.CustomerProfit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
	
	@Query("SELECT u FROM User u JOIN u.roles r WHERE r = 'ROLE_SALESMAN' OR r = 'SALESMAN'")
    List<User> findAllSalesmen();
	
	@Query("select coalesce(sum(b.total - b.totalCostBasis), 0) from Bill b")
	BigDecimal findTotalProfit();

	@Query("select coalesce(sum(b.previousDue), 0) from Bill b")
	BigDecimal findTotalPreviousDuesRecorded();

	@Query("select coalesce(sum(b.newDue), 0) from Bill b")
	BigDecimal findTotalNewDues();

	@Query("select b from Bill b where b.customer.id = :customerId order by (b.total - b.totalCostBasis) desc")
	List<Bill> findBillsByCustomerOrderByProfitDesc(@Param("customerId") Long customerId);

	@Query("select b.customer.id as customerId, b.customerNameSnapshot as customerName, sum(b.total - b.totalCostBasis) as profit from Bill b group by b.customer.id, b.customerNameSnapshot order by profit desc")
	List<CustomerProfit> findCustomerProfitLeaderboard();
	
	
	Optional<Bill> findTopByFinancialYearOrderByIdDesc(String financialYear);
} 