package bg.notify.utils;

import bg.notify.entities.Seminar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class EmbeddedMessages {


    public static MessageEmbed getSeminarMessage(Seminar seminar) {
        String imageUrl = seminar.getImageUrl().trim();

        return new EmbedBuilder()
                .setTitle(seminar.getTitle(), seminar.getLink())
                .setDescription("Онлайн събитие | Безплатно")
                .addField("Дата", seminar.getDate(), true)
                .addField("Час", seminar.getTime(), true)
                .addField("Лектори", seminar.getLecturers(), false)
                .setThumbnail(imageUrl)
                .setColor(Color.ORANGE)
                .addField("SoftUni Discord Community <:softuni:926272135255707718>", "", false)
                .build();
    }

    public static MessageEmbed getWelcomeMessage() {
        return new EmbedBuilder()
                .setTitle("🎉 Welcome to the SoftUni Discord Server!")
                .setDescription(
                        "We're excited to have you here! 🎊\n\n" +
                                "👉 **Please follow the instructions in the GIF below**:\n\n to pick your roles by selecting your programming language. Make sure to head over to " +
                                "#pick-a-role and select your roles to get started in the community.\n\n" +
                                "⚠️ **Don't forget**:\n\n to check out the #rules channel to familiarize yourself with our community guidelines. " +
                                "We want everyone to have a great experience here!\n\n" +
                                "We're here to help if you need anything. Enjoy your time with us!"
                )
                .setColor(Color.YELLOW)
                .setImage("attachment://pick-a-role.gif")
                .setFooter("Thank you!\nSoftUni Discord Community :softuni~1:")
                .build();
    }
}
