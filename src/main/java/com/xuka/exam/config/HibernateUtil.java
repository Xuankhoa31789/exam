package com.xuka.exam.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Utility class for Hibernate SessionFactory management
 * Provides singleton access to EntityManagerFactory
 */
public class HibernateUtil {
    private static EntityManagerFactory entityManagerFactory;

    static {
        try {
            // Create EntityManagerFactory from persistence unit
            entityManagerFactory = Persistence.createEntityManagerFactory("default");
        } catch (Exception e) {
            System.err.println("Error initializing Hibernate EntityManagerFactory");
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Get the EntityManagerFactory singleton instance
     *
     * @return EntityManagerFactory instance
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    /**
     * Shutdown Hibernate and close EntityManagerFactory
     */
    public static void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}

