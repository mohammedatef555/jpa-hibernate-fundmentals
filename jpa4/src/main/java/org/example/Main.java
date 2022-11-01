package org.example;

import org.example.entities.Building;
import org.example.entities.Company;
import org.example.entities.Department;
import org.example.entities.embeddables.Address;
import org.example.entities.embeddables.BuildingPk;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        // EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // EntityManager
        EntityManager em = emf.createEntityManager();

        Building building = new Building();
        building.setId(new BuildingPk());
        building.getId().setCode("ABC");
        building.getId().setNumber("11");
        building.setName("BUILDING");

        try {
            em.getTransaction().begin();
            em.persist(building);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}