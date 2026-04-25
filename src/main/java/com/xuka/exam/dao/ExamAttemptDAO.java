package com.xuka.exam.dao;

import java.util.List;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.ExamAttempt;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

/**
 * Data Access Object for ExamAttempt entity
 * Handles database operations for student exam attempts
 */
public class ExamAttemptDAO {

    /**
     * Save a new exam attempt to database
     *
     * @param examAttempt ExamAttempt object to save
     * @return true if successful, false otherwise
     */
    public boolean save(ExamAttempt examAttempt) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(examAttempt);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving exam attempt: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Update an existing exam attempt
     *
     * @param examAttempt ExamAttempt object to update
     * @return true if successful, false otherwise
     */
    public boolean update(ExamAttempt examAttempt) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(examAttempt);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating exam attempt: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete an exam attempt by ID
     *
     * @param attemptId Attempt ID
     * @return true if successful, false otherwise
     */
    public boolean delete(int attemptId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            ExamAttempt examAttempt = em.find(ExamAttempt.class, attemptId);
            if (examAttempt != null) {
                em.remove(examAttempt);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting exam attempt: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get exam attempt by ID
     *
     * @param attemptId Attempt ID
     * @return ExamAttempt object or null if not found
     */
    public ExamAttempt getById(int attemptId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(ExamAttempt.class, attemptId);
        } finally {
            em.close();
        }
    }

    /**
     * Get all exam attempts
     *
     * @return List of all exam attempts
     */
    public List<ExamAttempt> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamAttempt> query = em.createQuery("FROM ExamAttempt ORDER BY startTime DESC", ExamAttempt.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get attempts by student
     *
     * @param studentId Student user info ID
     * @return List of exam attempts by the student
     */
    public List<ExamAttempt> getByStudent(int studentId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamAttempt> query = em.createQuery("FROM ExamAttempt WHERE student.ucInfoId = :studentId ORDER BY startTime DESC", ExamAttempt.class);
            query.setParameter("studentId", studentId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get attempts for a specific exam
     *
     * @param examId Exam ID
     * @return List of all attempts for the exam
     */
    public List<ExamAttempt> getByExam(int examId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamAttempt> query = em.createQuery("FROM ExamAttempt WHERE exam.examId = :examId ORDER BY startTime DESC", ExamAttempt.class);
            query.setParameter("examId", examId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get attempts by status
     *
     * @param status Attempt status (In Progress, Completed, Abandoned)
     * @return List of attempts with specified status
     */
    public List<ExamAttempt> getByStatus(String status) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamAttempt> query = em.createQuery("FROM ExamAttempt WHERE status = :status ORDER BY startTime DESC", ExamAttempt.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get completed attempts for an exam
     *
     * @param examId Exam ID
     * @return List of completed attempts
     */
    public List<ExamAttempt> getCompletedAttempts(int examId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamAttempt> query = em.createQuery("FROM ExamAttempt WHERE exam.examId = :examId AND status = 'Completed' ORDER BY startTime DESC", ExamAttempt.class);
            query.setParameter("examId", examId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get in-progress attempts for an exam
     *
     * @param examId Exam ID
     * @return List of in-progress attempts
     */
    public List<ExamAttempt> getInProgressAttempts(int examId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamAttempt> query = em.createQuery("FROM ExamAttempt WHERE exam.examId = :examId AND status = 'In Progress'", ExamAttempt.class);
            query.setParameter("examId", examId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get all attempts for a teacher's exams
     *
     * @param teacherId Teacher user info ID
     * @return List of all attempts for teacher's exams
     */
    public List<ExamAttempt> getAttemptsByTeacher(int teacherId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamAttempt> query = em.createQuery(
                "FROM ExamAttempt ea WHERE ea.exam.teacher.ucInfoId = :teacherId ORDER BY ea.startTime DESC",
                ExamAttempt.class
            );
            query.setParameter("teacherId", teacherId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get recent attempts for a teacher's exams
     *
     * @param teacherId Teacher user info ID
     * @param limit Number of recent attempts to retrieve
     * @return List of recent attempts
     */
    public List<ExamAttempt> getRecentAttemptsByTeacher(int teacherId, int limit) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ExamAttempt> query = em.createQuery(
                "FROM ExamAttempt ea WHERE ea.exam.teacher.ucInfoId = :teacherId ORDER BY ea.startTime DESC",
                ExamAttempt.class
            );
            query.setParameter("teacherId", teacherId);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get average score for a teacher's exams
     *
     * @param teacherId Teacher user info ID
     * @return Average score or 0 if no attempts
     */
    public double getAverageScoreByTeacher(int teacherId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Double> query = em.createQuery(
                "SELECT AVG(ea.score) FROM ExamAttempt ea WHERE ea.exam.teacher.ucInfoId = :teacherId AND ea.status = 'Completed'",
                Double.class
            );
            query.setParameter("teacherId", teacherId);
            Double result = query.getSingleResult();
            return result != null ? result : 0.0;
        } finally {
            em.close();
        }
    }

    /**
     * Get count of completed attempts for a teacher
     *
     * @param teacherId Teacher user info ID
     * @return Count of completed attempts
     */
    public long getCompletedAttemptsCountByTeacher(int teacherId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(ea) FROM ExamAttempt ea WHERE ea.exam.teacher.ucInfoId = :teacherId AND ea.status = 'Completed'",
                Long.class
            );
            query.setParameter("teacherId", teacherId);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Get count of in-progress attempts for a teacher
     *
     * @param teacherId Teacher user info ID
     * @return Count of in-progress attempts
     */
    public long getInProgressAttemptsCountByTeacher(int teacherId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(ea) FROM ExamAttempt ea WHERE ea.exam.teacher.ucInfoId = :teacherId AND ea.status = 'In Progress'",
                Long.class
            );
            query.setParameter("teacherId", teacherId);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Get total unique students who took exams for a teacher
     *
     * @param teacherId Teacher user info ID
     * @return Count of unique students
     */
    public long getUniqueStudentsCountByTeacher(int teacherId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(DISTINCT ea.student.ucInfoId) FROM ExamAttempt ea WHERE ea.exam.teacher.ucInfoId = :teacherId",
                Long.class
            );
            query.setParameter("teacherId", teacherId);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
