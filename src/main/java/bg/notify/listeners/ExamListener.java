package bg.notify.listeners;

import bg.notify.config.GuildProperties;
import bg.notify.config.ManagerProperties;
import bg.notify.entities.Exam;
import bg.notify.entities.ManagerStatus;
import bg.notify.enums.ChannelStatus;
import bg.notify.enums.GuildNames;
import bg.notify.repositories.ExamRepository;
import bg.notify.repositories.ManagerStatusRepository;
import bg.notify.services.ExamService;
import bg.notify.utils.EmbeddedMessages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ExamListener extends ListenerAdapter {

    private final ExamRepository examRepository;
    private final ManagerStatusRepository managerStatusRepository;
    private final ManagerProperties managerProperties;
    private final GuildProperties guildProperties;
    private final ExamService examService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    public ExamListener(ExamRepository examRepository, ManagerStatusRepository managerStatusRepository, ManagerProperties managerProperties, GuildProperties guildProperties, ExamService examService) {
        this.examRepository = examRepository;
        this.managerStatusRepository = managerStatusRepository;
        this.managerProperties = managerProperties;
        this.guildProperties = guildProperties;
        this.examService = examService;
    }

    @Override
    public void onReady(ReadyEvent event) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        for (Map.Entry<GuildNames, String> currentGuild : guildProperties.getGuildIds().entrySet()) {
            Guild guild = event.getJDA().getGuildById(currentGuild.getValue());
            if (guild == null) return;

            TextChannel channel = guild.getTextChannelById(managerProperties.getManagerChannels().get(currentGuild.getValue()));
            if (channel == null) return;

            Exam closestExam = getClosestExamForGuild(guild.getName());
            final List<Exam> exams = new ArrayList<>();
            if (closestExam.equals(getDummyExam())) {
                if (guild.getId().equals(guildProperties.getGuildIds().get(GuildNames.BASICS))) {
                    try {
                        exams.addAll(examService.checkForNewExamsBasics());
                        saveExamAndSendMessage(guild, exams);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (guild.getId().equals(guildProperties.getGuildIds().get(GuildNames.FUNDAMENTALS))) {
                    try {
                        exams.addAll(examService.checkForNewExamsFundamentals());
                        saveExamAndSendMessage(guild, exams);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                exams.sort((exam1, exam2) -> {
                    try {
                        Date date1 = dateFormat.parse(exam1.getStartDate());
                        Date date2 = dateFormat.parse(exam2.getStartDate());
                        return date1.compareTo(date2);
                    } catch (ParseException e) {
                        throw new RuntimeException("Invalid date format. Please use dd-MM-yyyy.", e);
                    }
                });
                if (guild.getId().equals(guildProperties.getGuildIds().get(GuildNames.TEST))) {
                    updateManagerMessage(channel, closestExam, guild);
                }else {
                    updateManagerMessage(channel, exams.get(0), guild);
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                updateManagerMessage(channel, closestExam, guild);
            }
        }
    }

    public void saveExamAndSendMessage(Guild guild, List<Exam> exams) {

        exams.forEach(exam -> {
            executorService.submit(() -> {
                examRepository.save(exam);

                String channelId = guildProperties.getLogsChannels().get(guildProperties.getGuildIds().get(GuildNames.BASICS));
                EmbeddedMessages.createExamAddedMessage(guild, channelId, exam);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Thread was interrupted", e);
                }
            });
        });
    }
    private Exam getClosestExamForGuild(String guildName) {
        if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {
            Optional<Exam> exam = examRepository.findClosestUpcomingBasicsExam();
            return exam.orElse(getDummyExam());
        } else if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
            return examRepository.findClosestUpcomingFundamentalsExam().orElse(getDummyExam());
        } else {
            return examRepository.findClosestUpcomingTestExams().orElse(getDummyExam());
        }
    }

    private void updateManagerMessage(TextChannel channel, Exam exam, Guild guild) {
        Optional<ManagerStatus> managerStatusOpt = managerStatusRepository.findByGuildId(guild.getId());
        managerStatusOpt.ifPresentOrElse(
                status -> updateExistingMessage(channel, status, exam),
                () -> sendNewManagerMessage(channel, exam, guild)
        );
    }

    private void updateExistingMessage(TextChannel channel, ManagerStatus status, Exam exam) {
        channel.retrieveMessageById(status.getCommentId()).queue(existingMessage -> {
            existingMessage.editMessageEmbeds(EmbeddedMessages.getExamManagementPanelMessage(status, exam))
                    .setActionRow(
                            Button.primary("insert-exam", "Insert E."),
                            Button.secondary("view-exam", "View E."),
                            Button.primary("refresh-button", "🔄")
                    ).queue();
        });
    }

    private void sendNewManagerMessage(TextChannel channel, Exam exam, Guild guild) {
        boolean channelsAreOpen = checkIfTheChannelsAreOpened(guild);
        ManagerStatus defaultManagerStatus = new ManagerStatus();
        defaultManagerStatus.setGuildId(guild.getId());
        defaultManagerStatus.setCurrentStatus(channelsAreOpen ? ChannelStatus.UNLOCKED : ChannelStatus.LOCKED);
        MessageCreateAction message = channel.sendMessageEmbeds(EmbeddedMessages.getExamManagementPanelMessage(defaultManagerStatus, exam));
        message.setActionRow(
                Button.primary("insert-exam", "Insert E."),
                Button.secondary("view-exam", "View E."),
                Button.primary("refresh-button", "🔄")
        ).queue(sentMessage -> {
            defaultManagerStatus.setCommentId(sentMessage.getId());
            managerStatusRepository.save(defaultManagerStatus);
        });
    }

    private boolean checkIfTheChannelsAreOpened(Guild guild) {
        AtomicBoolean checked = new AtomicBoolean(false);
        for (Map.Entry<String, String> stringStringEntry : guildProperties.getVoiceChannelsToLock().entrySet()) {
            String key = stringStringEntry.getKey();
            if (guild.getId().equals(key)) {
                Category category = guild.getCategoryById(stringStringEntry.getValue());
                List<GuildChannel> channels = category.getChannels();
                for (GuildChannel channel : channels) {
                    if (channel instanceof VoiceChannel voiceChannel) {
                        if (voiceChannel.getUserLimit() > 1) {
                            checked.set(true);
                            break;
                        }
                    }
                }
            }
        }
        return checked.get();
    }

    private static @NotNull Exam getDummyExam() {
        return new Exam(1L, "No course", "No course", "No course");
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!"insert-exam".equals(event.getButton().getId())) return;

        if (!event.getMember().getPermissions().contains(Permission.MESSAGE_MANAGE)) {
            event.reply("You have no permission!").setEphemeral(true).queue();
            return;
        }

        String placeholder = getPlaceholderForGuild(event.getGuild().getName());
        event.replyModal(Modal.create("insert-exam-modal", "Insert Exam")
                .addActionRow(TextInput.create("course-name", "COURSE NAME", TextInputStyle.SHORT).setPlaceholder(placeholder).build())
                .addActionRow(TextInput.create("start-date", "START DATE (dd-MM-yyyy)", TextInputStyle.SHORT).setPlaceholder("e.g., 01-09-2023").build())
                .addActionRow(TextInput.create("end-date", "END DATE (dd-MM-yyyy)", TextInputStyle.SHORT).setPlaceholder("e.g., 30-09-2023").build())
                .build()).queue();
    }

    private String getPlaceholderForGuild(String guildName) {
        if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) {
            return "e.g., Programming Basics - септември 2023";
        } else if (guildName.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
            return "e.g., Programming Fundamentals - септември 2023";
        } else {
            return "e.g., Test Basics - септември 2023";
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if ("insert-exam-modal".equals(event.getModalId())) {
            processExamModal(event);
        }
    }

    private void processExamModal(ModalInteractionEvent event) {
        String courseName = event.getValue("course-name").getAsString();
        String startDateStr = event.getValue("start-date").getAsString();
        String endDateStr = event.getValue("end-date").getAsString();

        Guild guild = event.getGuild();

        if (!validateCourseForGuild(guild.getName(), courseName)) {
            event.reply("Error: " + courseName + " is not a course for this server!").setEphemeral(true).queue();
            return;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            dateFormat.setLenient(false);

            Date startDate = parseDate(startDateStr, dateFormat);
            Date endDate = parseDate(endDateStr, dateFormat);

            boolean validDates = validateDates(event, startDate, endDate, dateFormat);
            if (!validDates) return;

            if (!examExists(courseName, startDateStr, endDateStr)) {
                saveExam(courseName, startDate, endDate, guild);
                event.reply("Success: Exam inserted successfully!").setEphemeral(true).queue();
            } else {
                event.reply("Error: An exam with the same course name and dates already exists.").setEphemeral(true).queue();
            }
        } catch (ParseException e) {
            event.reply("Error: Invalid date format. Please use dd-MM-yyyy.").setEphemeral(true).queue();
        } catch (Exception e) {
            event.reply("An unexpected error occurred. Please try again.").setEphemeral(true).queue();
        }
    }

    private boolean validateCourseForGuild(String guildName, String courseName) {
        return (!guildName.contains(guildProperties.getGuildNames().get(GuildNames.BASICS)) || courseName.contains(guildProperties.getGuildNames().get(GuildNames.BASICS))) &&
                (!guildName.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS)) || courseName.contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS)));
    }

    private Date parseDate(String dateStr, SimpleDateFormat dateFormat) throws ParseException {
        return dateFormat.parse(dateStr);
    }

    private boolean validateDates(ModalInteractionEvent event, Date startDate, Date endDate, SimpleDateFormat dateFormat) throws ParseException {
        Date today = dateFormat.parse(dateFormat.format(new Date()));
        if (startDate.before(today) || endDate.before(today)) {
            event.reply("Error: The exam dates cannot be before today.").setEphemeral(true).queue();
            return false;
        } else if (!startDate.before(endDate)) {
            if (event.getGuild().getName().contains(guildProperties.getGuildNames().get(GuildNames.FUNDAMENTALS))) {
                return true;
            }
            event.reply("Error: Start date must be before the end date.").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private boolean examExists(String courseName, String startDateStr, String endDateStr) {
        return examRepository.findByCourseNameAndStartDateAndEndDate(courseName, startDateStr, endDateStr).isPresent();
    }

    private void saveExam(String courseName, Date startDate, Date endDate, Guild guild) {
        Exam exam = new Exam();
        exam.setCourseName(courseName);
        exam.setStartDate(new SimpleDateFormat("dd-MM-yyyy").format(startDate));
        exam.setEndDate(new SimpleDateFormat("dd-MM-yyyy").format(endDate));
        examRepository.save(exam);

        exam = getClosestExamForGuild(guild.getName());
        EmbeddedMessages.updateManagerMessage(guild, exam, managerStatusRepository, managerProperties);
    }
}
