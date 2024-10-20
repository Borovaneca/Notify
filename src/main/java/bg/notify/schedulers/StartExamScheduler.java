package bg.notify.schedulers;

import bg.notify.config.GuildProperties;
import bg.notify.config.ManagerProperties;
import bg.notify.entities.Exam;
import bg.notify.entities.ManagerStatus;
import bg.notify.enums.ChannelStatus;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.repositories.ManagerStatusRepository;
import bg.notify.services.EventService;
import bg.notify.utils.EmbeddedMessages;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static bg.notify.utils.EmbeddedMessages.updateManagerMessage;

@Slf4j
@Component
public class StartExamScheduler {

    private final ExamRepository examRepository;
    private final ManagerStatusRepository managerStatusRepository;
    private final EventService eventService;
    private final ManagerProperties managerProperties;
    private JDA jda;
    private final GuildProperties guildProperties;

    @Autowired
    public StartExamScheduler(ExamRepository examRepository, ManagerStatusRepository managerStatusRepository, EventService eventService, ManagerProperties managerProperties, JDA jda, GuildProperties guildProperties) {
        this.examRepository = examRepository;
        this.managerStatusRepository = managerStatusRepository;
        this.eventService = eventService;
        this.managerProperties = managerProperties;
        this.jda = jda;
        this.guildProperties = guildProperties;
    }

    @Scheduled(cron = "0 10 1 * * ?")
    public void checkExamDay() throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String today = dateFormat.format(new Date());

        List<Exam> examsToday = examRepository.findExamsByStartDate(today);


        for (Exam exam : examsToday) {
            Guild guild;
            log.info("Current day: " + dateFormat.format(new Date()) + " Current exam start date " + exam.getStartDate());
            if (exam.getCourseName().contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {

                log.info("Sending event to close the channels for Basics");
                guild = jda.getGuildById(guildProperties.getGuildIds().get(GuildNames.BASICS));
                proceed(exam, guild);

            } else if (exam.getCourseName().contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {

                guild = jda.getGuildById(guildProperties.getGuildIds().get(GuildNames.FUNDAMENTALS));
                proceed(exam, guild);

            } else {
                guild = jda.getGuildById(guildProperties.getGuildIds().get(GuildNames.TEST));
                proceed(exam, guild);
            }
        }
    }

    private void proceed(Exam exam, Guild guild) throws IOException {

        TextChannel logChannel = jda.getTextChannelById(guildProperties.getLogsChannels().get(guild.getId()));
        logChannel.sendMessageEmbeds(EmbeddedMessages.getChannelsClosedLogMessage(exam)).queue();

        eventService.createCloseChannelsEvent(guild.getId(), logChannel);

        Optional<ManagerStatus> status = managerStatusRepository.findByGuildId(guild.getId());
        if (status.isPresent()) {
            ManagerStatus currentStatus = status.get();
            currentStatus.setCurrentStatus(ChannelStatus.LOCKED);
            managerStatusRepository.save(currentStatus);
        }

        updateManagerMessage(guild, exam, managerStatusRepository, managerProperties);
    }
}
