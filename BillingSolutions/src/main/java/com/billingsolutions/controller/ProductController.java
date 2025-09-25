package com.billingsolutions.controller;

import com.billingsolutions.model.Product;
import com.billingsolutions.model.UnitType;
import com.billingsolutions.model.Vendor;
import com.billingsolutions.repository.ProductRepository;
import com.billingsolutions.repository.VendorRepository;
import com.billingsolutions.service.ProductService;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping("/products")
public class ProductController {
	
	private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    public ProductController(ProductService productService, VendorRepository vendorRepository, ProductRepository productRepository) {
        this.productService = productService;
        this.vendorRepository = vendorRepository;
        this.productRepository = productRepository;
    }
    // Show all products
    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "products/list";
    }

    // Create product form
    @GetMapping("/new")
    public String createForm(Model model) {
    	
        model.addAttribute("product", new Product());
        model.addAttribute("vendors", vendorRepository.findAll());
        return "products/form";
    }

    // Save new product
//    @PostMapping
//    public String create(@Valid @ModelAttribute("product") Product product,
//                         BindingResult result) {
//        if (result.hasErrors()) {
//        	log.warn("Validation failed while creating product: {}", result.getAllErrors());
//            return "products/form";
//        }
//        log.info("Creating new product: {}", product.getName());
//        applyBusinessLogic(product);
//        setVendorByName(product, vendorName);
//        productService.save(product);
//        
//        log.info("Product saved successfully!");
//
//        return "redirect:/products";
//    }
    @PostMapping
    public String create(@Valid @ModelAttribute("product") Product product,
                         BindingResult result,
                         @RequestParam(name = "vendorName", required = false) String vendorName, // take vendor name
                         RedirectAttributes attrs,
                         Model model) {
        if (result.hasErrors()) {
            log.warn("Validation failed while creating product: {}", result.getAllErrors());
            model.addAttribute("vendors", vendorRepository.findAll());
            return "products/form";
        }

        setVendorByName(product, vendorName); // set vendor if name matches
        applyBusinessLogic(product);
        productService.save(product);

        attrs.addFlashAttribute("successMessage", "Product created successfully!");
        return "redirect:/products";
    }
    
    private void applyBusinessLogic(Product product) {
        if (product.getUnitType() == UnitType.PCS) {
            product.setPcs(product.getUnitsPerBox() * product.getNumberOfBoxes());
            product.setTotalBags(0);
            product.setWeightPerItem(BigDecimal.ZERO);
            product.setTotalBagWeight(BigDecimal.ZERO);
        } else if (product.getUnitType() == UnitType.KG) {
            if (product.getWeightPerItem() != null && product.getWeightPerItem().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal x = product.getTotalBagWeight()
                        .divide(product.getWeightPerItem(), 2, RoundingMode.HALF_UP);
                int intValue = x.intValue();
                product.setPcs(intValue * product.getTotalBags());
            } else {
                product.setPcs(0);
            }
            product.setUnitsPerBox(0);
        }
    }
    // Edit product form
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("vendors", vendorRepository.findAll());
        return "products/form";
    }

    // Update product
    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("product") Product form,
                         @RequestParam(name = "vendorName", required = false) String vendorName,
                         BindingResult result) {
    	
        if (result.hasErrors()) return "products/form";

        Product product = productService.findById(id);

        // Common fields
        product.setName(form.getName());
        product.setSku(form.getSku());
        product.setSellingPrice(form.getSellingPrice());
        product.setCostPrice(form.getCostPrice());
        //product.setVendor(form.getVendor());
        setVendorByName(product, vendorName);
        product.setUnitType(form.getUnitType());

        if (form.getUnitType() == UnitType.PCS) {
            product.setPcs(form.getUnitsPerBox() * form.getNumberOfBoxes());
            product.setUnitsPerBox(form.getUnitsPerBox());
            product.setNumberOfBoxes(form.getNumberOfBoxes());
            product.setTotalBags(0);
            product.setWeightPerItem(new BigDecimal(0.0));
            product.setTotalBagWeight(new BigDecimal(0.0));
        } else if (form.getUnitType() == UnitType.KG) {
        	product.setWeightPerItem(form.getWeightPerItem());
            product.setTotalBagWeight(form.getTotalBagWeight());
            product.setTotalBags(form.getTotalBags());
            product.setUnitsPerBox(0);
            BigDecimal x = form.getTotalBagWeight().divide(form.getWeightPerItem());
            int intValue = x.intValue();
            product.setPcs(intValue * form.getTotalBags());
        }
        System.out.println(product.getPcs());

        productService.save(product);
        return "redirect:/products";
    }
 // Show product details
    @GetMapping("/{id}")
    public String details(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            return "redirect:/products";
        }
        if (product.getUnitType() == null) {
            product.setUnitType(UnitType.PCS); // default
        }
        model.addAttribute("product", product);
        return "products/details";
    }


    
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id) {
        productService.deleteById(id);
        return "redirect:/products";
    }
    
    private void setVendorByName(Product product, String vendorName) {
        if (vendorName != null && !vendorName.trim().isEmpty()) {
            Optional<Vendor> vendorOpt = vendorRepository.findByNameIgnoreCase(vendorName.trim());
            product.setVendor(vendorOpt.orElse(null)); // Set vendor if found, otherwise set to null
        } else {
            product.setVendor(null); // Ensure vendor is cleared if the name is empty
        }
    }
    @GetMapping("/vendors/search")
    @ResponseBody
    public List<String> searchVendors(@RequestParam("query") String query) {
        return vendorRepository.findByNameContainingIgnoreCase(query)
                               .stream()
                               .map(Vendor::getName)
                               .toList();
    }

}
