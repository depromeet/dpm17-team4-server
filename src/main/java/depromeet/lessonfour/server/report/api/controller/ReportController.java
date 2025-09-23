package depromeet.lessonfour.server.report.api.controller;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.report.api.mapper.DailyReportMapper;
import depromeet.lessonfour.server.report.app.dto.response.DailyReport;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto;
import depromeet.lessonfour.server.report.app.service.GetDailyReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Tag(name = "리포트", description = "리포트 관련 API 문서입니다.")
@Valid
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

  private final GetDailyReportUseCase getDailyReportUseCase;
  private final DailyReportMapper dailyReportMapper;
  private final Clock clock;

  @Operation(summary = "일간 리포트 조회", description = "특정 날짜의 일간 리포트를 조회합니다.")
  @GetMapping("/daily")
  public SuccessResponse<GetDailyReportResponseDto> getDailyReport(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @NotNull(message = "리포트 생성 시간은 필수입니다") @RequestParam(required = false)
          LocalDateTime dateTime) {
    LocalDateTime baseDateTime = (dateTime != null) ? dateTime : LocalDateTime.now(clock);
    DailyReport dailyReport = getDailyReportUseCase.getDailyReport(userId, baseDateTime);
    dailyReportMapper.map(dailyReport, baseDateTime);
  }
}
