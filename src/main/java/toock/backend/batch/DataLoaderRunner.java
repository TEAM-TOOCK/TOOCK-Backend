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

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.err.println("❌ CSV 파일이 있는 폴더 경로를 입력해주세요.");
            System.err.println("✅ 실행 예시: ... --args=\"C:/your/csv/folder\"");
            return;
        }

        String csvFolderPath = args[0];
        System.out.println("==================================================");
        System.out.println("🚀 CSV 데이터 로딩 작업을 시작합니다.");
        System.out.println("📂 대상 폴더: " + csvFolderPath);
        System.out.println("==================================================");

        batchService.loadAllCsvsInDirectory(csvFolderPath);

        System.out.println("==================================================");
        System.out.println("🎉 모든 CSV 파일 처리가 완료되었습니다.");
        System.out.println("==================================================");
    }
}