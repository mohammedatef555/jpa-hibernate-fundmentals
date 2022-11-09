package org.example;

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



        em.getTransaction().begin();

        Product p = em.find(Product.class, 2L);
        p.setName("Product"); // since this instance is on the context it will update it in the DB also
        em.refresh(p); // this will update the instance with what we have in the DB

        em.getTransaction().commit();
        em.close();

    }
}