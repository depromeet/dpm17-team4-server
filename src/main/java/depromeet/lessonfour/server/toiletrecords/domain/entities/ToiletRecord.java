package depromeet.lessonfour.server.toiletrecords.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import depromeet.lessonfour.server.common.domain.BaseTimeEntity;
import depromeet.lessonfour.server.users.domain.entities.User;
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
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  // 작성자
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column private boolean isTolietSuccess;

  @Column
  @Enumerated(EnumType.STRING)
  private ToiletColor toiletColor;

  @Column
  @Min(0)
  @Max(100)
  private int painScore;

  @Column private int toiletDuration;

  @Column(nullable = true)
  private String additionalNote;

  @Column(nullable = false)
  @NotNull private LocalDateTime toiletAt;

  public static ToiletRecord register(
      User user,
      boolean isTolietSuccess,
      ToiletColor toiletColor,
      int painScore,
      int toiletDuration,
      String additionalNote,
      LocalDateTime toiletAt) {
    return ToiletRecord.builder()
        .user(user)
        .isTolietSuccess(isTolietSuccess)
        .toiletColor(toiletColor)
        .painScore(painScore)
        .toiletDuration(toiletDuration)
        .additionalNote(additionalNote)
        .toiletAt(toiletAt)
        .build();
  }
}
