package depromeet.lessonfour.server.auth.service.dto;

public record ReIssueResult(
        String accessToken,
        String refreshToken
) {
}
