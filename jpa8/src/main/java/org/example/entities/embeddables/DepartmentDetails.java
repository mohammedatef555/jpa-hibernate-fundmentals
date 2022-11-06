package org.example.entities.embeddables;

import org.example.entities.Department;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class DepartmentDetails {
    private Integer contractNo;

    @ManyToOne
    private Department department;

    public Integer getContractNo() {
        return contractNo;
    }

    public void setContractNo(Integer contractNo) {
        this.contractNo = contractNo;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
