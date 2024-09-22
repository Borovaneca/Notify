package bg.notify.repositories;

import bg.notify.entities.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    @Query(value = "SELECT * FROM exams e WHERE e.course_name LIKE 'Programming Basics%' AND STR_TO_DATE(e.start_date, '%d-%m-%Y') >= CURDATE() ORDER BY STR_TO_DATE(e.start_date, '%d-%m-%Y') ASC LIMIT 1", nativeQuery = true)
    Optional<Exam> findClosestUpcomingBasicsExam();

    @Query(value = "SELECT * FROM exams e WHERE e.course_name LIKE 'Programming Fundamentals%' AND STR_TO_DATE(e.start_date, '%d-%m-%Y') >= CURDATE() ORDER BY STR_TO_DATE(e.start_date, '%d-%m-%Y') ASC LIMIT 1", nativeQuery = true)
    Optional<Exam> findClosestUpcomingFundamentalsExam();

    @Query(value = "SELECT * FROM exams e WHERE e.course_name LIKE 'Programming Fundamentals%' AND STR_TO_DATE(e.start_date, '%d-%m-%Y') >= CURDATE() ORDER BY STR_TO_DATE(e.start_date, '%d-%m-%Y') ASC", nativeQuery = true)
    List<Exam> findUpcomingFundamentalsExams();

    @Query(value = "SELECT * FROM exams e WHERE e.course_name LIKE 'Programming Basics%' AND STR_TO_DATE(e.start_date, '%d-%m-%Y') >= CURDATE() ORDER BY STR_TO_DATE(e.start_date, '%d-%m-%Y') ASC", nativeQuery = true)
    List<Exam> findUpcomingBasicsExams();

    @Query(value = "SELECT * FROM exams e WHERE STR_TO_DATE(e.start_date, '%d-%m-%Y') = DATE_ADD(CURDATE(), INTERVAL 1 DAY)", nativeQuery = true)
    List<Exam> findExamsByStartDateTomorrow();

    Optional<Exam> findByCourseNameAndStartDateAndEndDate(String courseName, String string, String string1);

    List<Exam> findExamsByStartDate(String today);

    List<Exam> findExamsByEndDate(String today);

    @Query(value = "SELECT * FROM exams e WHERE e.course_name LIKE 'Lover%' AND STR_TO_DATE(e.start_date, '%d-%m-%Y') >= CURDATE() ORDER BY STR_TO_DATE(e.start_date, '%d-%m-%Y') ASC", nativeQuery = true)
    List<Exam> findUpcomingTestExams();

    @Query(value = "SELECT * FROM exams e WHERE e.course_name LIKE 'Lover%' AND STR_TO_DATE(e.start_date, '%d-%m-%Y') >= CURDATE() ORDER BY STR_TO_DATE(e.start_date, '%d-%m-%Y') ASC LIMIT 1", nativeQuery = true)
    Optional<Exam> findClosestUpcomingTestExams();
}
