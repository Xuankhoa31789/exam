package com.xuka.exam.dao;

import com.xuka.exam.config.HibernateUtil;
import com.xuka.exam.models.Exam;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for Exam entity
 * Handles database operations for exam management
 */
public class ExamDAO {

    /**
     * Save a new exam to database
     *
     * @param exam Exam object to save
     * @return true if successful, false otherwise
     */
    public boolean save(Exam exam) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(exam);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error saving exam: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Update an existing exam
     *
     * @param exam Exam object to update
     * @return true if successful, false otherwise
     */
    public boolean update(Exam exam) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(exam);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating exam: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Delete an exam by ID
     *
     * @param examId Exam ID
     * @return true if successful, false otherwise
     */
    public boolean delete(int examId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Exam exam = em.find(Exam.class, examId);
            if (exam != null) {
                em.remove(exam);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error deleting exam: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Get exam by ID
     *
     * @param examId Exam ID
     * @return Exam object or null if not found
     */
    public Exam getById(int examId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Exam.class, examId);
        } finally {
            em.close();
        }
    }

    /**
     * Get all exams
     *
     * @return List of all exams
     */
    public List<Exam> getAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Exam> query = em.createQuery("FROM Exam ORDER BY examDate DESC", Exam.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get exams by subject ID
     *
     * @param subjectId Subject ID
     * @return List of exams for the subject
     */
    public List<Exam> getBySubject(int subjectId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Exam> query = em.createQuery("FROM Exam WHERE subject.subjectId = :subjectId ORDER BY examDate DESC", Exam.class);
            query.setParameter("subjectId", subjectId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get exams by teacher
     *
     * @param teacherId Teacher user info ID
     * @return List of exams created by the teacher
     */
    public List<Exam> getByTeacher(int teacherId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Exam> query = em.createQuery("FROM Exam WHERE teacher.ucInfoId = :teacherId ORDER BY examDate DESC", Exam.class);
            query.setParameter("teacherId", teacherId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get exams by status
     *
     * @param status Exam status (Scheduled, Ongoing, Completed)
     * @return List of exams with the specified status
     */
    public List<Exam> getByStatus(String status) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Exam> query = em.createQuery("FROM Exam WHERE status = :status ORDER BY examDate DESC", Exam.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get exams scheduled for a specific date
     *
     * @param examDate Exam date
     * @return List of exams on the specified date
     */
    public List<Exam> getByDate(LocalDate examDate) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Exam> query = em.createQuery("FROM Exam WHERE examDate = :examDate ORDER BY examDate", Exam.class);
            query.setParameter("examDate", examDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get upcoming exams (scheduled for future dates)
     *
     * @return List of upcoming exams
     */
    public List<Exam> getUpcomingExams() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Exam> query = em.createQuery("FROM Exam WHERE examDate > CURRENT_DATE ORDER BY examDate ASC", Exam.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get completed exams
     *
     * @return List of completed exams
     */
    public List<Exam> getCompletedExams() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Exam> query = em.createQuery("FROM Exam WHERE status = 'Completed' ORDER BY examDate DESC", Exam.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
