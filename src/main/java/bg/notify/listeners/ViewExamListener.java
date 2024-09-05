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
            event.reply("You don't have permission to view exams!").setEphemeral(true).queue();
            return;
        }

        String guildName = event.getGuild().getName();
        List<Exam> exams = fetchExamsForGuild(guildName);

        if (exams.isEmpty()) {
            event.reply("No upcoming exams found.").setEphemeral(true).queue();
            return;
        }

        try {
            event.replyEmbeds(EmbeddedMessages.getExamListMessage(exams, guildName)).setEphemeral(true).queue();
        } catch (ParseException e) {
            event.reply("An error occurred while processing the exam list. Please try again later.").setEphemeral(true).queue();
        }
    }

    private List<Exam> fetchExamsForGuild(String guildName) {
        if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
            return examRepository.findUpcomingFundamentalsExams();
        } else if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {
            return examRepository.findUpcomingBasicsExams();
        } else {
            return examRepository.findUpcomingTestExams();
        }
    }
}
