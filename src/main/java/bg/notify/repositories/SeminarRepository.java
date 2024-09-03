package bg.notify.repositories;

import bg.notify.entities.Seminar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeminarRepository extends JpaRepository<Seminar, Long> {

    List<Seminar> findTop6ByOrderByIdDesc();
}
