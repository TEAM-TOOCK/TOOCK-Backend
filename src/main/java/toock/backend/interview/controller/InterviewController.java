package toock.backend.interview.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import toock.backend.interview.dto.InterviewDto;
import toock.backend.interview.service.InterviewService;

@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/start")
    public ResponseEntity<InterviewDto.StartResponse> startInterview(@RequestBody InterviewDto.StartRequest request) {
        return ResponseEntity.ok(interviewService.startInterview(request));
    }

    @PostMapping("/next")
    public ResponseEntity<InterviewDto.NextResponse> nextQuestion(@RequestBody InterviewDto.NextRequest request) {
        return ResponseEntity.ok(interviewService.nextQuestion(request));
    }
}