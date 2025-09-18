package depromeet.lessonfour.server.toiletrecords.controllers;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.exception.code.SuccessCode;
import depromeet.lessonfour.server.common.exception.schemas.SuccessResponse;
import depromeet.lessonfour.server.toiletrecords.schemas.request.ToiletRecordCreateRequestDto;
import depromeet.lessonfour.server.toiletrecords.schemas.request.ToiletRecordUpdateRequestDto;
import depromeet.lessonfour.server.toiletrecords.schemas.response.ToiletRecordResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/poo-records")
public class ToiletRecordController {

  @Operation(summary = "배변기록 등록")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "success"),
    @ApiResponse(responseCode = "500", description = "server error", content = @Content)
  })
  @PostMapping
  public ResponseEntity<SuccessResponse<ToiletRecordResponseDto>> createToiletRecord(
      @Valid @RequestBody ToiletRecordCreateRequestDto request,
      @AuthenticationPrincipal(expression = "id") Long userId) {
    // 임시 더미 응답
    ToiletRecordResponseDto response =
        new ToiletRecordResponseDto(
            UUID.randomUUID(), // 생성된 레코드 id (더미)
            userId, // 작성자 id
            request.toiletAt(),
            request.isToiletSuccess(),
            request.toiletColor(),
            request.toiletShape(),
            request.painScore(),
            request.toiletDuration(),
            request.additionalNote());
    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, response));
  }

  @Operation(summary = "배변기록 상세조회")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "success"),
    @ApiResponse(responseCode = "404", description = "not found"),
    @ApiResponse(responseCode = "500", description = "server error", content = @Content)
  })
  @GetMapping("/{toiletRecordId}/detail")
  public ResponseEntity<SuccessResponse<ToiletRecordResponseDto>> getToiletRecordDetail(
      @PathVariable UUID toiletRecordId, @AuthenticationPrincipal(expression = "id") Long userId) {
    // 임시 더미 응답
    ToiletRecordResponseDto response =
        new ToiletRecordResponseDto(
            toiletRecordId,
            userId,
            LocalDateTime.now().minusMinutes(10),
            true,
            null,
            null,
            0,
            5,
            null);
    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, response));
  }

  @Operation(summary = "배변기록 수정")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "success"),
    @ApiResponse(responseCode = "404", description = "not found"),
    @ApiResponse(responseCode = "500", description = "server error", content = @Content)
  })
  @PatchMapping("/update/{toiletRecordId}")
  public ResponseEntity<SuccessResponse<ToiletRecordResponseDto>> updateToiletRecord(
      @Valid @RequestBody ToiletRecordUpdateRequestDto request,
      @PathVariable UUID toiletRecordId,
      @AuthenticationPrincipal(expression = "id") Long userId) {
    // 임시 더미 응답
    ToiletRecordResponseDto response =
        new ToiletRecordResponseDto(
            UUID.randomUUID(), // 생성된 레코드 id (더미)
            userId, // 작성자 id
            request.toiletAt(),
            request.isToiletSuccess(),
            request.toiletColor(),
            request.toiletShape(),
            request.painScore(),
            request.toiletDuration(),
            request.additionalNote());
    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_UPDATE, response));
  }

  @Operation(summary = "배변기록 삭제")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "success"),
    @ApiResponse(responseCode = "404", description = "not found"),
    @ApiResponse(responseCode = "500", description = "server error", content = @Content)
  })
  @DeleteMapping("/{toiletRecordId}")
  public ResponseEntity<SuccessResponse<Void>> deleteToiletRecord(
      @PathVariable UUID toiletRecordId, @AuthenticationPrincipal(expression = "id") UUID userId) {
    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_DELETE));
  }
}
