package bg.notify.schedulers;

import bg.notify.config.GuildProperties;
import bg.notify.entities.Exam;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ExamReminder {

    private final ExamRepository examRepository;
    private JDA jda;
    private final GuildProperties guildProperties;

    @Autowired
    public ExamReminder(ExamRepository examRepository, JDA jda, GuildProperties guildProperties) {
        this.examRepository = examRepository;
        this.jda = jda;
        this.guildProperties = guildProperties;
    }

    @Scheduled(cron = "0 0 13 * * ?")
    public void examReminder() {
        List<Exam> exams = examRepository.findExamsByStartDateTomorrow();
        if (exams.isEmpty()) {
            return;
        }

        for (Exam exam : exams) {
            if (exam.getCourseName().contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {

                Guild basics = jda.getGuildById(guildProperties.getGuildIds().get(GuildNames.BASICS));
                Objects.requireNonNull(basics.getTextChannelById(guildProperties.getAnnouncementChannels().get(basics.getId())))
                        .sendMessageEmbeds(EmbeddedMessages.getExamReminderBasics(exam))
                        .queue();

            } else {
                Guild fundamentals = jda.getGuildById(guildProperties.getGuildIds().get(GuildNames.FUNDAMENTALS));
                Objects.requireNonNull(fundamentals.getTextChannelById(guildProperties.getAnnouncementChannels().get(fundamentals.getId())))
                        .sendMessageEmbeds(EmbeddedMessages.getExamReminderFundamentals(exam))
                        .queue();
            }
        }
    }
}
