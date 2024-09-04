package bg.notify.entities;

import bg.notify.enums.ChannelStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "manager_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String commentId;
    private String guildId;

    @Enumerated(EnumType.STRING)
    private ChannelStatus currentStatus;
}
