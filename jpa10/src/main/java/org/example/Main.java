package org.example;

import org.example.entities.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();

        Bicycle bicycle = new Bicycle();
        bicycle.setName("Bicycle");
        bicycle.setModel("Bicycle2022");

        Car car = new Car();
        car.setName("Car");
        car.setGas("Petrol");

        try {
            em.getTransaction().begin();
            em.persist(bicycle);
            em.persist(car);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}