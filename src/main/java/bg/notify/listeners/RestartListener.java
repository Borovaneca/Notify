package bg.notify.listeners;

import bg.notify.config.GuildProperties;
import bg.notify.config.ManagerProperties;
import bg.notify.entities.Exam;
import bg.notify.entities.ManagerStatus;
import bg.notify.enums.ChannelStatus;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.repositories.ManagerStatusRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static bg.notify.listeners.ExamListener.getDummyExam;
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
            Optional<Exam> closestUpcomingExam = getExam(guild);
            Optional<ManagerStatus> managerStatus = managerStatusRepository.findByGuildId(guildId);
            String voiceCategory = guildProperties.getVoiceChannelsToLock().get(guildId);
            boolean channelsIsLocked = checkIfChannelsAreLocked(guild, voiceCategory);

            if (channelsIsLocked && managerStatus.isPresent()) {
                managerStatus.get().setCurrentStatus(ChannelStatus.LOCKED);
                managerStatusRepository.save(managerStatus.get());
            }
            updateManagerMessage(guild, closestUpcomingExam.get(), managerStatusRepository, managerProperties);
            event.getHook().sendMessage("Channels are checked and the closest exam has been updated! If there is No exam, please insert one from Insert E.").setEphemeral(true).queue();
        }
    }

    private Optional<Exam> getExam(Guild guild) {
        Optional<Exam> closestUpcomingExam;
        if (guild.getId().equals(guildProperties.getGuildIds().get(GuildNames.BASICS))) {
            closestUpcomingExam = examRepository.findClosestUpcomingBasicsExam();
        } else if (guild.getId().equals(guildProperties.getGuildIds().get(GuildNames.FUNDAMENTALS))) {
            closestUpcomingExam = examRepository.findClosestUpcomingFundamentalsExam();
        } else {
            closestUpcomingExam = examRepository.findClosestUpcomingTestExams();
        }
        return closestUpcomingExam.isEmpty() ? Optional.of(getDummyExam()) : closestUpcomingExam;
    }

    private boolean checkIfChannelsAreLocked(Guild guild, String voiceCategory) {
        boolean voiceChannelClosed = false;
        if (guild.getCategoryById(voiceCategory) instanceof VoiceChannel voice) {
            if (voice.getUserLimit() == 1) {
                voiceChannelClosed = true;
            }
        }
        return voiceChannelClosed;
    }
}
