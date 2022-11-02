package org.example.entities;

import javax.persistence.*;

@Entity
@SecondaryTable(name = "address", pkJoinColumns = @PrimaryKeyJoinColumn(name = "company"))
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    private String name;

    @Column(table = "address")
    private String street;
    @Column(table = "address")
    private String number;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
