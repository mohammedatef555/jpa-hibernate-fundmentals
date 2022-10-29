package org.example;

import org.example.entities.Product;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();

        Product product = new Product();
        product.setId(1);
        product.setName("water");
        product.setPrice(1.0);
        product.setExpDate(LocalDate.now());

        try {
            em.getTransaction().begin();
            em.persist(product);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }

    }
}