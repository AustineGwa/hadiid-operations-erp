package com.hadiid.erp.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** §E.8: work-in-progress count per section, computed on demand (never stored). */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WipRollupDto {
    private Integer sectionId;
    private String sectionLabel;
    private long wipCount; // jobs opened, not yet released, as of "now"
}
