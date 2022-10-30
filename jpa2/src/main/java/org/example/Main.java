package org.example;

import org.example.entities.Item;
import org.example.entities.Product;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();

//        Product product = new Product();
//        product.setName("water");
        Item item = new Item();
        item.setName("Item #3");

        try {
            em.getTransaction().begin();
            em.persist(item);
            em.getTransaction().commit();
        } catch (Exception e) {
             em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}