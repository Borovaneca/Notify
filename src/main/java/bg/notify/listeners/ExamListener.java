package bg.notify.listeners;

import bg.notify.config.GuildProperties;
import bg.notify.config.ManagerProperties;
import bg.notify.entities.Exam;
import bg.notify.entities.ManagerStatus;
import bg.notify.enums.ChannelStatus;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.repositories.ManagerStatusRepository;
import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Component
public class ExamListener extends ListenerAdapter {

    private final ExamRepository examRepository;
    private final ManagerStatusRepository managerStatusRepository;
    private final ManagerProperties managerProperties;
    private final GuildProperties guildProperties;

    @Autowired
    public ExamListener(ExamRepository examRepository, ManagerStatusRepository managerStatusRepository, ManagerProperties managerProperties, GuildProperties guildProperties) {
        this.examRepository = examRepository;
        this.managerStatusRepository = managerStatusRepository;
        this.managerProperties = managerProperties;
        this.guildProperties = guildProperties;
    }

    @Override
    public void onReady(ReadyEvent event) {

        final Exam[] basicsExamWrapper = new Exam[1];
        final Exam[] fundamentalsExamWrapper = new Exam[1];
        final Exam[] testExamWrapper = new Exam[1];
        final ManagerStatus[] managerStatusWrapper = new ManagerStatus[1];

        Optional<Exam> closestUpcomingBasicsExam = examRepository.findClosestUpcomingBasicsExam();
        basicsExamWrapper[0] = closestUpcomingBasicsExam.orElseGet(ExamListener::getDummyExam);

        Optional<Exam> closestUpcomingFundamentalsExam = examRepository.findClosestUpcomingFundamentalsExam();
        fundamentalsExamWrapper[0] = closestUpcomingFundamentalsExam.orElseGet(ExamListener::getDummyExam);

        Optional<Exam> closestUpcomingTestExams = examRepository.findClosestUpcomingTestExams();
        testExamWrapper[0] = closestUpcomingTestExams.orElseGet(ExamListener::getDummyExam);


        managerStatusWrapper[0] = new ManagerStatus(2L, "null", "null", ChannelStatus.UNLOCKED);

        guildProperties.getGuildIds().forEach((name, id) -> {
            TextChannel channel = event.getJDA().getGuildById(id).getTextChannelById(managerProperties.getManagerChannels().get(id));
            MessageCreateAction message;
            String guildName = event.getJDA().getGuildById(id).getName();
            if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {
                message = channel.sendMessageEmbeds(EmbeddedMessages.getExamManagementPanelMessage(managerStatusWrapper[0], basicsExamWrapper[0]));

            } else if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
                message = channel.sendMessageEmbeds(EmbeddedMessages.getExamManagementPanelMessage(managerStatusWrapper[0], fundamentalsExamWrapper[0]));

            } else {
                message = channel.sendMessageEmbeds(EmbeddedMessages.getExamManagementPanelMessage(managerStatusWrapper[0], testExamWrapper[0]));
            }

            Optional<ManagerStatus> managerStatus = managerStatusRepository.findByGuildId(id);

                    managerStatus.ifPresentOrElse(status -> {
                                if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {
                                    channel.retrieveMessageById(status.getCommentId()).queue(existingMessage -> {
                                        existingMessage.editMessageEmbeds(EmbeddedMessages.getExamManagementPanelMessage(status, basicsExamWrapper[0]))
                                                .setActionRow(
                                                        Button.primary("insert-exam", "Insert E."),
                                                        Button.secondary("view-exam", "View E.")
                                                )
                                                .queue();
                                    });
                                } else if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
                                    channel.retrieveMessageById(status.getCommentId()).queue(existingMessage -> {
                                        existingMessage.editMessageEmbeds(EmbeddedMessages.getExamManagementPanelMessage(status, fundamentalsExamWrapper[0]))
                                                .setActionRow(
                                                        Button.primary("insert-exam", "Insert E."),
                                                        Button.secondary("view-exam", "View E.")
                                                )
                                                .queue();
                                    });
                                } else {
                                    channel.retrieveMessageById(status.getCommentId()).queue(existingMessage -> {
                                        existingMessage.editMessageEmbeds(EmbeddedMessages.getExamManagementPanelMessage(status, testExamWrapper[0]))
                                                .setActionRow(
                                                        Button.primary("insert-exam", "Insert E."),
                                                        Button.secondary("view-exam", "View E.")
                                                )
                                                .queue();
                                    });
                                }
                            },
                            () -> message.setActionRow(
                                            Button.primary("insert-exam", "Insert E."),
                                            Button.secondary("view-exam", "View E.")
                                    )
                                    .queue(currentMessage -> {
                                        managerStatusWrapper[0].setCommentId(currentMessage.getId());
                                        managerStatusWrapper[0].setGuildId(id);
                                        managerStatusRepository.save(managerStatusWrapper[0]);
                                    }));

        });
    }

    private static @NotNull Exam getDummyExam() {
        return new Exam(1L, "No course", "No course", "No course");
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getButton().getId().equals("insert-exam")) return;

        if (!event.getMember().getPermissions().contains(Permission.MESSAGE_MANAGE)) {
            event.reply("You have no permission!").setEphemeral(true).queue();
            return;
        }
        String placeHolder;
        String guildName = event.getGuild().getName();
        if (guildName.contains("Basics")) {
            placeHolder = "e.g., Programming Basics - септември 2023";
        } else if (guildName.contains("Fundamentals")) {
            placeHolder = "e.g., Programming Fundamentals - септември 2023";
        } else {
            placeHolder = "e.g., Test Basics - септември 2023";
        }

        event.replyModal(Modal.create("insert-exam-modal", "Insert Exam")
                .addActionRow(TextInput.create("course-name", "COURSE NAME", TextInputStyle.SHORT)
                        .setPlaceholder(placeHolder)
                        .build())
                .addActionRow(TextInput.create("start-date", "START DATE (dd-MM-yyyy)", TextInputStyle.SHORT)
                        .setPlaceholder("e.g., 01-09-2023")
                        .build())
                .addActionRow(TextInput.create("end-date", "END DATE (dd-MM-yyyy)", TextInputStyle.SHORT)
                        .setPlaceholder("e.g., 30-09-2023")
                        .build())
                .build()).queue();
    }


    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("insert-exam-modal")) {
            String courseName = event.getValue("course-name").getAsString();
            String startDateStr = event.getValue("start-date").getAsString();
            String endDateStr = event.getValue("end-date").getAsString();

            Guild guild = event.getGuild();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            dateFormat.setLenient(false);
            boolean examIsNotForThisServer = guild.getName().contains(guildProperties.getGuildNames().get(GuildNames.BASICS)) && !courseName.contains(guildProperties.getGuildNames().get(GuildNames.BASICS));
            boolean examIsNotForThisServer2 = guild.getName().contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS)) && !courseName.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS));
            if (examIsNotForThisServer || examIsNotForThisServer2) {
                event.reply("Error: " + courseName + " is not a course for this server!").setEphemeral(true).queue();
                return;
            }

            try {
                Date startDate = dateFormat.parse(startDateStr);
                Date endDate = dateFormat.parse(endDateStr);

                Date today = new Date();
                today = dateFormat.parse(dateFormat.format(today));

                if (startDate.before(today) || endDate.before(today)) {
                    event.reply("Error: The exam dates cannot be before today.").setEphemeral(true).queue();
                    return;
                }

                if (!startDate.before(endDate)) {
                    if (!guild.getName().contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS)) && startDate.equals(endDate)) {
                        event.reply("Error: Start date must be before the end date.").setEphemeral(true).queue();
                        return;
                    }
                }

                Optional<Exam> existingExam = examRepository.findByCourseNameAndStartDateAndEndDate(courseName, startDateStr, endDateStr);
                if (existingExam.isPresent()) {
                    event.reply("Error: An exam with the same course name and dates already exists.").setEphemeral(true).queue();
                    return;
                }

                Exam exam = new Exam();
                exam.setCourseName(courseName);
                exam.setStartDate(dateFormat.format(startDate));
                exam.setEndDate(dateFormat.format(endDate));

                examRepository.save(exam);

                if (guild.getName().contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {
                    Optional<Exam> closestUpcomingBasicsExam = examRepository.findClosestUpcomingBasicsExam();
                    if (closestUpcomingBasicsExam.isPresent()) {
                        exam = closestUpcomingBasicsExam.get();
                    }
                } else if (guild.getName().contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
                    Optional<Exam> closestUpcomingFundamentalsExam = examRepository.findClosestUpcomingFundamentalsExam();
                    if (closestUpcomingFundamentalsExam.isPresent()) {
                        exam = closestUpcomingFundamentalsExam.get();
                    }
                } else {
                    Optional<Exam> closestUpcomingTestExams = examRepository.findClosestUpcomingTestExams();
                    if (closestUpcomingTestExams.isPresent()) {
                        exam = closestUpcomingTestExams.get();
                    }
                }

                EmbeddedMessages.updateManagerMessage(guild, exam, managerStatusRepository, managerProperties);

                event.reply("Success: Exam inserted successfully!").setEphemeral(true).queue();

            } catch (ParseException e) {
                event.reply("Error: Invalid date format. Please use dd-MM-yyyy.").setEphemeral(true).queue();
            } catch (Exception e) {
                event.reply("An unexpected error occurred. Please try again.").setEphemeral(true).queue();
            }
        }
    }
}
