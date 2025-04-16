package se.storkforge.petconnect.security;
/**
 * This class defines the roles used in the application.
 * The roles are used for authorization purposes.
 * They are defined as constants to avoid hardcoding strings throughout the codebase.
 * All roles are prefixed with "ROLE_" to follow Spring Security conventions.
 * And to avoid confusion with other potential string values.
 */
public class Roles {
    public static final String USER = "ROLE_USER";
    public static final String PREMIUM = "ROLE_PREMIUM";
    public static final String ADMIN = "ROLE_ADMIN";

    private Roles() {} // prevent instantiation
}