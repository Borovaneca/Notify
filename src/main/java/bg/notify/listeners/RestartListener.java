package bg.notify.listeners;

import bg.notify.config.GuildProperties;
import bg.notify.config.ManagerProperties;
import bg.notify.entities.Exam;
import bg.notify.entities.ManagerStatus;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.repositories.ManagerStatusRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static bg.notify.utils.EmbeddedMessages.updateManagerMessage;

@Component
public class RestartListener extends ListenerAdapter {

    private final ExamRepository examRepository;
    private final GuildProperties guildProperties;
    private final ManagerStatusRepository managerStatusRepository;
    private final ManagerProperties managerProperties;

    @Autowired
    public RestartListener(ExamRepository examRepository, GuildProperties guildProperties, ManagerStatusRepository managerStatusRepository, ManagerProperties managerProperties) {
        this.examRepository = examRepository;
        this.guildProperties = guildProperties;
        this.managerStatusRepository = managerStatusRepository;
        this.managerProperties = managerProperties;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getButton().getId().equals("refresh-button")) {
            event.deferReply(true).queue();
            event.getHook().sendMessage("Checking for closest exam...").setEphemeral(true).queue();

            Guild guild = event.getGuild();
            String guildId = guild.getId();
            if (guildId.equals(guildProperties.getGuildIds().get(GuildNames.BASICS))) {
                Optional<Exam> closestUpcomingBasicsExam = examRepository.findClosestUpcomingBasicsExam();
                updateManagerMessage(guild, closestUpcomingBasicsExam.get(), managerStatusRepository, managerProperties);
            }
            event.getHook().sendMessage("Closest exam updated! If there is No exam, please insert one from Insert E.").setEphemeral(true).queue();
        }
    }
}
