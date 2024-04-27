package sr.we.views.batches;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import sr.we.controllers.StoresRestController;
import sr.we.entity.Batch;
import sr.we.entity.BatchItems;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.security.AuthenticatedUser;
import sr.we.services.BatchItemsService;
import sr.we.services.BatchService;
import sr.we.views.MainLayout;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@PageTitle("Batches")
@Route(value = "batch/:batchId?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "SECTION_OWNER"})
public class BatchesView extends Div implements BeforeEnterObserver {

    final String BATCH_ID = "batchId";
    private final String BATCH_EDIT_ROUTE_TEMPLATE = "batch/%s/edit";
    private final Grid<Batch> grid = new Grid<>(Batch.class, false);
    private final Button cancel = new Button("Add new batch ");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<Batch> binder;
    private final BatchItemsService batchItemsService;
    private final StoresRestController storesRestController;
    private final BatchService batchService;
    private final AuthenticatedUser authenticatedUser;
    TextField description;
    DatePicker startDate;
    DatePicker endDate;
    Select<Batch.Status> status;
    ComboBox<String> sectionId;
    private Span batchIdFld;
    private Button uploadBtn;
    private Batch batch;
    private Set<String> linkSections;

    public BatchesView(BatchItemsService batchItemsService, BatchService batchService, AuthenticatedUser authenticatedUser, StoresRestController storesRestController) {
        this.batchItemsService = batchItemsService;
        this.batchService = batchService;
        this.authenticatedUser = authenticatedUser;
        this.storesRestController = storesRestController;

        User user = authenticatedUser.get().get();
        linkSections = user.getLinkSections();

        addClassNames("batches-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(batch -> "#" + batch.getId()).setHeader("#").setAutoWidth(true);
        grid.addColumn(Batch::getDescription).setHeader("Description").setAutoWidth(true);
        grid.addColumn(new LocalDateRenderer<>(Batch::getStartDate, () -> DateTimeFormatter.ofPattern("M/d/yyyy"))).setHeader("Start date").setAutoWidth(true);
        grid.addColumn(new LocalDateRenderer<>(Batch::getEndDate, () -> DateTimeFormatter.ofPattern("M/d/yyyy"))).setHeader("End date").setAutoWidth(true);
        grid.addColumn("status").setAutoWidth(true);

        grid.setItems(query -> {
            PageRequest pageable = PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query));
            return user.getRoles().contains(Role.ADMIN) ? batchService.list(pageable).stream() : batchService.list(pageable, (root, query2, cb) -> root.get("sectionId").in(linkSections)).stream();
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(BATCH_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(BatchesView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Batch.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.batch == null) {
                    this.batch = new Batch();
                }
                binder.writeBean(this.batch);
                Batch update = batchService.update(this.batch);
                clearForm();
                refreshGrid();
                populateForm(update);
                Notification.show("Data updated", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate(BatchesView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            } catch (IOException ex) {
                Notification.show("Error updating data", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
    }

    private void doForUploadBtn() {


        // create confirm dialog
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Batch items");
        dialog.setWidth("80%");
        UploadItemsView uploadItemsView = new UploadItemsView(batchItemsService, batchService, authenticatedUser, storesRestController, batch);
        dialog.add(uploadItemsView);
        dialog.setCancelable(true);
        dialog.open();
        dialog.addConfirmListener(l -> {
            List<BatchItems> list = uploadItemsView.getGrid().getDataProvider().fetch(new Query<>()).toList();
            batchItemsService.update(batch.getId(), list);
            Notification.show("Data updated", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> batchId = event.getRouteParameters().get(BATCH_ID).map(Long::parseLong);
        if (batchId.isPresent()) {
            Optional<Batch> batchFromBackend = batchService.get(batchId.get());
            if (batchFromBackend.isPresent()) {
                populateForm(batchFromBackend.get());
            } else {
                Notification.show(String.format("The requested batch was not found, ID = %s", batchId.get()), 10000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_WARNING);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(BatchesView.class);
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
        batchIdFld = new Span("!!!NEW!!!");
        batchIdFld.getElement().getThemeList().add("badge warning");
        status = new Select<>("Status", l -> {
        });
        status.setItems(Batch.Status.values());
        sectionId = new ComboBox<>("Section");
        sectionId.setItemLabelGenerator(label -> storesRestController.oneStore(getBusinessId(), label).getName());
        sectionId.setItems(query -> storesRestController.allSections(getBusinessId(), query.getPage(), query.getPageSize(), f -> {
            Optional<User> userOptional = authenticatedUser.get();
            if (userOptional.isEmpty()) {
                return false;
            }
            User user = userOptional.get();
            if (user.getRoles().contains(Role.ADMIN)) {
                return true;
            } else {
                // check sections uu ids
                linkSections = user.getLinkSections();
                if (linkSections.isEmpty()) {
                    return false;
                }
                return linkSections.stream().anyMatch(n -> n.equalsIgnoreCase(f.getUuId()));
            }

        }).map(Section::getUuId));
        description = new TextField("Description");
        startDate = new DatePicker("Start date");
        endDate = new DatePicker("End date");
        uploadBtn = new Button("Add batch items");
        formLayout.add(batchIdFld, status, sectionId, description, startDate, endDate, uploadBtn);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);

        setStatusNew();

        uploadBtn.addClickListener(l -> {
            doForUploadBtn();
        });
    }

    private Long getBusinessId() {
        return 0L;
    }

    private void setStatusNew() {
        status.setItems(Batch.Status.NEW);
        status.setValue(Batch.Status.NEW);
        status.setReadOnly(true);
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

    private void populateForm(Batch value) {
        this.batch = value;
        binder.readBean(this.batch);
        if (value != null) {
            // batch Id
            batchIdFld.getElement().getThemeList().remove("warning");
            batchIdFld.getElement().getThemeList().add("success");
            batchIdFld.setText("#" + value.getId());

            // status
            setStatusField(value);
        } else {
            // batch Id
            batchIdFld.setText("!!!NEW!!!");
            batchIdFld.getElement().getThemeList().remove("success");
            batchIdFld.getElement().getThemeList().add("warning");

            // status
            setStatusNew();

            // upload
            uploadBtn.setVisible(false);
            sectionId.setReadOnly(false);
            description.setReadOnly(false);
            startDate.setReadOnly(false);
            endDate.setReadOnly(false);
            save.setVisible(true);

        }
    }

    private void setStatusField(Batch batch) {
        status.setReadOnly(false);
        uploadBtn.setVisible(false);
        status.setReadOnly(false);
        sectionId.setReadOnly(false);
        description.setReadOnly(false);
        startDate.setReadOnly(false);
        endDate.setReadOnly(false);
        Optional<User> user = authenticatedUser.get();
        if (user.isPresent()) {
            Set<Role> roles = user.get().getRoles();
            uploadBtn.setText("View batch items");
            save.setVisible(true);
            switch (batch.getStatus()) {
                case NEW -> {
                    status.setItems(Batch.Status.NEW, Batch.Status.UPLOAD_ITEMS, Batch.Status.CANCEL);
                    status.setValue(Batch.Status.NEW);
                }
                case UPLOAD_ITEMS -> {
                    status.setItems(Batch.Status.UPLOAD_ITEMS, Batch.Status.VALIDATE_ITEMS, Batch.Status.CANCEL);
                    status.setValue(Batch.Status.UPLOAD_ITEMS);
                    uploadBtn.setVisible(true);
                    uploadBtn.setText("Add batch items");
                }
                case VALIDATE_ITEMS -> {
                    if (roles.contains(Role.ADMIN)) {
                        status.setItems(Batch.Status.VALIDATE_ITEMS, Batch.Status.APPROVED, Batch.Status.REJECTED, Batch.Status.CANCEL);
                    } else {
                        status.setItems(Batch.Status.VALIDATE_ITEMS, Batch.Status.CANCEL);
                    }
                    status.setValue(Batch.Status.VALIDATE_ITEMS);
                    uploadBtn.setVisible(true);
                    uploadBtn.setText("Count batch items");
                }
//                case SEND_FOR_APPROVAL -> {
//                    if (roles.contains(Role.ADMIN)) {
//                        status.setItems(Batch.Status.SEND_FOR_APPROVAL, Batch.Status.APPROVED, Batch.Status.REJECTED, Batch.Status.CANCEL);
//                    } else {
//                        status.setItems(Batch.Status.SEND_FOR_APPROVAL, Batch.Status.CANCEL);
//                    }
//                    status.setValue(Batch.Status.SEND_FOR_APPROVAL);
//                }
                case APPROVED -> {
                    status.setItems(Batch.Status.APPROVED);
                    status.setValue(Batch.Status.APPROVED);
                    save.setVisible(false);
                    status.setReadOnly(true);
                    sectionId.setReadOnly(true);
                    description.setReadOnly(true);
                    startDate.setReadOnly(true);
                    endDate.setReadOnly(true);
                }
                case REJECTED -> {
                    status.setItems(Batch.Status.REJECTED, Batch.Status.VALIDATE_ITEMS);
                    status.setValue(Batch.Status.REJECTED);
                    save.setVisible(false);
                    status.setReadOnly(true);
                    sectionId.setReadOnly(true);
                    description.setReadOnly(true);
                    startDate.setReadOnly(true);
                    endDate.setReadOnly(true);
                }
                case CANCEL -> {
                    status.setItems(Batch.Status.CANCEL);
                    status.setValue(Batch.Status.CANCEL);
                    save.setVisible(false);
                    status.setReadOnly(true);
                    sectionId.setReadOnly(true);
                    description.setReadOnly(true);
                    startDate.setReadOnly(true);
                    endDate.setReadOnly(true);
                }
            }
        } else {
            throw new RuntimeException("Something is wrong with the batches authentication");
        }
    }
}
