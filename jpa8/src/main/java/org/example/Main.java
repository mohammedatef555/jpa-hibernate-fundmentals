package org.example;

import org.example.entities.*;
import org.example.entities.embeddables.DepartmentDetails;
import org.example.entities.embeddables.Document;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Document document = new Document();
        document.setNumber("123");
        document.setReference("124");

        Person person = new Person();
        person.setName("Person1");
        //person.setPhones(Arrays.asList("123","456"));
        person.setDocuments(new ArrayList<>());
        person.getDocuments().add(document);

        try {
            em.getTransaction().begin();
            em.persist(person);
            em.getTransaction().commit();
        } catch (Exception exception) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}