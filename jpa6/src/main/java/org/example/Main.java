package org.example;

import org.example.entities.Department;
import org.example.entities.Document;
import org.example.entities.Employee;
import org.example.entities.Person;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Person person = new Person();
        person.setName("Mohamed");
        person.setDocuments(new ArrayList<>());

        Document document = new Document();
        document.setName("ABC");
        document.setPerson(person);

        person.getDocuments().add(document);

        try {
            em.getTransaction().begin();
            em.persist(person);
            em.persist(document);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}