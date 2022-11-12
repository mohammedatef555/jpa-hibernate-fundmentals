package org.example.entities;

import javax.persistence.*;

@Entity
@Cacheable(value = true) // or just @Cacheable
public class Product extends GeneralEntity {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
    LOAD -- @PostLoad
    UPDATE -- @PreUpdate @PostUpdate
    REMOVE -- @PreRemove @PostRemove
    PERSIST -- @PrePersist @PostPersist
     */

    @PostLoad
    public void postLoad() {
        System.out.println("Entity " + this + " was loaded!");
    }

    @PreRemove
    public void preRemove() {
        System.out.println("Entity " + this + " will be removed.");
    }

    @PostRemove
    public void postRemove() {
        System.out.println("Entity " + this + " was removed.");
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", dateCreated=" + dateCreated +
                ", lastModified=" + lastModified +
                '}';
    }
}
