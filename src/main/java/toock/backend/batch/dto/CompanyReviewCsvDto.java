package toock.backend.batch.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanyReviewCsvDto {

    @CsvBindByName(column = "Job Field")
    private String field;

    @CsvBindByName(column = "Job Level")
    private String level;

    @CsvBindByName(column = "Date Posted")
    private String createdAt;

    @CsvBindByName(column = "Interview Date")
    private String interviewedAt;

    @CsvBindByName(column = "Hiring Method")
    private String interviewFormat;

    @CsvBindByName(column = "Interview Difficulty")
    private String difficulty;

    @CsvBindByName(column = "Summary/Review")
    private String summary;

    @CsvBindByName(column = "Interview Path")
    private String interviewPath;

    @CsvBindByName(column = "Interview Questions")
    private String interviewQuestions;

    @CsvBindByName(column = "Interview Answer/Feeling")
    private String interviewAnswer;

    @CsvBindByName(column = "Announcement Period")
    private String announcementPeriod;

    @CsvBindByName(column = "Interview Result")
    private String interviewResult;

    @CsvBindByName(column = "Interview Experience")
    private String interviewExperience;
}