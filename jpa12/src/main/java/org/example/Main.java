package org.example;

import org.example.entities.Product;

import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        var emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        var em = emf.createEntityManager();

        em.getTransaction().begin();

        //        String jpql = "SELECT p FROM Product p";
//        TypedQuery<Product> q = em.createQuery(jpql, Product.class);

//        String jpql = "SELECT p FROM Product p WHERE p.price > :price";
//        TypedQuery<Product> q = em.createQuery(jpql, Product.class);
//        q.setParameter("price", 10.0);

//        String jpql = "SELECT SUM(p.price) FROM Product p WHERE p.price > :price";
//        TypedQuery<Double> q = em.createQuery(jpql, Double.class);
//        q.setParameter("price", 10.0);
//
//        double sum = q.getSingleResult();
//        System.out.println(sum);
//        list.forEach(System.out::println);

//        TypedQuery<Product> query =
//                em.createNamedQuery("Product.all", Product.class);
//        query.getResultStream()
//                .filter(p -> p.getPrice() > 10)
//                .forEach(System.out::println);

//          String jpql = "SELECT p FROM Product p WHERE p.price > ?1";
//          TypedQuery<Product> q = em.createQuery(jpql, Product.class);
//          q.setParameter(1, 10.0);

//        String sql = "SELECT * FROM product p";
//        Query q = em.createNativeQuery(sql, Product.class);
//        List<Product> products = q.getResultList();
//        products.forEach(System.out::println);

        String jpql = "SELECT p FROM Product p";

        TypedQuery<Product> q = em.createQuery(jpql, Product.class);
        List<Product> products = q.getResultList();

        products.forEach(System.out::println);


        TypedQuery<Product> namedQuery =
                em.createNamedQuery("Product.all", Product.class);
        List<Product> products1 = namedQuery.getResultList();

        products1.forEach(System.out::println);

        em.getTransaction().commit();
        em.close();
    }
}