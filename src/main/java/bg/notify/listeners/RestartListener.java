package bg.notify.listeners;

import bg.notify.config.GuildProperties;
import bg.notify.config.ManagerProperties;
import bg.notify.entities.Exam;
import bg.notify.entities.ManagerStatus;
import bg.notify.enums.ChannelStatus;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.repositories.ManagerStatusRepository;
import bg.notify.schedulers.ExamChecker;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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
            event.getHook().sendMessage("Check the channels and updating closest exam...").setEphemeral(true).queue();

            Guild guild = event.getGuild();
            String guildId = guild.getId();
            Optional<Exam> closestUpcomingBasicsExam = examRepository.findClosestUpcomingBasicsExam();
            Optional<ManagerStatus> managerStatus = managerStatusRepository.findByGuildId(guildId);
            List<String> textCategories = guildProperties.getTextChannelsToLock().get(guildId);
            String voiceCategory = guildProperties.getVoiceChannelsToLock().get(guildId);
            boolean channelsIsLocked = checkIfChannelsAreLocked(guild, textCategories, voiceCategory);

            if (channelsIsLocked && managerStatus.isPresent()) {
                managerStatus.get().setCurrentStatus(ChannelStatus.LOCKED);
                managerStatusRepository.save(managerStatus.get());
            }
            updateManagerMessage(guild, closestUpcomingBasicsExam.get(), managerStatusRepository, managerProperties);
            event.getHook().sendMessage("Channels are checked and the closest exam has been updated! If there is No exam, please insert one from Insert E.").setEphemeral(true).queue();
        }
    }

    private boolean checkIfChannelsAreLocked(Guild guild, List<String> textCategories, String voiceCategory) {
        boolean voiceChannelClosed = false;
        Role everyRole = guild.getPublicRole();
        if (guild.getCategoryById(voiceCategory) instanceof VoiceChannel voice) {
            if (voice.getUserLimit() == 1) {
                voiceChannelClosed = true;
            }
        }

        AtomicBoolean textChannelsClosed = new AtomicBoolean(false);

        textCategories.forEach(textCategory -> {
            Category category = guild.getCategoryById(textCategory);
            if (category == null) return;
            category.getChannels().forEach(channel -> {
                if (channel.getType() == ChannelType.TEXT) {
                    TextChannel textChannel = (TextChannel) channel;
                    PermissionOverride permissionOverride = textChannel.getPermissionOverride(everyRole);
                    if (permissionOverride != null && permissionOverride.getDenied().contains(Permission.MESSAGE_SEND)) {
                        textChannelsClosed.set(true);
                    }
                }
            });
        });
        return voiceChannelClosed || textChannelsClosed.get();
    }
}
