package com.xuka.exam.dao;

import java.util.List;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.SubjectRegistration;
import com.xuka.exam.models.SubjectRegistrationId;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

/**
 * Data Access Object for SubjectRegistration entity (junction table)
 * Handles database operations for student-subject enrollment relationships
 */
public class SubjectRegistrationDAO {

    /**
     * Register a user (student) for a subject
     *
     * @param subjectRegistration SubjectRegistration object to save
     * @return true if successful, false otherwise
     */
    public boolean save(SubjectRegistration subjectRegistration) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(subjectRegistration);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving subject registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Register a user for a subject by ID.
     *
     * @param subjectId Subject ID
     * @param ucInfoId User info ID
     * @return true if successful or already registered, false otherwise
     */
    public boolean registerByIds(int subjectId, int ucInfoId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            SubjectRegistrationId id = new SubjectRegistrationId(subjectId, ucInfoId);
            SubjectRegistration existingRegistration = em.find(SubjectRegistration.class, id);
            if (existingRegistration != null) {
                transaction.commit();
                return true;
            }

            SubjectRegistration registration = new SubjectRegistration();
            registration.setSubject(em.getReference(com.xuka.exam.models.Subject.class, subjectId));
            registration.setUserInfo(em.getReference(com.xuka.exam.models.UserInfo.class, ucInfoId));
            em.persist(registration);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error registering subject by IDs: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Unregister a user from a subject
     *
     * @param subjectId Subject ID
     * @param ucInfoId User info ID
     * @return true if successful, false otherwise
     */
    public boolean delete(int subjectId, int ucInfoId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            SubjectRegistrationId id = new SubjectRegistrationId(subjectId, ucInfoId);
            SubjectRegistration registration = em.find(SubjectRegistration.class, id);
            if (registration != null) {
                em.remove(registration);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting subject registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get subject registration by composite key
     *
     * @param subjectId Subject ID
     * @param ucInfoId User info ID
     * @return SubjectRegistration object or null if not found
     */
    public SubjectRegistration getById(int subjectId, int ucInfoId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            SubjectRegistrationId id = new SubjectRegistrationId(subjectId, ucInfoId);
            return em.find(SubjectRegistration.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Get all subject registrations
     *
     * @return List of all subject registrations
     */
    public List<SubjectRegistration> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<SubjectRegistration> query = em.createQuery("FROM SubjectRegistration ORDER BY subject.subjectId, userInfo.ucInfoId", SubjectRegistration.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get all subjects registered by a user
     *
     * @param ucInfoId User info ID
     * @return List of subject registrations for the user
     */
    public List<SubjectRegistration> getByUserInfo(int ucInfoId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<SubjectRegistration> query = em.createQuery("FROM SubjectRegistration WHERE userInfo.ucInfoId = :ucInfoId ORDER BY subject.subjectId", SubjectRegistration.class);
            query.setParameter("ucInfoId", ucInfoId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get all users registered for a subject
     *
     * @param subjectId Subject ID
     * @return List of subject registrations for the subject
     */
    public List<SubjectRegistration> getBySubject(int subjectId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<SubjectRegistration> query = em.createQuery("FROM SubjectRegistration WHERE subject.subjectId = :subjectId ORDER BY userInfo.ucInfoId", SubjectRegistration.class);
            query.setParameter("subjectId", subjectId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Check if a user is registered for a subject
     *
     * @param subjectId Subject ID
     * @param ucInfoId User info ID
     * @return true if registered, false otherwise
     */
    public boolean isRegistered(int subjectId, int ucInfoId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            SubjectRegistrationId id = new SubjectRegistrationId(subjectId, ucInfoId);
            SubjectRegistration registration = em.find(SubjectRegistration.class, id);
            return registration != null;
        } finally {
            em.close();
        }
    }

    /**
     * Get count of users registered for a subject
     *
     * @param subjectId Subject ID
     * @return Number of users registered for the subject
     */
    public long getRegistrationCount(int subjectId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(sr) FROM SubjectRegistration sr WHERE sr.subject.subjectId = :subjectId", Long.class);
            query.setParameter("subjectId", subjectId);
            Long result = query.getSingleResult();
            return result != null ? result : 0;
        } finally {
            em.close();
        }
    }

    /**
     * Delete all registrations for a subject (when subject is deleted)
     *
     * @param subjectId Subject ID
     * @return true if successful, false otherwise
     */
    public boolean deleteAllBySubject(int subjectId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.createQuery("DELETE FROM SubjectRegistration WHERE subject.subjectId = :subjectId")
                    .setParameter("subjectId", subjectId)
                    .executeUpdate();
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting subject registrations: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete all registrations for a user
     *
     * @param ucInfoId User info ID
     * @return true if successful, false otherwise
     */
    public boolean deleteAllByUserInfo(int ucInfoId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.createQuery("DELETE FROM SubjectRegistration WHERE userInfo.ucInfoId = :ucInfoId")
                    .setParameter("ucInfoId", ucInfoId)
                    .executeUpdate();
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting user registrations: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
