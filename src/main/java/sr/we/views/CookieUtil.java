package sr.we.views;

import com.vaadin.flow.server.VaadinService;
import org.apache.commons.lang3.RandomStringUtils;

import jakarta.servlet.http.Cookie;
import java.util.Locale;

public class CookieUtil {

	public static final String THEME = "THEME";
	
	public static Cookie getCookieByName(String name) {
		// Fetch all cookies from the request
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

		// Iterate to find cookie by its name
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (name.equals(cookie.getName())) {
					return cookie;
				}
			}
		}

		return null;
	}
	
	public static void setLocaleToCookie(Locale locale, String name) {
		if (locale == null) {
			locale = Locale.ENGLISH;
		}
		// Fetch all cookies from the request
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

		// Iterate to find cookie by its name
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				cookie.setValue(locale.getISO3Language());
			}
		}
		createNewCookie(locale.getISO3Language(), name);
	}

	public static Cookie createNewCookie(String value, String name) {
		Cookie localeCookie = new Cookie(name, value);

		// Make cookie expire in 2 minutes
		localeCookie.setMaxAge(86400);

		// Set the cookie path.
		localeCookie.setPath(VaadinService.getCurrentRequest().getContextPath());

		// Save cookie
		VaadinService.getCurrentResponse().addCookie(localeCookie);
		return localeCookie;
	}

	public static Cookie createNewCookie(String value, String name, int expire) {
		Cookie localeCookie = new Cookie(name, value);

		// Make cookie expire in 2 minutes
		localeCookie.setMaxAge(expire);

		// Set the cookie path.
		localeCookie.setPath(VaadinService.getCurrentRequest().getContextPath());

		// Save cookie
		VaadinService.getCurrentResponse().addCookie(localeCookie);
		return localeCookie;
	}
}
