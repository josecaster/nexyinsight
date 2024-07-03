package sr.we.views.batches;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.*;
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
import sr.we.entity.eclipsestore.tables.Variant;
import sr.we.security.AuthenticatedUser;
import sr.we.services.BatchItemsService;
import sr.we.services.BatchService;
import sr.we.views.components.MyLineAwesome;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;

import static sr.we.entity.Batch.Status.UPLOAD_ITEMS;

public class UploadItemsView extends VerticalLayout {

    public static final String[] COLUMNS = {"SKU", "CODE", "NAME", "DESCRIPTION", "OPTION_1", "OPTION_2", "OPTION_3", "OPTION_VALUE_1", "OPTION_VALUE_2", "OPTION_VALUE_3", "QUANTITY", "VARIABLE", "PRICE", "COST"};
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
    //    private ComboBox<Item> itemsCmb;
    //    private TextField sku;
//    private TextField code;
//    private TextField name, description, optionName1, optionValue1, optionName2, optionValue2, optionName3, optionValue3;
//    private ToggleButton variableBtn;
//    private IntegerField quantity;
//    private BigDecimalField cost;
//    private BigDecimalField price;
    private BatchItems batchItems;
    private boolean enabled = true;
    private List<BatchItems> byBatchId;

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
        HorizontalLayout splitLayout = new HorizontalLayout();
        splitLayout.setWidthFull();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        MemoryBuffer receiver = new MemoryBuffer();
        Upload upload = new Upload(receiver);
        upload.setVisible(batch.getStatus().compareTo(UPLOAD_ITEMS) == 0);
        upload.addSucceededListener(n -> {
            doForUploadSucceed(receiver, grid);
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout(upload);
        horizontalLayout.setWidthFull();
        Anchor anchor = new Anchor(new StreamResource("template.csv", () -> {


            ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayInputStream);
            try (BufferedWriter writer = new BufferedWriter(outputStreamWriter); CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(COLUMNS))) {
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
        add(/*horizontalLayout, new Hr(),*/cancel, splitLayout);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setHeight("400px");

        // Configure Grid
        Grid.Column<BatchItems> skuColumn = grid.addComponentColumn(l -> {
            ComboBox<Item> itemsCmb = new ComboBox<>();
            itemsCmb.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
            itemsCmb.setPlaceholder("FOR NEW ITEM LEAVE EMPTY");
            itemsCmb.setItems(query -> ItemService.allItems(getBusinessId(), query.getPage(), query.getPageSize(), Set.of(batch.getSectionId()), query.getFilter()));
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
                return name + "["+f.getStock_level()+"]";
            });
            if (StringUtils.isNotBlank(l.getItemId())) {
                itemsCmb.setValue(ItemService.id(l.getItemId()));
            }
            itemsCmb.addValueChangeListener(v -> {
                if (populateForm) {
                    return;
                }
                Item value = v.getValue();
//                if (this.batchItems == null) {
//                    this.batchItems = new BatchItems();
//                    this.batchItems.setBatchId(batchId);
//                }
                if (value != null) {
                    l.setItemId(value.getUuId());
                    l.setSku(StringUtils.isNotBlank(value.getVariant().getSku()) ? value.getVariant().getSku() : "");
                    l.setDescription(StringUtils.isNotBlank(value.getDescription()) ? value.getDescription() : "");
                    l.setOptional(!value.getVariant().getDefault_pricing_type().equalsIgnoreCase("FIXED"));
                    l.setCode(StringUtils.isNotBlank(value.getVariant().getBarcode()) ? value.getVariant().getBarcode() : "");
                    l.setName(StringUtils.isNotBlank(value.getItem_name()) ? value.getItem_name() : "");
                    l.setCost(value.getVariant().getCost());
                    l.setPrice(value.getVariantStore().getPrice());
                    l.setOptionName1(StringUtils.isBlank(value.getOption1_name()) ? "" : value.getOption1_name());
                    l.setOptionName2(StringUtils.isBlank(value.getOption2_name()) ? "" : value.getOption2_name());
                    l.setOptionName3(StringUtils.isBlank(value.getOption3_name()) ? "" : value.getOption3_name());
                    Variant variant = value.getVariant();
                    if (variant != null) {
                        l.setOptionValue1(StringUtils.isBlank(variant.getOption1_value()) ? "" : variant.getOption1_value());
                        l.setOptionValue2(StringUtils.isBlank(variant.getOption2_value()) ? "" : variant.getOption2_value());
                        l.setOptionValue3(StringUtils.isBlank(variant.getOption3_value()) ? "" : variant.getOption3_value());
                    }
                } else {
                    l.setItemId(null);
                }
                grid.getDataProvider().refreshItem(l);
            });


            itemsCmb.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
            itemsCmb.setClearButtonVisible(true);
            return itemsCmb;
        }).setHeader("Existing item").setAutoWidth(true);
        Grid.Column<BatchItems> codeColumn = grid.addComponentColumn(f -> {
            TextField code = new TextField();
            code.addThemeVariants(TextFieldVariant.LUMO_SMALL);
            code.setMaxLength(128);
            code.setClearButtonVisible(true);
            code.setValue(StringUtils.isBlank(f.getCode()) ? "" : f.getCode());
            code.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
            code.addValueChangeListener(l -> {
                f.setCode(l.getValue());
            });
            return code;
        }).setHeader("CODE").setAutoWidth(true);
        Grid.Column<BatchItems> nameColumn = grid.addComponentColumn(f -> {
            TextField name = new TextField();
            name.addThemeVariants(TextFieldVariant.LUMO_SMALL);
            name.setMaxLength(64);
            name.setClearButtonVisible(true);
            name.setValue(StringUtils.isBlank(f.getName()) ? "" : f.getName());
            name.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
            name.addValueChangeListener(l -> {
                f.setName(l.getValue());
            });
            return name;
        }).setHeader("NAME").setAutoWidth(true);
        Grid.Column<BatchItems> descriptionColumn = grid.addComponentColumn(f -> {
            TextField description = new TextField();
            description.addThemeVariants(TextFieldVariant.LUMO_SMALL);
            description.setMaxLength(255);
            description.setClearButtonVisible(true);
            description.setValue(StringUtils.isBlank(f.getDescription()) ? "" : f.getDescription());
            description.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
            description.addValueChangeListener(l -> {
                f.setDescription(l.getValue());
            });
            return description;
        }).setHeader("DESCRIPTION").setAutoWidth(true);
//        Grid.Column<BatchItems> optionName1Column = grid.addComponentColumn(f -> {
//            TextField option = new TextField();
//            option.addThemeVariants(TextFieldVariant.LUMO_SMALL);
//            option.setMaxLength(16);
//            option.setClearButtonVisible(true);
//            option.setValue(StringUtils.isBlank(f.getOptionName1()) ? "" : f.getOptionName1());
//            option.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
//            option.addValueChangeListener(l -> {
//                f.setOptionName1(l.getValue());
//            });
//            return option;
//        }).setHeader("OPTION 1").setAutoWidth(true);
//        Grid.Column<BatchItems> optionValue1Column = grid.addComponentColumn(f -> {
//            TextField option = new TextField();
//            option.addThemeVariants(TextFieldVariant.LUMO_SMALL);
//            option.setMaxLength(20);
//            option.setClearButtonVisible(true);
//            option.setValue(StringUtils.isBlank(f.getOptionValue1()) ? "" : f.getOptionValue1());
//            option.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
//            option.addValueChangeListener(l -> {
//                f.setOptionValue1(l.getValue());
//            });
//            return option;
//        }).setHeader("VALUE 1").setAutoWidth(true);
//
//        Grid.Column<BatchItems> optionName2Column = grid.addComponentColumn(f -> {
//            TextField option = new TextField();
//            option.addThemeVariants(TextFieldVariant.LUMO_SMALL);
//            option.setMaxLength(16);
//            option.setClearButtonVisible(true);
//            option.setValue(StringUtils.isBlank(f.getOptionName2()) ? "" : f.getOptionName2());
//            option.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
//            option.addValueChangeListener(l -> {
//                f.setOptionName2(l.getValue());
//            });
//            return option;
//        }).setHeader("OPTION 2").setAutoWidth(true);
//
//        Grid.Column<BatchItems> optionValue2Column = grid.addComponentColumn(f -> {
//            TextField option = new TextField();
//            option.addThemeVariants(TextFieldVariant.LUMO_SMALL);
//            option.setMaxLength(20);
//            option.setClearButtonVisible(true);
//            option.setValue(StringUtils.isBlank(f.getOptionValue2()) ? "" : f.getOptionValue2());
//            option.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
//            option.addValueChangeListener(l -> {
//                f.setOptionValue2(l.getValue());
//            });
//            return option;
//        }).setHeader("VALUE 2").setAutoWidth(true);
//
//        Grid.Column<BatchItems> optionName3Column = grid.addComponentColumn(f -> {
//            TextField option = new TextField();
//            option.addThemeVariants(TextFieldVariant.LUMO_SMALL);
//            option.setMaxLength(16);
//            option.setClearButtonVisible(true);
//            option.setValue(StringUtils.isBlank(f.getOptionName3()) ? "" : f.getOptionName3());
//            option.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
//            option.addValueChangeListener(l -> {
//                f.setOptionName3(l.getValue());
//            });
//            return option;
//        }).setHeader("OPTION 3").setAutoWidth(true);
//
//        Grid.Column<BatchItems> optionValue3Column = grid.addComponentColumn(f -> {
//            TextField option = new TextField();
//            option.addThemeVariants(TextFieldVariant.LUMO_SMALL);
//            option.setMaxLength(20);
//            option.setClearButtonVisible(true);
//            option.setValue(StringUtils.isBlank(f.getOptionValue3()) ? "" : f.getOptionValue3());
//            option.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
//            option.addValueChangeListener(l -> {
//                f.setOptionValue3(l.getValue());
//            });
//            return option;
//        }).setHeader("VALUE 3").setAutoWidth(true);


        Grid.Column<BatchItems> quantityColumn = grid.addComponentColumn(f -> {
            IntegerField quantity = new IntegerField();
            quantity.addThemeVariants(TextFieldVariant.LUMO_SMALL);
            quantity.setStepButtonsVisible(true);
            quantity.setClearButtonVisible(true);
            quantity.setValue(f.getQuantity());
            quantity.setMin(0);
            quantity.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
            quantity.addValueChangeListener(l -> {
                f.setQuantity(l.getValue());
            });
            return quantity;
        }).setHeader("QUANTITY").setAutoWidth(true);
        Grid.Column<BatchItems> costColumn = grid.addComponentColumn(f -> {
            BigDecimalField cost = new BigDecimalField();
            cost.addThemeVariants(TextFieldVariant.LUMO_SMALL);
            cost.setClearButtonVisible(true);
            cost.setValue(f.getCost());
            cost.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
            cost.addValueChangeListener(l -> {
                f.setCost(l.getValue());
            });
            return cost;
        }).setHeader("COST").setAutoWidth(true);
        Grid.Column<BatchItems> variableColumn = grid.addComponentColumn(f -> {
            ToggleButton variableBtn = new ToggleButton();
            variableBtn.setValue(f.isOptional());
            variableBtn.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
            variableBtn.addValueChangeListener(l -> {
                f.setOptional(l.getValue());
                f.setPrice(null);
                grid.getDataProvider().refreshItem(f);
            });
            return variableBtn;
        }).setHeader("VARIABLE PRICE").setAutoWidth(true);
        Grid.Column<BatchItems> priceColumn = grid.addComponentColumn(f -> {
            BigDecimalField price = new BigDecimalField();
            price.addThemeVariants(TextFieldVariant.LUMO_SMALL);
            price.setClearButtonVisible(true);
            price.setValue(f.getPrice());
            price.setReadOnly(f.isOptional());
            if(!price.isReadOnly()){
                price.setReadOnly(batch.getStatus().compareTo(UPLOAD_ITEMS) != 0);
            }
            price.addValueChangeListener(l -> {
                f.setPrice(l.getValue());
            });
            return price;
        }).setHeader("PRICE").setAutoWidth(true);
        Grid.Column<BatchItems> actualColumn = grid.addComponentColumn(l -> {
            IntegerField integerField = new IntegerField();
            integerField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
            integerField.setStepButtonsVisible(true);
            integerField.setMin(0);
            integerField.setPlaceholder("Counted amount");
            integerField.setReadOnly(batch.getStatus().compareTo(Batch.Status.VALIDATE_ITEMS) != 0);
            integerField.setValue(l.getRealQuantity());
            integerField.addValueChangeListener(v -> {
                l.setRealQuantity(v.getValue());
            });
            if (!integerField.isReadOnly()) {
                integerField.setReadOnly(!enabled);
            }
            return integerField;
        }).setHeader("COUNTED").setAutoWidth(true);
        Grid.Column<BatchItems> uploadColumn = grid.addComponentColumn(l -> {
            ToggleButton integerField = new ToggleButton(l.isUpload());
            integerField.addValueChangeListener(v -> {
                l.setUpload(v.getValue());
            });
            if (!integerField.isReadOnly()) {
                integerField.setReadOnly(!enabled);
            }
            return integerField;
        }).setHeader("UPLOAD").setTooltipGenerator(l -> "Upload will be turned off after already been uploaded. If you turn it on again it will retry the upload").setAutoWidth(true);


        Grid.Column<BatchItems> deleteColumn = grid.addComponentColumn(f -> {
            Button deleteBtn = new Button();
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_ICON);
            deleteBtn.setIcon(MyLineAwesome.TRASH_SOLID.create());
            deleteBtn.setEnabled(batch.getStatus().compareTo(Batch.Status.VALIDATE_ITEMS) == 0 || batch.getStatus().compareTo(UPLOAD_ITEMS) == 0);
            deleteBtn.addClickListener(l -> {
                if(f.getId() != null){
                    batchItemsService.delete(f.getId());
                }
                byBatchId.remove(f);
                grid.getDataProvider().refreshAll();
            });
            return deleteBtn;
        }).setHeader("Remove").setAutoWidth(true);

        byBatchId = batchItemsService.findByBatchId(batchId);
        grid.setItems(byBatchId);
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

        // Bind fields. This is where you'd define e.g. validation rules
//        binder.forField(sku).withValidator(v -> StringUtils.isNotBlank(sku.getValue()), "SKU is Required").bind(BatchItems::getSku, BatchItems::setSku);
//        binder.forField(code).withValidator(v -> StringUtils.isNotBlank(code.getValue()), "CODE is Required").bind(BatchItems::getCode, BatchItems::setCode);
//        binder.forField(name).withValidator(v -> StringUtils.isNotBlank(name.getValue()), "NAME is Required").bind(BatchItems::getName, BatchItems::setName);
//        binder.forField(price).withValidator(v -> variableBtn.getValue() || (!variableBtn.getValue() && price.getValue() != null), "PRICE is Required").bind(BatchItems::getPrice, BatchItems::setPrice);
//        binder.forField(quantity).withValidator(v -> quantity.getValue() != null, "QUANTITY is Required").bind(BatchItems::getQuantity, BatchItems::setQuantity);
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


//        quantity.setClearButtonVisible(true);
//        cost.setClearButtonVisible(true);
//        price.setClearButtonVisible(true);


        cancel.addClickListener(e -> {
            clearForm();
//            refreshGrid();
//            grid.getDataProvider().refreshAll();
//            List<BatchItems> byBatchId = batchItemsService.findByBatchId(batchId);
            BatchItems e1 = new BatchItems();
            e1.setBatchId(batchId);
            byBatchId.add(0,e1);
            grid.setItems(byBatchId);
        });

        cancel.addClickShortcut(Key.ARROW_DOWN);
        save.addClickShortcut(Key.ENTER);

        save.setTooltipText("For faster use, use Enter");
        cancel.setTooltipText("For faster use, use Arrow Down Key");

//        save.addClickListener(e -> {
//            save(batchItemsService);
//        });
    }

//    private void save(BatchItemsService batchItemsService) {
//        try {
//
//            BinderValidationStatus<BatchItems> validate = binder.validate();
//            if (validate.isOk()) {
//
//                if (this.batchItems == null) {
//                    this.batchItems = new BatchItems();
//                    this.batchItems.setBatchId(batchId);
//                }
//                binder.writeBean(this.batchItems);
////                this.batchItems.setOptional(variableBtn.getValue());
//                if (this.batchItems.isOptional()) {
//                    this.batchItems.setPrice(null);
//                }
//                BatchItems update = batchItemsService.update(this.batchItems);
//                clearForm();
//                refreshGrid();
//                populateForm(update);
//                Notification.show("Data updated", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//
//            } else {
//                Notification.show("Failed to update the data. Check again that all values are valid", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
//            }
//        } catch (ObjectOptimisticLockingFailureException exception) {
//            Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
//            n.setPosition(Position.MIDDLE);
//            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
//        } catch (ValidationException validationException) {
//            Notification.show("Failed to update the data. Check again that all values are valid", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
//        }
//    }

    private void doForUploadSucceed(MemoryBuffer receiver, Grid<BatchItems> itemImportGrid) {
        try {
            Reader in = new InputStreamReader(receiver.getInputStream());


            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(COLUMNS).setSkipHeaderRecord(true).build();

            Iterable<CSVRecord> records = null;
            records = csvFormat.parse(in);

            List<BatchItems> list1 = itemImportGrid.getDataProvider().fetch(new Query<>()).toList();
            List<BatchItems> list = new ArrayList<>(list1);
            for (CSVRecord record : records) {
                String sku = record.get("SKU");
                String code = record.get("CODE");
                String name = record.get("NAME");
                String description = record.get("DESCRIPTION");
                String option1 = record.get("OPTION_1");
                String optionValue1 = record.get("OPTION_VALUE_1");
                String option2 = record.get("OPTION_2");
                String optionValue2 = record.get("OPTION_VALUE_2");
                String option3 = record.get("OPTION_3");
                String optionValue3 = record.get("OPTION_VALUE_3");
                String quantity = record.get("QUANTITY");
                String price = record.get("PRICE");
                String cost = record.get("COST");
                String optional = record.get("VARIABLE");

                Integer quantity1 = StringUtils.isBlank(quantity) ? null : Integer.valueOf(quantity);
                BigDecimal price1 = StringUtils.isBlank(price) ? null : BigDecimal.valueOf(Double.parseDouble(price));
                BigDecimal cost1 = StringUtils.isBlank(cost) ? null : BigDecimal.valueOf(Double.parseDouble(cost));
                boolean optional1 = !StringUtils.isBlank(optional) && Boolean.parseBoolean(optional);

                BatchItems e = new BatchItems(UploadItemsView.this.batchId, sku, code, name, quantity1, price1, cost1, optional1, description, option1, optionValue1, option2, optionValue2, option3, optionValue3);
                if (StringUtils.isNotBlank(sku)) {
                    String itemId = ItemService.sku(sku, batch.getSectionId());
                    if (StringUtils.isBlank(itemId)) {
                        e.setSku(null);
                    } else {
                        e.setItemId(itemId);
                    }
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

    private void createEditorLayout(HorizontalLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();


//        sku = new TextField("SKU");


//        description = new TextField("DESCRIPTION");
////        optionName1, optionValue1,optionName2, optionValue2,optionName3, optionValue3
//        optionName1 = new TextField("OPTION 1");
//        optionValue1 = new TextField("OPTION VALUE 1");
//        optionName2 = new TextField("OPTION 2");
//        optionValue2 = new TextField("OPTION VALUE 2");
//        optionName3 = new TextField("OPTION 3");
//        optionValue3 = new TextField("OPTION VALUE 3");
//        variableBtn = new ToggleButton("VARIABLE PRICE");
//        quantity = new IntegerField("Quantity");
//        cost = new BigDecimalField("COST");
//        price = new BigDecimalField("PRICE");
//
//        optionName1.setMaxLength(16);
//        optionName2.setMaxLength(16);
//        optionName3.setMaxLength(16);
//
//        optionName1.setPlaceholder("for example \"Size\"");
//        optionName2.setPlaceholder("for example \"Color\"");
//        optionName3.setPlaceholder("for example \"Material\"");
//
//        optionValue1.setMaxLength(20);
//        optionValue2.setMaxLength(20);
//        optionValue3.setMaxLength(20);
//
//
//
//
//
//
//        variableBtn.addValueChangeListener(f -> {
//            price.setVisible(!f.getValue());
//        });
//
//        formLayout.add(itemsCmb, /*sku,*/ code, name, description, optionName1, optionValue1, optionName2, optionValue2, optionName3, optionValue3, quantity, cost, variableBtn, price);

        editorDiv.add(new Scroller(formLayout));
        createButtonLayout(editorLayoutDiv);

//        if (this.batch.getStatus().compareTo(UPLOAD_ITEMS) == 0) {
//            splitLayout.addToSecondary(editorLayoutDiv);
//        } else {
//            splitLayout.addToSecondary(new Span("Nothing to find here"));
//            splitLayout.setSplitterPosition(100);
//        }


    }

    private Long getBusinessId() {
        return 0L;
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save/*, cancel*/);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(HorizontalLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.add(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
        byBatchId = batchItemsService.findByBatchId(batchId);
        grid.setItems(byBatchId);
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
//            Optional<Batch> batchOptional = batchService.get(value.getBatchId());
//            if (batchOptional.isPresent()) {


//            variableBtn.setValue(value.isOptional());
        } else {
            // BatchItems Id
//            cost.setValue(null);
//            variableBtn.setValue(false);
//            itemsCmb.clear();
        }
        this.populateForm = false;
    }

    public Grid<BatchItems> getGrid() {
        return grid;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        refreshGrid();
        save.setEnabled(enabled);
        save.setVisible(enabled);
    }
}
