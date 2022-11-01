package org.example.entities;

import org.example.entities.embeddables.BuildingPk;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class Building {
    @EmbeddedId
    @AttributeOverride(name = "code", column = @Column(name = "code"))
    private BuildingPk id;

    private String name;

    public BuildingPk getId() {
        return id;
    }

    public void setId(BuildingPk id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
