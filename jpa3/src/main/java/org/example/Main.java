package org.example;

import org.example.entities.*;
import org.example.entities.embeddables.Address;
import org.example.entities.enums.Currency;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

//        Price price = new Price();
//        price.setAmount(1.0);
//        price.setCurrency(Currency.EUR);

//        Product product = new Product();
//        product.setExpDate(LocalDate.now());

//        Event event = new Event();
//        event.setEventTime(ZonedDateTime.now(ZoneId.of("Africa/Cairo")));

//        Employee employee = new Employee();
//        employee.setEmpDate(new Date());

        Company company = new Company();
        company.setName("ABC");
        company.setAddress(new Address());
        company.getAddress().setNumber("1");
        company.getAddress().setStreet("street 1");
        company.getAddress().setCity("Cairo");

        try {
            em.getTransaction().begin();
//            em.persist(price);
//            em.persist(product);
//            em.persist(event);
//            em.persist(employee);
            em.persist(company);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
             em.close();
        }
    }
}