package sr.we.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.servlet.http.Cookie;
import sr.we.security.AuthenticatedUser;
import sr.we.views.CookieUtil;
import sr.we.views.MainLayout;
import sr.we.views.dashboard.DashboardView;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

    public LoginView(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("NexyInsight");
        i18n.getHeader().setDescription("Login using user/user or admin/admin");
        i18n.setAdditionalInformation(null);
        setI18n(i18n);

        setForgotPasswordButtonVisible(false);
        setOpened(true);

        Cookie cookieByName = CookieUtil.getCookieByName(CookieUtil.THEME);
        if(cookieByName == null || cookieByName.getValue().equalsIgnoreCase("DARK")){
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            themeList.add(Lumo.DARK);
        } else {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            themeList.remove(Lumo.DARK);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            setOpened(false);
            if (accessChecker.hasAccess(DashboardView.class)) {
                event.forwardTo("");
            } else {
                event.forwardTo("receipts");
            }
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
