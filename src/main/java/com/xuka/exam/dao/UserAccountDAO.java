package com.xuka.exam.dao;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.UserAccount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data Access Object for UserAccount entity
 * Handles database operations for user authentication and account management
 */
public class UserAccountDAO {

    /**
     * Save a new user account to database
     *
     * @param userAccount UserAccount object to save
     * @return true if successful, false otherwise
     */
    public boolean save(UserAccount userAccount) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(userAccount);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving user account: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Update an existing user account
     *
     * @param userAccount UserAccount object to update
     * @return true if successful, false otherwise
     */
    public boolean update(UserAccount userAccount) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(userAccount);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating user account: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete a user account by ID
     *
     * @param ucId User account ID
     * @return true if successful, false otherwise
     */
    public boolean delete(int ucId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            UserAccount userAccount = em.find(UserAccount.class, ucId);
            if (userAccount != null) {
                em.remove(userAccount);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting user account: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get user account by ID
     *
     * @param ucId User account ID
     * @return UserAccount object or null if not found
     */
    public UserAccount getById(int ucId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(UserAccount.class, ucId);
        } finally {
            em.close();
        }
    }

    /**
     * Get all user accounts
     *
     * @return List of all user accounts
     */
    public List<UserAccount> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<UserAccount> query = em.createQuery("FROM UserAccount", UserAccount.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get user account by username
     *
     * @param username Username
     * @return UserAccount object or null if not found
     */
    public UserAccount getByUsername(String username) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<UserAccount> query = em.createQuery("FROM UserAccount WHERE username = :username", UserAccount.class);
            query.setParameter("username", username);
            List<UserAccount> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Get all accounts with specific role
     *
     * @param role Role (0 = Student, 1 = Teacher)
     * @return List of user accounts with specified role
     */
    public List<UserAccount> getByRole(int role) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<UserAccount> query = em.createQuery("FROM UserAccount WHERE role = :role", UserAccount.class);
            query.setParameter("role", role);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
