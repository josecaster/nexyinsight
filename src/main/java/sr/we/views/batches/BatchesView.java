package sr.we.views.batches;

import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.componentfactory.onboarding.OnboardingStep;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import sr.we.controllers.ItemsController;
import sr.we.controllers.StoresController;
import sr.we.entity.*;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.security.AuthenticatedUser;
import sr.we.services.BatchItemsService;
import sr.we.services.BatchService;
import sr.we.views.HelpFunction;
import sr.we.views.MainLayout;
import sr.we.views.components.MyLineAwesome;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PageTitle("Batches")
@Route(value = "batch/:batchId?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "SECTION_OWNER"})
public class BatchesView extends Div implements BeforeEnterObserver, HelpFunction {

    final String BATCH_ID = "batchId";
    private final String BATCH_EDIT_ROUTE_TEMPLATE = "batch/%s/edit";
    private final Grid<Batch> grid = new Grid<>(Batch.class, false);
    private final Button cancel = new Button("Add new batch ");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<Batch> binder;
    private final BatchItemsService batchItemsService;
    private final ItemsController ItemService;
    private final StoresController storesController;
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
    private List<Section> sections;
    private Div editorLayoutDiv;

    public BatchesView(ItemsController ItemService, BatchItemsService batchItemsService, BatchService batchService, AuthenticatedUser authenticatedUser, StoresController storesController) {
        this.batchItemsService = batchItemsService;
        this.batchService = batchService;
        this.authenticatedUser = authenticatedUser;
        this.storesController = storesController;
        this.ItemService = ItemService;

        User user = authenticatedUser.get().get();
        linkSections = user.getLinkSections();

        addClassNames("batches-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(AbstractEntity::getId).setHeader("#").setAutoWidth(true);
        grid.addColumn(Batch::getDescription).setHeader("Description").setAutoWidth(true);
        grid.addComponentColumn(r -> {
            String collect1 = getSection(r);
            Span span = new Span(collect1);
            span.getStyle().set("white-space", "pre-line");
            span.getElement().getThemeList().add("badge warning");
            span.setWidthFull();
            return span;
        }).setHeader("Section").setAutoWidth(true);
//        grid.addColumn(new LocalDateRenderer<>(Batch::getStartDate, () -> DateTimeFormatter.ofPattern("M/d/yyyy"))).setHeader("Start date").setAutoWidth(true);
//        grid.addColumn(new LocalDateRenderer<>(Batch::getEndDate, () -> DateTimeFormatter.ofPattern("M/d/yyyy"))).setHeader("End date").setAutoWidth(true);
        grid.addComponentColumn(bat -> {
            Span span = new Span(bat.getStatus().name());
            span.getElement().getThemeList().add("badge");
            span.setWidthFull();

            switch (bat.getStatus()) {
                case NEW -> {
                    span.getElement().getThemeList().add("warning");
                }
                case UPLOAD_ITEMS -> {
                    span.getElement().getThemeList().add("contrast");
                }
                case VALIDATE_ITEMS -> {
                    span.getElement().getThemeList().add("primary");
                }
                case APPROVED -> {
                    span.getElement().getThemeList().add("success");
                }
                case REJECTED, CANCEL -> {
                    span.getElement().getThemeList().add("error");
                }
            }

            return span;
        }).setAutoWidth(true);


        Set<Role> roles = authenticatedUser.get().get().getRoles();
        grid.addComponentColumn(c -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();

            Button listBtn = new Button("", click -> {
                populateForm(c);
                doForUploadBtn(Batch.Status.UPLOAD_ITEMS);
            });
            listBtn.setIcon(MyLineAwesome.LIST_ALT.create());

            Button checkBtn = new Button("", click -> {
                List<BatchItems> byBatchId = batchItemsService.findByBatchId(c.getId());
                if (!byBatchId.isEmpty()) {
                    populateForm(c);
                    doForUploadBtn(Batch.Status.VALIDATE_ITEMS);
                } else {
                    Notification n = Notification.show("Cannot validate yet! Please add some batch Items first");
                    n.setPosition(Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            checkBtn.setIcon(MyLineAwesome.CLIPBOARD_CHECK_SOLID.create());

            Button approveBtn = new Button("", click -> {

                List<BatchItems> byBatchId = batchItemsService.findByBatchId(c.getId());
                if (!byBatchId.isEmpty() && byBatchId.stream().anyMatch(f -> f.getRealQuantity() != null && f.getRealQuantity() != 0)) {
                    ConfirmDialog confirm = new ConfirmDialog("APPROVE BATCH", "Are you sure you want to approve this batch", "Yes", approved -> {
                        c.setStatus(Batch.Status.APPROVED);
                        try {
                            Batch update = batchService.update(c);
                            refreshGrid();
                            populateForm(update);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    confirm.open();
                } else {
                    Notification n = Notification.show("Cannot approve yet! Please add counted values first!");
                    n.setPosition(Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }


            });
            approveBtn.setIcon(MyLineAwesome.THUMBS_UP_SOLID.create());
            approveBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

            Button rejectBtn = new Button("", click -> {

                ConfirmDialog confirm = new ConfirmDialog("REJECT BATCH", "Are you sure you want to reject this batch", "Yes", approved -> {
                    c.setStatus(Batch.Status.REJECTED);
                    try {
                        Batch update = batchService.update(c);
                        refreshGrid();
                        populateForm(update);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                confirm.open();
            });
            rejectBtn.setIcon(MyLineAwesome.THUMBS_DOWN_SOLID.create());
            rejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button trashBtn = new Button("", click -> {

                ConfirmDialog confirm = new ConfirmDialog("CANCEL BATCH", "Are you sure you want to cancel this batch", "Yes", approved -> {
                    c.setStatus(Batch.Status.CANCEL);
                    try {
                        Batch update = batchService.update(c);
                        refreshGrid();
                        populateForm(update);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                confirm.open();


            });
            trashBtn.setIcon(MyLineAwesome.TRASH_ALT.create());


            horizontalLayout.add(listBtn, checkBtn, approveBtn, rejectBtn, trashBtn);
            switch (c.getStatus()) {
                case NEW -> {
                    listBtn.setVisible(true);
                    checkBtn.setVisible(false);
                    approveBtn.setVisible(false);
                    rejectBtn.setVisible(false);
                    trashBtn.setVisible(true);
                }
                case UPLOAD_ITEMS -> {
                    if (roles.contains(Role.ADMIN)) {
                        listBtn.setVisible(true);
                        checkBtn.setVisible(true);
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                        trashBtn.setVisible(true);
                    } else {
                        listBtn.setVisible(true);
                        checkBtn.setVisible(false);
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                        trashBtn.setVisible(true);
                    }
                }
                case VALIDATE_ITEMS -> {
                    if (roles.contains(Role.ADMIN)) {
                        listBtn.setVisible(false);
                        checkBtn.setVisible(true);
                        approveBtn.setVisible(true);
                        rejectBtn.setVisible(true);
                        trashBtn.setVisible(false);
                    } else {
                        listBtn.setVisible(false);
                        checkBtn.setVisible(false);
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                        trashBtn.setVisible(false);
                    }
                }
                case APPROVED, REJECTED, CANCEL -> {
                    listBtn.setVisible(false);
                    checkBtn.setVisible(false);
                    approveBtn.setVisible(false);
                    rejectBtn.setVisible(false);
                    trashBtn.setVisible(false);
                }
            }
            return horizontalLayout;
        });

        grid.setItems(query -> {
            PageRequest pageable = PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query));
            Stream<Batch> sectionId1 = user.getRoles().contains(Role.ADMIN) ? batchService.list(pageable).stream() : batchService.list(pageable, (root, query2, cb) -> root.get("sectionId").in(linkSections)).stream();
            return sectionId1.sorted(Comparator.comparingLong(Batch::getId).reversed());
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

//        binder.bindInstanceFields(this);
        binder.forField(status).withValidator(Objects::nonNull, "Status should not be empty").bind(Batch::getStatus, Batch::setStatus);
        binder.forField(sectionId).withValidator(StringUtils::isNotBlank, "Section should not be empty").bind(Batch::getSectionId, Batch::setSectionId);
        binder.forField(description).withValidator(StringUtils::isNotBlank, "Description should not be empty").bind(Batch::getDescription, Batch::setDescription);

        status.setRequiredIndicatorVisible(true);
        sectionId.setRequired(true);
        description.setRequired(true);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            BinderValidationStatus<Batch> validate = binder.validate();
            if(validate.isOk()) {
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
            } else {
                Notification.show("Failed to update the data. Check again that all values are valid", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
    }

    private String getSection(Batch r) {
        List<Section> collect = sections.stream().filter(l -> {
            return StringUtils.isNotBlank(r.getSectionId()) && r.getSectionId().equalsIgnoreCase(l.getUuId());
        }).toList();
        if (collect.size() > 1) {
            collect = collect.stream().filter(l -> !l.isDefault()).toList();
        }
//            sectionMultiSelectComboBox.setValue(collect);
        String collect1 = collect.stream().map(Section::getName).collect(Collectors.joining("\n"));
        return collect1;
    }

    private void doForUploadBtn(Batch.Status uploadItems) {


        switch (uploadItems) {
            case UPLOAD_ITEMS -> {
                ConfirmDialog confirm = new ConfirmDialog("Add Batch Items", "Are you ready to add batch items?", "Yes", approved -> {
                    try {
                        this.batch.setStatus(uploadItems);
                        this.batch = batchService.update(this.batch);
                        this.grid.getDataProvider().refreshItem(this.batch);
                        populateForm(this.batch);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    // create confirm dialog
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setHeader("Batch items");
                    dialog.setWidth("80%");
                    UploadItemsView uploadItemsView = new UploadItemsView(ItemService, batchItemsService, batchService, authenticatedUser, storesController, batch);
                    dialog.add(uploadItemsView);
                    dialog.setCancelable(true);
                    dialog.open();
                    dialog.addConfirmListener(l -> {
                        List<BatchItems> list = uploadItemsView.getGrid().getDataProvider().fetch(new Query<>()).toList();
                        batchItemsService.update(batch.getId(), list);
                        Notification.show("Data updated", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    });
                });
                confirm.open();
            }
            case VALIDATE_ITEMS -> {
                ConfirmDialog confirm = new ConfirmDialog("Item validation", "Validate the items count", "Yes", approved -> {
                    try {
                        this.batch.setStatus(uploadItems);
                        this.batch = batchService.update(this.batch);
                        this.grid.getDataProvider().refreshItem(this.batch);
                        populateForm(this.batch);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    // create confirm dialog
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setHeader("Validate batch items");
                    dialog.setWidth("80%");
                    UploadItemsView uploadItemsView = new UploadItemsView(ItemService, batchItemsService, batchService, authenticatedUser, storesController, batch);
                    dialog.add(uploadItemsView);
                    dialog.setCancelable(true);
                    dialog.open();
                    dialog.addConfirmListener(l -> {
                        List<BatchItems> list = uploadItemsView.getGrid().getDataProvider().fetch(new Query<>()).toList();
                        batchItemsService.update(batch.getId(), list);
                        Notification.show("Data updated", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    });
                });
                confirm.open();
            }
        }


    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sections = storesController.allStores(getBusinessId());
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
        editorLayoutDiv = new Div();
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
        sectionId.setItemLabelGenerator(label -> {
            Section section = storesController.oneStore(label);
            return section == null ? "Error" : section.getName();
        });
        sectionId.setItems(query -> storesController.allSections(getBusinessId(), query.getPage(), query.getPageSize(), authenticatedUser.get()).map(Section::getUuId));
        description = new TextField("Description");
        startDate = new DatePicker("Start date");
        endDate = new DatePicker("End date");
        uploadBtn = new Button("Show items");
        formLayout.add(batchIdFld, status, sectionId, description/*, startDate, endDate*/, uploadBtn);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);

        setStatusNew();

        uploadBtn.addClickListener(l -> {
            Dialog dialog = new Dialog();
            dialog.setWidth("80%");
            UploadItemsView uploadItemsView = new UploadItemsView(ItemService, batchItemsService, batchService, authenticatedUser, storesController, batch);
            uploadItemsView.setEnabled(false);
            dialog.add(uploadItemsView);
            dialog.open();

        });
        uploadBtn.setVisible(false);
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
                    uploadBtn.setVisible(false);
                }
                case UPLOAD_ITEMS -> {
                    status.setItems(Batch.Status.UPLOAD_ITEMS, Batch.Status.VALIDATE_ITEMS, Batch.Status.CANCEL);
                    status.setValue(Batch.Status.UPLOAD_ITEMS);
                    uploadBtn.setVisible(false);
                    uploadBtn.setText("Add batch items");
                }
                case VALIDATE_ITEMS -> {
                    if (roles.contains(Role.ADMIN)) {
                        status.setItems(Batch.Status.VALIDATE_ITEMS, Batch.Status.APPROVED, Batch.Status.REJECTED, Batch.Status.CANCEL);
                    } else {
                        status.setItems(Batch.Status.VALIDATE_ITEMS, Batch.Status.CANCEL);
                    }
                    status.setValue(Batch.Status.VALIDATE_ITEMS);
                    uploadBtn.setVisible(false);
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
                    uploadBtn.setVisible(true);
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
                    uploadBtn.setVisible(true);
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
                    uploadBtn.setVisible(true);
                }
            }
        } else {
            throw new RuntimeException("Something is wrong with the batches authentication");
        }
    }

    @Override
    public String help(Onboarding onboarding) {

        OnboardingStep step1 = new OnboardingStep(this);
        step1.setHeader("Introduction to Batches module");
        step1.setContent(new Html("""
                <div style="padding: var(--lumo-space-s); background-color: var(--lumo-shade-5pct);">
                  <p>Welcome to Batches! This feature helps you gather and manage your stock data easily. With Batches, you can create lists of items with their quantities, which you'll then send for approval. It's a simple way for section owners to organize and share their inventory info with the main store owner. Let's dive in and get you started!</p>
                </div>
                """));


        OnboardingStep step2 = new OnboardingStep(grid);
        step2.setHeader("Batches Grid Overview");
        step2.setContent(new Html("""
                <div style="padding: var(--lumo-space-s); background-color: var(--lumo-shade-5pct);">
                  <p>In the Batches grid, you'll find all the essential details at a glance to keep your workflow moving smoothly. Each batch is represented with its unique Batch number, allowing for easy identification and reference. The Description column provides a brief overview of the contents or purpose of the batch, ensuring clarity for all involved parties. The Status column keeps you informed about the current stage of approval, whether it's pending, approved, or in progress.</p>
                                
                  <p>But that's not all—Actions give you the power to expedite the workflow process. Whether it's submitting a batch for review, approving it, or taking other necessary steps, the Actions column provides quick access to perform these essential tasks, helping you keep your inventory management efficient and effective.</p>
                </div>
                """));


        OnboardingStep step3 = new OnboardingStep(editorLayoutDiv);
        step3.setHeader("Batch Form Fields");
        step3.setContent(new Html("""
                <div style="padding: var(--lumo-space-s); background-color: var(--lumo-shade-5pct);">
                                
                  <p>The Batch Form is your central hub for tracking and updating batch information effortlessly. Here, you'll find key details and tools to streamline your workflow.</p>
                                
                  <ul>
                    <li><strong>Process Status:</strong> Stay informed about the current status of your batch process. Whether it's pending, in progress, or completed, the process status keeps you up-to-date every step of the way.</li>
                    <li><strong>Batch ID:</strong> Each batch is assigned a unique ID for easy reference and organization. Quickly locate specific batches using their distinctive identifiers.</li>
                    <li><strong>Section Involvement:</strong> Identify the section associated with each batch, ensuring clear communication and accountability throughout the process.</li>
                    <li><strong>Description Field:</strong> Add or update descriptions to provide context and clarity regarding the contents or purpose of each batch.</li>
                  </ul>
                                
                  <p><strong style="color: var(--lumo-primary-text-color);">Save Button:</strong> Seamlessly save any changes or updates made to batch information with the click of a button, ensuring accuracy and efficiency in data management.</p>
                                
                  <p><strong style="color: var(--lumo-primary-text-color);">Add New Batch Button:</strong> Start a new batch with ease by initiating the creation process directly from the Batch Form. Simplify your workflow by effortlessly adding new batches as needed.</p>
                                
                  <p>With the Batch Form, managing and monitoring your batches has never been more convenient. Stay organized, informed, and productive as you navigate through your batch processes.</p>
                </div>
                """));
        onboarding.addStep(step1);
        onboarding.addStep(step2);
        onboarding.addStep(step3);
        return "BatchHelpFunction";
    }
}
