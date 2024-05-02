package sr.we.views.batches;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.Query;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import sr.we.controllers.StoresController;
import sr.we.entity.Batch;
import sr.we.entity.BatchItems;
import sr.we.security.AuthenticatedUser;
import sr.we.services.BatchItemsService;
import sr.we.services.BatchService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UploadItemsView extends VerticalLayout {

    private final Grid<BatchItems> grid = new Grid<>(BatchItems.class, false);
    private final Button cancel = new Button("Add new BatchItems ");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<BatchItems> binder;
    private final BatchItemsService batchItemsService;
    private final StoresController storesController;
    private final BatchService batchService;
    private final AuthenticatedUser authenticatedUser;
    private final Long batchId;
    private final Batch batch;
    private TextField sku;
    private TextField code;
    private TextField name;
    private IntegerField quantity;
    private BigDecimalField cost;
    private BigDecimalField price;
    private BatchItems batchItems;

    public UploadItemsView(BatchItemsService batchItemsService, BatchService batchService, AuthenticatedUser authenticatedUser, StoresController storesController, Batch batch) {
        this.batchItemsService = batchItemsService;
        this.batchService = batchService;
        this.authenticatedUser = authenticatedUser;
        this.storesController = storesController;
        this.batch = batch;
        this.batchId = this.batch.getId();

        addClassNames("batches-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        MemoryBuffer receiver = new MemoryBuffer();
        Upload upload = new Upload(receiver);
        upload.setVisible(batch.getStatus().compareTo(Batch.Status.UPLOAD_ITEMS) == 0);
        upload.addSucceededListener(n -> {
            doForUploadSucceed(receiver, grid);
        });

        add(upload, splitLayout);

        grid.setHeight("400px");

        // Configure Grid
        Grid.Column<BatchItems> skuColumn = grid.addColumn(BatchItems::getSku).setHeader("SKU").setAutoWidth(true);
        Grid.Column<BatchItems> codeColumn = grid.addColumn(BatchItems::getCode).setHeader("CODE").setAutoWidth(true);
        Grid.Column<BatchItems> nameColumn = grid.addColumn(BatchItems::getName).setHeader("NAME").setAutoWidth(true);
        Grid.Column<BatchItems> quantityColumn = grid.addColumn(BatchItems::getQuantity).setHeader("QUANTITY").setAutoWidth(true);
        Grid.Column<BatchItems> costColumn = grid.addColumn(BatchItems::getCost).setHeader("COST").setAutoWidth(true);
        Grid.Column<BatchItems> priceColumn = grid.addColumn(BatchItems::getPrice).setHeader("PRICE").setAutoWidth(true);
        Grid.Column<BatchItems> actualColumn = grid.addComponentColumn(l -> {
            IntegerField integerField = new IntegerField();
            integerField.setPlaceholder("Counted amount");
            integerField.setReadOnly(true);
            if(batch.getStatus().compareTo(Batch.Status.VALIDATE_ITEMS) == 0){
                integerField.setReadOnly(false);
            }
            integerField.setValue(l.getRealQuantity());
            integerField.addValueChangeListener( v -> {
               l.setRealQuantity(v.getValue());
            });
            return integerField;
        }).setHeader("COUNTED").setAutoWidth(true);


        grid.setItems(batchItemsService.findByBatchId(batchId));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);


        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                populateForm(event.getValue().getId());
            } else {
                clearForm();
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(BatchItems.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.batchItems == null) {
                    this.batchItems = new BatchItems();
                    this.batchItems.setBatchId(batchId);
                }
                binder.writeBean(this.batchItems);
                BatchItems update = batchItemsService.update(this.batchItems);
                clearForm();
                refreshGrid();
                populateForm(update);
                Notification.show("Data updated", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
    }

    private void doForUploadSucceed(MemoryBuffer receiver, Grid<BatchItems> itemImportGrid) {
        try {
            Reader in = new InputStreamReader(receiver.getInputStream());


            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader("SKU", "CODE", "NAME", "QUANTITY", "PRICE", "COST").setSkipHeaderRecord(true).build();

            Iterable<CSVRecord> records = null;
            records = csvFormat.parse(in);

            List<BatchItems> list1 = itemImportGrid.getDataProvider().fetch(new Query<>()).toList();
            List<BatchItems> list = new ArrayList<>(list1);
            for (CSVRecord record : records) {
                String sku = record.get("SKU");
                String code = record.get("CODE");
                String name = record.get("NAME");
                String quantity = record.get("QUANTITY");
                String price = record.get("PRICE");
                String cost = record.get("COST");
                list.add(new BatchItems(UploadItemsView.this.batchId, sku, code, name, Integer.valueOf(quantity), BigDecimal.valueOf(Double.parseDouble(price)), BigDecimal.valueOf(Double.parseDouble(cost))));
            }
            itemImportGrid.setItems(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void populateForm(Long batchItemId) {
        if (batchItemId != null) {
            Optional<BatchItems> batchItems1 = batchItemsService.get(batchItemId);
            if (batchItems1.isPresent()) {
                populateForm(batchItems1.get());
            } else {

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

        sku = new TextField("SKU");
        code = new TextField("CODE");
        name = new TextField("NAME");
        quantity = new IntegerField("Quantity");
        cost = new BigDecimalField("COST");
        price = new BigDecimalField("PRICE");
        formLayout.add(sku, code, name, quantity, cost, price);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        if(this.batch.getStatus().compareTo(Batch.Status.UPLOAD_ITEMS) == 0) {
            splitLayout.addToSecondary(editorLayoutDiv);
        } else {
            splitLayout.addToSecondary(new Span("Nothing to find here"));
            splitLayout.setSplitterPosition(100);
        }


    }

    private Long getBusinessId() {
        return 0L;
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
        grid.setItems(batchItemsService.findByBatchId(batchId));
    }

    private void clearForm() {
        populateForm((BatchItems) null);
    }

    private void populateForm(BatchItems value) {
        this.batchItems = value;
        binder.readBean(this.batchItems);
        if (value != null) {
            // BatchItems Id

            // status
            Optional<Batch> batchOptional = batchService.get(value.getBatchId());
            if (batchOptional.isPresent()) {
                Batch batch = batchOptional.get();
                switch (batch.getStatus()) {
                    case UPLOAD_ITEMS -> {
                        sku.setReadOnly(false);
                        code.setReadOnly(false);
                        name.setReadOnly(false);
                        quantity.setReadOnly(false);
                        cost.setReadOnly(false);
                        price.setReadOnly(false);
                    }
                }
            }
        } else {
            // BatchItems Id
        }
    }


    public Grid<BatchItems> getGrid() {
        return grid;
    }
}
