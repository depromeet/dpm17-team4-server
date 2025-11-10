package depromeet.lessonfour.server.report.api.mapper.suggestion;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.SuggestionSection;
import depromeet.lessonfour.server.report.api.dto.response.SuggestionSection.SuggestionItem;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.app.dto.response.WeeklyReport;
import depromeet.lessonfour.server.report.domain.vo.suggestion.SuggestionType;

@Component
public class SuggestionMapper {

  private static final String titleMessage = "장 상태를 개선하려면\n" + "이런 습관을 추천해요";

  public SuggestionSection map(WeeklyReport report) {
    List<SuggestionItem> items =
        report.suggestions().stream().map(this::mapSuggestionItem).toList();

    return new SuggestionSection(titleMessage, items);
  }

  public SuggestionSection map(MonthlyReport report) {
    List<SuggestionItem> recommendSuggestions =
        report.suggestions().stream().map(this::mapSuggestionItem).toList();

    return new SuggestionSection(titleMessage, recommendSuggestions);
  }

  public SuggestionItem mapSuggestionItem(SuggestionType suggestion) {
    return switch (suggestion) {
      case WATER_LACK -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Awater_lack.png",
          "물 섭취량을 더 늘려보세요", "하루 권장 물 섭취량은 성인 기준 2L 예요");
      case FIBER_LACK -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Afiber_lack.png",
          "충분한 식이섬유가 중요해요", "과일과 채소를 섭취하면 좋은 흐름이 유지돼요");
      case RECORD_LACK -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Arecord_lack.png",
          "충분한 식이섬유가 중요해요", "과일과 채소를 섭취하면 좋은 흐림이 유지돼요");
      case SPICY_FOOD_AT_BREAKFAST -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Aspicy_food_at_breakfast.png",
          "공복에 자극적인 음식을 피해주세요", "맵거나 기름진 음식은 공복에 피하는 게 좋아요");
      case DAILY_PRODUCT -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Adaily_product.png",
          "유제품이 장에 부담을 줄 수 있어요", "우유, 치즈, 요거트 섭취 후 불편함이 있었다면 주의!");
      case CAFFEINE -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Acaffeine.png",
          "카페인은 장을 자극할 수 있어요", "커피나 에너지 음료 대신 따뜻한 보리차를 마셔보세요");
      case ALCOHOL -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Aalcohol.png",
          "과도한 음주는 피해주세요", "음주는 장 점막을 자극해 배변에 영향을 줄 수 있어요");
      case HIGH_PROTEIN -> new SuggestionItem(
          "", "반찬에 샐러드나 과일도 곁들여보세요", "단백질 위주 식사는 변비를 유발할 수 있어요");
      case HIGH_FAT -> new SuggestionItem(
          "", "텐동, 치킨 같은 튀김류 당분간 참아보세요", "기름진 음식은 소화에 부담을 주고 장을 무겁게 만들어요");
      case SPICY_FOOD -> new SuggestionItem(
          "", "자극적인 음식은 장에 부담을 줄 수 있어요", "너무 맵거나 기름진 음식은 장을 예민하게 만들어요");
      case OVEREATING -> new SuggestionItem(
          "", "과식을 피하고 적정량을 나눠 드세요", "한 끼에 많은 양을 드셨다면 다음 식사는 가볍게 드세요");
      case HIGH_STRESS -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Ahigh_stress.png",
          "스트레스를 풀 나만의 방법을 찾아보세요", "스트레스가 장 운동과 배변 상태를 악화시킬 수 있어요");
      case LACTOBACILLUS -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Alactobacillus.png",
          "유산균을 꾸준히 섭취해보세요", "프로바이오틱스는 장내 환경을 개선하는 데 도움돼요");
      case ENTERITIS -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Aenteritis.png",
          "장염이나 설사 후엔 회복이 중요해요", "죽, 바나나, 연두부 등 소화가 잘되는 식단을 추천해요");
      case SOFT_FOOD -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Asoft_food.png",
          "장이 민감할 땐 부드러운 음식을 추천해요", "죽, 요거트, 감자 등 소화가 잘되는 음식을 섭취해보세요");
      case IRREGULAR_TOILET_TIME -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Airregular_toilet.png",
          "매일 같은 시간에 화장실에 가보세요", "규칙적인 시도로 나만의 배변 루틴을 만들어 보세요");
      case MENSTRUATION -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Amenstruation.png",
          "생리 중에는 따뜻한 물을 추천해요", "생리나 호르몬 변화는 배변 패턴에 영향을 줄 수 있어요");
      case STRETCHING -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Astretching.png",
          "잠들기 전 5분 스트레칭을 해보세요", "스트레칭이 부교감신경을 자극해 장 운동을 도와줘요");
      case SEAWEED -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Aseaweed.png",
          "식단에 해조류를 추가해보세요", "해조류의 수용성 식이섬유는 장 환경에 도움을 줘요");
      case MIXED_GRAIN -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Amixed_grain.png",
          "식단에 잡곡을 넣어보세요", "정제 탄수화물보다 장에 좋은 영향을 줄 수 있어요");
      case BREAKFAST_LACK -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Abreakfast_lack.png",
          "아침밥을 챙겨 먹어보세요", "하루 세끼 규칙적인 식사는 쾌변을 도와요");
      case WATER_MEDIUM -> new SuggestionItem(
          "", "아침에 일어나 물 한 잔 드셔보세요", "아침 물 한 잔은 장을 깨우는 데 효과적이에요");
      case LONG_TOILET_TIME -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Along_toilet_time.png",
          "화장실에 너무 오래 앉아있지 마세요", "오랜 시간 앉아 있을수록 치질 위험이 증가합니다");
      case LONG_TOILET_DURATION -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Along_toilet_duration.png",
          "배변 신호를 참지 마세요", "배변을 참는 습관은 장 리듬을 깨트릴 수 있어요");
      case NEED_MEDICAL_EXAMINATION -> new SuggestionItem(
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/suggestion%3Aneed_medical_examination.png",
          "전문가와 상담을 권장합니다", "검진이 필요한 배변 상태가 확인되었어요");
    };
  }
}
