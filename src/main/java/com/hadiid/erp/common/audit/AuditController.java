package com.hadiid.erp.common.audit;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/settings/audit")
public class AuditController {

    private final AuditLogRepository repository;

    public AuditController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String module, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("logs", repository.search(module, null, 50, page * 50));
        model.addAttribute("module", module);
        model.addAttribute("page", page);
        return "settings/audit";
    }
}
