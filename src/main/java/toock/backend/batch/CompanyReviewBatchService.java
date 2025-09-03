package toock.backend.batch;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toock.backend.batch.dto.CompanyReviewCsvDto;
import toock.backend.company.domain.Company;
import toock.backend.company.domain.CompanyReview;
import toock.backend.company.repository.CompanyRepository;
import toock.backend.company.repository.CompanyReviewRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyReviewBatchService {

    private final CompanyRepository companyRepository;
    private final CompanyReviewRepository companyReviewRepository;

    public void loadAllCsvsInDirectory(String directoryPath) {
        File folder = new File(directoryPath);
        File[] fileList = folder.listFiles();

        if (fileList != null) {
            for (File file : fileList) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".csv")) {
                    System.out.println("✅ 처리 시작: " + file.getName());
                    try {
                        loadCsvData(file);
                    } catch (Exception e) {
                        System.err.println("❌ 처리 실패: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Transactional
    public void loadCsvData(File csvFile) throws Exception {
        String companyName = csvFile.getName().split("_")[0];

        Company company = companyRepository.findByName(companyName)
                .orElseGet(() -> companyRepository.save(
                        Company.builder()
                                .name(companyName)
                                .code(UUID.randomUUID().toString())
                                .build()
                ));

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8)) {
            List<CompanyReviewCsvDto> dtos = new CsvToBeanBuilder<CompanyReviewCsvDto>(reader)
                    .withType(CompanyReviewCsvDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            for (CompanyReviewCsvDto dto : dtos) {
                CompanyReview review = CompanyReview.builder()
                        .company(company)
                        .field(dto.getField())
                        .level(dto.getLevel())
                        .createdAt(parseDateTime(dto.getCreatedAt()))
                        .interviewedAt(parseDate(dto.getInterviewedAt()))
                        .interviewFormat(dto.getInterviewFormat())
                        .difficulty(dto.getDifficulty())
                        .summary(dto.getSummary())
                        .interviewPath(dto.getInterviewPath())
                        .interviewQuestions(dto.getInterviewQuestions())
                        .interviewAnswer(dto.getInterviewAnswer())
                        .announcementPeriod(dto.getAnnouncementPeriod())
                        .interviewResult(dto.getInterviewResult())
                        .interviewExperience(dto.getInterviewExperience())
                        .build();

                companyReviewRepository.save(review);
            }
            System.out.println("✅ 처리 완료: " + csvFile.getName() + ", " + dtos.size() + "개 행 저장");
        }
    }

    private OffsetDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");
            LocalDate date = LocalDate.parse(dateTimeStr, formatter);
            return date.atStartOfDay().atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            System.err.println("잘못된 날짜 시간 형식: " + dateTimeStr);
            return null;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            YearMonth ym = YearMonth.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM"));
            return ym.atDay(1);
        } catch (DateTimeParseException e) {
            System.err.println("잘못된 날짜 형식: " + dateStr);
            return null;
        }
    }
}