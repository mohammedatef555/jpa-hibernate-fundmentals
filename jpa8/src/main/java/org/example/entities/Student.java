package org.example.entities;

import javax.persistence.*;

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    @AssociationOverride(
            name = "professors", // the name of  List<Professor> in ProfessorDetails
            joinTable = @JoinTable(
                    name = "student_prof", // overriding the default table name
                    joinColumns = @JoinColumn(name = "student"), // overriding the default column name
                    inverseJoinColumns = @JoinColumn(name = "professor")// overriding the default column name
            )
    )
    private ProfessorDetails details;

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

    public ProfessorDetails getDetails() {
        return details;
    }

    public void setDetails(ProfessorDetails details) {
        this.details = details;
    }
}
