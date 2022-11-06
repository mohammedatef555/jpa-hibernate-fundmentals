package org.example.entities;

import org.example.entities.embeddables.Document;

import javax.persistence.*;
import java.util.List;

@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    @CollectionTable(
            name = "person_phones", // table name
            joinColumns = @JoinColumn(name = "person") // FK column name
    )
    /*because this is only simple type (String) we can override the
    name using @Column it's an object we wil use AttributeOverride*/
    @Column (name = "phone")
    private List<String> phones;

    @ElementCollection
    @CollectionTable(
            name = "person_documents",// table name
            joinColumns = @JoinColumn(name = "person") // FK column name
    )
    private List<Document> documents;

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

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
