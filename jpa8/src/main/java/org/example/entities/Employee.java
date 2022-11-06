package org.example.entities;

import org.example.entities.embeddables.DepartmentDetails;

import javax.persistence.*;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    @AttributeOverride(name = "contractNo", column = @Column(name = "contract_no"))
    @AssociationOverride(name = "department", joinColumns = @JoinColumn(name = "department"))
    private DepartmentDetails details;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DepartmentDetails getDetails() {
        return details;
    }

    public void setDetails(DepartmentDetails details) {
        this.details = details;
    }
}
