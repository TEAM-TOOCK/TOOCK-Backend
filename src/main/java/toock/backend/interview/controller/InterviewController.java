package toock.backend.interview.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import toock.backend.infra.s3.S3Service;
import toock.backend.infra.whisper.service.WhisperService;
import toock.backend.interview.dto.InterviewDto;
import toock.backend.interview.dto.InterviewAnalysisResponseDto;
import toock.backend.interview.service.InterviewService;
import toock.backend.global.dto.CommonResponseDto;

@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final S3Service s3Service;
    private final WhisperService whisperService;

    @PostMapping("/start")
    public ResponseEntity<InterviewDto.StartResponse> startInterview(@RequestBody InterviewDto.StartRequest request, @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(interviewService.startInterview(request,memberId));
    }

    @PostMapping("/next")
    public ResponseEntity<InterviewDto.NextResponse> nextQuestion(
            @RequestParam("interviewSessionId") Long interviewSessionId,
            @RequestParam("audioFile") MultipartFile audioFile,
            @AuthenticationPrincipal Long memberId
            ) {

        // 1. 음성 파일을 S3에 업로드하고 URL을 받음.
        String s3Url = s3Service.uploadAudio(audioFile, "interview-audio");

        // 2. 음성 파일을 텍스트로 변환
        String answerText = whisperService.transcribeAudio(audioFile);

        // 3. 기존 InterviewService의 nextQuestion 메서드에 필요한 DTO를 생성
        InterviewDto.NextRequest request = new InterviewDto.NextRequest();
        request.setInterviewSessionId(interviewSessionId);
        request.setAnswerText(answerText);
        request.setS3Url(s3Url);

        // 4. 면접 로직을 처리하고 다음 질문을 받아 응답합니다.
        return ResponseEntity.ok(interviewService.nextQuestion(request,memberId));
    }

    @PostMapping("/analyze/{interviewSessionId}")
    public ResponseEntity<CommonResponseDto<InterviewAnalysisResponseDto>> analyzeInterview(@PathVariable Long interviewSessionId) {
        InterviewAnalysisResponseDto analysis = interviewService.evaluateInterview(interviewSessionId);
        return ResponseEntity.ok(CommonResponseDto.success(analysis));
    }

    @GetMapping("/results/{interviewSessionId}")
    public ResponseEntity<CommonResponseDto<InterviewAnalysisResponseDto>> getInterviewResult(@PathVariable Long interviewSessionId) {
        InterviewAnalysisResponseDto analysis = interviewService.getInterviewAnalysis(interviewSessionId);
        return ResponseEntity.ok(CommonResponseDto.success(analysis));
    }
}