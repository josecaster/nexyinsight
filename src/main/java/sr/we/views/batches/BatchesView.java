package sr.we.views.batches;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import sr.we.entity.Batch;
import sr.we.entity.BatchItems;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.security.AuthenticatedUser;
import sr.we.services.BatchItemsService;
import sr.we.services.BatchService;
import sr.we.views.MainLayout;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@PageTitle("Batches")
@Route(value = "batch/:batchId?/:action?(edit)", layout = MainLayout.class)
@PermitAll
public class BatchesView extends Div implements BeforeEnterObserver {

    final String BATCH_ID = "batchId";
    private final String BATCH_EDIT_ROUTE_TEMPLATE = "batch/%s/edit";
    private final Grid<Batch> grid = new Grid<>(Batch.class, false);
    private final Button cancel = new Button("Add new batch ");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<Batch> binder;
    private final BatchItemsService batchItemsService;
    private final BatchService batchService;
    private final AuthenticatedUser authenticatedUser;
    TextField description;
    DatePicker startDate;
    DatePicker endDate;
    Select<Batch.Status> status;
    private Span batchIdFld;
    private Button uploadBtn;
    private Batch batch;
    private Grid<BatchItems> itemImportGrid;

    public BatchesView(BatchItemsService batchItemsService, BatchService batchService, AuthenticatedUser authenticatedUser) {
        this.batchItemsService = batchItemsService;
        this.batchService = batchService;
        this.authenticatedUser = authenticatedUser;

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
        grid.setItems(query -> batchService.list(PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
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
            }
        });
    }

    private void doForUploadBtn() {

        // create grid function
        itemImportGrid = new Grid<>(BatchItems.class, false);
        Grid.Column<BatchItems> skuColumn = itemImportGrid.addColumn(BatchItems::getSku).setHeader("SKU").setAutoWidth(true);
        Grid.Column<BatchItems> codeColumn = itemImportGrid.addColumn(BatchItems::getCode).setHeader("CODE").setAutoWidth(true);
        Grid.Column<BatchItems> nameColumn = itemImportGrid.addColumn(BatchItems::getName).setHeader("NAME").setAutoWidth(true);
        Grid.Column<BatchItems> quantityColumn = itemImportGrid.addColumn(BatchItems::getQuantity).setHeader("QUANTITY").setAutoWidth(true);
        Grid.Column<BatchItems> costColumn = itemImportGrid.addColumn(BatchItems::getCost).setHeader("COST").setAutoWidth(true);
        Grid.Column<BatchItems> priceColumn = itemImportGrid.addColumn(BatchItems::getPrice).setHeader("PRICE").setAutoWidth(true);

        // enable editor
        Binder<BatchItems> binder1 = new Binder<>(BatchItems.class);
        Editor<BatchItems> editor = itemImportGrid.getEditor();
        editor.setBuffered(false);
        editor.setBinder(binder1);

        TextField skuFld = new TextField();
        TextField codeFld = new TextField();
        TextField nameFld = new TextField();
        TextField quantityFld = new TextField();
        TextField costFld = new TextField();
        TextField priceFld = new TextField();



        binder1.forField(skuFld).bind(BatchItems::getSku,BatchItems::setSku);
        binder1.forField(codeFld).bind(BatchItems::getCode,BatchItems::setCode);
        binder1.forField(nameFld).bind(BatchItems::getName,BatchItems::setName);
        binder1.forField(quantityFld).bind(BatchItems::getQuantity,BatchItems::setQuantity);
        binder1.forField(costFld).bind(BatchItems::getCost,BatchItems::setCost);
        binder1.forField(priceFld).bind(BatchItems::getPrice,BatchItems::setPrice);

        skuColumn.setEditorComponent(skuFld);
        codeColumn.setEditorComponent(codeFld);
        nameColumn.setEditorComponent(nameFld);
        quantityColumn.setEditorComponent(quantityFld);
        costColumn.setEditorComponent(costFld);
        priceColumn.setEditorComponent(priceFld);


        itemImportGrid.addItemDoubleClickListener(e -> {
            editor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable) editorComponent).focus();
            }
        });



        List<BatchItems> byBatchId = batchItemsService.findByBatchId(batch.getId());
        itemImportGrid.setItems(byBatchId);

        // create upload function
        MemoryBuffer receiver = new MemoryBuffer();
        Upload upload = new Upload(receiver);
        upload.addSucceededListener(n -> {
            doForUploadSucceed(receiver, itemImportGrid);
        });

        // create confirm dialog
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Upload your items");
        dialog.setWidth("80%");
        dialog.add(upload);
        dialog.add(itemImportGrid);
        dialog.open();
        dialog.addConfirmListener(l ->{
            List<BatchItems> list = itemImportGrid.getDataProvider().fetch(new Query<>()).toList();
            batchItemsService.update(batch.getId(),list);
        });
    }

    private void doForUploadSucceed(MemoryBuffer receiver, Grid<BatchItems> itemImportGrid) {
        try {
            Reader in = new InputStreamReader(receiver.getInputStream());


            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader("SKU", "CODE", "NAME", "QUANTITY","PRICE","COST").setSkipHeaderRecord(true).build();

            Iterable<CSVRecord> records = null;
            records = csvFormat.parse(in);

            List<BatchItems> list = new ArrayList<>();
            for (CSVRecord record : records) {
                String sku = record.get("SKU");
                String code = record.get("CODE");
                String name = record.get("NAME");
                String quantity = record.get("QUANTITY");
                String price = record.get("PRICE");
                String cost = record.get("COST");
                list.add(new BatchItems(BatchesView.this.batch.getId(), sku, code, name, quantity, price, cost));
                itemImportGrid.setItems(list);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        description = new TextField("Description");
        startDate = new DatePicker("Start date");
        endDate = new DatePicker("End date");
        uploadBtn = new Button("Upload .csv file");
        formLayout.add(batchIdFld, status, description, startDate, endDate, uploadBtn);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);

        setStatusNew();

        uploadBtn.addClickListener(l -> {
            doForUploadBtn();
        });
    }

    private void setStatusNew() {
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
        }
    }

    private void setStatusField(Batch batch) {
        status.setReadOnly(false);
        uploadBtn.setVisible(false);
        Optional<User> user = authenticatedUser.get();
        if (user.isPresent()) {
            Set<Role> roles = user.get().getRoles();
            switch (batch.getStatus()) {
                case NEW -> {
                    status.setItems(Batch.Status.NEW, Batch.Status.UPLOAD_ITEMS, Batch.Status.CANCEL);
                    status.setValue(Batch.Status.NEW);
                }
                case UPLOAD_ITEMS -> {
                    status.setItems(Batch.Status.UPLOAD_ITEMS, Batch.Status.VALIDATE_ITEMS, Batch.Status.CANCEL);
                    status.setValue(Batch.Status.UPLOAD_ITEMS);
                    uploadBtn.setVisible(true);
                }
                case VALIDATE_ITEMS -> {
                    status.setItems(Batch.Status.VALIDATE_ITEMS, Batch.Status.SEND_FOR_APPROVAL, Batch.Status.CANCEL);
                    status.setValue(Batch.Status.VALIDATE_ITEMS);
                }
                case SEND_FOR_APPROVAL -> {
                    if (roles.contains(Role.ADMIN)) {
                        status.setItems(Batch.Status.SEND_FOR_APPROVAL, Batch.Status.APPROVED, Batch.Status.REJECTED, Batch.Status.CANCEL);
                    } else {
                        status.setItems(Batch.Status.SEND_FOR_APPROVAL, Batch.Status.CANCEL);
                    }
                    status.setValue(Batch.Status.SEND_FOR_APPROVAL);
                }
                case APPROVED -> {
                    status.setItems(Batch.Status.APPROVED);
                    status.setValue(Batch.Status.APPROVED);
                }
                case REJECTED -> {
                    status.setItems(Batch.Status.REJECTED, Batch.Status.VALIDATE_ITEMS);
                    status.setValue(Batch.Status.REJECTED);
                }
                case CANCEL -> {
                    status.setItems(Batch.Status.CANCEL);
                    status.setValue(Batch.Status.CANCEL);
                }
            }
        } else {
            throw new RuntimeException("Something is wrong with the batches authentication");
        }
    }
}
