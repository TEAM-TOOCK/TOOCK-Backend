package toock.backend.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("csv-loader")
@Component
@RequiredArgsConstructor
public class DataLoaderRunner implements CommandLineRunner {

    private final CompanyReviewBatchService batchService;

    private static final String CSV_FOLDER_PATH = "src/main/resources/csv/";

    @Override
    public void run(String... args) throws Exception {
        System.out.println("==================================================");
        System.out.println("🚀 CSV 데이터 로딩 작업을 시작합니다.");
        System.out.println("📂 대상 폴더: " + CSV_FOLDER_PATH);
        System.out.println("==================================================");

        batchService.loadAllCsvsInDirectory(CSV_FOLDER_PATH);

        System.out.println("==================================================");
        System.out.println("🎉 모든 CSV 파일 처리가 완료되었습니다.");
        System.out.println("==================================================");
    }
}