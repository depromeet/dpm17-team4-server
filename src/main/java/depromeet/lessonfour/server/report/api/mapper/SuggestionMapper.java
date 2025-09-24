package depromeet.lessonfour.server.report.api.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.SuggestionDto;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.SuggestionItem;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;

@Component
public class SuggestionMapper {

  public SuggestionDto map(Suggestion suggestion) {
    return new SuggestionDto(
        "장 상태를 개선하려면 이런 습관을 추천해요",
        List.of(
            new SuggestionItem(
                "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion_water.png",
                "물 섭취량을 더 늘려보세요",
                "하루 권장 물 섭취량은 성인 기준 2L 예요"),
            new SuggestionItem(
                "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion_food.png",
                "충분한 식이섬유가 중요해요",
                "과일과 채소를 섭취하면 좋은 흐름이 유지돼요"),
            new SuggestionItem(
                "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion_note.png",
                "지속적으로 배변을 기록해요",
                "배변이 잘 되는 나만의 루틴을 만들 수 있어요")));
  }
}
