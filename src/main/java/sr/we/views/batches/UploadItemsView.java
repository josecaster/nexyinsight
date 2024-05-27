package sr.we.views.batches;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
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
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import sr.we.controllers.ItemsController;
import sr.we.controllers.StoresController;
import sr.we.entity.Batch;
import sr.we.entity.BatchItems;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.security.AuthenticatedUser;
import sr.we.services.BatchItemsService;
import sr.we.services.BatchService;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UploadItemsView extends VerticalLayout {

    private final Grid<BatchItems> grid = new Grid<>(BatchItems.class, false);
    private final Button cancel = new Button("Add new BatchItems ");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<BatchItems> binder;
    private final BatchItemsService batchItemsService;
    private final StoresController storesController;
    private final ItemsController ItemService;
    private final BatchService batchService;
    private final AuthenticatedUser authenticatedUser;
    private final Long batchId;
    private final Batch batch;
    boolean populateForm = false;
    private ComboBox<Item> itemsCmb;
    //    private TextField sku;
    private TextField code;
    private TextField name, description;
    private ToggleButton variableBtn;
    private IntegerField quantity;
    private BigDecimalField cost;
    private BigDecimalField price;
    private BatchItems batchItems;

    public UploadItemsView(ItemsController ItemService, BatchItemsService batchItemsService, BatchService batchService, AuthenticatedUser authenticatedUser, StoresController storesController, Batch batch) {
        this.batchItemsService = batchItemsService;
        this.batchService = batchService;
        this.authenticatedUser = authenticatedUser;
        this.storesController = storesController;
        this.ItemService = ItemService;
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

        HorizontalLayout horizontalLayout = new HorizontalLayout(upload);
        horizontalLayout.setWidthFull();
        Anchor anchor = new Anchor(new StreamResource("template.csv", () -> {


            ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayInputStream);
            try (BufferedWriter writer = new BufferedWriter(outputStreamWriter); CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("SKU", "CODE", "NAME", "DESCRIPTION", "QUANTITY", "VARIABLE", "PRICE", "COST"))) {
                csvPrinter.flush();
                byte[] buf = byteArrayInputStream.toByteArray();
                return new ByteArrayInputStream(buf);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }), "Download .csv template template");
        anchor.getElement().getStyle().set("margin-left", "auto");
        anchor.setTarget(AnchorTarget.BLANK);
        horizontalLayout.add(anchor);
        add(horizontalLayout, splitLayout);

        grid.setHeight("400px");

        // Configure Grid
        Grid.Column<BatchItems> skuColumn = grid.addColumn(BatchItems::getSku).setHeader("SKU").setAutoWidth(true);
        Grid.Column<BatchItems> codeColumn = grid.addColumn(BatchItems::getCode).setHeader("CODE").setAutoWidth(true);
        Grid.Column<BatchItems> nameColumn = grid.addColumn(BatchItems::getName).setHeader("NAME").setAutoWidth(true);
        Grid.Column<BatchItems> quantityColumn = grid.addColumn(BatchItems::getQuantity).setHeader("QUANTITY").setAutoWidth(true);
        Grid.Column<BatchItems> costColumn = grid.addColumn(BatchItems::getCost).setHeader("COST").setAutoWidth(true);
        Grid.Column<BatchItems> variableColumn = grid.addColumn(BatchItems::isOptional).setHeader("VARIABLE PRICE").setAutoWidth(true);
        Grid.Column<BatchItems> priceColumn = grid.addColumn(BatchItems::getPrice).setHeader("PRICE").setAutoWidth(true);
        Grid.Column<BatchItems> actualColumn = grid.addComponentColumn(l -> {
            IntegerField integerField = new IntegerField();
            integerField.setStepButtonsVisible(true);
            integerField.setMin(0);
            integerField.setPlaceholder("Counted amount");
            integerField.setReadOnly(true);
            if (batch.getStatus().compareTo(Batch.Status.VALIDATE_ITEMS) == 0) {
                integerField.setReadOnly(false);
            }
            integerField.setValue(l.getRealQuantity());
            integerField.addValueChangeListener(v -> {
                l.setRealQuantity(v.getValue());
            });
            if(!integerField.isReadOnly()){
                integerField.setReadOnly(!enabled);
            }
            return integerField;
        }).setHeader("COUNTED").setAutoWidth(true);
        Grid.Column<BatchItems> uploadColumn = grid.addComponentColumn(l -> {
            ToggleButton integerField = new ToggleButton(l.isUpload());
            integerField.addValueChangeListener(v -> {
                l.setUpload(v.getValue());
            });
            if(!integerField.isReadOnly()){
                integerField.setReadOnly(!enabled);
            }
            return integerField;
        }).setHeader("UPLOAD").setTooltipGenerator(l -> "Upload will be turned off after already been uploaded. If you turn it on again it will retry the upload").setAutoWidth(true);


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


//        sku.setMaxLength(40);
        code.setMaxLength(128);
        name.setMaxLength(128);

        // Bind fields. This is where you'd define e.g. validation rules
//        binder.forField(sku).withValidator(v -> StringUtils.isNotBlank(sku.getValue()), "SKU is Required").bind(BatchItems::getSku, BatchItems::setSku);
        binder.forField(code).withValidator(v -> StringUtils.isNotBlank(code.getValue()), "CODE is Required").bind(BatchItems::getCode, BatchItems::setCode);
        binder.forField(name).withValidator(v -> StringUtils.isNotBlank(name.getValue()), "NAME is Required").bind(BatchItems::getName, BatchItems::setName);
        binder.forField(price).withValidator(v -> variableBtn.getValue() || (!variableBtn.getValue() && price.getValue() != null), "PRICE is Required").bind(BatchItems::getPrice, BatchItems::setPrice);
        binder.forField(quantity).withValidator(v -> quantity.getValue() != null, "QUANTITY is Required").bind(BatchItems::getQuantity, BatchItems::setQuantity);
//        binder.bindInstanceFields(this);

//        sku.addValueChangeListener(v -> {
//            if (!populateForm) {
//                Optional<Item> any = ItemService.allItems(getBusinessId(), 0, Integer.MAX_VALUE, authenticatedUser.get().get(), l -> {
//                    boolean contains = true;
//                    String s = v.getValue().toUpperCase();
//                    contains = l.getVariant().getSku().contains(s);
//                    return contains;
//                }).findAny();
//                any.ifPresent(item -> itemsCmb.setValue(item));
//            }
//        });

//        sku.setClearButtonVisible(true);
        itemsCmb.setClearButtonVisible(true);
        code.setClearButtonVisible(true);
        name.setClearButtonVisible(true);
        quantity.setClearButtonVisible(true);
        cost.setClearButtonVisible(true);
        price.setClearButtonVisible(true);


        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        cancel.addClickShortcut(Key.ARROW_DOWN);
        save.addClickShortcut(Key.ENTER);

        save.setTooltipText("For faster use, use Enter");
        cancel.setTooltipText("For faster use, use Arrow Down Key");

        save.addClickListener(e -> {
            save(batchItemsService);
        });
    }

    private void save(BatchItemsService batchItemsService) {
        try {

            BinderValidationStatus<BatchItems> validate = binder.validate();
            if (validate.isOk()) {

                if (this.batchItems == null) {
                    this.batchItems = new BatchItems();
                    this.batchItems.setBatchId(batchId);
                }
                binder.writeBean(this.batchItems);
                this.batchItems.setOptional(variableBtn.getValue());
                if (this.batchItems.isOptional()) {
                    this.batchItems.setPrice(null);
                }
                BatchItems update = batchItemsService.update(this.batchItems);
                clearForm();
                refreshGrid();
                populateForm(update);
                Notification.show("Data updated", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } else {
                Notification.show("Failed to update the data. Check again that all values are valid", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        } catch (ObjectOptimisticLockingFailureException exception) {
            Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
            n.setPosition(Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (ValidationException validationException) {
            Notification.show("Failed to update the data. Check again that all values are valid", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private void doForUploadSucceed(MemoryBuffer receiver, Grid<BatchItems> itemImportGrid) {
        try {
            Reader in = new InputStreamReader(receiver.getInputStream());


            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader("SKU", "CODE", "NAME", "DESCRIPTION", "QUANTITY", "VARIABLE", "PRICE", "COST").setSkipHeaderRecord(true).build();

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
                String optional = record.get("VARIABLE");
                String description = record.get("DESCRIPTION");

                Integer quantity1 = StringUtils.isBlank(quantity) ? null : Integer.valueOf(quantity);
                BigDecimal price1 = StringUtils.isBlank(price) ? null : BigDecimal.valueOf(Double.parseDouble(price));
                BigDecimal cost1 = StringUtils.isBlank(cost) ? null : BigDecimal.valueOf(Double.parseDouble(cost));
                boolean optional1 = !StringUtils.isBlank(optional) && Boolean.parseBoolean(optional);

                BatchItems e = new BatchItems(UploadItemsView.this.batchId, sku, code, name, quantity1, price1, cost1, optional1, description);
                if(StringUtils.isNotBlank(sku)) {
                    String itemId = ItemService.sku(sku, batch.getSectionId());
                    e.setItemId(itemId);
                }
                list.add(e);
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

        itemsCmb = new ComboBox<>("ITEM");
//        sku = new TextField("SKU");
        code = new TextField("CODE");
        name = new TextField("NAME");
        description = new TextField("DESCRIPTION");
        variableBtn = new ToggleButton("VARIABLE PRICE");
        quantity = new IntegerField("Quantity");
        cost = new BigDecimalField("COST");
        price = new BigDecimalField("PRICE");

        code.setMaxLength(128);
        name.setMaxLength(64);
        description.setMaxLength(512);


        quantity.setMin(0);

        itemsCmb.setPlaceholder("FOR NEW ITEM LEAVE EMPTY");
        itemsCmb.setItems(query -> ItemService.allItems(getBusinessId(), query.getPage(), query.getPageSize(), authenticatedUser.get().get(), query.getFilter()));
        itemsCmb.setItemLabelGenerator(f -> {
            String name = f.getVariant().getSku() + " - " + f.getItem_name();
            if (f.getVariant() != null) {
                if (StringUtils.isNotBlank(f.getVariant().getOption1_value())) {
                    name += " " + f.getOption1_name() + " (" + f.getVariant().getOption1_value() + ")";
                } else if (StringUtils.isNotBlank(f.getVariant().getOption2_value())) {
                    name += " " + f.getOption2_name() + " (" + f.getVariant().getOption2_value() + ")";
                } else if (StringUtils.isNotBlank(f.getVariant().getOption3_value())) {
                    name += " " + f.getOption3_name() + " (" + f.getVariant().getOption3_value() + ")";
                }
            }
            return name;
        });

        itemsCmb.addValueChangeListener(v -> {
            if (populateForm) {
                return;
            }
            Item value = v.getValue();
            if (this.batchItems == null) {
                this.batchItems = new BatchItems();
                this.batchItems.setBatchId(batchId);
            }
            if (value != null) {
                this.batchItems.setItemId(value.getId());
//                sku.setValue(StringUtils.isNotBlank(value.getVariant().getSku()) ? value.getVariant().getSku() : "");
                description.setValue(StringUtils.isNotBlank(value.getDescription()) ? value.getDescription() : "");
                variableBtn.setValue(!value.getVariant().getDefault_pricing_type().equalsIgnoreCase("FIXED"));
                code.setValue(StringUtils.isNotBlank(value.getVariant().getBarcode()) ? value.getVariant().getBarcode() : "");
                name.setValue(StringUtils.isNotBlank(value.getItem_name()) ? value.getItem_name() : "");
                cost.setValue(value.getVariant().getCost());
                price.setValue(value.getVariant().getDefault_price());
            } else {
                this.batchItems.setItemId(null);
            }
        });

        variableBtn.addValueChangeListener(f -> {
            price.setVisible(!f.getValue());
        });

        formLayout.add(itemsCmb, /*sku,*/ code, name, description, quantity, cost, variableBtn, price);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        if (this.batch.getStatus().compareTo(Batch.Status.UPLOAD_ITEMS) == 0) {
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
        this.populateForm = true;
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
                        itemsCmb.setReadOnly(false);
//                        sku.setReadOnly(false);
                        code.setReadOnly(false);
                        name.setReadOnly(false);
                        description.setRequired(false);
                        variableBtn.setReadOnly(false);
                        quantity.setReadOnly(false);
                        cost.setReadOnly(false);
                        price.setReadOnly(false);
                    }
                }
            }
            if (StringUtils.isNotBlank(value.getItemId())) {
                itemsCmb.setValue(ItemService.id(value.getItemId()));
            }
            variableBtn.setValue(value.isOptional());
        } else {
            // BatchItems Id
            cost.setValue(null);
            variableBtn.setValue(false);
            itemsCmb.clear();
        }
        this.populateForm = false;
    }


    public Grid<BatchItems> getGrid() {
        return grid;
    }

    private boolean enabled = true;

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        refreshGrid();
        save.setEnabled(enabled);
        save.setVisible(enabled);
    }
}
