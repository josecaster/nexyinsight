package sr.we.views;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import jakarta.servlet.http.Cookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vaadin.addons.joelpop.changepassword.ChangePassword;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordDialog;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordRule;
import org.vaadin.lineawesome.LineAwesomeIcon;
import sr.we.entity.User;
import sr.we.security.AuthenticatedUser;
import sr.we.services.UserService;
import sr.we.views.batches.BatchesView;
import sr.we.views.dashboard.DashboardView;
import sr.we.views.inventoryvaluation.InventoryValuationView;
import sr.we.views.items.ItemsView;
import sr.we.views.receipts.ReceiptsView;
import sr.we.views.sections.SectionsView;
import sr.we.views.stockadjustment.StockAdjustmentView;
import sr.we.views.users.UsersView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private User user;
    private ToggleButton toggleButton;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker, UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;

        addToNavbar(createHeaderContent());
        setDrawerOpened(false);
    }

    private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN, Width.FULL);

        Div layout = new Div();
        layout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.Horizontal.LARGE);

        H1 appName = new H1("NexyInsight");
        appName.addClassNames(Margin.Vertical.MEDIUM, Margin.End.AUTO, FontSize.LARGE);
        layout.add(appName);

        String themeLabel = "Dark";
        Cookie cookieByName = CookieUtil.getCookieByName(CookieUtil.THEME);
        if (cookieByName == null || cookieByName.getValue().equalsIgnoreCase("DARK")) {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            themeList.add(Lumo.DARK);
            themeLabel = "Dark";
        } else {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            themeList.remove(Lumo.DARK);
            themeLabel = "Light";
        }

        toggleButton = new ToggleButton(themeLabel.equalsIgnoreCase("Light"));
        toggleButton.setLabel(themeLabel);
        toggleButton.addClickListener(click -> {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();

            if (themeList.contains(Lumo.DARK)) {
                themeList.remove(Lumo.DARK);
                CookieUtil.createNewCookie("LIGHT", CookieUtil.THEME);
                toggleButton.setLabel("Light");
            } else {
                themeList.add(Lumo.DARK);
                CookieUtil.createNewCookie("DARK", CookieUtil.THEME);
                toggleButton.setLabel("Dark");
            }
        });



        layout.add(toggleButton);

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            StreamResource resource = new StreamResource("profile-pic", () -> new ByteArrayInputStream(user.getProfilePicture()));
            avatar.setImageResource(resource);
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });
            userName.getSubMenu().addItem("Account", e -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Account " + user.getUsername());
                FormLayout formLayout = new FormLayout();

                TextField nameFld = new TextField("", user.getName(), "");
                EmailField emailFld = new EmailField();
//                PasswordField newPswFld = new PasswordField();
//                PasswordField conPswFld = new PasswordField();

                nameFld.setWidthFull();
                emailFld.setWidthFull();
//                newPswFld.setWidthFull();
//                conPswFld.setWidthFull();

                formLayout.addFormItem(nameFld, "Name");
                formLayout.addFormItem(emailFld, "Email");
//                formLayout.addFormItem(newPswFld, "New password");
//                formLayout.addFormItem(conPswFld, "Confirm password");
                dialog.setWidth("50%");
                dialog.getFooter().add(new Button("Update account info", c -> {
                    Optional<User> user1 = authenticatedUser.get();
                    user1.ifPresent(value -> user = value);
                    user.setName(nameFld.getValue());
                    user.setEmail(emailFld.getValue());
//                    user.setHashedPassword(passwordEncoder.encode(conPswFld.getValue()));
                    userService.update(user);
                    dialog.close();
                }));
                dialog.add(formLayout);
                dialog.open();
            });
            userName.getSubMenu().addItem("Change password", e -> {
                Optional<User> user1 = authenticatedUser.get();
                user1.ifPresent(value -> user = value);
                ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
                changePasswordDialog.setChangePasswordMode(ChangePassword.ChangePasswordMode.CHANGE_KNOWN);
                changePasswordDialog.addOkListener(ok -> {
                    boolean matches = passwordEncoder.matches(ok.getCurrentPassword(), user.getHashedPassword());
                    if (matches) {
                        user.setHashedPassword(passwordEncoder.encode(ok.getDesiredPassword()));
                        userService.update(user);
                    } else {
                        Notification n = Notification.show("Current given password is incorrect. Password not updated");
                        n.setPosition(Notification.Position.MIDDLE);
                        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                List<ChangePasswordRule> rules = new ArrayList<>();
                rules.add(ChangePasswordRule.length(Double.valueOf(8).intValue()));
                rules.add(ChangePasswordRule.hasUppercaseLetters(Double.valueOf(1).intValue()));
                rules.add(ChangePasswordRule.hasLowercaseLetters(Double.valueOf(1).intValue()));
                rules.add(ChangePasswordRule.hasDigits(Double.valueOf(1).intValue()));
                rules.add(ChangePasswordRule.hasSpecials(Double.valueOf(1).intValue()));
                changePasswordDialog.addPasswordRules(rules.toArray(new ChangePasswordRule[]{}));
                changePasswordDialog.open();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        Nav nav = new Nav();
        nav.addClassNames(Display.FLEX, Overflow.AUTO, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL);

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames(Display.FLEX, Gap.SMALL, ListStyleType.NONE, Margin.NONE, Padding.NONE);
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            if (accessChecker.hasAccess(menuItem.getView())) {
                list.add(menuItem);
            }

        }

        header.add(layout, nav);
        return header;
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("Dashboard", LineAwesomeIcon.CHART_AREA_SOLID.create(), DashboardView.class), //

                new MenuItemInfo("Sections", LineAwesomeIcon.LAYER_GROUP_SOLID.create(), SectionsView.class), //
                new MenuItemInfo("Receipts", LineAwesomeIcon.RECEIPT_SOLID.create(), ReceiptsView.class), //

                new MenuItemInfo("Batches", LineAwesomeIcon.OBJECT_GROUP.create(), BatchesView.class), //

                new MenuItemInfo("Items", LineAwesomeIcon.PRODUCT_HUNT.create(), ItemsView.class), //
                new MenuItemInfo("Stock adjustment", LineAwesomeIcon.BALANCE_SCALE_LEFT_SOLID.create(), StockAdjustmentView.class), //

                new MenuItemInfo("Inventory valuation", LineAwesomeIcon.CALCULATOR_SOLID.create(), InventoryValuationView.class), //

                new MenuItemInfo("Users", LineAwesomeIcon.USERS_SOLID.create(), UsersView.class), //

        };
    }

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, Component icon, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames(Display.FLEX, Gap.XSMALL, Height.MEDIUM, AlignItems.CENTER, Padding.Horizontal.SMALL, TextColor.BODY);
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames(FontWeight.MEDIUM, FontSize.MEDIUM, Whitespace.NOWRAP);

            if (icon != null) {
                link.add(icon);
            }
            link.add(text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

    }

}
