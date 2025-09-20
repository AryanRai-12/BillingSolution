package com.billingsolutions.controller;

import com.billingsolutions.model.Bill;
import com.billingsolutions.service.AdminService;
import com.billingsolutions.service.BillingService;
import com.billingsolutions.repository.BillRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
	private final AdminService adminService;
	private final BillingService billingService;
	private final BillRepository billRepository;

	public AdminController(AdminService adminService, BillingService billingService, BillRepository billRepository) {
		this.adminService = adminService;
		this.billingService = billingService;
		this.billRepository = billRepository;
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("totalProfit", adminService.getTotalProfit());
		model.addAttribute("totalDuesReceivable", adminService.getTotalDuesReceivable());
		model.addAttribute("totalVendorPayables", adminService.getTotalVendorPayables());
		model.addAttribute("top10Customers", adminService.getTopCustomersByProfit(10));
		return "admin/dashboard";
	}

	@GetMapping("/bills/{id}")
	public String viewBillAdmin(@PathVariable("id") Long id, Model model) {
		Bill bill = billRepository.findById(id).orElseThrow();
		model.addAttribute("bill", bill);
		return "admin/bill_edit";
	}

	@PostMapping("/bills/{id}")
	public String updateBillAdmin(@PathVariable("id") Long id, @ModelAttribute BillUpdateForm form, Model model) {
		BillingService.UpdateBillRequest req = new BillingService.UpdateBillRequest();
		req.billId = id;
		req.paymentAgainstPreviousDue = form.paymentAgainstPreviousDue;
		req.items = form.toUpdateItems();
		Bill bill = billingService.adminUpdateBill(req);
		return "redirect:/admin/bills/" + bill.getId();
	}

	public static class BillUpdateForm {
		public java.math.BigDecimal paymentAgainstPreviousDue;
		public java.util.List<Item> items;
		public java.util.List<BillingService.UpdateBillItemRequest> toUpdateItems() {
			java.util.List<BillingService.UpdateBillItemRequest> list = new java.util.ArrayList<>();
			if (items != null) {
				for (Item i : items) {
					BillingService.UpdateBillItemRequest r = new BillingService.UpdateBillItemRequest();
					r.itemId = i.itemId;
					r.discountPercent = i.discountPercent;
					list.add(r);
				}
			}
			return list;
		}
		public static class Item {
			public Long itemId;
			public java.math.BigDecimal discountPercent;
		}
	}
} 