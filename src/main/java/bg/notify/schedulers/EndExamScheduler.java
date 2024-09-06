package bg.notify.schedulers;

import bg.notify.config.GuildProperties;
import bg.notify.config.ManagerProperties;
import bg.notify.entities.Exam;
import bg.notify.entities.ManagerStatus;
import bg.notify.enums.ChannelStatus;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.repositories.ManagerStatusRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static bg.notify.utils.EmbeddedMessages.getChannelsOpenedLogMessage;
import static bg.notify.utils.EmbeddedMessages.updateManagerMessage;

@Component
public class EndExamScheduler {

    private final ExamRepository examRepository;
    private final ManagerStatusRepository managerStatusRepository;
    private final ManagerProperties managerProperties;
    private final GuildProperties guildProperties;
    private JDA jda;

    @Autowired
    public EndExamScheduler(ExamRepository examRepository, ManagerStatusRepository managerStatusRepository, ManagerProperties managerProperties, GuildProperties guildProperties, JDA jda) {
        this.examRepository = examRepository;
        this.managerStatusRepository = managerStatusRepository;
        this.managerProperties = managerProperties;
        this.guildProperties = guildProperties;
        this.jda = jda;
    }

    @Scheduled(cron = "0 59 23 * * ?")
    public void unlockBasicsChannelsForCompletedExams() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String today = dateFormat.format(new Date());

        List<Exam> examsToday = examRepository.findExamsByEndDate(today);
        Guild basics = jda.getGuildById(guildProperties.getGuildIds().get(GuildNames.BASICS));
        for (Exam exam : examsToday) {
            if (exam.getCourseName().contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {
                unlockTextChannels(basics, guildProperties.getTextChannelsToLock().get(basics.getId()));
                unlockVoiceChannels(basics, guildProperties.getVoiceChannelsToLock().get(basics.getId()));
            }
            examRepository.delete(exam);
            Optional<Exam> closestUpcomingBasicsExam = examRepository.findClosestUpcomingBasicsExam();
            closestUpcomingBasicsExam.ifPresentOrElse(closestExam -> {
                        updateManagerMessage(basics, closestExam, managerStatusRepository, managerProperties);
                    },
                    () -> {
                        updateManagerMessage(basics, new Exam(1L, "No course", "No course", "No course"), managerStatusRepository, managerProperties);
                    });
            basics.getTextChannelById(guildProperties.getLogsChannels().get(basics.getId()))
                    .sendMessageEmbeds(getChannelsOpenedLogMessage(exam)).queue();
        }
    }

    @Scheduled(cron = "0 0 19 * * ?")
    public void unlockFundamentalsChannelsForCompletedExams() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String today = dateFormat.format(new Date());

        List<Exam> examsToday = examRepository.findExamsByEndDate(today);
        Guild fundamentals = jda.getGuildById(guildProperties.getGuildIds().get(GuildNames.FUNDAMENTALS));
        for (Exam exam : examsToday) {
            if (exam.getCourseName().contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
                unlockTextChannels(fundamentals, guildProperties.getTextChannelsToLock().get(fundamentals.getId()));
                unlockVoiceChannels(fundamentals, guildProperties.getVoiceChannelsToLock().get(fundamentals.getId()));
            }
            examRepository.delete(exam);
            Optional<Exam> closestUpcomingFundamentalsExam = examRepository.findClosestUpcomingFundamentalsExam();
            closestUpcomingFundamentalsExam.ifPresentOrElse(closestExam -> {
                        updateManagerMessage(fundamentals, closestExam, managerStatusRepository, managerProperties);
                    },
                    () -> {
                        updateManagerMessage(fundamentals, new Exam(1L, "No course", "No course", "No course"), managerStatusRepository, managerProperties);
                    });
            fundamentals.getTextChannelById(guildProperties.getLogsChannels().get(fundamentals.getId()))
                    .sendMessageEmbeds(getChannelsOpenedLogMessage(exam)).queue();
        }
    }

//    @Scheduled(cron = "0 5 19 * * ?")
//    public void unlockTestChannelsForCompletedExams() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
//        String today = dateFormat.format(new Date());
//
//        List<Exam> examsToday = examRepository.findExamsByEndDate(today);
//        Guild test = jda.getGuildById(guildProperties.getGuildIds().get(GuildNames.TEST));
//        for (Exam exam : examsToday) {
//            if (exam.getCourseName().contains("Test")) {
//                unlockTextChannels(test, guildProperties.getTextChannelsToLock().get(test.getId()));
//                unlockVoiceChannels(test, guildProperties.getVoiceChannelsToLock().get(test.getId()));
//            }
//            examRepository.delete(exam);
//            Optional<Exam> closestUpcomingTestExam = examRepository.findClosestUpcomingTestExams();
//            closestUpcomingTestExam.ifPresentOrElse(closestExam -> {
//                        updateManagerMessage(test, closestExam, managerStatusRepository, managerProperties);
//                    },
//                    () -> {
//                        updateManagerMessage(test, new Exam(1L, "No course", "No course", "No course"), managerStatusRepository, managerProperties);
//                    });
//            test.getTextChannelById(guildProperties.getLogsChannels().get(test.getId()))
//                    .sendMessageEmbeds(getChannelsOpenedLogMessage(exam)).queue();
//        }
//    }

    private void unlockTextChannels(Guild guild, List<String> textCategories) {
        Role everyRole = guild.getPublicRole();
        for (String categoryId : textCategories) {
            guild.getCategoryById(categoryId).getChannels().forEach(channel -> {
                if (channel instanceof TextChannel textChannel) {
                    textChannel.getManager().removePermissionOverride(everyRole).queue();
                } else if (channel instanceof NewsChannel newsChannel) {
                    newsChannel.getManager().removePermissionOverride(everyRole).queue();
                }
            });
        }
        Optional<ManagerStatus> status = managerStatusRepository.findByGuildId(guild.getId());
        if (status.isPresent()) {
            ManagerStatus currentStatus = status.get();
            currentStatus.setCurrentStatus(ChannelStatus.UNLOCKED);
            managerStatusRepository.save(currentStatus);
        }
    }

    private void unlockVoiceChannels(Guild guild, String categoryId) {
        guild.getCategoryById(categoryId).getChannels().forEach(channel -> {
            if (channel instanceof VoiceChannel voiceChannel) {
                voiceChannel.getManager().setUserLimit(50).queue();
            }
        });
    }
}
