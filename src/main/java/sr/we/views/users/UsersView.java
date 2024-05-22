package sr.we.views.users;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vaadin.addons.joelpop.changepassword.ChangePassword;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordDialog;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordRule;
import sr.we.controllers.StoresController;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.security.AuthenticatedUser;
import sr.we.services.UserService;
import sr.we.views.MainLayout;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PageTitle("users")
@Route(value = "users/:usersId?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@RolesAllowed("ADMIN")
public class UsersView extends Div implements BeforeEnterObserver {

    final String IV_ID = "usersId";
    private final String IV_EDIT_ROUTE_TEMPLATE = "users/%s/edit";
    private final Grid<User> grid = new Grid<>(User.class, false);
    private final Button cancel = new Button("Add new user");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<User> binder;
    private final UserService userService;
    private final AuthenticatedUser authenticatedUser;
    private final StoresController storesController;
    private final PasswordEncoder passwordEncoder;
    private final SplitLayout splitLayout;
    private final Grid.Column<User> userMobileColumn;
    private final Grid.Column<User> emailColumn;
    private final Grid.Column<User> usernameColumn;
    private final Grid.Column<User> userColumn;
    private boolean isMobile;
    private TextField username;
    private TextField name;
    //    private PasswordField hashedPassword;
    private MultiSelectComboBox<Role> roles;

    //    private TextField profilePicture;
    private EmailField email;
    private MultiSelectComboBox<String> linkSections;
    private Checkbox enabled;
    private User user;
    private Button changePwdBtn;
    private Header editHeader;
    private Div wrapper;


    public UsersView(UserService userService, AuthenticatedUser authenticatedUser, StoresController storesController, PasswordEncoder passwordEncoder) {


        this.userService = userService;
        this.authenticatedUser = authenticatedUser;
        this.storesController = storesController;
        this.passwordEncoder = passwordEncoder;

        addClassNames("batches-view");

        // Create UI
        splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);


        add(splitLayout);

        // Configure Grid
        userColumn = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            Avatar avatar = new Avatar(client.getName());
            if (client.getProfilePicture() != null) {
                StreamResource resource = new StreamResource("profile-pic", () -> new ByteArrayInputStream(client.getProfilePicture()));
                avatar.setImageResource(resource);
            }
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");
            Span span = new Span();
            span.setClassName("name");
            span.setText(client.getName());
            hl.add(avatar, span);
            return hl;
        })).setComparator(User::getName).setHeader("User");
        usernameColumn = grid.addColumn(User::getUsername).setHeader("Username").setAutoWidth(true);
        emailColumn = grid.addColumn(User::getEmail).setHeader("Email").setAutoWidth(true);
        userMobileColumn = grid.addComponentColumn(u -> CardView.createCard(u.getProfilePicture(), u.getName(), u.getUsername(), u.getEmail())).setHeader("List of users");
        userColumn.setVisible(false);


        grid.setItems(query -> userService.list(PageRequest.of(query.getPage(), query.getPageSize())).get());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(IV_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(UsersView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(User.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
            if(isMobile){
                splitLayout.setSplitterPosition(0);
            }
        });

        save.addClickListener(e -> {
            try {
                if (this.user == null) {
                    this.user = new User();
                    this.user.setBusinessId(getBusinessId());
                }
                binder.writeBean(UsersView.this.user);
                User update = userService.update(this.user);
                clearForm();
                refreshGrid();
                populateForm(update);
                Notification.show("Data updated", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate(UsersView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
    }

    public  boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        isMobile = isMobileDevice();
        editHeader.setVisible(isMobile);
        if(isMobile){
            if(user == null){
                splitLayout.setSplitterPosition(100);
            } else {
                splitLayout.setSplitterPosition(0);
            }

            wrapper.addClassName("card-view");
            emailColumn.setVisible(false);
            userColumn.setVisible(false);
            usernameColumn.setVisible(false);
            userMobileColumn.setVisible(true);
        } else {
            wrapper.removeClassName("card-view");
            emailColumn.setVisible(true);
            userColumn.setVisible(true);
            usernameColumn.setVisible(true);
            userMobileColumn.setVisible(false);
        }
    }

    private Long getBusinessId() {
        return 0L;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> usersId = event.getRouteParameters().get(IV_ID);
        if (usersId.isPresent()) {
            Optional<User> optionalUser = userService.get(Long.valueOf(usersId.get()));
            if (optionalUser.isPresent()) {
                populateForm(optionalUser.get());
                if(isMobile){
                    splitLayout.setSplitterPosition(0);
                }
            } else {
                Notification.show(String.format("The requested user was not found, ID = %s", usersId.get()), 10000, Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_WARNING);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(UsersView.class);
            }
        } else {
            if(isMobile){
                splitLayout.setSplitterPosition(100);
            }
        }

    }

    private void createEditorLayout(SplitLayout splitLayout) {
        HorizontalLayout horizontalLayout = new HorizontalLayout(new Button("View all", l -> {
            if (isMobile) {
                splitLayout.setSplitterPosition(100);
            }
        }));
        horizontalLayout.setClassName("button-layout");
        horizontalLayout.setPadding(true);
        editHeader = new Header(horizontalLayout);
        editHeader.setVisible(false);
        Div editorLayoutDiv = new Div(editHeader);
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();

        username = new TextField("Username");
        name = new TextField("Name");
//        hashedPassword = new PasswordField("Password");
        roles = new MultiSelectComboBox<>("Roles", Role.ADMIN, Role.SECTION_OWNER);
//        profilePicture = new TextField("Profile picture");
        email = new EmailField("Email");
        linkSections = new MultiSelectComboBox<>("Link sections");
        enabled = new Checkbox("Enabled");

        username.setWidthFull();
        name.setWidthFull();
//        hashedPassword.setWidthFull();
        roles.setWidthFull();
//        profilePicture.setWidthFull();
        email.setWidthFull();
        linkSections.setWidthFull();
//        enabled.setWidthFull();

        username.setClearButtonVisible(true);
        name.setClearButtonVisible(true);
        roles.setClearButtonVisible(true);
        email.setClearButtonVisible(true);
        linkSections.setClearButtonVisible(true);

        linkSections.setItemLabelGenerator(label -> {
            Section section = storesController.oneStore(label);
            return section == null ? "Error" : section.getName();
        });
        linkSections.setItems(query -> storesController.allSections(getBusinessId(), query.getPage(), query.getPageSize()).map(Section::getUuId));

        changePwdBtn = new Button("change password");
        changePwdBtn.setVisible(false);
        changePwdBtn.addClickListener(click -> {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
            changePasswordDialog.setChangePasswordMode(ChangePassword.ChangePasswordMode.ESTABLISH_NEW);
            changePasswordDialog.addOkListener(ok -> {
//                boolean matches = passwordEncoder.matches(ok.getCurrentPassword(), user.getHashedPassword());
                if (user.getUsername().equalsIgnoreCase(ok.getUserid())) {
                    user.setHashedPassword(passwordEncoder.encode(ok.getDesiredPassword()));
                    userService.update(user);
                } else {
                    Notification n = Notification.show("You may not edit the userid. Password not changed");
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
            changePasswordDialog.setUserid(user.getUsername());
            changePasswordDialog.open();
        });
        formLayout.add(username, name, roles, email, linkSections, changePwdBtn, enabled);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);

    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
//        cancel.addThemeVariants(ButtonVariant.);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        wrapper = new Div();
        wrapper.setClassName("grid-wrapper");


        HorizontalLayout horizontalLayout = new HorizontalLayout(cancel);
        horizontalLayout.setPadding(true);
        wrapper.add(new Header(horizontalLayout));

        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(User value) {
        this.user = value;
        binder.readBean(this.user);
        if (value != null) {
            // user Id
            username.setValue(StringUtils.isBlank(user.getUsername()) ? "" : user.getUsername());
            username.setReadOnly(true);
            name.setValue(StringUtils.isBlank(user.getName()) ? "" : user.getName());
            roles.setValue(user.getRoles());
            email.setValue(StringUtils.isBlank(user.getEmail()) ? "" : user.getEmail());
            linkSections.setValue(user.getLinkSections());
            enabled.setValue(user.isEnabledd());
            changePwdBtn.setVisible(true);
        } else {
            username.setReadOnly(false);
            changePwdBtn.setVisible(false);

            username.clear();
            name.clear();
            roles.clear();
            email.clear();
            linkSections.clear();
            enabled.setValue(true);
        }
    }


}
