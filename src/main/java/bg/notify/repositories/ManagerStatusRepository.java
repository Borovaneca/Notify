package bg.notify.repositories;

import bg.notify.entities.ManagerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerStatusRepository extends JpaRepository<ManagerStatus, Long> {

    Optional<ManagerStatus> findByGuildId(String guildId);
}
