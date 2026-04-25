package com.xuka.exam.dao;

import java.util.List;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.Subject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

/**
 * Data Access Object for Subject entity
 * Handles database operations using Hibernate
 */
public class SubjectDAO {

    /**
     * Save a new subject to database
     *
     * @param subject Subject object to save
     * @return true if successful, false otherwise
     */
    public boolean save(Subject subject) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(subject);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving subject: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Update an existing subject
     *
     * @param subject Subject object to update
     * @return true if successful, false otherwise
     */
    public boolean update(Subject subject) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(subject);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating subject: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete a subject by ID
     *
     * @param subjectId Subject ID
     * @return true if successful, false otherwise
     */
    public boolean delete(int subjectId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Subject subject = em.find(Subject.class, subjectId);
            if (subject != null) {
                em.remove(subject);
                transaction.commit();
                return true;
            }
            transaction.rollback();
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting subject: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get subject by ID
     *
     * @param subjectId Subject ID
     * @return Subject object or null if not found
     */
    public Subject getById(int subjectId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Subject.class, subjectId);
        } finally {
            em.close();
        }
    }

    /**
     * Get all subjects from database
     *
     * @return List of all subjects
     */
    public List<Subject> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Subject> query = em.createQuery("FROM Subject ORDER BY subjectCode", Subject.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get subject by name
     *
     * @param subjectName Subject name
     * @return Subject object or null if not found
     */
    public Subject getByName(String subjectName) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Subject> query = em.createQuery("FROM Subject WHERE subjectName = :subjectName", Subject.class);
            query.setParameter("subjectName", subjectName);
            List<Subject> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Get subject by code
     *
     * @param subjectCode Subject code
     * @return Subject object or null if not found
     */
    public Subject getByCode(String subjectCode) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Subject> query = em.createQuery("FROM Subject WHERE subjectCode = :subjectCode", Subject.class);
            query.setParameter("subjectCode", subjectCode);
            List<Subject> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
}
