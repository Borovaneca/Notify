package bg.notify.listeners;

import bg.notify.config.GuildProperties;
import bg.notify.entities.Exam;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.List;

@Component
public class ViewExamListener extends ListenerAdapter {

    private final ExamRepository examRepository;
    private final GuildProperties guildProperties;

    @Autowired
    public ViewExamListener(ExamRepository examRepository, GuildProperties guildProperties) {
        this.examRepository = examRepository;
        this.guildProperties = guildProperties;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getButton().getId().equals("view-exam")) return;
        if (!event.getMember().getPermissions().contains(Permission.MESSAGE_MANAGE)) {
            event.reply("You have no permission!").setEphemeral(true).queue();
            return;
        }
        String name = event.getGuild().getName();
        List<Exam> exams;
        if (name.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
            exams = examRepository.findUpcomingFundamentalsExams();
        } else if (name.contains(guildProperties.getGuildNames().get(GuildNames.BASICS))){
            exams = examRepository.findUpcomingBasicsExams();
        } else {
            exams = examRepository.findUpcomingTestExams();
        }
        try {
            event.replyEmbeds(EmbeddedMessages.getExamListMessage(exams, name)).setEphemeral(true).queue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
