package com.xuka.exam.dao;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

/**
 * Data Access Object for Student entity
 * Handles database operations using Hibernate
 */
public class StudentDAO {

    /**
     * Save a new student to database
     *
     * @param student Student object to save
     * @return true if successful, false otherwise
     */
    public boolean save(Student student) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(student);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving student: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Update an existing student
     *
     * @param student Student object to update
     * @return true if successful, false otherwise
     */
    public boolean update(Student student) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(student);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete a student by ID
     *
     * @param studentId Student ID
     * @return true if successful, false otherwise
     */
    public boolean delete(int studentId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Student student = em.find(Student.class, studentId);
            if (student != null) {
                em.remove(student);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get student by ID
     *
     * @param studentId Student ID
     * @return Student object or null if not found
     */
    public Student getById(int studentId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Student.class, studentId);
        } finally {
            em.close();
        }
    }

    /**
     * Get all students from database
     *
     * @return List of all students
     */
    public List<Student> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            jakarta.persistence.TypedQuery<Student> query = em.createQuery("FROM Student", Student.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get student by email
     *
     * @param email Student email
     * @return Student object or null if not found
     */
    public Student getByEmail(String email) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            jakarta.persistence.TypedQuery<Student> query = em.createQuery("FROM Student WHERE email = :email", Student.class);
            query.setParameter("email", email);
            List<Student> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Get student by username
     *
     * @param username Student username
     * @return Student object or null if not found
     */
    public Student getByUsername(String username) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            jakarta.persistence.TypedQuery<Student> query = em.createQuery("FROM Student WHERE username = :username", Student.class);
            query.setParameter("username", username);
            List<Student> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
}

