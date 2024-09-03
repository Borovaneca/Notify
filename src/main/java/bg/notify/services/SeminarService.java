package bg.notify.services;

import bg.notify.entities.Seminar;
import bg.notify.repositories.SeminarRepository;
import bg.notify.utils.DateChecker;
import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static bg.notify.constants.Urls.SOFT_UNI_SEMINARS_URL;

@Service
public class SeminarService {

    private final SeminarRepository seminarRepository;

    @Autowired
    public SeminarService(SeminarRepository seminarRepository) {
        this.seminarRepository = seminarRepository;
    }


    @PostConstruct
    public void initializeSeminarsDb() {
        if (seminarRepository.count() != 0) return;

        try {
            BufferedReader in = getConnection();
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            Document doc = Jsoup.parse(content.toString());
            Elements seminarElements = doc.select(".events-container-item");

            List<Seminar> seminars = new ArrayList<>();

            for (Element seminarElement : seminarElements) {
                Seminar seminar = mapToSeminar(seminarElement);
                if (DateChecker.checkDateIfItsAfter(seminar.getDate())) {
                    seminars.add(seminar);
                }
            }

            seminarRepository.saveAll(seminars);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedReader getConnection() throws IOException {
        String url = SOFT_UNI_SEMINARS_URL;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Language", "bg-BG,bg;q=0.9");
        connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    public static Seminar mapToSeminar(Element seminarElement) {
        String title = seminarElement.select(".events-container-item-header-content-title").text();
        String date = seminarElement.select(".events-container-item-body-content-date .events-container-item-body-content-value").text();
        String time = seminarElement.select(".events-container-item-body-content-hour .events-container-item-body-content-value").text();
        String lecturers = seminarElement.select(".events-container-item-body-content-lecturer .events-container-item-body-content-lecturer-value").text().replace("\n", ", ");

        String[] lecturersArray = lecturers.replace("Лекторски състав", "").trim().split("\\s+");

        StringBuilder formattedLecturers = new StringBuilder();
        for (int i = 0; i < lecturersArray.length; i += 2) {
            if (i > 0) {
                formattedLecturers.append(", ");
            }
            formattedLecturers.append(lecturersArray[i]);
            if (i + 1 < lecturersArray.length) {
                formattedLecturers.append(" ").append(lecturersArray[i + 1]);
            }
        }

        String processedLecturers = formattedLecturers.toString();

        String link = "https://softuni.bg" + seminarElement.select("a").attr("href");
        String imageUrl = seminarElement.select(".events-container-item-header-avatar-image").attr("src");

        if (imageUrl.startsWith("http://")) {
            imageUrl = imageUrl.replace("http://", "https://");
        } else if (!imageUrl.startsWith("https://")) {
            imageUrl = "https://softuni.bg" + imageUrl;
        }

        try {
            String[] urlParts = imageUrl.split("/", 4);
            if (urlParts.length == 4) {
                String encodedPath = URLEncoder.encode(urlParts[3], "UTF-8").replace("+", "%20");
                imageUrl = urlParts[0] + "//" + urlParts[2] + "/" + encodedPath; // Rebuild the URL
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new Seminar()
                .setTitle(title)
                .setDate(date)
                .setTime(time)
                .setLecturers(processedLecturers)
                .setLink(link)
                .setImageUrl(imageUrl);
    }
}
