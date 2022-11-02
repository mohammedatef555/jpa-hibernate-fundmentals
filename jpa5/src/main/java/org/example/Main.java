package org.example;

import org.example.entities.Company;
import org.example.entities.Detail;
import org.example.entities.Product;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Product product = em.find(Product.class, 4);
//        Product product = new Product();
//        product.setName("Water");
//        product.setPrice(1.0);
//
//        Detail detail = new Detail();
//        detail.setKcal(0);
//        detail.setProduct(product);
//        product.setDetail(detail);
//
//        try {
//            em.getTransaction().begin();
////            em.persist(product);
//            em.persist(detail);
//            em.getTransaction().commit();
//        } catch (Exception e) {
//            em.getTransaction().rollback();
//        } finally {
//            em.close();
//        }
    }
}