package bg.notify.listeners;

import bg.notify.repositories.ExamRepository;
import bg.notify.repositories.ManagerStatusRepository;
import bg.notify.repositories.SeminarRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RetrieveDB extends ListenerAdapter {

    private final ExamRepository examRepository;
    private final ManagerStatusRepository managerStatusRepository;
    private final SeminarRepository seminarRepository;
    private final ObjectMapper objectMapper;
    @Autowired
    public RetrieveDB(ExamRepository examRepository, ManagerStatusRepository managerStatusRepository, SeminarRepository seminarRepository, ObjectMapper objectMapper) {
        this.examRepository = examRepository;
        this.managerStatusRepository = managerStatusRepository;
        this.seminarRepository = seminarRepository;
        this.objectMapper = objectMapper;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String contentRaw = event.getMessage().getContentRaw();
        if (!contentRaw.startsWith("!give")) return;

        contentRaw = contentRaw.split("\\s+")[1];
        if (contentRaw.equals("exams")) {
            event.getAuthor().openPrivateChannel()
                    .queue((privateChannel -> {
                        try {
                            String jsonResponse = objectMapper.writeValueAsString(examRepository.findAll());
                            privateChannel.sendMessage("`" + jsonResponse + "`").queue();
                        } catch (JsonProcessingException e) {
                            privateChannel.sendMessage("Error processing JSON").queue();
                        }
                    }));

        } else if (contentRaw.equals("seminar")) {
            event.getAuthor().openPrivateChannel()
                    .queue((privateChannel -> {
                        try {
                            String jsonResponse = objectMapper.writeValueAsString(seminarRepository.findAll());
                            privateChannel.sendMessage("`" + jsonResponse + "`").queue();
                        } catch (JsonProcessingException e) {
                            privateChannel.sendMessage("Error processing JSON").queue();
                        }
                    }));
        } else if (contentRaw.equals("manager")) {
            event.getAuthor().openPrivateChannel()
                    .queue((privateChannel -> {
                        try {
                            String jsonResponse = objectMapper.writeValueAsString(managerStatusRepository.findAll());
                            privateChannel.sendMessage("`" + jsonResponse + "`").queue();
                        } catch (JsonProcessingException e) {
                            privateChannel.sendMessage("Error processing JSON").queue();
                        }
                    }));
        }
    }
}
