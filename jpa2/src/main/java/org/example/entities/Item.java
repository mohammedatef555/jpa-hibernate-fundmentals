package org.example.entities;

import javax.persistence.*;

@Entity
public class Item {
    @Id
    @TableGenerator(
            name = "key_generator",
            table = "key_generator",
            pkColumnName = "key_name",
            pkColumnValue = "item_sequence",
            valueColumnName = "key_value"
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "key_generator")
    private Integer id;
    private String name;

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
}
