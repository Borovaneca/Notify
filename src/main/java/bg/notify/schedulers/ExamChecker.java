package bg.notify.schedulers;

import bg.notify.config.GuildProperties;
import bg.notify.entities.Exam;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.services.ExamService;
import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ExamChecker {

    private final ExamRepository examRepository;
    private final ExamService examService;
    private final GuildProperties guildProperties;
    private final JDA jda;

    @Autowired
    public ExamChecker(ExamRepository examRepository, ExamService examService, GuildProperties guildProperties, JDA jda) {
        this.examRepository = examRepository;
        this.examService = examService;
        this.guildProperties = guildProperties;
        this.jda = jda;
    }

    @Scheduled(cron = "0 0 16 * * ?")
    public void checkForNewBasicsExam() throws IOException {
        List<Exam> all = examRepository.findAll();
        examService.checkForNewExamsBasics()
                .forEach(exam -> {
                    if (!all.contains(exam)) {
                        examRepository.save(exam);
                        String basicId = guildProperties.getGuildIds().get(GuildNames.BASICS);
                        Guild guild = jda.getGuildById(basicId);
                        String channelId = guildProperties.getLogsChannels().get(basicId);
                        EmbeddedMessages.createExamAddedMessage(guild, channelId, exam);
                    }
                });
    }

    @Scheduled(cron = "0 0 15 * * ?")
    public void checkForNewFundamentalsExam() throws IOException {
        List<Exam> all = examRepository.findAll();
        examService.checkForNewExamsFundamentals()
                .forEach(exam -> {
                    if (!all.contains(exam)) {
                        examRepository.save(exam);
                        String fundamentalsId = guildProperties.getGuildIds().get(GuildNames.FUNDAMENTALS);
                        Guild guild = jda.getGuildById(fundamentalsId);
                        String channelId = guildProperties.getLogsChannels().get(fundamentalsId);
                        EmbeddedMessages.createExamAddedMessage(guild, channelId, exam);
                    }
                });
    }
}
