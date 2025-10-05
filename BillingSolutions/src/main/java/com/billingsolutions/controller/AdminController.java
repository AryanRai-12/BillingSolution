//package com.billingsolutions.controller;
//
//import com.billingsolutions.model.Bill;
//import com.billingsolutions.model.User;
//import com.billingsolutions.service.AdminService;
//import com.billingsolutions.service.BillingService;
//import com.billingsolutions.repository.BillRepository;
//import com.billingsolutions.repository.UserRepository;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
////@Controller
////@RequestMapping("/admin")
////public class AdminController {
////	private final AdminService adminService;
////	private final BillingService billingService;
////	private final BillRepository billRepository;
////
////	public AdminController(AdminService adminService, BillingService billingService, BillRepository billRepository) {
////		this.adminService = adminService;
////		this.billingService = billingService;
////		this.billRepository = billRepository;
////	}
////
////	@GetMapping("/dashboard")
////	public String dashboard(Model model) {
////		model.addAttribute("totalProfit", adminService.getTotalProfit());
////		model.addAttribute("totalDuesReceivable", adminService.getTotalDuesReceivable());
////		model.addAttribute("totalVendorPayables", adminService.getTotalVendorPayables());
////		model.addAttribute("top10Customers", adminService.getTopCustomersByProfit(10));
////		return "admin/dashboard";
////	}
////
////	@GetMapping("/bills/{id}")
////	public String viewBillAdmin(@PathVariable("id") Long id, Model model) {
////		Bill bill = billRepository.findById(id).orElseThrow();
////		model.addAttribute("bill", bill);
////		return "admin/bill_edit";
////	}
////
////	@PostMapping("/bills/{id}")
////	public String updateBillAdmin(@PathVariable("id") Long id, @ModelAttribute BillUpdateForm form, Model model) {
////		BillingService.UpdateBillRequest req = new BillingService.UpdateBillRequest();
////		req.billId = id;
////		req.paymentAgainstPreviousDue = form.paymentAgainstPreviousDue;
////		req.items = form.toUpdateItems();
////		Bill bill = billingService.adminUpdateBill(req);
////		return "redirect:/admin/bills/" + bill.getId();
////	}
////
////	public static class BillUpdateForm {
////		public java.math.BigDecimal paymentAgainstPreviousDue;
////		public java.util.List<Item> items;
////		public java.util.List<BillingService.UpdateBillItemRequest> toUpdateItems() {
////			java.util.List<BillingService.UpdateBillItemRequest> list = new java.util.ArrayList<>();
////			if (items != null) {
////				for (Item i : items) {
////					BillingService.UpdateBillItemRequest r = new BillingService.UpdateBillItemRequest();
////					r.itemId = i.itemId;
////					r.discountPercent = i.discountPercent;
////					list.add(r);
////				}
////			}
////			return list;
////		}
////		public static class Item {
////			public Long itemId;
////			public java.math.BigDecimal discountPercent;
////		}
////	}
////}
//
//
//
//
//@Controller
//@RequestMapping("/admin")
//public class AdminController {
//	private final AdminService adminService;
//	private final BillingService billingService;
//	// ADDED: UserRepository is needed to get the current user's business
//	private final UserRepository userRepository;
//
//	public AdminController(AdminService adminService, BillingService billingService, UserRepository userRepository) {
//		this.adminService = adminService;
//		this.billingService = billingService;
//		this.userRepository = userRepository;
//	}
//	
//	/**
//     * Helper method to securely get the Business of the currently authenticated user.
//     */
//    private com.billingsolutions.model.Business getCurrentBusiness() {
//        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = authentication.getName();
//        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
//        return currentUser.getBusiness();
//    }
//
//	// MODIFIED: This dashboard is now secure and only shows data for the current business.
//	@GetMapping("/dashboard")
//	public String dashboard(Model model) {
//		com.billingsolutions.model.Business currentBusiness = getCurrentBusiness();
//		model.addAttribute("totalProfit", adminService.getTotalProfit(currentBusiness));
//		model.addAttribute("totalDuesReceivable", adminService.getTotalDuesReceivable(currentBusiness));
//		model.addAttribute("totalVendorPayables", adminService.getTotalVendorPayables(currentBusiness));
//		model.addAttribute("top10Customers", adminService.getTopCustomersByProfit(10, currentBusiness));
//		return "admin/dashboard";
//	}
//
//	// MODIFIED: This now uses the secure billingService to find the bill.
//	@GetMapping("/bills/{id}")
//	public String viewBillAdmin(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
//		try {
//			// This call is now secure and will throw an exception if the bill doesn't belong to this business.
//			Bill bill = billingService.getBillById(id);
//			model.addAttribute("bill", bill);
//			return "admin/bill_edit";
//		} catch (EntityNotFoundException e) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
//			return "redirect:/admin/dashboard";
//		}
//	}
//
//	// This method is already secure because billingService.adminUpdateBill() is secure.
//	@PostMapping("/bills/{id}")
//	public String updateBillAdmin(@PathVariable("id") Long id, @ModelAttribute BillUpdateForm form, RedirectAttributes redirectAttributes) {
//		try {
//			BillingService.UpdateBillRequest req = new BillingService.UpdateBillRequest();
//			req.billId = id;
//			req.paymentAgainstPreviousDue = form.paymentAgainstPreviousDue;
//			req.items = form.toUpdateItems();
//			billingService.adminUpdateBill(req);
//			redirectAttributes.addFlashAttribute("successMessage", "Bill updated successfully!");
//			return "redirect:/admin/bills/" + id;
//		} catch (EntityNotFoundException e) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
//			return "redirect:/admin/dashboard";
//		}
//	}
//
//	// This form-backing object remains the same.
//	public static class BillUpdateForm {
//		public java.math.BigDecimal paymentAgainstPreviousDue;
//		public java.util.List<Item> items;
//		public java.util.List<BillingService.UpdateBillItemRequest> toUpdateItems() {
//			java.util.List<BillingService.UpdateBillItemRequest> list = new java.util.ArrayList<>();
//			if (items != null) {
//				for (Item i : items) {
//					BillingService.UpdateBillItemRequest r = new BillingService.UpdateBillItemRequest();
//					r.itemId = i.itemId;
//					r.discountPercent = i.discountPercent;
//					list.add(r);
//				}
//			}
//			return list;
//		}
//		public static class Item {
//			public Long itemId;
//			public java.math.BigDecimal discountPercent;
//		}
//	}
//}
package com.billingsolutions.controller;

import com.billingsolutions.model.Bill;
import com.billingsolutions.model.User;
import com.billingsolutions.repository.UserRepository;
import com.billingsolutions.service.AdminService;
import com.billingsolutions.service.BillingService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
	private final AdminService adminService;
	private final BillingService billingService;
	private final UserRepository userRepository;

	public AdminController(AdminService adminService, BillingService billingService, UserRepository userRepository) {
		this.adminService = adminService;
		this.billingService = billingService;
		this.userRepository = userRepository;
	}
	
	/**
     * Helper method to securely get the Business of the currently authenticated user.
     */
    private com.billingsolutions.model.Business getCurrentBusiness() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
        return currentUser.getBusiness();
    }

	/**
	 * SECURE: This dashboard now only shows data for the current admin's business.
	 */
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		com.billingsolutions.model.Business currentBusiness = getCurrentBusiness();
		model.addAttribute("totalProfit", adminService.getTotalProfit(currentBusiness));
		model.addAttribute("totalDuesReceivable", adminService.getTotalDuesReceivable(currentBusiness));
		model.addAttribute("totalVendorPayables", adminService.getTotalVendorPayables(currentBusiness));
		model.addAttribute("top10Customers", adminService.getTopCustomersByProfit(10, currentBusiness));
		return "admin/dashboard";
	}

	/**
	 * SECURE: This now uses the secure billingService to find and display a bill for editing.
	 */
	@GetMapping("/bills/{id}")
	public String viewBillAdmin(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
		try {
			// This call is now secure and will throw an exception if the bill doesn't belong to this business.
			Bill bill = billingService.getBillById(id);
			model.addAttribute("bill", bill);
			return "admin/bill_edit";
		} catch (EntityNotFoundException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
			return "redirect:/admin/dashboard";
		}
	}

	/**
	 * SECURE: This method is secure because billingService.adminUpdateBill() is secure.
	 */
	@PostMapping("/bills/{id}")
	public String updateBillAdmin(@PathVariable("id") Long id, @ModelAttribute BillUpdateForm form, RedirectAttributes redirectAttributes) {
		try {
			BillingService.UpdateBillRequest req = new BillingService.UpdateBillRequest();
			req.billId = id;
			req.paymentAgainstPreviousDue = form.paymentAgainstPreviousDue;
			req.items = form.toUpdateItems();
			billingService.adminUpdateBill(req);
			redirectAttributes.addFlashAttribute("successMessage", "Bill updated successfully!");
			return "redirect:/admin/bills/" + id;
		} catch (EntityNotFoundException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Bill not found.");
			return "redirect:/admin/dashboard";
		}
	}

	// This form-backing object remains the same.
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

