package org.example.entities;

import org.example.entities.embeddables.Address;

import javax.persistence.*;

@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "no", column = @Column(name = "number")),
            @AttributeOverride(name = "str", column = @Column(name = "street"))
    })
    private Address address;

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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
