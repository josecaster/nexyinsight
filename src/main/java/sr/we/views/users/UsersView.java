package sr.we.views.users;

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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vaadin.addons.joelpop.changepassword.ChangePassword;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordDialog;
import org.vaadin.addons.joelpop.changepassword.ChangePasswordRule;
import sr.we.controllers.StoresRestController;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.security.AuthenticatedUser;
import sr.we.services.UserService;
import sr.we.views.MainLayout;

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
    private final StoresRestController storesRestController;
    private final PasswordEncoder passwordEncoder;
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

    public UsersView(UserService userService, AuthenticatedUser authenticatedUser, StoresRestController storesRestController, PasswordEncoder passwordEncoder) {

        this.userService = userService;
        this.authenticatedUser = authenticatedUser;
        this.storesRestController = storesRestController;
        this.passwordEncoder = passwordEncoder;

        addClassNames("batches-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(FlexComponent.Alignment.CENTER);
            Avatar avatar = new Avatar(client.getName());
//            StreamResource resource = new StreamResource("profile-pic", () -> null/*new ByteArrayInputStream(client.getProfilePicture())*/);
//            avatar.setImageResource(resource);
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");
            Span span = new Span();
            span.setClassName("name");
            span.setText(client.getName());
            hl.add(avatar, span);
            return hl;
        })).setComparator(User::getName).setHeader("User");
        grid.addColumn(User::getUsername).setHeader("Username").setAutoWidth(true);
        grid.addColumn(User::getEmail).setHeader("Email").setAutoWidth(true);
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
            } else {
                Notification.show(String.format("The requested user was not found, ID = %s", usersId.get()), 10000, Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_WARNING);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(UsersView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
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

        linkSections.setItemLabelGenerator(label -> storesRestController.oneStore(getBusinessId(), label).getName());
        linkSections.setItems(query -> storesRestController.allSections(getBusinessId(), query.getPage(), query.getPageSize(), f -> true).map(Section::getUuId));

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
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
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
