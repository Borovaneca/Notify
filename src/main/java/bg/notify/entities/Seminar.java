package bg.mck.sentinel.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Seminar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String title;
    String date;
    String time;
    String lecturers;
    String link;
    String imageUrl;

    public Seminar() {
    }

    public Seminar(Long id, String title, String date, String time, String lecturers, String link, String imageUrl) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
        this.lecturers = lecturers;
        this.link = link;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public Seminar setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Seminar setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDate() {
        return date;
    }

    public Seminar setDate(String date) {
        this.date = date;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Seminar setTime(String time) {
        this.time = time;
        return this;
    }

    public String getLecturers() {
        return lecturers;
    }

    public Seminar setLecturers(String lecturers) {
        this.lecturers = lecturers;
        return this;
    }

    public String getLink() {
        return link;
    }

    public Seminar setLink(String link) {
        this.link = link;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Seminar setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seminar seminar = (Seminar) o;
        return  Objects.equals(title, seminar.title) &&
                Objects.equals(date, seminar.date) &&
                Objects.equals(time, seminar.time) &&
                Objects.equals(lecturers, seminar.lecturers) &&
                Objects.equals(link, seminar.link) &&
                Objects.equals(imageUrl, seminar.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, date, time, lecturers, link, imageUrl);
    }

    @Override
    public String toString() {
        return "Seminar{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", lecturers='" + lecturers + '\'' +
                ", link='" + link + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
