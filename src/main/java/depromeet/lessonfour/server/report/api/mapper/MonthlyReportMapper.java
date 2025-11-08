package depromeet.lessonfour.server.report.api.mapper;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.FoodSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.FoodSection.MonthlyComparison;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.FoodSection.WeeklyGroup;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyRecordCount;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletColor;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletColor.ColorCount;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletPain;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletPain.ToiletPainComparison;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletPeriod;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletTime;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.TimeOfDaySection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.ToiletShapeSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.UserAverage;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.WaterSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.WaterSection.WaterItem;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.app.dto.response.RecordCounts;
import depromeet.lessonfour.server.report.app.dto.response.ToiletColorCount;
import depromeet.lessonfour.server.report.app.dto.response.ToiletShapeCount;
import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterLevel;
import depromeet.lessonfour.server.report.domain.vo.monthly.DayPeriod;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPainDistribution;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPeriodCount;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletTimeDistribution;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MonthlyReportMapper {

  private final StressMapper stressMapper;
  private final SuggestionMapper suggestionMapper;

  // 컨트롤러 기본 사용
  public GetMonthlyReportResponseDto map(MonthlyReport m) {
    return internalMap(m, null);
  }

  // YearMonth까지 넘기고 싶을 때 사용
  public GetMonthlyReportResponseDto map(MonthlyReport m, YearMonth month) {
    return internalMap(m, month);
  }

  private GetMonthlyReportResponseDto internalMap(MonthlyReport m, YearMonth month) {
    MonthlyRecordCount recordCount = mapRecordCount(m.recordCounts());
    List<Integer> monthlyDefecationScore = safeList(m.weeklyAverageScores());
    UserAverage userAverage = mapUserAverage(m.averageScore());
    var monthlyScore = m.monthlyScore();

    ToiletShapeSection shapeSection = mapShapeSection(m.shape());
    MonthlyToiletTime timeSection = mapTimeDistribution(m.timeDistribution());
    MonthlyToiletColor colorSection = mapColorSection(m.color());
    MonthlyToiletPain painSection = mapPainSection(m.pain());
    TimeOfDaySection timeOfDaySection = mapTimeOfDay(m.timeOfDay());

    // 월간 Activity (주차 단위 → DTO 주차그룹으로 변환)
    FoodSection foodSection = mapFoodSection(m.activityReport(), month);
    WaterSection waterSection = mapWaterSection(m.activityReport());
    var stressSection = mapStressSection(m.activityReport());

    var suggestionDto = mapSuggestionSection(m.suggestion());

    return new GetMonthlyReportResponseDto(
        recordCount,
        monthlyDefecationScore,
        userAverage,
        monthlyScore,
        shapeSection,
        timeSection,
        colorSection,
        painSection,
        timeOfDaySection,
        foodSection,
        waterSection,
        stressSection,
        suggestionDto);
  }

  // ===== 상단 =====
  private static MonthlyRecordCount mapRecordCount(RecordCounts rc) {
    return new MonthlyRecordCount(rc.totalCount(), rc.toiletCount(), rc.activityCount());
  }

  private static UserAverage mapUserAverage(double myAverage) {
    double overallAvg = myAverage; // 전체 평균 미정 → 우선 동일값
    double topPercent = (myAverage >= 80) ? 20.0 : (myAverage >= 60) ? 50.0 : 80.0;
    String title = String.format("이번 달 배변 점수는\n상위 %.0f%%예요", topPercent);
    return new UserAverage(myAverage, overallAvg, topPercent, title);
  }

  // ===== 화장실 섹션 =====
  private ToiletShapeSection mapShapeSection(List<ToiletShapeCount> shapes) {
    String title = "이번 달 자주 본 배변 모양이에요";
    List<GetMonthlyReportResponseDto.MonthlyToiletShape> items =
        (shapes == null)
            ? List.of()
            : shapes.stream()
                .sorted(Comparator.comparing(ToiletShapeCount::count).reversed())
                .limit(3)
                .map(
                    sc ->
                        new GetMonthlyReportResponseDto.MonthlyToiletShape(
                            sc.shape().getValue(), sc.count(), warningByShape(sc.shape())))
                .collect(toList());
    return new ToiletShapeSection(title, items);
  }

  private static String warningByShape(ToiletShape shape) {
    return switch (shape) {
      case RABBIT -> "변비 주의";
      case CORN -> "수분 충전 필요";
      case CREAM, PORRIDGE, WATER -> "설사 주의";
      default -> null;
    };
  }

  private static MonthlyToiletTime mapTimeDistribution(ToiletTimeDistribution td) {
    String title = "이번 달 배변 소요 시간을 살펴봤어요";
    ToiletTimeDistribution safe = (td == null) ? new ToiletTimeDistribution(0, 0, 0) : td;
    return MonthlyToiletTime.from(safe, title);
  }

  private MonthlyToiletColor mapColorSection(List<ToiletColorCount> colors) {
    List<ColorCount> items =
        (colors == null) ? List.of() : colors.stream().map(ColorCount::from).collect(toList());

    String topLabel =
        (items.isEmpty())
            ? "없어요"
            : items.stream()
                .max(Comparator.comparingInt(ColorCount::count))
                .map(ColorCount::color)
                .orElse("없어요");

    String title = "가장 많이 확인한 색상은\n" + topLabel + "예요";
    String priorityMsg = topPriorityColorMessage(colors);

    return new MonthlyToiletColor(items, priorityMsg, title);
  }

  private static String topPriorityColorMessage(List<ToiletColorCount> counts) {
    if (counts == null || counts.isEmpty()) return null;
    if (counts.stream().anyMatch(c -> c.color() == ToiletColor.RED && c.count() > 0))
      return "혈변은 건강의 적신호예요. 대장염, 대장암, 치질 등의 문제일 수도 있어요. 빠른 병원 방문을 권장해요.";
    if (counts.stream().anyMatch(c -> c.color() == ToiletColor.GRAY && c.count() > 0))
      return "흰색은 건강의 적신호예요. 간이나 담도가 좋지 않은 상태일 수도 있어요. 빠른 병원 방문을 권장해요.";
    if (counts.stream().anyMatch(c -> c.color() == ToiletColor.BLACK && c.count() > 0))
      return "흑변은 건강의 적신호예요. 위궤양, 위암 등 위 관련 문제일 수도 있어요. 즉시 병원을 방문하셔야 해요.";
    return null;
  }

  private static MonthlyToiletPain mapPainSection(ToiletPainDistribution pd) {
    int painfulDays = (pd == null) ? 0 : pd.high() + pd.veryHigh();
    String title = String.format("이번 달은 배를 부여잡은 날들이\n%d회 있었어요", painfulDays);

    ToiletPainComparison diff =
        (pd == null) ? new ToiletPainComparison("same", 0) : fromPainDiff(pd.painDiff());

    return new MonthlyToiletPain(
        title,
        pd == null ? 0 : pd.veryLow(),
        pd == null ? 0 : pd.low(),
        pd == null ? 0 : pd.medium(),
        pd == null ? 0 : pd.high(),
        pd == null ? 0 : pd.veryHigh(),
        diff);
  }

  private static ToiletPainComparison fromPainDiff(int d) {
    if (d > 0) return new ToiletPainComparison("increased", d);
    if (d < 0) return new ToiletPainComparison("decreased", -d);
    return new ToiletPainComparison("same", 0);
  }

  private static TimeOfDaySection mapTimeOfDay(List<ToiletPeriodCount> counts) {
    List<MonthlyToiletPeriod> items =
        (counts == null)
            ? List.of()
            : counts.stream()
                .map(pc -> new MonthlyToiletPeriod(pc.period().getValue(), pc.count()))
                .collect(toList());

    String best =
        (counts == null || counts.isEmpty())
            ? "오전"
            : counts.stream()
                .max(Comparator.comparingInt(ToiletPeriodCount::count))
                .map(pc -> toKorean(pc.period()))
                .orElse("오전");
    String title = String.format("이번 달은 주로 %s에 성공했어요", best);

    return new TimeOfDaySection(title, items);
  }

  private static String toKorean(DayPeriod p) {
    return switch (p) {
      case MORNING -> "오전";
      case AFTERNOON -> "오후";
      case EVENING -> "저녁";
    };
  }

  // ===== 월간 Activity → DTO 변환 =====

  private FoodSection mapFoodSection(MonthlyActivityReport ar, YearMonth month) {
    if (ar == null || ar.weeklyGroups() == null || ar.weeklyGroups().isEmpty()) {
      return new FoodSection(
          "음식 기록이 없어요. 장에 좋은 음식을 먹어볼까요?", new MonthlyComparison(0, 0), List.of());
    }

    List<WeeklyGroup> dtoGroups = new ArrayList<>();
    for (MonthlyActivityReport.WeeklyGroup g : ar.weeklyGroups()) {
      // (옵션) month 검증이 필요하면 여기서 YearMonth.from(start/end) 체크
      dtoGroups.add(toDtoWeeklyFoodGroup(g));
    }

    int lastMonthDangerous = ar.lastMonthDangerousDays();
    int thisMonthDangerous = ar.dangerousFoodDays();

    String message =
        (thisMonthDangerous >= 10)
            ? "자극적인 음식을 10회 이상 섭취했어요\n식단 관리가 필요해요!"
            : (thisMonthDangerous > 0)
                ? "자극적인 음식을 10회 미만으로 섭취했어요.\n지속적으로 줄여나가요!"
                : "건강한 식단을\n열심히 유지하고 계시네요!";

    return new FoodSection(
        message, new MonthlyComparison(lastMonthDangerous, thisMonthDangerous), dtoGroups);
  }

  private WeeklyGroup toDtoWeeklyFoodGroup(MonthlyActivityReport.WeeklyGroup g) {
    String weekLabel = g.weekIndex() + "주차";
    LocalDate start = g.startDate();
    LocalDate end = g.endDate();

    List<GetMonthlyReportResponseDto.FoodSection.WeeklyGroup.FoodItem> items = new ArrayList<>();

    int dayIdx = 0;
    for (DailyActivityReport daily : safeList(g.dailyReports())) {
      // DailyActivityReport에는 날짜가 없으니 주차 시작일 + dayIdx로 보정
      LocalDate date = start.plusDays(dayIdx);

      for (FoodEvaluation fe : safeList(daily.getFoodEvaluations())) {
        fe.getFoodsByMealTime()
            .forEach(
                (mealTime, foods) -> {
                  if (foods != null && !foods.isEmpty()) {
                    items.add(
                        new GetMonthlyReportResponseDto.FoodSection.WeeklyGroup.FoodItem(
                            date.toString(), // occurredAt = 실제 일자
                            mealTime.name(), // 조식/중식/석식 등 enum 이름
                            foods));
                  }
                });
      }
      dayIdx++;
      if (date.isAfter(end)) break; // 안전장치: 주차 범위 초과 방지(선택)
    }

    return new GetMonthlyReportResponseDto.FoodSection.WeeklyGroup(
        weekLabel, start.toString(), end.toString(), items);
  }

  private WaterSection mapWaterSection(MonthlyActivityReport ar) {
    if (ar == null || ar.weeklyGroups() == null || ar.weeklyGroups().isEmpty()) {
      return new WaterSection("물 섭취 기록이 없어요\n물을 자주 마셔주세요", List.of());
    }

    List<WaterItem> items = new ArrayList<>();
    double highValue = 2000.0, mediumValue = 1200.0, lowValue = 600.0;

    List<WaterLevel> weekLevels = new ArrayList<>();
    for (MonthlyActivityReport.WeeklyGroup g : ar.weeklyGroups()) {
      WaterLevel weekLevel = aggregateWeeklyWaterLevel(g); // 일별 레벨 평균(반올림)
      weekLevels.add(weekLevel);

      double value =
          switch (weekLevel) {
            case HIGH -> highValue;
            case MEDIUM -> mediumValue;
            case LOW -> lowValue;
            case NONE -> 0.0;
          };
      items.add(new WaterItem(g.weekIndex() + "주차", value));
    }

    // 월 레벨 = NONE 제외 주차 레벨들의 ordinal 평균(반올림)
    int sum = 0, cnt = 0;
    for (WaterLevel wl : weekLevels) {
      if (wl != WaterLevel.NONE) {
        sum += wl.ordinal();
        cnt++;
      }
    }
    WaterLevel monthLevel;
    if (cnt == 0) {
      monthLevel = WaterLevel.NONE;
    } else {
      int avgOrdinal = Math.round((float) sum / cnt);
      WaterLevel[] lvls = WaterLevel.values();
      // 안전 클램프
      avgOrdinal = Math.min(Math.max(avgOrdinal, 0), lvls.length - 1);
      monthLevel = lvls[avgOrdinal];
    }

    String message =
        switch (monthLevel) {
          case NONE -> "물 섭취 기록이 없어요\n물을 자주 마셔주세요";
          case LOW -> "장이 말라가고 있어요!\n물 섭취량을 늘려야 해요";
          case MEDIUM, HIGH -> "물을 잘 섭취하고 계시군요!\n앞으로도 잘 유지해봐요";
        };

    return new WaterSection(message, items);
  }

  private static WaterLevel aggregateWeeklyWaterLevel(MonthlyActivityReport.WeeklyGroup g) {
    if (g.dailyReports() == null || g.dailyReports().isEmpty()) return WaterLevel.NONE;

    int sumOrdinal = 0, cnt = 0;
    for (DailyActivityReport d : g.dailyReports()) {
      List<WaterEvaluation> ws = d.getWaterEvaluations();
      if (ws != null && !ws.isEmpty()) {
        WaterLevel lvl = ws.get(0).getLevel(); // 동일 기준 유지
        sumOrdinal += lvl.ordinal();
        cnt++;
      }
    }
    if (cnt == 0) return WaterLevel.NONE;
    int avgOrdinal = Math.round((float) sumOrdinal / cnt);
    WaterLevel[] levels = WaterLevel.values();
    return levels[Math.min(Math.max(avgOrdinal, 0), levels.length - 1)];
  }

  private GetMonthlyReportResponseDto.StressSection mapStressSection(MonthlyActivityReport ar) {
    List<GetMonthlyReportResponseDto.StressSection.StressItem> items = new ArrayList<>();

    if (ar == null || ar.weeklyGroups() == null || ar.weeklyGroups().isEmpty()) {
      return new GetMonthlyReportResponseDto.StressSection(
          "스트레스 기록이 없어요\n마음 편한 한 달이었네요!",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_medium.png",
          items);
    }

    StressEvaluation monthWorst = StressEvaluation.NONE;

    for (MonthlyActivityReport.WeeklyGroup g : ar.weeklyGroups()) {
      // 주차 최악: NONE 제외 후 min(ordinal)
      StressEvaluation weekWorst =
          (g.dailyReports() == null
              ? null
              : g.dailyReports().stream()
                  .map(DailyActivityReport::getStressEvaluation)
                  .filter(s -> s != null && s != StressEvaluation.NONE)
                  .min(Comparator.comparingInt(Enum::ordinal))
                  .orElse(null));

      if (weekWorst == null) weekWorst = StressEvaluation.NONE;

      items.add(
          new GetMonthlyReportResponseDto.StressSection.StressItem(
              g.weekIndex() + "주차", weekWorst.name()));

      // 월 최악 갱신 (NONE 제외, 더 낮은 ordinal일수록 더 ‘나쁨’)
      if (weekWorst != StressEvaluation.NONE) {
        if (monthWorst == StressEvaluation.NONE || weekWorst.ordinal() < monthWorst.ordinal()) {
          monthWorst = weekWorst;
        }
      }
    }

    GetDailyReportResponseDto.StressReport base = stressMapper.map(monthWorst);
    if (base == null) {
      base =
          new GetDailyReportResponseDto.StressReport(
              "스트레스 기록이 없어요\n마음 편한 한 달이었네요!",
              "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_medium.png");
    }

    return new GetMonthlyReportResponseDto.StressSection(base.message(), base.image(), items);
  }

  private GetMonthlyReportResponseDto.SuggestionSection mapSuggestionSection(
      Suggestion suggestion) {
    var dto = suggestionMapper.map(suggestion);
    var items =
        dto.items().stream()
            .map(
                i ->
                    new GetMonthlyReportResponseDto.SuggestionSection.SuggestionItem(
                        i.image(), i.title(), i.content()))
            .toList();
    return new GetMonthlyReportResponseDto.SuggestionSection(dto.message(), items);
  }

  private static <T> List<T> safeList(List<T> in) {
    return (in == null) ? List.of() : in;
  }
}
