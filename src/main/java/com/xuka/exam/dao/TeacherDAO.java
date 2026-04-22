package com.xuka.exam.dao;

import java.util.List;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.Teacher;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

/**
 * Data Access Object for Teacher entity
 * Handles database operations using Hibernate
 */
public class TeacherDAO {

    /**
     * Save a new teacher to database
     *
     * @param teacher Teacher object to save
     * @return true if successful, false otherwise
     */
    public boolean save(Teacher teacher) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(teacher);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving teacher: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Update an existing teacher
     *
     * @param teacher Teacher object to update
     * @return true if successful, false otherwise
     */
    public boolean update(Teacher teacher) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(teacher);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating teacher: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete a teacher by ID
     *
     * @param teacherId 
     * @return 
     */
    public boolean delete(int teacherId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Teacher teacher = em.find(Teacher.class, teacherId);
            if (teacher != null) {
                em.remove(teacher);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting teacher: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get teacher by ID
     *
     * @param teacherId 
     * @return 
     */
    public Teacher getById(int teacherId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Teacher.class, teacherId);
        } finally {
            em.close();
        }
    }

    /**
     * 
     *
     * @return 
     */
    public List<Teacher> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Teacher> query = em.createQuery("FROM Teacher", Teacher.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get teacher by email
     *
     * @param email 
     * @return 
     */
    public Teacher getByEmail(String email) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Teacher> query = em.createQuery("FROM Teacher WHERE email = :email", Teacher.class);
            query.setParameter("email", email);
            List<Teacher> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Get teacher by username
     *
     * @param username 
     * @return 
     */
    public Teacher getByUsername(String username) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Teacher> query = em.createQuery("FROM Teacher WHERE username = :username", Teacher.class);
            query.setParameter("username", username);
            List<Teacher> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
}
