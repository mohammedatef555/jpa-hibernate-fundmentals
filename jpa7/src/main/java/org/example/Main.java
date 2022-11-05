package org.example;

import org.example.entities.Professor;
import org.example.entities.Student;

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

        Professor professor = new Professor();
        professor.setName("Prof100");

        Student student = new Student();
        student.setName("Stud100");

        professor.setStudents(new ArrayList<>());
        professor.getStudents().add(student);

        try {
            em.getTransaction().begin();
            em.persist(professor);
            em.persist(student);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}