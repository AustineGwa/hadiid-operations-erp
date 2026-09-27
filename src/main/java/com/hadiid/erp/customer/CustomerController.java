package com.hadiid.erp.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/customers")
    public String list(@RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        var result = customerService.search(q, page, 20);
        model.addAttribute("result", result);
        model.addAttribute("q", q);
        return "customer/list";
    }

    @GetMapping("/customers/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getOrThrow(id));
        return "customer/detail";
    }
}
