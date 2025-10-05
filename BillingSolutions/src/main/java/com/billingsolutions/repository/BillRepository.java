package com.billingsolutions.repository;

import com.billingsolutions.model.Bill;
import com.billingsolutions.model.Business;
import com.billingsolutions.model.Customer;
import com.billingsolutions.repository.projection.CustomerProfit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    // --- NEW METHODS FOR MULTI-TENANCY ---

    /**
     * Finds all bills for a specific business (without pagination).
     */
    List<Bill> findByBusiness(Business business);
    
    /**
     * ADDED: A new custom query to power the unified search box.
     * It securely finds bills for a given business where either the bill number or the
     * customer's name contains the search query.
     */
    @Query("SELECT b FROM Bill b WHERE b.business = :business AND (b.billNo LIKE %:query% OR b.customerNameSnapshot LIKE %:query%)")
    Page<Bill> searchByBillNoOrCustomerName(@Param("business") Business business, @Param("query") String query, Pageable pageable);

 

    
    
    @Query("SELECT b FROM Bill b WHERE b.business = :business " +
            "AND (:financialYear IS NULL OR b.financialYear = :financialYear) " +
            "AND (:query IS NULL OR b.billNo LIKE %:query% OR b.customerNameSnapshot LIKE %:query%)")
     Page<Bill> searchBills(
             @Param("business") Business business,
             @Param("financialYear") String financialYear,
             @Param("query") String query,
             Pageable pageable
     );
    
    /**
     * THE FIX: This is the overloaded method that adds pagination support.
     * It allows the AdminService to efficiently fetch just a 'page' of recent bills.
     * @param business The business entity to filter by.
     * @param pageable The pagination information (page number, size, and sort order).
     * @return A 'Page' of bills for the given business.
     */
    Page<Bill> findByBusiness(Business business, Pageable pageable);
    
    /**
     * ADDED: A new method to find all bills for a specific business and financial year.
     * This will be used to populate the dropdown on the payment form.
     */
    List<Bill> findByBusinessAndFinancialYear(Business business, String financialYear, Sort sort);

    /**
     * Securely finds a single bill by its ID, but only if it belongs to the specified business.
     */
    /**
     * MODIFIED: This now fetches the bill along with its associated payments
     * to prevent lazy loading exceptions on the view page.
     */
    @Query("SELECT b FROM Bill b LEFT JOIN FETCH b.payments WHERE b.id = :id AND b.business = :business")
    Optional<Bill> findByIdAndBusiness(@Param("id") Long id, @Param("business") Business business);
    
    @Query("SELECT b FROM Bill b LEFT JOIN FETCH b.payments WHERE b.id = :id AND b.business = :business")
    Optional<Bill> findByIdAndBusinessWithPayments(@Param("id") Long id, @Param("business") Business business);
    
    /**
     * Securely finds the latest bill for a specific financial year AND business.
     */
    Optional<Bill> findTopByFinancialYearAndBusinessOrderByIdDesc(String financialYear, Business business);
    
    
    
    @Query("SELECT b FROM Bill b LEFT JOIN b.payments p WHERE b.customer = :customer " +
            "GROUP BY b.id HAVING b.total > COALESCE(SUM(p.amount), 0) ORDER BY b.id ASC")
     List<Bill> findUnpaidBillsByCustomer(@Param("customer") Customer customer);
    /**
     * ADDED: A new, secure query to power the live search on the payment form.
     * It finds bills for a business within a specific year where the bill number
     * or customer name matches the search query.
     */
    @Query("SELECT b FROM Bill b WHERE b.business = :business AND b.financialYear = :financialYear " +
           "AND (b.billNo LIKE %:query% OR b.customerNameSnapshot LIKE %:query%) ORDER BY b.id DESC")
    List<Bill> searchForPaymentForm(
            @Param("business") Business business,
            @Param("financialYear") String financialYear,
            @Param("query") String query
    );
    
    // --- YOUR CUSTOM METHODS (NOW UPDATED AND SECURED FOR MULTI-TENANCY) ---

    @Query("SELECT COALESCE(SUM(b.total - b.totalCostBasis), 0) FROM Bill b WHERE b.business = :business")
    BigDecimal findTotalProfitByBusiness(@Param("business") Business business);

    @Query("SELECT COALESCE(SUM(b.previousDue), 0) FROM Bill b WHERE b.business = :business")
    BigDecimal findTotalPreviousDuesRecordedByBusiness(@Param("business") Business business);

    @Query("SELECT COALESCE(SUM(b.newDue), 0) FROM Bill b WHERE b.business = :business")
    BigDecimal findTotalNewDuesByBusiness(@Param("business") Business business);

    @Query("SELECT b FROM Bill b WHERE b.customer.id = :customerId AND b.business = :business ORDER BY (b.total - b.totalCostBasis) DESC")
    List<Bill> findBillsByCustomerAndBusinessOrderByProfitDesc(@Param("customerId") Long customerId, @Param("business") Business business);

    @Query("SELECT b.customer.id as customerId, b.customerNameSnapshot as customerName, SUM(b.total - b.totalCostBasis) as profit FROM Bill b WHERE b.business = :business GROUP BY b.customer.id, b.customerNameSnapshot ORDER BY profit DESC")
    List<CustomerProfit> findCustomerProfitLeaderboardByBusiness(@Param("business") Business business);
}

