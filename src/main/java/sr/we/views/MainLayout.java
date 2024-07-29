package sr.we.views;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import jakarta.servlet.http.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vaadin.addons.joelpop.changepassword.ChangePassword;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordDialog;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordRule;
import software.xdev.vaadin.daterange_picker.business.DateRangeModel;
import software.xdev.vaadin.daterange_picker.business.SimpleDateRange;
import software.xdev.vaadin.daterange_picker.business.SimpleDateRanges;
import software.xdev.vaadin.daterange_picker.ui.DateRangePicker;
import sr.we.entity.*;
import sr.we.integration.AuthController;
import sr.we.repository.IntegrationRepository;
import sr.we.repository.TaskRepository;
import sr.we.repository.WebhookRepository;
import sr.we.schedule.CustomTaskScheduler;
import sr.we.schedule.JobbyLauncher;
import sr.we.schedule.MainTaskTrigger;
import sr.we.security.AuthenticatedUser;
import sr.we.services.IntegrationService;
import sr.we.services.TaskService;
import sr.we.services.UserService;
import sr.we.services.WebhookService;
import sr.we.views.batches.BatchesView;
import sr.we.views.components.MyLineAwesome;
import sr.we.views.customers.CustomersView;
import sr.we.views.dashboard.DashboardView;
import sr.we.views.inventoryvaluation.InventoryValuationView;
import sr.we.views.items.ItemsView;
import sr.we.views.receipts.ReceiptsView;
import sr.we.views.sections.SectionsView;
import sr.we.views.stockadjustment.StockAdjustmentView;
import sr.we.views.users.CardView;
import sr.we.views.users.UsersView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * The main view is a top-level placeholder for other views.
 */


public class MainLayout extends AppLayout implements BeforeEnterObserver {

    protected static final List<SimpleDateRange> DATERANGE_VALUES = Arrays.asList(SimpleDateRanges.allValues());
    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CustomTaskScheduler executor;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final JobbyLauncher jobbyLauncher;
    private final IntegrationRepository integrationRepository;
    private final IntegrationService integrationService;
    private final AuthController authController;
    private final WebhookRepository webhookRepository;
    private final WebhookService webhookService;
    List<SideNavItem> webNavs;
    private User user;
    private ToggleButton toggleButton;
    private Task byTypeAndBusinessId;
    private PasswordField clientSecretFld;
    private TextField clientIdFld;
    private TextField personalAccessTokenFld;
    private TextField redirectUrlFld;
    private Integration integration;
    private H2 viewTitle;
    private SvgIcon infoButton;
    private DateRangePicker<SimpleDateRange> rangePicker;
    private Onboarding onboarding;
    private Header header;
    private VerticalLayout headerLayout;
    @Value("${sr.we.build.version}")
    private String appVresion;
    private SvgIcon filterBtn;
    private HorizontalLayout headerRightLayout;
    private MenuItemInfo dashboardMenuItem;
    private MenuItemInfo receiptsMenuItem;
    private Class<? extends Component> navigationTarget;
    private RouteParameters routeParameters;

    public MainLayout(WebhookRepository webhookRepository, WebhookService webhookService, AuthController authController, IntegrationRepository integrationRepository, IntegrationService integrationService, JobbyLauncher jobbyLauncher, CustomTaskScheduler executor, AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker, UserService userService, PasswordEncoder passwordEncoder, TaskRepository taskRepository, TaskService taskService) {
        this.webhookRepository = webhookRepository;
        this.webhookService = webhookService;
        this.integrationRepository = integrationRepository;
        this.integrationService = integrationService;
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.executor = executor;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.jobbyLauncher = jobbyLauncher;
        this.authController = authController;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();


//        addToNavbar(createHeaderContent());
//        setDrawerOpened(false);
    }

    private static boolean isInitialValue(Webhook iuw) {
        return iuw != null && iuw.getStatus() != null && iuw.getStatus().compareTo(Webhook.Status.ENABLED) == 0;
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        rangePicker = new DateRangePicker<SimpleDateRange>(() -> new DateRangeModel<>(LocalDate.now(), LocalDate.now(), SimpleDateRanges.DAY), DATERANGE_VALUES);
//        rangePicker.setWidthFull();

        rangePicker.addValueChangeListener(l -> {

            if (/*content != null &&*/ (l.getOldValue() != null && !l.getOldValue().equals(l.getValue()))) {
                DateRangeModel<SimpleDateRange> value = l.getValue();
                if (value.getStart() != null && value.getEnd() != null) {
                    Map<String, String> map = new HashMap<>();
                    map.put("start_date", l.getValue().getStart().format(DateTimeFormatter.ISO_DATE));
                    map.put("end_date", l.getValue().getEnd().format(DateTimeFormatter.ISO_DATE));
                    if (routeParameters == null) {
                        UI.getCurrent().navigate(navigationTarget, QueryParameters.simple(map));
                    } else {
                        UI.getCurrent().navigate(navigationTarget, routeParameters, QueryParameters.simple(map));
                    }
                } else {
                    Map<String, String> map = new HashMap<>();
                    map.put("start_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
                    map.put("end_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
                    if (routeParameters == null) {
                        UI.getCurrent().navigate(navigationTarget, QueryParameters.simple(map));
                    } else {
                        UI.getCurrent().navigate(navigationTarget, routeParameters, QueryParameters.simple(map));
                    }
                }
            }
        });

        infoButton = MyLineAwesome.INFO_CIRCLE_SOLID.create();
        infoButton.setColor("var(--lumo-primary-text-color)");
        infoButton.getElement().getStyle().set("margin-right", "var(--lumo-space-s)");
        infoButton.setVisible(false);
        infoButton.addClickListener(c -> onboarding.start());


        filterBtn = MyLineAwesome.FILTER_SOLID.create();
        filterBtn.setColor("var(--lumo-primary-text-color)");
        filterBtn.getElement().getStyle().set("margin-right", "var(--lumo-space-s)");
        filterBtn.addClickListener(c -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Filter");
            dialog.add(rangePicker);
            Button cancelButton = new Button("Search", (e) -> dialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(cancelButton);

            Button deleteButton = new Button("Cancel", (e) -> dialog.close());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            deleteButton.getStyle().set("margin-right", "auto");
            dialog.getFooter().add(deleteButton);
            dialog.setHeight("400px");
            dialog.open();
        });


        headerRightLayout = new HorizontalLayout();
        headerRightLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        headerRightLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRightLayout.getElement().getStyle().set("margin-left", "auto");
        addToNavbar(true, toggle, viewTitle, headerRightLayout);
    }

    private void addDrawerContent() {
        H1 appName = new H1("Nexyinsight");
//        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);


        headerLayout = new VerticalLayout(appName);
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        Header header = new Header(headerLayout);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());

        if (user != null) {
            Avatar avatar = new Avatar(user.getName());
            StreamResource resource = new StreamResource("profile-pic", () -> new ByteArrayInputStream(user.getProfilePicture()));
            avatar.setImageResource(resource);
            avatar.setThemeName("xxxlarge");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar menuBar = new MenuBar();
            headerLayout.add(menuBar, new Hr());
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

            MenuItem menuItem = menuBar.addItem(avatar);
            menuItem.getElement().getStyle().setHeight("150px");
            SubMenu subMenu = menuItem.getSubMenu();
            subMenu.addItem("Upload profile", l -> {
                Upload upload = new Upload();
                upload.setDropAllowed(true);
                Dialog dialog = new Dialog(upload);
                dialog.open();
                upload.setAcceptedFileTypes("image/jpeg", "image/png");
                MemoryBuffer receiver = new MemoryBuffer();
                upload.setReceiver(receiver);
                int maxFileSizeInBytes = 5 * 1024 * 1024; // 5.0 MB
                upload.setMaxFileSize(maxFileSizeInBytes);
                upload.addSucceededListener(s -> {
                    Optional<User> user1 = authenticatedUser.get();
                    user1.ifPresent(value -> user = value);
                    if (receiver.getInputStream() != null) {
                        try {
                            user.setProfilePicture(receiver.getInputStream().readAllBytes());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    userService.update(user);
                    UI.getCurrent().getPage().reload();
                });
            });

        }
    }

    @Override
    public void setContent(Component content) {
        super.setContent(content);


        if (content instanceof HelpFunction) {
            onboarding = new Onboarding();
            String help = ((HelpFunction) content).help(onboarding);

            Cookie cookieByName = CookieUtil.getCookieByName(help);
            if (cookieByName == null) {
                CookieUtil.createNewCookie("true", help, Integer.MAX_VALUE);
                onboarding.start();
            }

            infoButton.setVisible(true);
        } else {
            infoButton.setVisible(false);
        }

    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());

    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    private Footer createFooter() {
        VerticalLayout footer = new VerticalLayout();


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


        footer.add(toggleButton);


        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            user = maybeUser.get();


            Div div = new Div();
            Avatar avatar = new Avatar(user.getName());
            StreamResource resource = new StreamResource("profile-pic", () -> new ByteArrayInputStream(user.getProfilePicture()));
            avatar.setImageResource(resource);
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");
            div.add(avatar);

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            account(userName);
            changePassword(userName);
            if (user.getRoles().contains(Role.ADMIN)) {
                loyverseIntegration(userName);
                synchronize(userName);
            }
            signOut(userName);
            footer.add(userMenu);
            footer.add(appVresion);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            footer.add(loginLink);
        }

        return new Footer(footer);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        boolean isMobile = CardView.isMobileDevice();
        headerRightLayout.removeAll();
        if (isMobile) {
            for (SideNavItem sideNavItem : webNavs) {
                sideNavItem.setVisible(false);
            }
            headerRightLayout.add(filterBtn, infoButton);
        } else {
            headerRightLayout.add(rangePicker, infoButton);
        }
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        webNavs = new ArrayList<>();
        for (MenuItemInfo menuItem : createMenuItems()) {
            if (accessChecker.hasAccess(menuItem.getView())) {
                SideNavItem sideNavItem = new SideNavItem(menuItem.getMenuTitle(), menuItem.getView(), menuItem.getIcon());
                if (!MobileSupport.class.isAssignableFrom(menuItem.getView())) {
                    webNavs.add(sideNavItem);
                }
                nav.addItem(sideNavItem);
            }

        }

        return nav;
    }

//    private Component createHeaderContent() {
//        Header header = new Header();
//        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN, Width.FULL);
//
//        Div layout = new Div();
//        layout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.Horizontal.LARGE);
//
//        H1 appName = new H1("NexyInsight");
//        appName.addClassNames(Margin.Vertical.MEDIUM, Margin.End.AUTO, FontSize.LARGE);
//        layout.add(appName);
//
//        String themeLabel = "Dark";
//        Cookie cookieByName = CookieUtil.getCookieByName(CookieUtil.THEME);
//        if (cookieByName == null || cookieByName.getValue().equalsIgnoreCase("DARK")) {
//            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
//            themeList.add(Lumo.DARK);
//            themeLabel = "Dark";
//        } else {
//            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
//            themeList.remove(Lumo.DARK);
//            themeLabel = "Light";
//        }
//
//        toggleButton = new ToggleButton(themeLabel.equalsIgnoreCase("Light"));
//        toggleButton.setLabel(themeLabel);
//        toggleButton.addClickListener(click -> {
//            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
//
//            if (themeList.contains(Lumo.DARK)) {
//                themeList.remove(Lumo.DARK);
//                CookieUtil.createNewCookie("LIGHT", CookieUtil.THEME);
//                toggleButton.setLabel("Light");
//            } else {
//                themeList.add(Lumo.DARK);
//                CookieUtil.createNewCookie("DARK", CookieUtil.THEME);
//                toggleButton.setLabel("Dark");
//            }
//        });
//
//
//        layout.add(toggleButton);
//
//        Optional<User> maybeUser = authenticatedUser.get();
//        if (maybeUser.isPresent()) {
//            user = maybeUser.get();
//
//            Avatar avatar = new Avatar(user.getName());
//            StreamResource resource = new StreamResource("profile-pic", () -> new ByteArrayInputStream(user.getProfilePicture()));
//            avatar.setImageResource(resource);
//            avatar.setThemeName("xsmall");
//            avatar.getElement().setAttribute("tabindex", "-1");
//
//            MenuBar userMenu = new MenuBar();
//            userMenu.setThemeName("tertiary-inline contrast");
//
//            MenuItem userName = userMenu.addItem("");
//            Div div = new Div();
//            div.add(avatar);
//            div.add(user.getName());
//            div.add(new Icon("lumo", "dropdown"));
//            div.getElement().getStyle().set("display", "flex");
//            div.getElement().getStyle().set("align-items", "center");
//            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
//            userName.add(div);
//            account(userName);
//            changePassword(userName);
//            loyverseIntegration(userName);
//            synchronize(userName);
//            signOut(userName);
//            layout.add(userMenu);
//            layout.add(appVresion);
//        } else {
//            Anchor loginLink = new Anchor("login", "Sign in");
//            layout.add(loginLink);
//        }
//
//        Nav nav = new Nav();
//        nav.addClassNames(Display.FLEX, Overflow.AUTO, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL);
//
//        // Wrap the links in a list; improves accessibility
//        UnorderedList list = new UnorderedList();
//        list.addClassNames(Display.FLEX, Gap.SMALL, ListStyleType.NONE, Margin.NONE, Padding.NONE);
//        nav.add(list);
//
//        for (MenuItemInfo menuItem : createMenuItems()) {
//            if (accessChecker.hasAccess(menuItem.getView())) {
//                list.add(menuItem);
//            }
//
//        }
//
//        header.add(layout, nav);
//        return header;
//    }

    private void account(MenuItem userName) {
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
            VerticalLayout field = new VerticalLayout();
            Upload upload = new Upload();
            upload.setAcceptedFileTypes("image/jpeg", "image/png");
            MemoryBuffer receiver = new MemoryBuffer();
            upload.setReceiver(receiver);
            int maxFileSizeInBytes = 5 * 1024 * 1024; // 5.0 MB
            upload.setMaxFileSize(maxFileSizeInBytes);
            field.add(upload);
            FormLayout.FormItem formItem = formLayout.addFormItem(field, "Profile picture");
            formLayout.setColspan(formItem, 2);
//                formLayout.addFormItem(newPswFld, "New password");
//                formLayout.addFormItem(conPswFld, "Confirm password");
            dialog.setWidth("50%");
            dialog.getFooter().add(new Button("Update account info", c -> {
                Optional<User> user1 = authenticatedUser.get();
                user1.ifPresent(value -> user = value);
                user.setName(nameFld.getValue());
                user.setEmail(emailFld.getValue());
//                    user.setHashedPassword(passwordEncoder.encode(conPswFld.getValue()));
                if (receiver.getInputStream() != null) {
                    try {
                        user.setProfilePicture(receiver.getInputStream().readAllBytes());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                userService.update(user);
                dialog.close();
            }));
            dialog.add(formLayout);
            dialog.open();
        });
    }

    private void changePassword(MenuItem userName) {
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
    }

    private void signOut(MenuItem userName) {
        userName.getSubMenu().addItem("Sign out", e -> {
            authenticatedUser.logout();
        });
    }

    private void synchronize(MenuItem userName) {
        userName.getSubMenu().addItem("Synchronize", e -> {
            Dialog dialog = new Dialog("Synchronize");
            Button button = new Button("Synchronize Loyverse now");
            byTypeAndBusinessId = taskRepository.getByTypeAndBusinessId(Task.Type.MAIN, user.getBusinessId());
            FormLayout formLayout = getFormLayout(dialog);
            formLayout.add(button);
//                formLayout.addFormItem(automaticSync,"Automatic sync");
            formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
            dialog.add(formLayout);
            dialog.open();
            button.addClickListener(f -> {

                executor.schedule(jobbyLauncher, Instant.now());


                Notification n = Notification.show("System is synchronizing with Loyverse");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                n.setDuration(10000);
                dialog.close();
            });
        });
    }

    private FormLayout getFormLayout(Dialog dialog) {
        ToggleButton automaticSync = new ToggleButton("", byTypeAndBusinessId.getEnabled());
        automaticSync.addValueChangeListener(l -> {
            byTypeAndBusinessId.setEnabled(l.getValue());
            byTypeAndBusinessId = taskService.update(byTypeAndBusinessId);
            dialog.close();

            if (byTypeAndBusinessId.getEnabled()) {
                executor.schedule(jobbyLauncher, new MainTaskTrigger(byTypeAndBusinessId.getBusinessId(), taskRepository, taskService));
            } else {
                executor.cancelSchedule(1);
            }

            Notification n = Notification.show("Automatic sync activated. Next sync at [" + byTypeAndBusinessId.getMaxTime() + "]");
            n.setPosition(Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            n.setDuration(10000);
        });
//                new VerticalLayout(automaticSync,button,new Text("Next sync at [" + byTypeAndBusinessId.getMaxTime() + "]"));
        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(automaticSync, "Automatic sync");
        return formLayout;
    }

    private void loyverseIntegration(MenuItem userName) {
        userName.getSubMenu().addItem("Loyverse Integrations", e -> {
            Dialog dialog = new Dialog("Loyverse Integrations");
            dialog.setWidth("400px");
            Button button = new Button("Save");
            byTypeAndBusinessId = taskRepository.getByTypeAndBusinessId(Task.Type.MAIN, user.getBusinessId());
            FormLayout formLayout = new FormLayout();

            personalAccessTokenFld = new TextField();
            clientIdFld = new TextField();
            clientSecretFld = new PasswordField();
            redirectUrlFld = new TextField();


            formLayout.addFormItem(personalAccessTokenFld, "Personal Access Token");
            formLayout.addFormItem(clientIdFld, "Client ID");
            formLayout.addFormItem(clientSecretFld, "Client secret");
            formLayout.addFormItem(redirectUrlFld, "Redirect url");

            personalAccessTokenFld.setWidthFull();
            clientIdFld.setWidthFull();
            clientSecretFld.setWidthFull();
            redirectUrlFld.setWidthFull();


            formLayout.add(button);
            integration = integrationRepository.getByBusinessId(user.getBusinessId());
            if (integration != null) {
                personalAccessTokenFld.setValue(StringUtils.isBlank(integration.getPersonalAccessToken()) ? "" : integration.getPersonalAccessToken());
                clientIdFld.setValue(StringUtils.isBlank(integration.getClientId()) ? "" : integration.getClientId());
                clientSecretFld.setValue(StringUtils.isBlank(integration.getClientSecret()) ? "" : integration.getClientSecret());
                redirectUrlFld.setValue(StringUtils.isBlank(integration.getRedirectUri()) ? "" : integration.getRedirectUri());
                if ((StringUtils.isBlank(integration.getCode()) && StringUtils.isBlank(integration.getAccessToken())) || StringUtils.isBlank(integration.getAccessToken())) {
                    String authorize = authController.authorize(user.getBusinessId());
                    if (StringUtils.isNotBlank(authorize)) {
                        Anchor anchor = new Anchor(authorize, "Authorize application");
                        anchor.setTarget(AnchorTarget.BLANK);
                        formLayout.add(anchor);
                    }
                }
            }


            formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
            dialog.add(formLayout);


            if (integration != null && StringUtils.isNotBlank(integration.getAccessToken())) {

                ListItem listItem = new ListItem("Webhooks");
                listItem.add(new HorizontalLayout(new Text("Inventory updated"), getToggleButton(Webhook.Type.ILU)));
                listItem.add(new HorizontalLayout(new Text("Item created, updated or deleted"), getToggleButton(Webhook.Type.IU)));
                listItem.add(new HorizontalLayout(new Text("Customer created, updated or deleted"), getToggleButton(Webhook.Type.CU)));
                listItem.add(new HorizontalLayout(new Text("Receipt created or updated"), getToggleButton(Webhook.Type.RU)));
                listItem.add(new HorizontalLayout(new Text("Shift created"), getToggleButton(Webhook.Type.SU)));

                dialog.add(listItem);
            }


            dialog.open();
            button.addClickListener(f -> {

                if (integration == null) {
                    integration = new Integration();
                    integration.setBusinessId(user.getBusinessId());
                    integration.setState(UUID.randomUUID().toString());
                }

                integration.setClientId(clientIdFld.getValue());
                integration.setClientSecret(clientSecretFld.getValue());
                integration.setPersonalAccessToken(personalAccessTokenFld.getValue());
                integration.setRedirectUri(redirectUrlFld.getValue());

                integration = integrationService.update(integration);

                if (StringUtils.isBlank(integration.getCode()) && StringUtils.isBlank(integration.getAccessToken())) {
                    String authorize = authController.authorize(user.getBusinessId());
                    if (StringUtils.isNotBlank(authorize)) {
                        UI.getCurrent().getPage().open(authorize, "_blank");
                    }
                }

                Notification n = Notification.show("Data saved");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                n.setDuration(10000);
                dialog.close();
            });
        });
    }

    private ToggleButton getToggleButton(Webhook.Type type) {
        Webhook byTypeAndBusinessId1 = webhookRepository.getByTypeAndBusinessId(type, integration.getBusinessId());
        ToggleButton toggleButton1 = new ToggleButton(isInitialValue(byTypeAndBusinessId1));
        toggleButton1.addValueChangeListener(l -> {
            Webhook webhook = webhookRepository.getByTypeAndBusinessId(type, integration.getBusinessId());
            if (webhook == null) {
                // create one
                webhook = new Webhook();
                webhook.setTypee(type);
                webhook.setStatus(l.getValue() ? Webhook.Status.ENABLED : Webhook.Status.DISABLED);
                webhook.setBusinessId(integration.getBusinessId());
            } else {
                webhook.setStatus(l.getValue() ? Webhook.Status.ENABLED : Webhook.Status.DISABLED);
            }
            try {
                webhookService.update(webhook);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return toggleButton1;
    }

    private MenuItemInfo[] createMenuItems() {
        Map<String, String> map = new HashMap<>();
        map.put("start_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        map.put("end_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        dashboardMenuItem = new MenuItemInfo("Dashboard", MyLineAwesome.CHART_AREA_SOLID.create(), DashboardView.class, QueryParameters.simple(map));
        receiptsMenuItem = new MenuItemInfo("Receipts", MyLineAwesome.RECEIPT_SOLID.create(), ReceiptsView.class, QueryParameters.simple(map));
        return new MenuItemInfo[]{ //
                dashboardMenuItem, //

                new MenuItemInfo("Sections", MyLineAwesome.LAYER_GROUP_SOLID.create(), SectionsView.class, null), //
                receiptsMenuItem, //
                new MenuItemInfo("Customers", MyLineAwesome.PERSON_BOOTH_SOLID.create(), CustomersView.class, null), //

                new MenuItemInfo("Batches", MyLineAwesome.OBJECT_GROUP.create(), BatchesView.class, null), //

                new MenuItemInfo("Items", MyLineAwesome.PRODUCT_HUNT.create(), ItemsView.class, null), //
                new MenuItemInfo("Stock adjustment", MyLineAwesome.BALANCE_SCALE_LEFT_SOLID.create(), StockAdjustmentView.class, null), //

                new MenuItemInfo("Inventory valuation", MyLineAwesome.CALCULATOR_SOLID.create(), InventoryValuationView.class, null), //

                new MenuItemInfo("Users", MyLineAwesome.USERS_SOLID.create(), UsersView.class, null), //

        };
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            user = maybeUser.get();
            Integration byBusinessId = integrationRepository.getByBusinessId(user.getBusinessId());

            QueryParameters queryParameters = beforeEnterEvent.getLocation().getQueryParameters();
            if (byBusinessId != null && StringUtils.isNotBlank(byBusinessId.getState())) {
                boolean containsCode = queryParameters.getParameters().containsKey("code");
                boolean containsState = queryParameters.getParameters().containsKey("state");

                List<String> state = queryParameters.getParameters().get("state");
                if (containsCode && containsState && state.contains(byBusinessId.getState())) {
                    List<String> code = queryParameters.getParameters().get("code");
                    byBusinessId.setCode(code.get(0));
                    integrationService.update(byBusinessId);
                    authController.authorize(byBusinessId.getBusinessId(), false);
//                    beforeEnterEvent.getRedirectQueryParameters().excluding("code","stats");
                }
            }

            Optional<String> startDate = queryParameters.getSingleParameter("start_date");
            Optional<String> endDate = queryParameters.getSingleParameter("end_date");

            navigationTarget = (Class<? extends Component>) beforeEnterEvent.getNavigationTarget();
            routeParameters = beforeEnterEvent.getRouteParameters();
            // first check the dates
            Map<String, List<String>> map = queryParameters.getParameters() == null ? new HashMap<>() : new HashMap<>(queryParameters.getParameters());
            if (startDate.isPresent() && endDate.isPresent()) {
                try {
                    String startString = startDate.get();
                    map.put("start_date", List.of(startString));
                    LocalDate start = LocalDate.parse(startString, DateTimeFormatter.ISO_DATE);

                    String endString = endDate.get();
                    map.put("end_date", List.of(startString));
                    LocalDate end = LocalDate.parse(endString, DateTimeFormatter.ISO_DATE);

                    // first check if they are aligned with the filter
                    DateRangeModel<SimpleDateRange> value = rangePicker.getValue();
                    if (value != null && (end.isAfter(start) || end.isEqual(start))) {
                        if (!value.getStart().isEqual(start) || !value.getEnd().isEqual(end)) {
                            // update query parameters and navigate again
                            rangePicker.setValue(new DateRangeModel<>(start, end, SimpleDateRanges.FREE));
                            return;
                        }
                        // else proceed
                    } else if (value != null && end.isBefore(start)) {
                        // update query parameters and navigate again
                        rangePicker.setValue(new DateRangeModel<>(LocalDate.now(), LocalDate.now(), SimpleDateRanges.DAY));
                        return;
                    }


                } catch (DateTimeParseException e) {
                    rangePicker.setValue(new DateRangeModel<>(LocalDate.now(), LocalDate.now(), SimpleDateRanges.DAY));
                }
                dashboardMenuItem.setQueryParameters(new QueryParameters(map));
                receiptsMenuItem.setQueryParameters(new QueryParameters(map));
            } else {
//                rangePicker.setValue(new DateRangeModel<>(, , rangePicker.getDateRange()));
                map.put("start_date", List.of(rangePicker.getStart().format(DateTimeFormatter.ISO_DATE)));
                map.put("end_date", List.of(rangePicker.getEnd().format(DateTimeFormatter.ISO_DATE)));
                if (routeParameters == null) {
                    beforeEnterEvent.forwardTo(navigationTarget, new QueryParameters(map));
                } else {
                    beforeEnterEvent.forwardTo(navigationTarget, routeParameters, new QueryParameters(map));
                }
            }


        }
    }

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;
        private final String menuTitle;
        private final Component icon;
        private final RouterLink link;

        public MenuItemInfo(String menuTitle, Component icon, Class<? extends Component> view, QueryParameters queryParameters) {
            this.view = view;
            this.menuTitle = menuTitle;
            this.icon = icon;
            link = new RouterLink();
            link.addClassNames(Display.FLEX, Gap.XSMALL, Height.MEDIUM, AlignItems.CENTER, Padding.Horizontal.SMALL, TextColor.BODY);
            link.setRoute(view);
            setQueryParameters(queryParameters);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames(FontWeight.MEDIUM, FontSize.MEDIUM, Whitespace.NOWRAP);

            if (icon != null) {
                link.add(icon);
            }
            link.add(text);
            add(link);
        }

        private void setQueryParameters(QueryParameters queryParameters) {
            link.setQueryParameters(queryParameters);
        }

        public Class<? extends Component> getView() {
            return view;
        }

        public String getMenuTitle() {
            return menuTitle;
        }

        public Component getIcon() {
            return icon;
        }
    }

}
