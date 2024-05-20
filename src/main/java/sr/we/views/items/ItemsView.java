package sr.we.views.items;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.componentfactory.onboarding.OnboardingStep;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import sr.we.controllers.ItemsController;
import sr.we.controllers.StoresController;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.security.AuthenticatedUser;
import sr.we.views.HelpFunction;
import sr.we.views.MainLayout;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Items")
@Route(value = "items", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "SECTION_OWNER"})
@Uses(Icon.class)
public class ItemsView extends Div implements BeforeEnterObserver, HelpFunction {

    private final ItemsController ItemService;
    private final Filters filters;
    private final StoresController sectionService;
    private final AuthenticatedUser authenticatedUser;
    private final StoresController storesController;
    private final Set<String> linkSections;
    private Grid<Item> grid;
    private Grid.Column<Item> storeColumn;
    private Grid.Column<Item> priceColumn;
    private Grid.Column<Item> inventoryValueColumn;
    private Grid.Column<Item> costColumn;
    private Grid.Column<Item> stockLevelColumn;
    private Grid.Column<Item> itemNameColumn;
    private TextField itemNameFld;
    private IntegerField stockLevelFld;
    private BigDecimalField costFld;
    private BigDecimalField inventoryValueFld;
    private BigDecimalField priceFld;
    private ComboBox<Section> storeFld;
    private User user;
    private List<Section> sections;
    private Grid.Column<Item> sectionColumn;
    private MultiSelectComboBox<String> sectionId;
//    private Grid.Column<Item> stockLevelDate;

    public ItemsView(ItemsController ItemService, StoresController sectionService, AuthenticatedUser authenticatedUser, StoresController storesController) {
        this.ItemService = ItemService;
        this.sectionService = sectionService;
        this.authenticatedUser = authenticatedUser;
        this.storesController = storesController;
        setSizeFull();
        addClassNames("items-view");

        linkSections = authenticatedUser.get().get().getLinkSections();

        filters = new Filters(() -> refreshGrid());
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private long getBusinessId() {
        return 0L;
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER, LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    private Component createGrid() {
        grid = new Grid<>(Item.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

//        stockLevelDate = grid.addColumn(Item::getLastUpdateStockLevel).setHeader("Stock Level date").setFrozen(true).setFlexGrow(1).setResizable(true);
        itemNameColumn = grid.addColumn(i -> i.getVariant().getSku()).setHeader("SKU").setFlexGrow(0).setResizable(true);
        itemNameColumn = grid.addColumn(i -> i.getVariant().getBarcode()).setHeader("Code").setFlexGrow(0).setResizable(true);
        itemNameColumn = grid.addColumn(Item::getItem_name).setHeader("Item name").setFrozen(true).setFlexGrow(1).setResizable(true);
        stockLevelColumn = grid.addColumn(Item::getStock_level).setHeader("Stock level").setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
        costColumn = grid.addColumn(l -> l.getVariant().getCost()).setHeader("Cost").setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
        inventoryValueColumn = grid.addComponentColumn(l -> {
            Span span = new Span(l.getVariant().getCost().multiply(BigDecimal.valueOf(l.getStock_level())).toString());
            if (l.getStock_level() > 0) {
                span.getElement().getThemeList().add("badge success");
            } else if (l.getStock_level() < 0) {
                span.getElement().getThemeList().add("badge error");
            }
            return span;
        }).setHeader("Inventory value").setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
        priceColumn = grid.addColumn(l -> l.getVariant().getDefault_price()).setHeader("Price").setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
        storeColumn = grid.addColumn(l -> {
            String storeId = l.getVariantStore().getStore_id();
            Optional<Section> any = sectionService.allStores(getBusinessId()).stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(storeId)).findAny();
            return any.map(Section::getDefault_name).orElse(storeId);
        }).setHeader("Link Store").setFlexGrow(1);
        sectionColumn = grid.addComponentColumn(r -> {
            String collect1 = getSection(r);
            Span span = new Span(collect1);
            span.getStyle().set("white-space", "pre-line");
            span.getElement().getThemeList().add("badge warning");
            span.setWidthFull();
            return span;
        }).setHeader("Section").setAutoWidth(true);


        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        createGridFilter();

        GridExporter<Item> exporter = GridExporter.createFor(grid);
        exporter.setCsvExportEnabled(false);
        exporter.setDocxExportEnabled(false);
        exporter.setPdfExportEnabled(false);
        exporter.setTitle("Items");
        exporter.setExportValue(inventoryValueColumn, l -> l.getVariant().getCost().multiply(BigDecimal.valueOf(l.getStock_level())));
        exporter.setExportValue(sectionColumn, this::getSection);
        exporter.setFileName("GridExportItems" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
        exporter.setCustomHeader(itemNameColumn, "Item name");
//        exporter.setCustomHeader(stockLevelDate, "Stock level date");
        exporter.setCustomHeader(stockLevelColumn, "Stock level");
        exporter.setCustomHeader(costColumn, "Cost");
        exporter.setCustomHeader(inventoryValueColumn, "Inventory Value");
        exporter.setCustomHeader(priceColumn, "Price");
        exporter.setCustomHeader(storeColumn, "Link Store");
        exporter.setCustomHeader(sectionColumn, "Section");

        return grid;
    }

    private String getSection(Item r) {
        List<Section> collect = sections.stream().filter(l -> ItemsController.linkSection(l.getId(), r.getCategory_id(), r.getForm(), r.getColor(), l)).toList();
        if (collect.size() > 1) {
            collect = collect.stream().filter(l -> !l.isDefault()).toList();
        }
//            sectionMultiSelectComboBox.setValue(collect);
        String collect1 = collect.stream().map(Section::getName).collect(Collectors.joining("\n"));
        return collect1;
    }

    private void createGridFilter() {
        HeaderRow headerRow = grid.appendHeaderRow();

        itemNameFld = new TextField("", "", l -> grid.getDataProvider().refreshAll());
        stockLevelFld = new IntegerField("", null, l -> grid.getDataProvider().refreshAll());
        costFld = new BigDecimalField("", null, l -> grid.getDataProvider().refreshAll());
        inventoryValueFld = new BigDecimalField("", null, l -> grid.getDataProvider().refreshAll());
        priceFld = new BigDecimalField("", null, l -> grid.getDataProvider().refreshAll());
        storeFld = new ComboBox<>(l -> grid.getDataProvider().refreshAll());
        storeFld.setItemLabelGenerator(Section::getDefault_name);
        storeFld.setItems(query -> sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize()).filter(Section::isDefault));
        sectionId = new MultiSelectComboBox<>();
        sectionId.setItemLabelGenerator(label -> {
            Section section = storesController.oneStore(label);
            return section == null ? "Error" : section.getName();
        });
        List<String> sects = storesController.allSections(getBusinessId(), 0, Integer.MAX_VALUE, authenticatedUser.get()).map(Section::getUuId).toList();
        sectionId.setItems(sects);
        sectionId.setValue(sects);
        sectionId.addValueChangeListener(l -> grid.getDataProvider().refreshAll());

        sectionId.setPlaceholder("Filter");
        itemNameFld.setPlaceholder("Filter");
        stockLevelFld.setPlaceholder("Filter");
        costFld.setPlaceholder("Filter");
        inventoryValueFld.setPlaceholder("Filter");
        priceFld.setPlaceholder("Filter");
        storeFld.setPlaceholder("Filter");

        sectionId.setWidthFull();
        itemNameFld.setWidthFull();
        stockLevelFld.setWidthFull();
        costFld.setWidthFull();
        inventoryValueFld.setWidthFull();
        priceFld.setWidthFull();
        storeFld.setWidthFull();

        sectionId.setClearButtonVisible(true);
        itemNameFld.setClearButtonVisible(true);
        stockLevelFld.setClearButtonVisible(true);
        costFld.setClearButtonVisible(true);
        inventoryValueFld.setClearButtonVisible(true);
        priceFld.setClearButtonVisible(true);
        storeFld.setClearButtonVisible(true);

        stockLevelFld.setStepButtonsVisible(true);

        headerRow.getCell(itemNameColumn).setComponent(itemNameFld);
        headerRow.getCell(stockLevelColumn).setComponent(stockLevelFld);
        headerRow.getCell(costColumn).setComponent(costFld);
        headerRow.getCell(inventoryValueColumn).setComponent(inventoryValueFld);
        headerRow.getCell(priceColumn).setComponent(priceFld);
        headerRow.getCell(storeColumn).setComponent(storeFld);
        headerRow.getCell(sectionColumn).setComponent(sectionId);
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            user = maybeUser.get();
            sections = sectionService.allSections(getBusinessId(), 0, Integer.MAX_VALUE, authenticatedUser.get()).toList();
            grid.setItems(query -> ItemService.allItems(getBusinessId(), query.getPage(), query.getPageSize(), sectionId.getValue(), check()));
        }
    }

    private Collection<Criteria> check() {
        List<Criteria> criterias = new ArrayList<>();
//        boolean check = true;
        if (StringUtils.isNotBlank(itemNameFld.getValue())) {
//            check = item.getItem_name().toUpperCase().contains(itemNameFld.getValue().toUpperCase());
            criterias.add(Criteria.where("item_name").regex(".*"+itemNameFld.getValue()+".*"));
        }
        if (storeFld.getValue() != null) {
//            check = item.getVariantStore().getStore_id().equalsIgnoreCase(storeFld.getValue().getId());
            criterias.add(Criteria.where("variantStore.store_id").is(storeFld.getValue().getId()));
        }
        if (stockLevelFld.getValue() != null) {
//            check = item.getStock_level() == stockLevelFld.getValue();
            criterias.add(Criteria.where("stock_level").is(stockLevelFld.getValue()));
        }
        if (costFld.getValue() != null) {
//            check = item.getVariant().getCost().compareTo(costFld.getValue()) == 0;
            criterias.add(Criteria.where("variant.cost").is(costFld.getValue()));
        }
        if (priceFld.getValue() != null) {
//            check = item.getVariant().getDefault_price() != null && item.getVariant().getDefault_price().compareTo(priceFld.getValue()) == 0;
            criterias.add(Criteria.where("variant.default_price").is(priceFld.getValue()));
        }
        if (inventoryValueFld.getValue() != null) {
//            check = item.getVariant().getCost().multiply(BigDecimal.valueOf(item.getStock_level())).compareTo(inventoryValueFld.getValue()) == 0;
//            criterias.add(Criteria.where("item_name").regex(".*ab.*", itemNameFld.getValue()));TODO
        }
//
//        if (check) {
//            check = filters.check(item);
//        }
//
//        if (user.getRoles().contains(Role.SECTION_OWNER)) {
//            String categoryId = item.getCategory_id();
//            VariantStore variantStore = item.getVariantStore();
//            String storeId = variantStore.getStore_id();
//
//            Optional<Section> any = sections.stream().filter(f -> f.getId().compareTo(storeId) == 0 && (f.getCategories() == null || f.getCategories().contains(categoryId))).findAny();
//            if (any.isEmpty()) {
//                check = false;
//            }
//        }
//
//
//        if(check) {
//            Optional<String> any = sectionId.getValue().stream().filter(n -> {
//                boolean containsCatregory = true;
//                Section l = storesController.oneStore(getBusinessId(), n);
////                boolean containsStore = l.getId().equalsIgnoreCase(item.getVariantStore().getStore_id());
////                if (containsStore) {
////
////                    if (l.getCategories() != null && !l.getCategories().isEmpty()) {
////                        // check on item category
////                        containsCatregory = l.getCategories().contains(item.getCategory_id());
////                    }
////                }
////                return containsStore && containsCatregory;
//                return ItemsController.linkSection(l.getId(), item.getCategory_id(), item.getForm(), item.getColor(), l);
//            }).findAny();
//            if (any.isEmpty()) {
//                check = false;
//            }
//        }

        return criterias;
    }

    @Override
    public String help(Onboarding onboarding) {
        OnboardingStep step1 = new OnboardingStep(this);
        step1.setHeader("Introduction to Items view");
        step1.setContent(new Html("""
                <div style="padding: var(--lumo-space-s); background-color: var(--lumo-shade-5pct);">
                              <p>Welcome to the Items view! This section provides a comprehensive overview and filtering system for all items managed within our platform.</p>
                            
                              <p>Here, you can explore a complete list of items available in your inventory. Whether it's products, materials, or equipment, you'll find detailed information for each item, including names, descriptions, prices, and quantities.</p>
                            
                              <p>Utilize our filtering options to streamline your search based on specific criteria such as category, brand, or stock availability. This enables you to efficiently navigate through your inventory and locate items with ease.</p>
                            
                              <p>With the Items view, you can effectively manage and monitor your inventory, ensuring accurate stock levels and facilitating smooth operations for your business.</p>
                            </div>
                """));
        onboarding.addStep(step1);
        return "ItemsHelpFunction";
    }

    public static class Filters extends Div {
        private final Select<String> selector = new Select<>();
        private final DatePicker startDate = new DatePicker("Stock level date");
        private final DatePicker endDate = new DatePicker();

        public Filters(Runnable onSearch) {

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM, LumoUtility.BoxSizing.BORDER);
//

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                startDate.clear();
                endDate.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

//            add(createDateRangeFilter(), actions);
        }

        private Component createDateRangeFilter() {
            selector.setItems("Today", "Yesterday", "This week", "Last week", "This month", "Last month", "Last 7 days", "Last 30 days");
            selector.addValueChangeListener(l -> {
                if (StringUtils.isBlank(l.getValue())) {
                    selector.setValue("Today");
                } else {
                    String value = l.getValue();
                    switch (value) {
                        case "Today" -> {
                            startDate.setValue(LocalDate.now());
                            endDate.setValue(startDate.getValue());
                        }
                        case "Yesterday" -> {
                            startDate.setValue(LocalDate.now().minusDays(1));
                            endDate.setValue(startDate.getValue());
                        }
                        case "This week" -> {
                            LocalDate lastWeekSameDay = LocalDate.now();
                            startDate.setValue(lastWeekSameDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
                            endDate.setValue(startDate.getValue().plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)));
                        }
                        case "Last week" -> {
                            LocalDate lastWeekSameDay = LocalDate.now().minusWeeks(1);
                            startDate.setValue(lastWeekSameDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
                            endDate.setValue(startDate.getValue().plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)));
                        }
                        case "This month" -> {
                            startDate.setValue(YearMonth.now().atDay(1));
                            endDate.setValue(YearMonth.from(startDate.getValue()).atEndOfMonth());
                        }
                        case "Last month" -> {
                            startDate.setValue(YearMonth.now().atDay(1).minusMonths(1));
                            endDate.setValue(YearMonth.from(startDate.getValue()).atEndOfMonth());
                        }
                        case "Last 7 days" -> {
                            startDate.setValue(LocalDate.now().minusWeeks(1));
                            endDate.setValue(LocalDate.now());
                        }
                        case "Last 30 days" -> {
                            startDate.setValue(LocalDate.now().minusDays(30));
                            endDate.setValue(LocalDate.now());
                        }
                    }
                }
            });
            selector.setValue("Today");
            startDate.setPlaceholder("From");

            endDate.setPlaceholder("To");

            // For screen readers
            startDate.setAriaLabel("From date");
            endDate.setAriaLabel("To date");


            startDate.setWidth("166px");
            endDate.setWidth("166px");

            FlexLayout dateRangeComponent = new FlexLayout(selector, new Text(" : "), startDate, new Text(" – "), endDate);
            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            dateRangeComponent.getElement().getStyle().set("justify-self", "self-start");
            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

            return dateRangeComponent;
        }


        public boolean check(Item item) {
            boolean check = true;
//            if (startDate.getValue() != null && endDate.getValue() != null && item.getLastUpdateStockLevel() != null) {
//                LocalDate receiptDate = item.getLastUpdateStockLevel().toLocalDate();
//                if (receiptDate.isBefore(startDate.getValue()) || receiptDate.isAfter(endDate.getValue())) {
//                    check = false;
//                }
//            }
            return check;
        }
    }

}


