package bg.notify.schedulers;

import bg.notify.config.GuildProperties;
import bg.notify.config.ManagerProperties;
import bg.notify.entities.Exam;
import bg.notify.entities.ManagerStatus;
import bg.notify.enums.ChannelStatus;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.repositories.ManagerStatusRepository;
import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static bg.notify.utils.EmbeddedMessages.updateManagerMessage;

@Component
public class StartExamScheduler {

    private final ExamRepository examRepository;
    private final ManagerStatusRepository managerStatusRepository;
    private final ManagerProperties managerProperties;
    private JDA jda;
    private final GuildProperties guildProperties;

    @Autowired
    public StartExamScheduler(ExamRepository examRepository, ManagerStatusRepository managerStatusRepository, ManagerProperties managerProperties, JDA jda, GuildProperties guildProperties) {
        this.examRepository = examRepository;
        this.managerStatusRepository = managerStatusRepository;
        this.managerProperties = managerProperties;
        this.jda = jda;
        this.guildProperties = guildProperties;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkExamDay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String today = dateFormat.format(new Date());

        List<Exam> examsToday = examRepository.findExamsByStartDate(today);


        for (Exam exam : examsToday) {
            Guild guild;
            if (exam.getCourseName().contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {

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

    private void proceed(Exam exam, Guild guild) {
        List<String> textChannelsIds = guildProperties.getTextChannelsToLock().get(guild.getId());
        String voiceCategory = guildProperties.getVoiceChannelsToLock().get(guild.getId());

        lockTextCategories(guild, textChannelsIds);
        lockVoiceCategories(guild, voiceCategory);

        guild.getTextChannelById(guildProperties.getLogsChannels().get(guild.getId()))
                        .sendMessageEmbeds(EmbeddedMessages.getChannelsClosedLogMessage(exam))
                                .queue();

        updateManagerMessage(guild, exam, managerStatusRepository, managerProperties);
    }

    private void lockTextCategories(Guild guild, List<String> textCategoriesIds) {

        Role everyOneRole = guild.getPublicRole();
        for (String categoryId : textCategoriesIds) {
            Category category = guild.getCategoryById(categoryId);
            category.getChannels().forEach(channel -> {
                if (channel.getType() == ChannelType.TEXT) {
                    TextChannel textChannel = (TextChannel) channel;
                    textChannel.getManager().putPermissionOverride(everyOneRole, null, EnumSet.of(Permission.MESSAGE_SEND)).queue();
                    textChannel.getManager().putPermissionOverride(guild.getPublicRole(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND)).queue();
                }
            });
        }
        Optional<ManagerStatus> status = managerStatusRepository.findByGuildId(guild.getId());
        if (status.isPresent()) {
            ManagerStatus currentStatus = status.get();
            currentStatus.setCurrentStatus(ChannelStatus.LOCKED);
            managerStatusRepository.save(currentStatus);
        }
    }

    private void lockVoiceCategories(Guild guild, String voiceCategory) {

            Category category = guild.getCategoryById(voiceCategory);
            category.getChannels().forEach(channel -> {
                if (channel.getType() == ChannelType.VOICE) {
                    VoiceChannel voiceChannel = (VoiceChannel) channel;
                    voiceChannel.getManager().setUserLimit(1).queue();
                }
            });
    }
}
