package com.hadiid.erp.identity;

import com.hadiid.erp.common.audit.AuditService;
import com.hadiid.erp.common.exception.BusinessRuleException;
import com.hadiid.erp.common.exception.NotFoundException;
import com.hadiid.erp.identity.dto.UserCreateRequest;
import com.hadiid.erp.identity.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** §G/§M: Admin-only user management (create, edit role/status, disable). */
@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public List<UserDto> listAll() {
        return repository.findAll();
    }

    public UserDto getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Transactional
    public Long create(UserCreateRequest req, Long actingUserId) {
        if (req.getRawPassword() == null || req.getRawPassword().length() < 8) {
            throw new BusinessRuleException("Password must be at least 8 characters.");
        }
        String hash = passwordEncoder.encode(req.getRawPassword());
        Long id = repository.insert(req, hash);
        auditService.record(actingUserId, "USER_CREATE", "USER", id, null, req.getUsername(), null);
        return id;
    }

    @Transactional
    public void updateProfile(Long id, String fullName, String email, String phone, Integer roleId, String team, Long actingUserId) {
        repository.updateProfile(id, fullName, email, phone, roleId, team);
        auditService.record(actingUserId, "USER_EDIT", "USER", id, null, fullName, null);
    }

    @Transactional
    public void setStatus(Long id, String status, Long actingUserId) {
        if (!"ACTIVE".equals(status) && !"DISABLED".equals(status)) {
            throw new BusinessRuleException("Invalid status: " + status);
        }
        if (id.equals(actingUserId) && "DISABLED".equals(status)) {
            throw new BusinessRuleException("You cannot disable your own account.");
        }
        repository.updateStatus(id, status);
        auditService.record(actingUserId, "DISABLED".equals(status) ? "USER_DISABLE" : "USER_ENABLE", "USER", id);
    }
}
