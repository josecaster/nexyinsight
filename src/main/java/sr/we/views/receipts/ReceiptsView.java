package sr.we.views.receipts;

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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import sr.we.controllers.ItemsController;
import sr.we.controllers.ReceiptsController;
import sr.we.controllers.StoresController;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Receipt;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.security.AuthenticatedUser;
import sr.we.views.HelpFunction;
import sr.we.views.MainLayout;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@PageTitle("Receipts")
@Route(value = "receipts", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "SECTION_OWNER"})
@Uses(Icon.class)
public class ReceiptsView extends Div implements HelpFunction {

    private static List<Section> sections;
    private static User user;
    private final ReceiptsController receiptService;
    private final StoresController sectionService;
    private final AuthenticatedUser authenticatedUser;
    private final Filters filters;
    private final StoresController storesController;
    private Grid<Receipt> grid;
    private TextField receiptNumberFld;
    private BigDecimalField costFld;
    private BigDecimalField discountFld;
    private BigDecimalField totalFld;
    private BigDecimalField grossTotalFld;
    private MultiSelectComboBox<String> sectionId;
    private Select<Type> typeFld;
    private Set<String> linkSections;
    private TextField itemFld;
    private Grid.Column<Receipt> receiptNumberColumn;
    private Grid.Column<Receipt> storeColumn;
    private Grid.Column<Receipt> itemColumn;
    private Grid.Column<Receipt> sectionColumn;
    private Grid.Column<Receipt> costColumn;
    private Grid.Column<Receipt> typeColumn;
    private Grid.Column<Receipt> discountColumn;
    private Grid.Column<Receipt> totalColumn;
    private Grid.Column<Receipt> grossTotalColumn;
    private Grid.Column<Receipt> createDateColumn;
    private ComboBox<Section> storeFld;

    public ReceiptsView(StoresController sectionService, ReceiptsController ReceiptService, AuthenticatedUser authenticatedUser, StoresController storesController) {
        this.receiptService = ReceiptService;
        this.sectionService = sectionService;
        this.authenticatedUser = authenticatedUser;
        this.storesController = storesController;
        setSizeFull();
        addClassNames("items-view");

        filters = new Filters(this::refreshGrid);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private static long getBusinessId() {
        return 0L;
    }

    private static List<Section> getSections(Receipt r) {
        List<Section> collect = sections.stream().filter(l -> {

//            boolean containsDevice = true;
//            boolean containsCatregory = true;
//            boolean containsStore = l.getId().equalsIgnoreCase(r.getStore_id());
//            if (containsStore) {
//                if (l.getDevices() != null && !l.getDevices().isEmpty()) {
//                    // check on pos device
//                    containsDevice = l.getDevices().contains(r.getPos_device_id());
//                }
//
//                if (containsDevice && l.getCategories() != null && !l.getCategories().isEmpty()) {
//                    // check on item category
//                    containsCatregory = l.getCategories().contains(r.getCategory_id());
//                }
//            }
//            return containsStore && containsDevice && containsCatregory;
            return ItemsController.linkSection(l.getId(), r.getCategory_id(), r.getForm(), r.getColor(), l);
        }).toList();
        if (collect.size() > 1) {
            collect = collect.stream().filter(l -> !l.isDefault()).toList();
        }
        return collect;
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
        grid = new Grid<>(Receipt.class, false);
        createDateColumn = grid.addColumn(Receipt::getReceipt_date).setHeader("Create date").setAutoWidth(true).setResizable(true);
        receiptNumberColumn = grid.addColumn(r -> {
            String[] number = r.getReceipt_number().split("-");
            return number[0] + "-" + number[1];
        });
        receiptNumberColumn.setHeader("Receipt #").setAutoWidth(true).setResizable(true);
        typeColumn = grid.addColumn(Receipt::getReceipt_type).setHeader("Type").setAutoWidth(true);
        storeColumn = grid.addColumn(l -> {
            String storeId = l.getStore_id();
            Optional<Section> any = sectionService.allStores(getBusinessId()).stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(storeId)).findAny();
            return any.map(Section::getDefault_name).orElse(storeId);
        }).setHeader("Store").setAutoWidth(true);
        itemColumn = grid.addColumn(r -> r.getLine_item().getItem_name()).setHeader("Item").setAutoWidth(true);
        sectionColumn = grid.addComponentColumn(r -> {

            List<Section> collect = getSections(r);
            Span span = new Span(collect.stream().map(Section::getName).collect(Collectors.joining("\n")));
            span.getStyle().set("white-space", "pre-line");
            span.getElement().getThemeList().add("badge warning");
            span.setWidthFull();
            return span;
        }).setHeader("Section").setAutoWidth(true);
        costColumn = grid.addColumn(r -> r.getLine_item().getCost_total()).setHeader("Cost").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        discountColumn = grid.addColumn(r -> r.getLine_item().getTotal_discount()).setHeader("Discount").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        totalColumn = grid.addColumn(r -> r.getLine_item().getTotal_money()).setHeader("Total").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        grossTotalColumn = grid.addColumn(r -> r.getLine_item().getGross_total_money()).setHeader("Gross Total").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);


        sections = sectionService.allStores(getBusinessId());
        Optional<User> userOptional = authenticatedUser.get();
        userOptional.ifPresent(value -> user = value);
        grid.setItems(query -> receiptService.allReceipts(getBusinessId(), query.getPage(), query.getPageSize(), filters::check));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        createGridFilter();

        GridExporter<Receipt> exporter = GridExporter.createFor(grid);
        exporter.setCsvExportEnabled(false);
        exporter.setDocxExportEnabled(false);
        exporter.setPdfExportEnabled(false);
        exporter.setTitle("Receipts");
        exporter.setExportValue(sectionColumn, r -> {
            List<Section> collect = getSections(r);
            return collect.stream().map(Section::getName).collect(Collectors.joining("\n"));
        });
//        headerRow.getCell(receiptNumberColumn).setComponent(receiptNumberFld);
//        headerRow.getCell(typeColumn).setComponent(typeFld);
//        headerRow.getCell(itemColumn).setComponent(itemFld);
//        headerRow.getCell(sectionColumn).setComponent(sectionId);
//        headerRow.getCell(costColumn).setComponent(costFld);
//        headerRow.getCell(discountColumn).setComponent(discountFld);
//        headerRow.getCell(totalColumn).setComponent(totalFld);
//        headerRow.getCell(grossTotalColumn).setComponent(grossTotalFld);
        exporter.setCustomHeader(createDateColumn, "Create date");
        exporter.setCustomHeader(receiptNumberColumn, "Receipt #");
        exporter.setCustomHeader(storeColumn, "Store");
        exporter.setCustomHeader(typeColumn, "Type");
        exporter.setCustomHeader(itemColumn, "Item");
        exporter.setCustomHeader(sectionColumn, "Section");
        exporter.setCustomHeader(costColumn, "Cost");
        exporter.setCustomHeader(discountColumn, "Discount");
        exporter.setCustomHeader(totalColumn, "Total");
        exporter.setCustomHeader(grossTotalColumn, "Gross Total");
        exporter.setFileName("GridExportReceipts" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));

        return grid;
    }

    @Override
    public String help(Onboarding onboarding) {
        OnboardingStep step1 = new OnboardingStep(this);
        step1.setHeader("Introduction to Receipts view");
        step1.setContent(new Html("""
                <div style="padding: var(--lumo-space-s); background-color: var(--lumo-shade-5pct);">
                        <p>Welcome to the Receipts module! This module serves as a comprehensive view and filtering system for all receipts synced with our backend system.</p>
                      
                        <p>In this view, you'll have access to all the receipts that are seamlessly integrated with our platform. Whether it's sales, returns, or exchanges, you'll find a detailed overview of every transaction.</p>
                      
                        <p>Utilize our filtering options to narrow down your search based on specific criteria such as date range, payment method, or customer details. This allows for quick and efficient access to the information you need.</p>
                      
                        <p>With the Receipts module, you can effortlessly track and analyze your transaction history, helping you make informed decisions to optimize your business operations.</p>
                      </div>
                """));
        onboarding.addStep(step1);
        return "ReceiptsHelpFunction";
    }

    private void createGridFilter() {
        HeaderRow headerRow = grid.appendHeaderRow();

        receiptNumberFld = new TextField("", "", l -> grid.getDataProvider().refreshAll());
        itemFld = new TextField("", "", l -> grid.getDataProvider().refreshAll());
        typeFld = new Select<Type>();
        costFld = new BigDecimalField("", null, l -> grid.getDataProvider().refreshAll());
        discountFld = new BigDecimalField("", null, l -> grid.getDataProvider().refreshAll());
        totalFld = new BigDecimalField("", null, l -> grid.getDataProvider().refreshAll());
        grossTotalFld = new BigDecimalField("", null, l -> grid.getDataProvider().refreshAll());
        storeFld = new ComboBox<>(l -> grid.getDataProvider().refreshAll());
        storeFld.setItemLabelGenerator(Section::getDefault_name);
        storeFld.setItems(query -> sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize(), f -> true).filter(Section::isDefault));
        sectionId = new MultiSelectComboBox<>();
        sectionId.setItemLabelGenerator(label -> {
            Section section = storesController.oneStore(getBusinessId(), label);
            return section == null ? "Error" : section.getName();
        });
        List<String> sects = storesController.allSections(getBusinessId(), 0, Integer.MAX_VALUE, f -> {
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

        }).map(Section::getUuId).toList();
        sectionId.setItems(sects);
        sectionId.setValue(sects);
        sectionId.addValueChangeListener(l -> grid.getDataProvider().refreshAll());

        storeFld.setPlaceholder("Filter");
        sectionId.setPlaceholder("Filter");
        receiptNumberFld.setPlaceholder("Filter");
        typeFld.setPlaceholder("Filter");
        costFld.setPlaceholder("Filter");
        discountFld.setPlaceholder("Filter");
        totalFld.setPlaceholder("Filter");
        grossTotalFld.setPlaceholder("Filter");
        itemFld.setPlaceholder("Filter");

        storeFld.setWidthFull();
        sectionId.setWidthFull();
        receiptNumberFld.setWidthFull();
        typeFld.setWidthFull();
        costFld.setWidthFull();
        discountFld.setWidthFull();
        totalFld.setWidthFull();
        grossTotalFld.setWidthFull();
        itemFld.setWidthFull();

        storeFld.setClearButtonVisible(true);
        sectionId.setClearButtonVisible(true);
        receiptNumberFld.setClearButtonVisible(true);
        costFld.setClearButtonVisible(true);
        discountFld.setClearButtonVisible(true);
        totalFld.setClearButtonVisible(true);
        grossTotalFld.setClearButtonVisible(true);
        itemFld.setClearButtonVisible(true);

        typeFld.setItems(Type.ALL, Type.SALE, Type.REFUND);
        typeFld.setValue(Type.ALL);
        typeFld.addValueChangeListener(l -> grid.getDataProvider().refreshAll());

        headerRow.getCell(storeColumn).setComponent(storeFld);
        headerRow.getCell(receiptNumberColumn).setComponent(receiptNumberFld);
        headerRow.getCell(typeColumn).setComponent(typeFld);
        headerRow.getCell(itemColumn).setComponent(itemFld);
        headerRow.getCell(sectionColumn).setComponent(sectionId);
        headerRow.getCell(costColumn).setComponent(costFld);
        headerRow.getCell(discountColumn).setComponent(discountFld);
        headerRow.getCell(totalColumn).setComponent(totalFld);
        headerRow.getCell(grossTotalColumn).setComponent(grossTotalFld);
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    public enum Type {
        ALL, SALE, REFUND
    }

    public class Filters extends Div {
        private final Select<String> selector = new Select<>();
        private final DatePicker startDate = new DatePicker("Receipt date");
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
//                name.clear();
//                phone.clear();
//                occupations.clear();
//                roles.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(/*name, phone, */createDateRangeFilter()/*, occupations, roles*/, actions);
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


        public boolean check(Receipt receipt) {
            boolean check = true;
            if (startDate.getValue() != null && endDate.getValue() != null) {
                LocalDate receiptDate = receipt.getReceipt_date().toLocalDate();
                if (receiptDate.isBefore(startDate.getValue()) || receiptDate.isAfter(endDate.getValue())) {
                    check = false;
                }
            }
            if (StringUtils.isNotBlank(itemFld.getValue())) {
                check = receipt.getLine_item().getItem_name().toUpperCase().contains(itemFld.getValue().toUpperCase());
            }
            if (check && storeFld.getValue() != null) {
                check = receipt.getStore_id().equalsIgnoreCase(storeFld.getValue().getId());
            }
            if (check && receiptNumberFld.getValue() != null) {
                check = receipt.getReceipt_number().contains(receiptNumberFld.getValue());
            }
            if (check && typeFld.getValue() != null && typeFld.getValue().compareTo(Type.ALL) != 0) {
                check = receipt.getReceipt_type().equalsIgnoreCase(typeFld.getValue().name());
            }
            if (check && costFld.getValue() != null) {
                check = receipt.getLine_item().getCost().compareTo(costFld.getValue()) == 0;
            }
            if (check && totalFld.getValue() != null) {
                check = receipt.getLine_item().getTotal_money().compareTo(totalFld.getValue()) == 0;
            }
            if (check && grossTotalFld.getValue() != null) {
                check = receipt.getLine_item().getGross_total_money().compareTo(grossTotalFld.getValue()) == 0;
            }
            if (check && discountFld.getValue() != null) {
                check = receipt.getLine_item().getTotal_discount().compareTo(discountFld.getValue()) == 0;
            }
            if (check && user.getRoles().contains(Role.SECTION_OWNER)) {

                Optional<Section> any = sections.stream().filter(l -> {
//                    boolean containsDevice = true;
//                    boolean containsCatregory = true;
//                    boolean containsStore = l.getId().equalsIgnoreCase(receipt.getStore_id());
//                    if (containsStore) {
//                        if (l.getDevices() != null && !l.getDevices().isEmpty()) {
//                            // check on pos device
//                            containsDevice = l.getDevices().contains(receipt.getPos_device_id());
//                        }
//
//                        if (containsDevice && l.getCategories() != null && !l.getCategories().isEmpty()) {
//                            // check on item category
//                            containsCatregory = l.getCategories().contains(receipt.getCategory_id());
//                        }
//                    }
//                    return containsStore && containsDevice && containsCatregory && user.getLinkSections().contains(l.getUuId());
                    return ItemsController.linkSection(l.getId(), receipt.getCategory_id(), null, null, l);
                }).findAny();
                if (any.isEmpty()) {
                    check = false;
                }
            }
            if (check) {
                Optional<String> any = sectionId.getValue().stream().filter(n -> {
                    boolean containsDevice = true;
                    boolean containsCatregory = true;
                    Section l = storesController.oneStore(getBusinessId(), n);
//                    boolean containsStore = l.getId().equalsIgnoreCase(receipt.getStore_id());
//                    if (containsStore) {
//                        if (l.getDevices() != null && !l.getDevices().isEmpty()) {
//                            // check on pos device
//                            containsDevice = l.getDevices().contains(receipt.getPos_device_id());
//                        }
//
//                        if (containsDevice && l.getCategories() != null && !l.getCategories().isEmpty()) {
//                            // check on item category
//                            containsCatregory = l.getCategories().contains(receipt.getCategory_id());
//                        }
//                    }
//                    return containsStore && containsDevice && containsCatregory;
                    return ItemsController.linkSection(l.getId(), receipt.getCategory_id(), receipt.getForm(), receipt.getColor(), l);
                }).findAny();
                if (any.isEmpty()) {
                    check = false;
                }
            }
            return check;
        }
    }

}
