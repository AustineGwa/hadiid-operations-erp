package com.hadiid.erp.reference;

import com.hadiid.erp.common.audit.AuditService;
import com.hadiid.erp.reference.dto.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReferenceDataService {

    private final ReferenceDataRepository repository;
    private final AuditService auditService;

    public ReferenceDataService(ReferenceDataRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    public List<VehicleSectionDto> sections() {
        return repository.findAllSections();
    }

    public List<BodyTypeDto> bodyTypes() {
        return repository.findAllBodyTypes();
    }

    public List<BodyTypeDto> bodyTypesForSection(int sectionId) {
        return repository.findActiveBodyTypesForSection(sectionId);
    }

    public List<FabricationStageDto> stages() {
        return repository.findAllStages();
    }

    public List<PaymentMethodDto> paymentMethods() {
        return repository.findAllPaymentMethods();
    }

    public List<CalendarDayDto> calendarRange(LocalDate from, LocalDate to) {
        return repository.findCalendarRange(from, to);
    }

    public void addBodyType(Long actingUserId, int sectionId, String code, String label) {
        repository.addBodyType(sectionId, code, label);
        auditService.record(actingUserId, "BODY_TYPE_CREATE", "REFERENCE_DATA", null, null, code + "/" + label, null);
    }

    public void setBodyTypeActive(Long actingUserId, int id, boolean active) {
        repository.setBodyTypeActive(id, active);
        auditService.record(actingUserId, active ? "BODY_TYPE_ENABLE" : "BODY_TYPE_DISABLE", "REFERENCE_DATA", (long) id);
    }

    /** Sets or clears a Day Status override for one calendar date (§B.10/§H.4). */
    public void setCalendarDay(Long actingUserId, LocalDate date, String status, String note) {
        repository.upsertCalendarDay(date, status, note, actingUserId);
        auditService.record(actingUserId, "CALENDAR_DAY_SET", "PRODUCTION_CALENDAR", null, null, date + "=" + status, note);
    }
}
