package depromeet.lessonfour.server.toiletrecord.domain.entity;

import java.time.LocalDateTime;

import depromeet.lessonfour.server.common.domain.entity.BaseTimeEntity;
import depromeet.lessonfour.server.report.domain.policy.StoolEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.vo.StoolEvaluation;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import depromeet.lessonfour.server.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// TODO : 추후 기능 명세에 따라 notnull 추가
@Entity
@Table(name = "toilet_record")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToiletRecord extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 작성자
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column private boolean isSuccessful;

  @Column
  @Enumerated(EnumType.STRING)
  private ToiletColor color;

  @Column
  @Enumerated(EnumType.STRING)
  private ToiletShape shape;

  @Column
  @Min(0)
  @Max(100)
  private int pain;

  @Column private int duration;

  @Column(nullable = true)
  private String note;

  @Column(nullable = false)
  @NotNull private LocalDateTime occurredAt;

  @Column(nullable = false)
  @NotNull private boolean isDeleted;

  public static ToiletRecord register(
      User user,
      boolean isSuccessful,
      ToiletColor color,
      ToiletShape shape,
      int pain,
      int duration,
      String note,
      LocalDateTime occurredAt) {
    return ToiletRecord.builder()
        .user(user)
        .isSuccessful(isSuccessful)
        .color(color)
        .shape(shape)
        .pain(pain)
        .duration(duration)
        .note(note)
        .occurredAt(occurredAt)
        .isDeleted(false)
        .build();
  }

  public void applyPatch(
      Boolean isSuccessful,
      ToiletColor color,
      ToiletShape shape,
      Integer pain,
      Integer duration,
      String note,
      LocalDateTime occurredAt) {
    if (isSuccessful != null) this.isSuccessful = isSuccessful;
    if (color != null) this.color = color;
    if (shape != null) this.shape = shape;
    if (pain != null) this.pain = pain;
    if (duration != null) this.duration = duration;
    if (note != null) this.note = note;
    if (occurredAt != null) this.occurredAt = occurredAt;
  }

  public void delete() {
    this.isDeleted = true;
  }

  public StoolEvaluation evaluatePoo(StoolEvaluationPolicy policy) {
    return policy.evaluate(this);
  }
}
