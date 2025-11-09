package depromeet.lessonfour.server.report.api.mapper;

public final class ReportMapperUtils {
	private ReportMapperUtils() {}

	/**
	 * 평균 avg 대비 점수 me의 상대 위치를 상위 백분위(작을수록 상위)로 추정.
	 * 로지스틱: top = 100 / (1 + exp((me - avg) / k))
	 *  - me == avg → 50.0
	 *  - k(스케일)가 클수록 완만, 작을수록 가팔라짐 (기본 8.0)
	 *  - 0.5 ~ 99.5 사이로 클램프
	 *  - 소수 1자리 반올림
	 */
	public static double estimateTopPercent(double me, double avg) {
		return estimateTopPercent(me, avg, 8.0);
	}

	public static double estimateTopPercent(double me, double avg, double k) {
		if (k <= 0) k = 8.0;
		double top = 100.0 / (1.0 + Math.exp((me - avg) / k));
		// 안전 클램프 (완전 0/100 노출 방지)
		top = Math.max(0.5, Math.min(99.5, top));
		// 소수 1자리
		return Math.round(top * 10.0) / 10.0;
	}
}