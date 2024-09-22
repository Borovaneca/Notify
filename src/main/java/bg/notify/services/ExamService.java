package bg.notify.services;

import bg.notify.entities.Exam;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bg.notify.constants.Constants.*;
import static bg.notify.services.SeminarService.getConnection;

@Service
public class ExamService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("bg", "BG"));
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public List<Exam> checkForNewExamsBasics() throws IOException {
        String moduleId = extractModuleId(BASICS_COURSE_URL);
        String moduleName = extractModuleName(BASICS_COURSE_URL);

        String[] examHrefs = fetchExamHrefs(moduleName);

        String preliminaryExam = fetchAndPrintExamDates(moduleId, examHrefs[0]);
        String regularExam = fetchAndPrintExamDates(moduleId, examHrefs[1]);

        validateExamDates(preliminaryExam, regularExam);

        String month = months.get(getMonthName(moduleName));
        String year = String.valueOf(LocalDateTime.now().getYear());

        Exam preliminaryExamEntity = createExam("Programming Basics", preliminaryExam, month, year);
        Exam regularExamEntity = createExam("Programming Basics", regularExam, month, year);

        return List.of(preliminaryExamEntity, regularExamEntity);
    }

    private void validateExamDates(String preliminaryExam, String regularExam) {
        if (preliminaryExam.equals("No dates found") || regularExam.equals("No dates found")) {
            throw new IllegalArgumentException("Month not found");
        }
    }

    private Exam createExam(String coursePrefix, String examDates, String month, String year) {
        String[] mappedDates = getMappedDate(examDates);
        return new Exam(String.format("%s - %s %s", coursePrefix, month, year), mappedDates[0], mappedDates[1]);
    }

    private String getMonthName(String moduleName) {
        Matcher matcher = Pattern.compile("-([a-zA-Z]+)-\\d{4}$").matcher(moduleName);
        return matcher.find() ? matcher.group(1) : "Month not found";
    }

    private String[] getMappedDate(String date) {
        try {
            String[] parts = date.split(" ");
            String startDay = parts[0];
            String endDay = parts[2];
            String monthInWords = parts[3];
            String year = monthInWords.equals("януари") ? String.valueOf(LocalDateTime.now().getYear() + 1) : String.valueOf(LocalDateTime.now().getYear());

            LocalDate startDate = LocalDate.parse(startDay + " " + monthInWords + " " + year, MONTH_FORMATTER);
            LocalDate endDate = LocalDate.parse(endDay + " " + monthInWords + " " + year, MONTH_FORMATTER);

            return new String[]{startDate.format(OUTPUT_FORMATTER), endDate.format(OUTPUT_FORMATTER)};
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Error parsing date: " + e.getMessage());
        }
    }

    private String extractModuleName(String courseUrl) throws IOException {
        Document document = Jsoup.parse(fetchContent(courseUrl));
        return document.selectFirst("article.page-wrapper.course-details-page-sticky")
                .selectFirst("a.softuni-btn-primary").attr("href");
    }

    private String extractModuleId(String url) throws IOException {
        Document document = Jsoup.parse(fetchContent(url));
        String href = document.selectFirst("article.page-wrapper.course-details-page-sticky")
                .selectFirst("a.softuni-btn-primary").attr("href");

        Matcher matcher = Pattern.compile("/trainings/(\\d+)/").matcher(href);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String[] fetchExamHrefs(String moduleHref) throws IOException {
        Document document = Jsoup.parse(fetchContent(SOFTUNI_URL + moduleHref));
        Elements elements = document.select("article.lecture-title");

        String firstExamHref = "";
        String secondExamHref = "";

        for (Element element : elements) {
            if (element.text().contains("Предварителен онлайн изпит")) {
                firstExamHref = element.attr("href").replace("#lesson-", "");
            } else if (element.text().contains("Редовен онлайн изпит")) {
                secondExamHref = element.attr("href").replace("#lesson-", "");
            }
        }
        return new String[]{firstExamHref, secondExamHref};
    }

    private String fetchAndPrintExamDates(String moduleId, String examHref) throws IOException {
        String examUrl = String.format(RECEIVED_ENDPOINT, moduleId, examHref);
        Document document = Jsoup.parse(fetchContent(examUrl));

        Matcher matcher = Pattern.compile(DATE_REGEX).matcher(document.text());
        return matcher.find() ? matcher.group(1) + " и " + matcher.group(2) + " " + matcher.group(3) : "No dates found";
    }

    private String fetchContent(String url) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader response = getConnection(url);
        String inputLine;
        while ((inputLine = response.readLine()) != null) {
            content.append(inputLine);
        }
        response.close();
        return content.toString();
    }


    public List<Exam> checkForNewExamsFundamentals() throws IOException {
        String[] moduleInfo = extractModuleIdForFundamentals(FUNDAMENTALS_COURSE_URL);

        String[] examHrefs = fetchExamHrefsForFundamentals(moduleInfo[1]);

        String monthName = getMonthName(moduleInfo[1]);
        String monthInBulgarian = months.get(monthName);
        String year = monthName.equals("януари") ? String.valueOf(LocalDateTime.now().getYear() + 1) : String.valueOf(LocalDateTime.now().getYear());

        return createExams(moduleInfo[0], examHrefs, "Programming Fundamentals", monthInBulgarian, year);
    }

    private List<Exam> createExams(String moduleId, String[] examHrefs, String courseNamePrefix, String month, String year) throws IOException {
        List<Exam> exams = new ArrayList<>();

        String[] examTypes = {"Mid Exam", "Practical Exam", "Retake Mid Exam", "Retake Practical Exam"};

        for (int i = 0; i < examHrefs.length; i++) {
            String examDate = fetchAndGetExamDates(moduleId, examHrefs[i]);
            exams.add(createSingleExam(courseNamePrefix, examDate, examTypes[i], month, year));
        }

        return exams;
    }

    private Exam createSingleExam(String coursePrefix, String examDate, String examType, String month, String year) {
        return new Exam(String.format("%s - %s %s (%s)", coursePrefix, month, year, examType), examDate, examDate);
    }

    private String fetchAndGetExamDates(String moduleId, String examHref) throws IOException {
        Document document = Jsoup.parse(fetchContent(String.format(RECEIVED_ENDPOINT, moduleId, examHref)));
        Element element = document.selectFirst(".lecture-details-short-description-list");

        if (element != null) {
            String[] parts = element.text().replace("Дата: ", "").trim().split(" ");
            if (parts.length == 2) {
                String dateString = String.format("%02d-%s-%04d", Integer.parseInt(parts[0]), monthInNumberMap.get(parts[1]), LocalDate.now().getYear());
                return LocalDate.parse(dateString, OUTPUT_FORMATTER).format(OUTPUT_FORMATTER);
            }
        }
        return null;
    }

    private String[] extractModuleIdForFundamentals(String url) throws IOException {
        Document document = Jsoup.parse(fetchContent(url));
        String href = document.select("article.course-details-page-course-info-course-card a").attr("href");

        Matcher matcher = Pattern.compile("/trainings/(\\d+)/").matcher(href);
        String[] data = new String[2];
        if (matcher.find()) {
            data[0] = matcher.group(1);
            data[1] = href.replaceAll("https://softuni.bg/trainings0/\\d{3,4}", "");
        } else {
            throw new IllegalArgumentException("No course id was found or name");
        }
        return data;
    }

    private String[] fetchExamHrefsForFundamentals(String moduleHref) throws IOException {
        Document document = Jsoup.parse(fetchContent(SOFTUNI_URL + moduleHref));
        Elements elements = document.select("article.lecture-title");

        String[] examHrefs = new String[4];
        for (Element element : elements) {
            if (element.text().equals("Mid Exam")) {
                examHrefs[0] = element.attr("href").replace("#lesson-", "");
            } else if (element.text().equals("Practical Exam")) {
                examHrefs[1] = element.attr("href").replace("#lesson-", "");
            } else if (element.text().equals("Retake Mid Exam")) {
                examHrefs[2] = element.attr("href").replace("#lesson-", "");
            } else if (element.text().equals("Retake Practical Exam")) {
                examHrefs[3] = element.attr("href").replace("#lesson-", "");
            }
        }
        return examHrefs;
    }
}
