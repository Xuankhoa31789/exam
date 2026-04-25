package com.xuka.exam.dao;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.Exam;
import com.xuka.exam.models.ExamQuestion;
import com.xuka.exam.models.ExamQuestionId;
import com.xuka.exam.models.Question;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data Access Object for ExamQuestion entity (junction table)
 * Handles database operations for exam-question associations
 */
public class ExamQuestionDAO {

    /**
     * Save a new exam-question association to database
     *
     * @param examQuestion ExamQuestion object to save
     * @return true if successful, false otherwise
     */
    public boolean save(ExamQuestion examQuestion) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(examQuestion);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving exam question: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean saveByIds(int examId, int questionId, int questionOrder) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Exam exam = em.getReference(Exam.class, examId);
            Question question = em.getReference(Question.class, questionId);
            em.persist(new ExamQuestion(exam, question, questionOrder));
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving exam question: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Update an existing exam-question association
     *
     * @param examQuestion ExamQuestion object to update
     * @return true if successful, false otherwise
     */
    public boolean update(ExamQuestion examQuestion) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(examQuestion);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating exam question: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete an exam-question association by composite key
     *
     * @param examId Exam ID
     * @param questionId Question ID
     * @return true if successful, false otherwise
     */
    public boolean delete(int examId, int questionId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            ExamQuestionId id = new ExamQuestionId(examId, questionId);
            ExamQuestion examQuestion = em.find(ExamQuestion.class, id);
            if (examQuestion != null) {
                em.remove(examQuestion);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting exam question: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get exam-question by composite key
     *
     * @param examId Exam ID
     * @param questionId Question ID
     * @return ExamQuestion object or null if not found
     */
    public ExamQuestion getById(int examId, int questionId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            ExamQuestionId id = new ExamQuestionId(examId, questionId);
            return em.find(ExamQuestion.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Get all exam-question associations
     *
     * @return List of all exam-question associations
     */
    public List<ExamQuestion> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamQuestion> query = em.createQuery("FROM ExamQuestion ORDER BY exam.examId, questionOrder", ExamQuestion.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get all questions for an exam
     *
     * @param examId Exam ID
     * @return List of exam-question associations for the exam, ordered by question order
     */
    public List<ExamQuestion> getByExam(int examId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamQuestion> query = em.createQuery("FROM ExamQuestion WHERE exam.examId = :examId ORDER BY questionOrder ASC", ExamQuestion.class);
            query.setParameter("examId", examId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get all exams that contain a specific question
     *
     * @param questionId Question ID
     * @return List of exam-question associations containing the question
     */
    public List<ExamQuestion> getByQuestion(int questionId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamQuestion> query = em.createQuery("FROM ExamQuestion WHERE question.questionId = :questionId ORDER BY exam.examId", ExamQuestion.class);
            query.setParameter("questionId", questionId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get number of questions in an exam
     *
     * @param examId Exam ID
     * @return Number of questions in the exam
     */
    public long getQuestionCountForExam(int examId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(eq) FROM ExamQuestion eq WHERE eq.exam.examId = :examId", Long.class);
            query.setParameter("examId", examId);
            Long result = query.getSingleResult();
            return result != null ? result : 0;
        } finally {
            em.close();
        }
    }

    /**
     * Delete all questions from an exam
     *
     * @param examId Exam ID
     * @return true if successful, false otherwise
     */
    public boolean deleteAllByExam(int examId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.createQuery("DELETE FROM ExamQuestion WHERE exam.examId = :examId")
                    .setParameter("examId", examId)
                    .executeUpdate();
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting exam questions: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
