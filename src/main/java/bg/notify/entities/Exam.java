package bg.notify.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "exams")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;
    private String startDate;
    private String endDate;

    @Override
    public String toString() {
        return "Exam{" +
                " courseName='" + courseName + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return Objects.equals(courseName, exam.courseName) &&
                Objects.equals(startDate, exam.startDate) &&
                Objects.equals(endDate, exam.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, courseName, startDate, endDate);
    }
}
