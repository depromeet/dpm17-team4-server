package depromeet.lessonfour.server.activityrecords.controllers;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.activityrecords.schemas.request.CreateActivityRecordsRequest;
import depromeet.lessonfour.server.activityrecords.schemas.request.UpdateActivityRecordsRequest;
import depromeet.lessonfour.server.activityrecords.schemas.response.GetActivityRecordsResponse;
import depromeet.lessonfour.server.common.exception.code.SuccessCode;
import depromeet.lessonfour.server.common.exception.schemas.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "생활 기록", description = "생활 기록에 대한 API 문서입니다.")
@RestController
@RequestMapping("/api/v1/activity-records")
@RequiredArgsConstructor
public class ActivityRecordsController {

  @Operation(summary = "생활 기록 생성", description = "새로운 생활 기록을 생성합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "생활 기록 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
      })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public SuccessResponse<?> createActivityRecord(
      @RequestBody @Valid CreateActivityRecordsRequest dto) {
    return SuccessResponse.of(SuccessCode.SUCCESS_CREATE);
  }

  @Operation(summary = "생활 기록 수정", description = "기존 생활 기록을 수정합니다. 없던 음식은 추가됩니다.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "생활 기록 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 생활 기록"),
      })
  @PatchMapping("/{activity-record-id}")
  public SuccessResponse<?> updateActivityRecord(
      @PathVariable("activity-record-id") UUID activityRecordId,
      @RequestBody @Valid UpdateActivityRecordsRequest dto) {
    return SuccessResponse.of(SuccessCode.SUCCESS_UPDATE);
  }

  @Operation(summary = "생활 기록 삭제", description = "기존 생활 기록을 삭제합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "생활 기록 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 생활 기록"),
      })
  @DeleteMapping("/{activity-record-id}")
  public SuccessResponse<?> deleteActivityRecord(
      @PathVariable("activity-record-id") UUID activityRecordId) {
    return SuccessResponse.of(SuccessCode.SUCCESS_DELETE);
  }

  @Operation(summary = "생활 기록 조회", description = "기존 생활 기록을 조회합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "생활 기록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식"),
        @ApiResponse(responseCode = "404", description = "해당 날짜의 생활 기록이 없음"),
      })
  @GetMapping
  public SuccessResponse<GetActivityRecordsResponse> getActivityRecord(
      @RequestParam(required = true) LocalDate date) {
    return SuccessResponse.of(null);
  }
}
