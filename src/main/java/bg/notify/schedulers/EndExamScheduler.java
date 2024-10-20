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
import java.util.function.Supplier;

import static bg.notify.utils.EmbeddedMessages.updateManagerMessage;

@Component
public class EndExamScheduler {

    private final ExamRepository examRepository;
    private final ManagerStatusRepository managerStatusRepository;
    private final ManagerProperties managerProperties;
    private final GuildProperties guildProperties;
    private final EventService eventService;
    private final JDA jda;

    @Autowired
    public EndExamScheduler(ExamRepository examRepository, ManagerStatusRepository managerStatusRepository, ManagerProperties managerProperties, GuildProperties guildProperties, EventService eventService, JDA jda) {
        this.examRepository = examRepository;
        this.managerStatusRepository = managerStatusRepository;
        this.managerProperties = managerProperties;
        this.guildProperties = guildProperties;
        this.eventService = eventService;
        this.jda = jda;
    }

    @Scheduled(cron = "0 30 1 * * ?")
    public void unlockBasicsChannelsForCompletedExams() throws IOException {
        unlockChannelsForCompletedExams(GuildNames.BASICS, examRepository::findClosestUpcomingBasicsExam);
    }

    @Scheduled(cron = "0 0 19 * * ?")
    public void unlockFundamentalsChannelsForCompletedExams() throws IOException {
        unlockChannelsForCompletedExams(GuildNames.FUNDAMENTALS, examRepository::findClosestUpcomingFundamentalsExam);
    }

    private void unlockChannelsForCompletedExams(GuildNames guildName, Supplier<Optional<Exam>> closestUpcomingExamSupplier) throws IOException {
        String today = getCurrentDate();

        List<Exam> examsToday = examRepository.findExamsByEndDate(today);

        Guild guild = jda.getGuildById(guildProperties.getGuildIds().get(guildName));

        for (Exam exam : examsToday) {
            if (exam.getCourseName().contains(guildProperties.getGuildNames().get(guildName))) {
                logChannelAction(guild, exam);

                eventService.createOpenChannelsEvent(guild.getId(), jda.getTextChannelById(guildProperties.getLogsChannels().get(guild.getId())));

                updateManagerStatusToUnlocked(guild);

                examRepository.delete(exam);

                updateManagerWithClosestExam(guild, closestUpcomingExamSupplier);
            }
        }
    }

    private void logChannelAction(Guild guild, Exam exam) {
        TextChannel logChannel = jda.getTextChannelById(guildProperties.getLogsChannels().get(guild.getId()));
        logChannel.sendMessageEmbeds(EmbeddedMessages.getChannelsOpenedLogMessage(exam)).queue();
    }

    private void updateManagerStatusToUnlocked(Guild guild) {
        Optional<ManagerStatus> status = managerStatusRepository.findByGuildId(guild.getId());
        status.ifPresent(currentStatus -> {
            currentStatus.setCurrentStatus(ChannelStatus.UNLOCKED);
            managerStatusRepository.save(currentStatus);
        });
    }

    private void updateManagerWithClosestExam(Guild guild, Supplier<Optional<Exam>> closestUpcomingExamSupplier) {
        Optional<Exam> closestUpcomingExam = closestUpcomingExamSupplier.get();
        closestUpcomingExam.ifPresentOrElse(closestExam -> {
            updateManagerMessage(guild, closestExam, managerStatusRepository, managerProperties);
        }, () -> {
            updateManagerMessage(guild, new Exam(1L, "No course", "No course", "No course"), managerStatusRepository, managerProperties);
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(new Date());
    }
}
