//package com.billingsolutions.controller;
//
//import com.billingsolutions.model.Product;
//import com.billingsolutions.model.Vendor;
//import com.billingsolutions.service.ProductService;
//import com.billingsolutions.service.VendorService;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.validation.Valid;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Controller
//@RequestMapping("/products")
//public class ProductController {
//	
//	private static final Logger log = LoggerFactory.getLogger(ProductController.class);
//
//    private final ProductService productService;
//    private final VendorService vendorService;
//
//    public ProductController(ProductService productService, VendorService vendorService) {
//        this.productService = productService;
//        this.vendorService = vendorService;
//    }
//
//    @GetMapping
//    public String list(Model model) {
//        model.addAttribute("products", productService.findAll());
//        return "products/list";
//    }
//
//    @GetMapping("/new")
//    public String createForm(Model model) {
//        model.addAttribute("product", new Product());
//        model.addAttribute("vendors", vendorService.findAll());
//        return "products/form";
//    }
//
//    @PostMapping
//    public String create(@Valid @ModelAttribute("product") Product product,
//                         BindingResult result,
//                         @RequestParam(name = "vendorName", required = false) String vendorName,
//                         RedirectAttributes attrs,
//                         Model model) {
//        if (result.hasErrors()) {
//            log.warn("Validation failed while creating product: {}", result.getAllErrors());
//            model.addAttribute("vendors", vendorService.findAll());
//            return "products/form";
//        }
//        
//        if (vendorName != null && !vendorName.trim().isEmpty()) {
//            vendorService.findByNameIgnoreCase(vendorName.trim())
//                .ifPresent(product::setVendor);
//        }
//
//        // NOTE: Any specific business logic for creation is now handled inside the productService.save() method.
//        productService.save(product);
//        attrs.addFlashAttribute("successMessage", "Product created successfully!");
//        return "redirect:/products";
//    }
//    
//    @GetMapping("/{id}/edit")
//    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
//        try {
//            Product product = productService.findById(id);
//            model.addAttribute("product", product);
//            model.addAttribute("vendors", vendorService.findAll());
//            return "products/form";
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
//            return "redirect:/products";
//        }
//    }
//
//    @PostMapping("/{id}")
//    public String update(@PathVariable("id") Long id,
//                         @Valid @ModelAttribute("product") Product form,
//                         @RequestParam(name = "vendorName", required = false) String vendorName,
//                         BindingResult result, RedirectAttributes redirectAttributes) {
//    	
//        if (result.hasErrors()) return "products/form";
//
//        try {
//            Product product = productService.findById(id); // Securely finds the product
//
//            // MODIFIED: All fields from your original controller are now copied here.
//            product.setName(form.getName());
//            product.setSku(form.getSku());
//            product.setSellingPrice(form.getSellingPrice());
//            product.setCostPrice(form.getCostPrice());
//            product.setUnitType(form.getUnitType());
//            product.setNumberOfBoxes(form.getNumberOfBoxes());
//            product.setUnitsPerBox(form.getUnitsPerBox());
//            product.setPcs(form.getPcs());
//            product.setTotalBags(form.getTotalBags());
//            product.setWeightPerItem(form.getWeightPerItem());
//            product.setTotalBagWeight(form.getTotalBagWeight());
//            product.setGstRate(form.getGstRate());
//            if (vendorName != null && !vendorName.trim().isEmpty()) {
//                 vendorService.findByNameIgnoreCase(vendorName.trim())
//                    .ifPresent(product::setVendor);
//            } else {
//                product.setVendor(null);
//            }
//            
//            // NOTE: Any specific business logic for updates is now handled inside the productService.save() method.
//            productService.save(product);
//            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
//            return "redirect:/products";
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
//            return "redirect:/products";
//        }
//    }
//
//    @GetMapping("/{id}")
//    public String details(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
//        try {
//            model.addAttribute("product", productService.findById(id));
//            return "products/details";
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
//            return "redirect:/products";
//        }
//    }
//
//    @PostMapping("/{id}/delete")
//    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes attrs) {
//        try {
//            productService.deleteById(id);
//            attrs.addFlashAttribute("successMessage", "Product deleted successfully!");
//        } catch (EntityNotFoundException e) {
//            attrs.addFlashAttribute("errorMessage", "Product not found.");
//        }
//        return "redirect:/products";
//    }
//
//    @GetMapping("/vendors/search")
//    @ResponseBody
//    public List<String> searchVendors(@RequestParam("query") String query) {
//        return vendorService.searchByName(query)
//                               .stream()
//                               .map(Vendor::getName)
//                               .collect(Collectors.toList());
//    }
//}
//

package com.billingsolutions.controller;

import com.billingsolutions.model.Product;
import com.billingsolutions.model.Vendor;
import com.billingsolutions.service.ProductService;
import com.billingsolutions.service.VendorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class ProductController {
	
	private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final VendorService vendorService;

    public ProductController(ProductService productService, VendorService vendorService) {
        this.productService = productService;
        this.vendorService = vendorService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("product") Product product,
                         BindingResult result,
                         @RequestParam(name = "vendorName", required = false) String vendorName,
                         RedirectAttributes attrs,
                         Model model) {
        
        if (product.getSku() != null && product.getSku().trim().isEmpty()) {
            product.setSku(null);
        }

        // This call is now secure! The ProductService automatically checks
        // the SKU only within the current user's business.
        if (product.getSku() != null && productService.existsBySku(product.getSku())) {
            result.addError(new FieldError("product", "sku", "A product with this SKU already exists."));
        }
        
        if (result.hasErrors()) {
            log.warn("Validation failed while creating product: {}", result.getAllErrors());
            model.addAttribute("vendors", vendorService.findAll()); 
            return "products/form";
        }
        
        if (vendorName != null && !vendorName.trim().isEmpty()) {
            vendorService.findByNameIgnoreCase(vendorName.trim())
                .ifPresent(product::setVendor);
        }

        productService.save(product); 
        attrs.addFlashAttribute("successMessage", "Product created successfully!");
        return "redirect:/products";
    }
    
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            model.addAttribute("product", product);
            model.addAttribute("vendors", vendorService.findAll());
            return "products/form";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/products";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("product") Product form,
                         BindingResult result,
                         @RequestParam(name = "vendorName", required = false) String vendorName,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (form.getSku() != null && form.getSku().trim().isEmpty()) {
            form.setSku(null);
        }

        // This call is also secure. The ProductService will only find a product
        // if the SKU belongs to the current user's business.
        if (form.getSku() != null) {
            productService.findBySku(form.getSku()).ifPresent(existingProduct -> {
                if (!existingProduct.getId().equals(id)) {
                    result.addError(new FieldError("product", "sku", "Another product with this SKU already exists."));
                }
            });
        }
    	
        if (result.hasErrors()) {
            model.addAttribute("vendors", vendorService.findAll()); 
            return "products/form";
        }

        try {
            Product product = productService.findById(id); 

            product.setName(form.getName());
            product.setSku(form.getSku());
            product.setSellingPrice(form.getSellingPrice());
            product.setCostPrice(form.getCostPrice());
            product.setUnitType(form.getUnitType());
            product.setNumberOfBoxes(form.getNumberOfBoxes());
            product.setUnitsPerBox(form.getUnitsPerBox());
            product.setPcs(form.getPcs());
            product.setTotalBags(form.getTotalBags());
            product.setWeightPerItem(form.getWeightPerItem());
            product.setTotalBagWeight(form.getTotalBagWeight());
            product.setGstRate(form.getGstRate());
            product.setProductGroup(form.getProductGroup());

            if (vendorName != null && !vendorName.trim().isEmpty()) {
                 vendorService.findByNameIgnoreCase(vendorName.trim())
                    .ifPresent(product::setVendor);
            } else {
                product.setVendor(null);
            }
            
            productService.save(product);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            return "redirect:/products";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/products";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("product", productService.findById(id));
            return "products/details";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/products";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes attrs) {
        try {
            productService.deleteById(id);
            attrs.addFlashAttribute("successMessage", "Product deleted successfully!");
        } catch (EntityNotFoundException e) {
            attrs.addFlashAttribute("errorMessage", "Product not found.");
        }
        return "redirect:/products";
    }

    @GetMapping("/vendors/search")
    @ResponseBody
    public List<String> searchVendors(@RequestParam("query") String query) {
        return vendorService.searchByName(query)
                               .stream()
                               .map(Vendor::getName)
                               .collect(Collectors.toList());
    }
}