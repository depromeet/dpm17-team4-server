package depromeet.lessonfour.server.report.domain.vo;

import static depromeet.lessonfour.server.report.domain.vo.Suggestion.Quality.NEGATIVE;
import static depromeet.lessonfour.server.report.domain.vo.Suggestion.Quality.POSITIVE;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Suggestion {

  private WaterSuggestion waterSuggestion;
  private ToiletSuggestion toiletSuggestion;
  private List<HabitSuggestion> habitSuggestion;

  @Getter
  public enum WaterSuggestion {
    STANDARD("#4E5560"),
    HIGH("#23ABFF"),
    MEDIUM("#F4B005"),
    LOW("#F13A49"),
    NONE("#D9D9D9");

    private final String color;

    WaterSuggestion(String color) {
      this.color = color;
    }

    public static WaterSuggestion from(WaterLevel level) {
      return switch (level) {
        case HIGH -> HIGH;
        case MEDIUM -> MEDIUM;
        case LOW -> LOW;
        case NONE -> NONE;
      };
    }
  }

  @Getter
  public enum ToiletSuggestion {
    CONSTIPATION(NEGATIVE), // 변비 (배변 횟수 적음, 딱딱함)
    DIARRHEA(NEGATIVE), // 설사 (묽고 잦음)
    HARD_STOOL(NEGATIVE), // 딱딱한 변
    SOFT_STOOL(NEGATIVE), // 묽은 변
    BLOODY_STOOL(NEGATIVE), // 혈변
    COLOR_ABNORMAL(NEGATIVE), // 비정상 색 (녹색, 회색 등)
    PAINFUL_DEFECATION(NEGATIVE), // 배변 시 통증 있음
    LONG_DEFECATION_TIME(NEGATIVE), // 배변 시간 길음
    IRREGULAR_DEFECATION(NEGATIVE), // 불규칙한 배변 패턴

    HEALTHY_REGULAR(POSITIVE), // 규칙적인 배변
    HEALTHY_COLOR(POSITIVE), // 정상 색상 (갈색, 금색 등)
    IDEAL_SHAPE(POSITIVE), // 바나나/크림 형태 (이상적)
    PAIN_FREE(POSITIVE), // 통증 없는 배변
    NORMAL_DURATION(POSITIVE); // 적절한 시간 내 배변

    private final Quality quality;

    ToiletSuggestion(Quality quality) {
      this.quality = quality;
    }
  }

  @Getter
  public enum HabitSuggestion {
    LACK_OF_EXERCISE(NEGATIVE), // 운동 부족
    EXCESSIVE_ALCOHOL(NEGATIVE), // 알코올 과다
    EXCESSIVE_CAFFEINE(NEGATIVE), // 카페인 과다 섭취
    POOR_SLEEP_HABITS(NEGATIVE), // 수면 부족/불규칙
    HIGH_STRESS_LEVEL(NEGATIVE), // 스트레스 과다
    IRREGULAR_TOILET_HABITS(NEGATIVE), // 화장실 가는 습관 불규칙 (참기, 억지로 누르기 등)
    LONG_TOILET_TIME(NEGATIVE), // 화장실에 오래 앉아 있음 (스마트폰 등으로)

    STRESS_MANAGEMENT(POSITIVE), // 적절한 스트레스 관리 잘함
    REGULAR_TOILET_HABITS(POSITIVE), // 규칙적으로 화장실 가는 습관
    SHORT_TOILET_TIME(POSITIVE); // 화장실에 오래 머무르지 않음

    private final Quality quality;

    HabitSuggestion(Quality quality) {
      this.quality = quality;
    }

    public boolean isPositive() {
      return this.quality == POSITIVE;
    }
  }

  enum Quality {
    POSITIVE,
    NEGATIVE;
  }
}
