package depromeet.lessonfour.server.auth.persist.jpa.entity;

public enum UserRoleEnum {
  USER,
  ADMIN;

  public static final String ROLE_PREFIX = "ROLE_";

  public String getAuthority() {
    return ROLE_PREFIX + this.name();
  }
}
