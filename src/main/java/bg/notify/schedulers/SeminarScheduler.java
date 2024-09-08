package bg.notify.schedulers;

import bg.notify.config.ValuableMaterialsProperties;
import bg.notify.entities.Seminar;
import bg.notify.repositories.SeminarRepository;
import bg.notify.services.SeminarService;
import bg.notify.utils.DateChecker;
import bg.notify.utils.EmbeddedMessages;
import bg.notify.utils.TextMessages;
import net.dv8tion.jda.api.JDA;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.util.List;
import java.util.Objects;

import static bg.notify.constants.Constants.SOFT_UNI_SEMINARS_URL;
import static bg.notify.services.SeminarService.mapToSeminar;


@Component
public class SeminarScheduler {

    private JDA jda;

    private ValuableMaterialsProperties valuableMaterialsChannels;
    private SeminarRepository repository;

    @Autowired
    public SeminarScheduler(JDA jda, ValuableMaterialsProperties valuableMaterialsChannels, SeminarRepository repository) {
        this.jda = jda;
        this.valuableMaterialsChannels = valuableMaterialsChannels;
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void sendDailyMessage() {
        try {
            BufferedReader in = SeminarService.getConnection(SOFT_UNI_SEMINARS_URL);
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            Document doc = Jsoup.parse(content.toString());
            Elements seminarElements = doc.select(".events-container-item");

            List<Seminar> seminars = repository.findAll();

            for (Element seminarElement : seminarElements) {
                Seminar seminar = mapToSeminar(seminarElement);
                if (!seminars.contains(seminar)) {
                    if (DateChecker.checkDateIfItsAfter(seminar.getDate())) {
                        valuableMaterialsChannels.getChannels().forEach((guild, channel) -> {
                            Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(guild)).getTextChannelById(channel))
                                    .sendMessage(TextMessages.getSeminarMessage()).queue();

                            Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(guild)).getTextChannelById(channel))
                                    .sendMessageEmbeds(EmbeddedMessages.getSeminarMessage(seminar)).queue();
                        });
                        repository.save(seminar);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 30 13 * * ?")
    public void removeOldSeminars() {
        List<Seminar> allBeforeToday = repository.findAllBeforeToday();
        repository.deleteAll(allBeforeToday);
        
    }
}
