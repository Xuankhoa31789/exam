package com.xuka.exam.dao;

import java.util.List;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.Question;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

/**
 * Data Access Object for Question entity
 * Handles database operations using Hibernate
 */
public class QuestionDAO {

    /**
     * Save a new question to database
     *
     * @param question Question object to save
     * @return true if successful, false otherwise
     */
    public boolean save(Question question) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(question);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving question: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Update an existing question
     *
     * @param question Question object to update
     * @return true if successful, false otherwise
     */
    public boolean update(Question question) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(question);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating question: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete a question by ID
     *
     * @param questionId Question ID
     * @return true if successful, false otherwise
     */
    public boolean delete(String questionId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Question question = em.find(Question.class, questionId);
            if (question != null) {
                em.remove(question);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting question: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get question by ID
     *
     * @param questionId Question ID
     * @return Question object or null if not found
     */
    public Question getById(String questionId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Question.class, questionId);
        } finally {
            em.close();
        }
    }

    /**
     * Get all questions from database
     *
     * @return List of all questions
     */
    public List<Question> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Question> query = em.createQuery("FROM Question", Question.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get questions by subject
     *
     * @param subjectId Subject ID
     * @return List of questions for the subject
     */
    public List<Question> getBySubject(String subjectId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Question> query = em.createQuery("FROM Question WHERE subject.subjectId = :subjectId", Question.class);
            query.setParameter("subjectId", subjectId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get questions by question type
     *
     * @param questionType Question type
     * @return List of questions of the specified type
     */
    public List<Question> getByType(String questionType) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Question> query = em.createQuery("FROM Question WHERE questionType = :questionType", Question.class);
            query.setParameter("questionType", questionType);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get total marks for all questions in a subject
     *
     * @param subjectId Subject ID
     * @return Total marks
     */
    public int getTotalMarksBySubject(String subjectId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Integer> query = em.createQuery("SELECT SUM(q.marks) FROM Question q WHERE q.subject.subjectId = :subjectId", Integer.class);
            query.setParameter("subjectId", subjectId);
            Integer result = query.getSingleResult();
            return result != null ? result : 0;
        } finally {
            em.close();
        }
    }
}
