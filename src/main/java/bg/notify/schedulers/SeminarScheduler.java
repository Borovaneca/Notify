package bg.mck.sentinel.scheduler;

import bg.mck.sentinel.config.ValuableMaterialsProperties;
import bg.mck.sentinel.entities.Seminar;
import bg.mck.sentinel.reposotories.SeminarRepository;
import bg.mck.sentinel.service.SeminarService;
import bg.mck.sentinel.utils.DateChecker;
import bg.mck.sentinel.utils.EmbeddedMessages;
import net.dv8tion.jda.api.JDA;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static bg.mck.sentinel.constants.Urls.SOFT_UNI_SEMINARS_URL;
import static bg.mck.sentinel.service.SeminarService.mapToSeminar;
import static bg.mck.sentinel.utils.TextMessages.getSeminarMessage;

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

    @Scheduled(cron = "0 12 12 * * ?")
    public void sendDailyMessage() {
        try {
            BufferedReader in = SeminarService.getConnection();
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            Document doc = Jsoup.parse(content.toString());
            Elements seminarElements = doc.select(".events-container-item");

            List<Seminar> seminars = repository.findTop5ByOrderByIdDesc();

            for (Element seminarElement : seminarElements) {
                Seminar seminar = mapToSeminar(seminarElement);
                if (!seminars.contains(seminar)) {
                    if (DateChecker.checkDateIfItsBefore(seminar.getDate())) {
                        valuableMaterialsChannels.getChannels().forEach((guild, channel) -> {
                            Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(guild)).getTextChannelById(channel))
                                    .sendMessage(getSeminarMessage()).queue();

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
}
