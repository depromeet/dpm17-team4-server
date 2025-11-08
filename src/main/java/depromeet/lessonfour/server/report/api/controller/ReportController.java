package depromeet.lessonfour.server.report.api.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.SuccessCode;
import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto;
import depromeet.lessonfour.server.report.api.mapper.DailyReportMapper;
import depromeet.lessonfour.server.report.api.mapper.MonthlyReportMapper;
import depromeet.lessonfour.server.report.api.mapper.WeeklyReportMapper;
import depromeet.lessonfour.server.report.app.dto.response.DailyReport;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.app.dto.response.WeeklyReport;
import depromeet.lessonfour.server.report.app.service.GetDailyReportUseCase;
import depromeet.lessonfour.server.report.app.service.MonthlyReportUseCase;
import depromeet.lessonfour.server.report.app.service.WeeklyReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "리포트", description = "리포트 관련 API 문서입니다.")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

  private final GetDailyReportUseCase getDailyReportUseCase;
  private final WeeklyReportUseCase weeklyReportUseCase;
  private final MonthlyReportUseCase monthlyReportUseCase;
  private final DailyReportMapper dailyReportMapper;
  private final WeeklyReportMapper weeklyReportMapper;
  private final MonthlyReportMapper monthlyReportMapper;
  private final Clock clock;

  @Operation(summary = "일간 리포트 조회", description = "특정 날짜의 일간 리포트를 조회합니다.")
  @GetMapping("/daily")
  public SuccessResponse<GetDailyReportResponseDto> getDailyReport(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @RequestParam(required = false) LocalDateTime dateTime) {
    LocalDateTime baseDateTime = (dateTime != null) ? dateTime : LocalDateTime.now(clock);
    DailyReport dailyReport = getDailyReportUseCase.getDailyReport(userId, baseDateTime);
    return SuccessResponse.of(
        SuccessCode.SUCCESS_FETCH, dailyReportMapper.map(dailyReport, baseDateTime));
  }

  @Operation(summary = "주간 리포트 조회", description = "특정 주의 주간 리포트를 조회합니다.")
  @GetMapping("/weekly")
  public SuccessResponse<GetWeeklyReportResponseDto> getWeeklyReport(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @RequestParam(required = false) LocalDateTime dateTime) {
    LocalDateTime baseDateTime = (dateTime != null) ? dateTime : LocalDateTime.now(clock);
    WeeklyReport weeklyReport = weeklyReportUseCase.generateWeeklyReport(userId, baseDateTime);
    return SuccessResponse.of(
        SuccessCode.SUCCESS_FETCH, weeklyReportMapper.map(weeklyReport, baseDateTime));
  }

  @Operation(summary = "월간 리포트 조회", description = "특정 달의 월간 리포트를 조회합니다.")
  @PostMapping("/monthly")
  public SuccessResponse<GetMonthlyReportResponseDto> generateMonthlyReport(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @RequestParam(required = false) YearMonth yearMonth) {
    YearMonth baseMonth = (yearMonth != null) ? yearMonth : YearMonth.now(clock);

    MonthlyReport monthlyReport = monthlyReportUseCase.generateMonthlyReport(userId, baseMonth);

    return SuccessResponse.of(SuccessCode.SUCCESS_CREATE, monthlyReportMapper.map(monthlyReport));
  }
}
