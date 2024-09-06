package bg.notify.repositories;

import bg.notify.entities.Seminar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeminarRepository extends JpaRepository<Seminar, Long> {

    @Query(value = "SELECT * FROM seminar WHERE STR_TO_DATE(CONCAT(SUBSTRING_INDEX(date, ' ', 1), ' ', " +
            "CASE " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'януари' THEN '01' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'февруари' THEN '02' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'март' THEN '03' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'април' THEN '04' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'май' THEN '05' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'юни' THEN '06' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'юли' THEN '07' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'август' THEN '08' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'септември' THEN '09' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'октомври' THEN '10' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'ноември' THEN '11' " +
            "WHEN SUBSTRING_INDEX(SUBSTRING_INDEX(date, ' ', -2), ' ', 1) = 'декември' THEN '12' " +
            "END, ' ', SUBSTRING_INDEX(date, ' ', -1)), '%d %m %Y') < CURDATE()", nativeQuery = true)
    List<Seminar> findAllBeforeToday();
}
