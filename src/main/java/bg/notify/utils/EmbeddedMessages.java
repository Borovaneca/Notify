package bg.notify.utils;

import bg.notify.config.ManagerProperties;
import bg.notify.entities.Exam;
import bg.notify.entities.ManagerStatus;
import bg.notify.entities.Seminar;
import bg.notify.enums.ChannelStatus;
import bg.notify.repositories.ManagerStatusRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class EmbeddedMessages {


    public static List<MessageEmbed> getSeminarMessage(Seminar... seminars) {
        List<MessageEmbed> messages = new ArrayList<>();
        Arrays.stream(seminars)
                .forEach(seminar -> {
                    String imageUrl = seminar.getImageUrl().trim();
                    messages.add(new EmbedBuilder()
                            .setTitle(seminar.getTitle(), seminar.getLink())
                            .setDescription("Онлайн събитие | Безплатно")
                            .addField("Дата", seminar.getDate(), true)
                            .addField("Час", seminar.getTime(), true)
                            .addField("Лектори", seminar.getLecturers(), false)
                            .setThumbnail(imageUrl)
                            .setColor(Color.ORANGE)
                            .addField("SoftUni Discord Community <:softuni:926272135255707718>", "", false)
                            .build());
                });
        return messages;
    }

    public static MessageEmbed getExamManagementPanelMessage(ManagerStatus managerStatus, Exam exam) {
        return new EmbedBuilder()
                .setTitle("Exam Management Panel")
                .setColor(Color.DARK_GRAY)
                .addField("1. Channels", "Status :" + (managerStatus.getCurrentStatus().equals(ChannelStatus.LOCKED) ? "**Locked** :lock:" : "**Unlocked** :unlock:"), false)
                .addField("2. Next upcoming exam", exam.getCourseName() + "\n"
                        + "Start on: **" + exam.getStartDate().replace("-", ".") + "**\n"
                        + "End on: **" + exam.getEndDate().replace("-", ".") +"**", false)
                .build();
    }

    public static MessageEmbed getExamListMessage(List<Exam> exams, String guildName) throws ParseException {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (guildName.contains("Fundamentals")) {
            embedBuilder.setTitle("Programming Fundamentals Upcoming/Active Exams");
        } else {
            embedBuilder.setTitle("Programming Basics Upcoming/Active Exams");
        }
        embedBuilder.setColor(Color.CYAN);
        if (exams.isEmpty()) {
            embedBuilder.setDescription("No upcoming exams found. PLEASE ADD SOME!");
        } else {
            embedBuilder.setDescription("Total exams: " + exams.size());
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd.MM.yyyy (EEEE)");
            for (Exam exam : exams) {
                Date startDate = inputDateFormat.parse(exam.getStartDate());
                Date endDate = inputDateFormat.parse(exam.getEndDate());

                String formattedStartDate = outputDateFormat.format(startDate);
                String formattedEndDate = outputDateFormat.format(endDate);

                embedBuilder.addField(
                        "Course: " + exam.getCourseName(),
                        "Start on: " + formattedStartDate + "\nEnd on: " + formattedEndDate,
                        false
                );
            }
        }
        embedBuilder.setFooter("Management Panel");
        return embedBuilder.build();
    }



    public static MessageEmbed getExamReminderFundamentals(Exam exam) {
            String dayAndMonthName = formatCustomDate(exam.getStartDate());

            return new EmbedBuilder()
                    .setTitle("ВАЖНО")
                    .setDescription("@everyone\n\n"
                            + "**Скъпи курсисти,**\n\n"
                            + "Искаме да ви напомним, че на **("+ dayAndMonthName +")** ще се проведе изпит на курсистите от **"+ exam.getCourseName() +"**.\n"
                            + "С цел да се осигури нормалното протичане на изпита, **гласовите и текстовите** канали ще бъдат заключени "
                            + "от **09:00** часа на **"+ dayAndMonthName +"** и ще бъдат отключени след **19:00** часа на същия ден. Призоваваме курсистите "
                            + "да се придържат към самостоятелно решаване на изпитните задачи. **Преписването и подсказването са строго забранени.**\n\n"
                            + "Молим ви за отговорност и разбиране!\n\n"
                            + "Благодарим и успех!\n"
                            + "**@SoftUni Discord Community** <:softuni:961297265270616124>").build();
        }

    public static MessageEmbed getExamReminderBasics(Exam exam) {
        String startDate = formatCustomDate(exam.getStartDate());
        String endDate = formatCustomDate(exam.getEndDate());

        return new EmbedBuilder()
                .setTitle("ВАЖНО")
                .setDescription("@everyone\n\n"
                + "**Скъпи курсисти,**\n\n"
                + "Искаме да ви напомним, че този уикенд (**" + startDate + " - " + endDate + "**) ще се проведе изпит на курсистите от **"+ exam.getCourseName() +"**.\n"
                + "С цел да се осигури нормалното протичане на изпита, **гласовите и текстовите** канали ще бъдат заключени "
                + "на **" + startDate + "** и ще бъдат отключени след **23:59 часа на " + endDate + "**. Призоваваме курсистите "
                + "да се придържат към самостоятелно решаване на изпитните задачи. **Преписването и подсказването са строго забранени.**\n\n"
                + "Молим ви за отговорност и разбиране!\n\n"
                + "Благодарим и успех!\n"
                + "**@SoftUni Discord Community** <:softuni:926272135255707718>").build();
    }

    private static String formatCustomDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");

            Date date = inputFormat.parse(dateString);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);

            String monthName = new DateFormatSymbols(new Locale("bg", "BG")).getMonths()[month];

            return day + " " + monthName;
        } catch (Exception e) {
            e.printStackTrace();
            return dateString;
        }
    }

    public static void updateManagerMessage(Guild guild, Exam exam, ManagerStatusRepository managerStatusRepository, ManagerProperties managerProperties) {
        Optional<ManagerStatus> managerStatus = managerStatusRepository.findByGuildId(guild.getId());
        managerStatus.ifPresent(status -> Objects.requireNonNull(guild.getTextChannelById(managerProperties.getManagerChannels().get(guild.getId())))
                .retrieveMessageById(status.getCommentId()).queue(existingMessage -> {
                    existingMessage.editMessageEmbeds(getExamManagementPanelMessage(status, exam))
                            .setActionRow(
                                    net.dv8tion.jda.api.interactions.components.buttons.Button.primary("insert-exam", "Insert E."),
                                    Button.secondary("view-exam", "View E."),
                                    Button.success("announce", "Announce"),
                                    Button.primary("refresh-button", "🔄")
                            )
                            .queue();
                }));
    }

    public static MessageEmbed getChannelsClosedLogMessage(Exam exam) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("🚨 Closing channels event")
                .setDescription("Sending event to Sentinel to close the channels!\n\n"
                        + "**Exam:** " + exam.getCourseName() + "\n"
                        + "**Start Date:** " + exam.getStartDate() + "\n"
                        + "**End Date:** " + exam.getEndDate() + "\n\n"
                        + "Please wait 1-2 minutes for Sentinel to lock all channels!")
                .setColor(Color.RED)
                .setFooter("Channels will be unlocked after the exam ends.");

        return embedBuilder.build();
    }

    public static MessageEmbed getChannelsOpenedLogMessage(Exam exam) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("✅ Reopening channels event")
                .setDescription("Sending event to Sentinel to open the channels!\n\n"
                        + "**Exam:** " + exam.getCourseName() + "\n"
                        + "**End Date:** " + exam.getEndDate() + "\n\n"
                        + "Please wait 1-2 minutes for Sentinel to unlock all channels!")
                .setColor(Color.GREEN)
                .setFooter("Thank you for your patience during the exam period!");

        return embedBuilder.build();
    }

    public static MessageEmbed getEventExecutedSuccessfully() {
        return new EmbedBuilder()
                .setTitle("✅ EVENT EXECUTED SUCCESSFULLY!")
                .build();
    }

    public static MessageEmbed getEventExecutedUnSuccessfully() {
        return new EmbedBuilder()
                .setTitle("🚨 SOMETHING WENT WRONG!!!")
                .build();
    }

    public static MessageEmbed getBotInfoMessage() {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("🤖 **Bot Information**")
                .setDescription("Welcome to the official notification bot! Stay up-to-date with all the latest events and notifications in your server.")
                .addField("Features", "• 📅 **Event Reminders**\n" +
                        "• 🔔 **Notification Scheduling**\n" +
                        "• 📢 **Automated Announcements**", false)
                .addField("Developed and Maintained by", "👨‍💻 **<@312912204175245333>**", false)
                .setColor(0x00FF00)  // Green color
                .setFooter("SoftUni Discord Community!", "https://cdn.discordapp.com/avatars/1280446385061105686/3161a22831ee1f47b11c795d7ada8dba.png")
                .setThumbnail("https://cdn.discordapp.com/avatars/1280446385061105686/3161a22831ee1f47b11c795d7ada8dba.png");

        return embed.build();
    }

    public static void createExamAddedMessage(Guild guild, String channelId, Exam exam) {

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("New Exam Added Automatically");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField("Course Name", exam.getCourseName(), false);
        embedBuilder.addField("Start Date", exam.getStartDate(), true);
        embedBuilder.addField("End Date", exam.getEndDate(), true);
        embedBuilder.setFooter("Exam Notification System", null);


        guild.getTextChannelById(channelId)
                .sendMessageEmbeds(embedBuilder.build())
                .queue();
    }
}
