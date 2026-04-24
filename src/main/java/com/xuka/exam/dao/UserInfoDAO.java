package com.xuka.exam.dao;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.UserInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data Access Object for UserInfo entity
 * Handles database operations for user profile information (Students and Teachers)
 */
public class UserInfoDAO {

    /**
     * Save a new user info to database
     *
     * @param userInfo UserInfo object to save
     * @return true if successful, false otherwise
     */
    public boolean save(UserInfo userInfo) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(userInfo);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving user info: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Update an existing user info
     *
     * @param userInfo UserInfo object to update
     * @return true if successful, false otherwise
     */
    public boolean update(UserInfo userInfo) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(userInfo);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating user info: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete user info by ID
     *
     * @param ucInfoId User info ID
     * @return true if successful, false otherwise
     */
    public boolean delete(int ucInfoId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            UserInfo userInfo = em.find(UserInfo.class, ucInfoId);
            if (userInfo != null) {
                em.remove(userInfo);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting user info: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get user info by ID
     *
     * @param ucInfoId User info ID
     * @return UserInfo object or null if not found
     */
    public UserInfo getById(int ucInfoId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(UserInfo.class, ucInfoId);
        } finally {
            em.close();
        }
    }

    /**
     * Get all user info records
     *
     * @return List of all user info records
     */
    public List<UserInfo> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<UserInfo> query = em.createQuery("FROM UserInfo", UserInfo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get user info by email
     *
     * @param email Email address
     * @return UserInfo object or null if not found
     */
    public UserInfo getByEmail(String email) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<UserInfo> query = em.createQuery("FROM UserInfo WHERE email = :email", UserInfo.class);
            query.setParameter("email", email);
            List<UserInfo> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Get user info by code
     *
     * @param code User code (student ID, teacher ID, etc.)
     * @return UserInfo object or null if not found
     */
    public UserInfo getByCode(String code) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<UserInfo> query = em.createQuery("FROM UserInfo WHERE code = :code", UserInfo.class);
            query.setParameter("code", code);
            List<UserInfo> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Get all users by department
     *
     * @param department Department name
     * @return List of users in the department
     */
    public List<UserInfo> getByDepartment(String department) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<UserInfo> query = em.createQuery("FROM UserInfo WHERE department = :department", UserInfo.class);
            query.setParameter("department", department);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get teachers (role = 1)
     *
     * @return List of all teachers
     */
    public List<UserInfo> getAllTeachers() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<UserInfo> query = em.createQuery("FROM UserInfo ui WHERE ui.userAccount.role = 1", UserInfo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get students (role = 0)
     *
     * @return List of all students
     */
    public List<UserInfo> getAllStudents() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<UserInfo> query = em.createQuery("FROM UserInfo ui WHERE ui.userAccount.role = 0", UserInfo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
