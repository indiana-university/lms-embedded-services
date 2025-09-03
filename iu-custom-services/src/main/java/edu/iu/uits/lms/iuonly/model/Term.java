package edu.iu.uits.lms.iuonly.model;

import lombok.Data;

@Data
public class Term {
    private String termId;
    private String name;
    private String status;
    private String startDate;
    private String endDate;

    public Term(ProvisioningTerm provisioningTerm, String status) {
        this.termId = provisioningTerm.getSisTermId();
        this.name = provisioningTerm.getName();
        this.startDate = provisioningTerm.getStartDate();
        this.endDate = provisioningTerm.getEndDate();
        this.status = status;
    }
}
