package com.xuka.exam.dao;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.StudentAnswer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data Access Object for StudentAnswer entity
 * Handles database operations for student answers to exam questions
 */
public class StudentAnswerDAO {

    /**
     * Save a new student answer to database
     *
     * @param studentAnswer StudentAnswer object to save
     * @return true if successful, false otherwise
     */
    public boolean save(StudentAnswer studentAnswer) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(studentAnswer);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving student answer: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Update an existing student answer
     *
     * @param studentAnswer StudentAnswer object to update
     * @return true if successful, false otherwise
     */
    public boolean update(StudentAnswer studentAnswer) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(studentAnswer);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating student answer: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete a student answer by ID
     *
     * @param answerId Answer ID
     * @return true if successful, false otherwise
     */
    public boolean delete(int answerId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            StudentAnswer studentAnswer = em.find(StudentAnswer.class, answerId);
            if (studentAnswer != null) {
                em.remove(studentAnswer);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting student answer: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get student answer by ID
     *
     * @param answerId Answer ID
     * @return StudentAnswer object or null if not found
     */
    public StudentAnswer getById(int answerId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(StudentAnswer.class, answerId);
        } finally {
            em.close();
        }
    }

    /**
     * Get all student answers
     *
     * @return List of all student answers
     */
    public List<StudentAnswer> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<StudentAnswer> query = em.createQuery("FROM StudentAnswer", StudentAnswer.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get all answers for an exam attempt
     *
     * @param attemptId Exam attempt ID
     * @return List of answers for the attempt
     */
    public List<StudentAnswer> getByAttempt(int attemptId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<StudentAnswer> query = em.createQuery("FROM StudentAnswer WHERE attempt.attemptId = :attemptId", StudentAnswer.class);
            query.setParameter("attemptId", attemptId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get all answers for a specific question
     *
     * @param questionId Question ID
     * @return List of answers for the question
     */
    public List<StudentAnswer> getByQuestion(int questionId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<StudentAnswer> query = em.createQuery("FROM StudentAnswer WHERE question.questionId = :questionId", StudentAnswer.class);
            query.setParameter("questionId", questionId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get total score for an attempt
     *
     * @param attemptId Exam attempt ID
     * @return Total obtained marks
     */
    public int getTotalScoreForAttempt(int attemptId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Integer> query = em.createQuery("SELECT COALESCE(SUM(sa.obtainedMarks), 0) FROM StudentAnswer sa WHERE sa.attempt.attemptId = :attemptId", Integer.class);
            query.setParameter("attemptId", attemptId);
            Integer result = query.getSingleResult();
            return result != null ? result : 0;
        } finally {
            em.close();
        }
    }

    /**
     * Get number of answered questions in an attempt
     *
     * @param attemptId Exam attempt ID
     * @return Number of questions answered (not null)
     */
    public long getAnsweredQuestionCount(int attemptId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.attempt.attemptId = :attemptId AND sa.answerText IS NOT NULL", Long.class);
            query.setParameter("attemptId", attemptId);
            Long result = query.getSingleResult();
            return result != null ? result : 0;
        } finally {
            em.close();
        }
    }
}
