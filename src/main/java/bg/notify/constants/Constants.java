package bg.notify.constants;

import java.util.HashMap;
import java.util.Map;

public final class Constants {
    public static final String SOFT_UNI_SEMINARS_URL = "https://softuni.bg/trainings/seminars/GetEventsForPageByCountAndFilter?filter=1&pageSize=6&pageNumber=1";
    public static final String SOFTUNI_URL = "https://softuni.bg";
    public static String RECEIVED_ENDPOINT = "https://softuni.bg/trainings/trainings/GetLectureDetails?trainingId=%s&lectureId=%s";
    public static final String BASICS_COURSE_URL = "https://softuni.bg/courses/programming-basics";
    public static final String FUNDAMENTALS_COURSE_URL = "https://softuni.bg/courses/programming-fundamentals-csharp-java-js-python";
    public static final String DATE_REGEX = "(\\d{1,2})\\sи\\s(\\d{1,2})\\s([а-яА-Я]+)";
    public static final Map<String, String> months = new HashMap<>() {
        {
            put("january", "януари");
            put("february", "февруари");
            put("march", "март");
            put("april", "април");
            put("may", "май");
            put("june", "юни");
            put("july", "юли");
            put("august", "август");
            put("september", "септември");
            put("october", "октомври");
            put("november", "ноември");
            put("december", "декември");
        }
    };

    public static final Map<String, String> monthInNumberMap = new HashMap<>() {
        {
            put("януари", "01");
            put("февруари", "02");
            put("март", "03");
            put("април", "04");
            put("май", "05");
            put("юни", "06");
            put("юли", "07");
            put("август", "08");
            put("септември", "09");
            put("октомври", "10");
            put("ноември", "11");
            put("декември", "12");
        }
    };
}
