package sr.we.views.customers;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.componentfactory.onboarding.Onboarding;
import com.vaadin.componentfactory.onboarding.OnboardingStep;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
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
import sr.we.controllers.CustomersController;
import sr.we.controllers.StoresController;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Customer;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.integration.LoyCustomersController;
import sr.we.security.AuthenticatedUser;
import sr.we.storage.ICustomerStorage;
import sr.we.views.HelpFunction;
import sr.we.views.MainLayout;
import sr.we.views.MobileSupport;
import sr.we.views.users.CardView;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@PageTitle("Customers")
@Route(value = "customers", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
@Uses(Icon.class)
public class CustomersView extends Div implements BeforeEnterObserver, /*HelpFunction,*/ MobileSupport {

    private final CustomersController customerStorage;
    private final Filters filters;
    private final StoresController sectionService;
    private final AuthenticatedUser authenticatedUser;
    private final StoresController storesController;
    private final Set<String> linkSections;
    private final VerticalLayout layout;
    private Grid<Customer> grid;
//    private Grid.Column<Customer> storeColumn;
//    private Grid.Column<Customer> priceColumn;
//    private Grid.Column<Customer> inventoryValueColumn;
//    private Grid.Column<Customer> costColumn;
//    private Grid.Column<Customer> stockLevelColumn;
//    private Grid.Column<Customer> customerNameColumn;

    private TextField contactFld;
    private TextField barCodeFld;
    private TextField nameFld;
    private IntegerField stockLevelFld;
    private IntegerField totalVists;
    private BigDecimalField totalSpentFld;
//    private BigDecimalField priceFld;
//    private ComboBox<Section> storeFld;
    private User user;
    private List<Section> sections;
//    private Grid.Column<Customer> sectionColumn;
//    private MultiSelectComboBox<String> sectionId;
//    private Grid.Column<Customer> customersMobileColumn;
    private GridExporter<Customer> exporter;
    private Grid.Column<Customer> nameColumn;
    private Grid.Column<Customer> contactsColumn;
    private Grid.Column<Customer> firstVisitColumn;
    private Grid.Column<Customer> lastVisitColumn;
    private Grid.Column<Customer> totalVisitsColumn;
    private Grid.Column<Customer> totalSpentColumn;
    private Grid.Column<Customer> customerMobileColumn;
    //    private Grid.Column<Customer> skuColumn;
//    private Grid.Column<Customer> codeColumn;
    //    private Grid.Column<Customer> stockLevelDate;

    public CustomersView(CustomersController customerStorage, StoresController sectionService, AuthenticatedUser authenticatedUser, StoresController storesController) {
        this.customerStorage = customerStorage;
        this.sectionService = sectionService;
        this.authenticatedUser = authenticatedUser;
        this.storesController = storesController;
        setSizeFull();
        addClassNames("customers-view");

        linkSections = authenticatedUser.get().get().getLinkSections();

        filters = new Filters(() -> refreshGrid());
        layout = new VerticalLayout(/*createMobileFilters(),*/ filters, createGrid());
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
        grid = new Grid<>(Customer.class, false);

//        stockLevelDate = grid.addColumn(Customer::getLastUpdateStockLevel).setHeader("Stock Level date").setFrozen(true).setFlexGrow(1).setResizable(true);
//        skuColumn = grid.addColumn(i -> i.getVariant().getSku()).setHeader("SKU").setFlexGrow(0).setResizable(true);
//        codeColumn = grid.addColumn(i -> i.getVariant().getBarcode()).setHeader("Code").setFlexGrow(0).setResizable(true);
//        customerNameColumn = grid.addColumn(f -> {
//            String name = f.getItem_name();
//            if (f.getVariant() != null) {
//                if (StringUtils.isNotBlank(f.getVariant().getOption1_value())) {
//                    name += " " + f.getOption1_name() + " (" + f.getVariant().getOption1_value() + ")";
//                } else if (StringUtils.isNotBlank(f.getVariant().getOption2_value())) {
//                    name += " " + f.getOption2_name() + " (" + f.getVariant().getOption2_value() + ")";
//                } else if (StringUtils.isNotBlank(f.getVariant().getOption3_value())) {
//                    name += " " + f.getOption3_name() + " (" + f.getVariant().getOption3_value() + ")";
//                }
//            }
//            return name;
//        }).setHeader("Customer name").setFrozen(true).setFlexGrow(1).setResizable(true);
//        stockLevelColumn = grid.addColumn(Customer::getStock_level).setHeader("Stock level").setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
//        costColumn = grid.addColumn(l -> l.getVariant().getCost()).setHeader("Cost").setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
//        inventoryValueColumn = grid.addComponentColumn(l -> {
//            Span span = new Span(l.getVariant().getCost().multiply(BigDecimal.valueOf(l.getStock_level())).toString());
//            if (l.getStock_level() > 0) {
//                span.getElement().getThemeList().add("badge success");
//            } else if (l.getStock_level() < 0) {
//                span.getElement().getThemeList().add("badge error");
//            }
//            return span;
//        }).setHeader("Inventory value").setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
//        priceColumn = grid.addColumn(l -> l.getVariantStore().getPrice()).setHeader("Price").setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
//        storeColumn = grid.addColumn(l -> {
//            String storeId = l.getVariantStore().getStore_id();
//            Optional<Section> any = sectionService.allStores(getBusinessId()).stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(storeId)).findAny();
//            return any.map(Section::getDefault_name).orElse(storeId);
//        }).setHeader("Link Store").setFlexGrow(1);
//        sectionColumn = grid.addComponentColumn(r -> {
//            String collect1 = getSection(r);
//            Span span = new Span(collect1);
//            span.getStyle().set("white-space", "pre-line");
//            span.getElement().getThemeList().add("badge warning");
//            span.setWidthFull();
//            return span;
//        }).setHeader("Section").setAutoWidth(true);
//
        customerMobileColumn = grid.addComponentColumn(i -> {
            return CardView.createCard(null, i.getName(), i.getPhone_number(), "Total spent: "+i.getTotal_spent());
        }).setHeader("List of customers");
        customerMobileColumn.setVisible(false);

        nameColumn = grid.addColumn(Customer::getName).setHeader("Customer").setFlexGrow(1);
        contactsColumn = grid.addColumn(Customer::getPhone_number).setHeader("Contacts").setFlexGrow(1);
        firstVisitColumn = grid.addColumn(Customer::getFirst_visit).setHeader("First visit").setFlexGrow(1);
        lastVisitColumn = grid.addColumn(Customer::getLast_visit).setHeader("Last visit").setFlexGrow(1);
        totalVisitsColumn = grid.addColumn(Customer::getTotal_visits).setHeader("Total visits").setFlexGrow(1);
        totalSpentColumn = grid.addColumn(Customer::getTotal_spent).setHeader("Total spent").setFlexGrow(1);
//        grid.addColumn(Customer::getName).setHeader("Customer").setFlexGrow(1);


        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

//        createGridFilter();

        exporter = GridExporter.createFor(grid);
        exporter.setCsvExportEnabled(false);
        exporter.setDocxExportEnabled(false);
        exporter.setPdfExportEnabled(false);
        exporter.setTitle("Customers");
//        exporter.setExportValue(inventoryValueColumn, l -> l.getVariant().getCost().multiply(BigDecimal.valueOf(l.getStock_level())));
//        exporter.setExportValue(sectionColumn, this::getSection);
        exporter.setFileName("GridExportCustomer" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
        exporter.setCustomHeader(nameColumn, "Customer");
//        exporter.setCustomHeader(stockLevelDate, "Stock level date");
        exporter.setCustomHeader(contactsColumn, "Contacts");
        exporter.setCustomHeader(firstVisitColumn, "First visit");
        exporter.setCustomHeader(lastVisitColumn, "Last visit");
        exporter.setCustomHeader(totalVisitsColumn, "Total visits");
        exporter.setCustomHeader(totalSpentColumn, "Total spent");
//        exporter.setCustomHeader(sectionColumn, "Section");

        return grid;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        boolean isMobile = CardView.isMobileDevice();
//        editHeader.setVisible(isMobile);
        if (isMobile) {
//            if(user == null){
//                splitLayout.setSplitterPosition(100);
//            } else {
//                splitLayout.setSplitterPosition(0);
//            }

            layout.addClassName("card-view");
            nameColumn.setVisible(false);
            contactsColumn.setVisible(false);
            firstVisitColumn.setVisible(false);
            lastVisitColumn.setVisible(false);
            totalVisitsColumn.setVisible(false);
            totalSpentColumn.setVisible(false);
            customerMobileColumn.setVisible(true);

            filters.setVisible(false);
            exporter.setExcelExportEnabled(false);
        } else {
            layout.removeClassName("card-view");
            nameColumn.setVisible(true);
            contactsColumn.setVisible(true);
            firstVisitColumn.setVisible(true);
            lastVisitColumn.setVisible(true);
            totalVisitsColumn.setVisible(true);
            totalSpentColumn.setVisible(true);
            customerMobileColumn.setVisible(false);

            filters.setVisible(true);
            exporter.setExcelExportEnabled(true);
        }
    }

//    private String getSection(Customer r) {
//        List<Section> collect = sections.stream().filter(l -> CustomerController.linkSection(l.getId(), r.getCategory_id(), r.getForm(), r.getColor(), l)).toList();
//        if (collect.size() > 1) {
//            collect = collect.stream().filter(l -> !l.isDefault()).toList();
//        }
////            sectionMultiSelectComboBox.setValue(collect);
//        String collect1 = collect.stream().map(Section::getName).collect(Collectors.joining("\n"));
//        return collect1;
//    }

    private void createGridFilter() {
        HeaderRow headerRow = grid.appendHeaderRow();

        nameFld = new TextField("", "", l -> grid.getDataProvider().refreshAll());
        contactFld = new TextField("", "", l -> grid.getDataProvider().refreshAll());
        barCodeFld = new TextField("", "", l -> grid.getDataProvider().refreshAll());
        stockLevelFld = new IntegerField("", null, l -> grid.getDataProvider().refreshAll());
        totalVists = new IntegerField("", null, l -> grid.getDataProvider().refreshAll());
        totalSpentFld = new BigDecimalField("", null, l -> grid.getDataProvider().refreshAll());
//        priceFld = new BigDecimalField("", null, l -> grid.getDataProvider().refreshAll());
//        storeFld = new ComboBox<>(l -> grid.getDataProvider().refreshAll());
//        storeFld.setItemLabelGenerator(Section::getDefault_name);
//        storeFld.setItems(query -> sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize()).filter(Section::isDefault));
//        sectionId = new MultiSelectComboBox<>();
//        sectionId.setItemLabelGenerator(label -> {
//            Section section = storesController.oneStore(label);
//            return section == null ? "Error" : section.getName();
//        });
//        List<String> sects = storesController.allSections(getBusinessId(), 0, Integer.MAX_VALUE, authenticatedUser.get()).map(Section::getUuId).toList();
//        sectionId.setItems(sects);
//        sectionId.setValue(sects);
//        sectionId.addValueChangeListener(l -> grid.getDataProvider().refreshAll());
//
//        sectionId.setPlaceholder("Filter");
        nameFld.setPlaceholder("Filter");
        contactFld.setPlaceholder("Filter");
        barCodeFld.setPlaceholder("Filter");
        stockLevelFld.setPlaceholder("Filter");
        totalVists.setPlaceholder("Filter");
        totalSpentFld.setPlaceholder("Filter");
//        priceFld.setPlaceholder("Filter");
//        storeFld.setPlaceholder("Filter");
//
//        sectionId.setWidthFull();
        nameFld.setWidthFull();
        contactFld.setWidthFull();
        barCodeFld.setWidthFull();
        stockLevelFld.setWidthFull();
        totalVists.setWidthFull();
        totalSpentFld.setWidthFull();
//        priceFld.setWidthFull();
//        storeFld.setWidthFull();
//
//        sectionId.setClearButtonVisible(true);
        nameFld.setClearButtonVisible(true);
        contactFld.setClearButtonVisible(true);
        barCodeFld.setClearButtonVisible(true);
        stockLevelFld.setClearButtonVisible(true);
        totalVists.setClearButtonVisible(true);
        totalSpentFld.setClearButtonVisible(true);
//        priceFld.setClearButtonVisible(true);
//        storeFld.setClearButtonVisible(true);

        stockLevelFld.setStepButtonsVisible(true);

        headerRow.getCell(nameColumn).setComponent(nameFld);
        headerRow.getCell(contactsColumn).setComponent(contactFld);
//        headerRow.getCell(firstVisitColumn).setComponent(barCodeFld);
//        headerRow.getCell(lastVisitColumn).setComponent(stockLevelFld);
        headerRow.getCell(totalVisitsColumn).setComponent(totalVists);
        headerRow.getCell(totalSpentColumn).setComponent(totalSpentFld);
//        headerRow.getCell(priceColumn).setComponent(priceFld);
//        headerRow.getCell(storeColumn).setComponent(storeFld);
//        headerRow.getCell(sectionColumn).setComponent(sectionId);
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
            grid.setItems(query -> customerStorage.allcustomers(getBusinessId(), query.getPage(), query.getPageSize(),user,Optional.empty()/*, sectionId.getValue(), check()*/));
        }
    }

//    private Collection<Criteria> check() {
//        List<Criteria> criterias = new ArrayList<>();
////        boolean check = true;
//        if (StringUtils.isNotBlank(nameFld.getValue())) {
////            check = customer.getItem_name().toUpperCase().contains(customerNameFld.getValue().toUpperCase());
//            criterias.add(Criteria.where("customer_name").regex(".*" + nameFld.getValue() + ".*"));
//        }
//        if (StringUtils.isNotBlank(contactFld.getValue())) {
//            criterias.add(Criteria.where("variant.sku").regex(".*" + contactFld.getValue() + ".*"));
//        }
//        if (StringUtils.isNotBlank(barCodeFld.getValue())) {
//            criterias.add(Criteria.where("variant.barcode").regex(".*" + barCodeFld.getValue() + ".*"));
//        }
//        if (storeFld.getValue() != null) {
////            check = customer.getVariantStore().getStore_id().equalsIgnoreCase(storeFld.getValue().getId());
//            criterias.add(Criteria.where("variantStore.store_id").is(storeFld.getValue().getId()));
//        }
//        if (stockLevelFld.getValue() != null) {
////            check = customer.getStock_level() == stockLevelFld.getValue();
//            criterias.add(Criteria.where("stock_level").is(stockLevelFld.getValue()));
//        }
//        if (totalVists.getValue() != null) {
////            check = customer.getVariant().getCost().compareTo(costFld.getValue()) == 0;
//            criterias.add(Criteria.where("variant.cost").is(totalVists.getValue()));
//        }
//        if (priceFld.getValue() != null) {
////            check = customer.getVariant().getDefault_price() != null && customer.getVariant().getDefault_price().compareTo(priceFld.getValue()) == 0;
//            criterias.add(Criteria.where("variant.default_price").is(priceFld.getValue()));
//        }
//        if (totalSpentFld.getValue() != null) {
////            check = customer.getVariant().getCost().multiply(BigDecimal.valueOf(customer.getStock_level())).compareTo(inventoryValueFld.getValue()) == 0;
////            criterias.add(Criteria.where("customer_name").regex(".*ab.*", customerNameFld.getValue()));TODO
//        }
////
////        if (check) {
////            check = filters.check(customer);
////        }
////
////        if (user.getRoles().contains(Role.SECTION_OWNER)) {
////            String categoryId = customer.getCategory_id();
////            VariantStore variantStore = customer.getVariantStore();
////            String storeId = variantStore.getStore_id();
////
////            Optional<Section> any = sections.stream().filter(f -> f.getId().compareTo(storeId) == 0 && (f.getCategories() == null || f.getCategories().contains(categoryId))).findAny();
////            if (any.isEmpty()) {
////                check = false;
////            }
////        }
////
////
////        if(check) {
////            Optional<String> any = sectionId.getValue().stream().filter(n -> {
////                boolean containsCatregory = true;
////                Section l = storesController.oneStore(getBusinessId(), n);
//////                boolean containsStore = l.getId().equalsIgnoreCase(customer.getVariantStore().getStore_id());
//////                if (containsStore) {
//////
//////                    if (l.getCategories() != null && !l.getCategories().isEmpty()) {
//////                        // check on customer category
//////                        containsCatregory = l.getCategories().contains(customer.getCategory_id());
//////                    }
//////                }
//////                return containsStore && containsCatregory;
////                return CustomerController.linkSection(l.getId(), customer.getCategory_id(), customer.getForm(), customer.getColor(), l);
////            }).findAny();
////            if (any.isEmpty()) {
////                check = false;
////            }
////        }
//
//        return criterias;
//    }

//    @Override
//    public String help(Onboarding onboarding) {
//        OnboardingStep step1 = new OnboardingStep(this);
//        step1.setHeader("Introduction to Customers view");
//        step1.setContent(new Html("""
//                <div style="padding: var(--lumo-space-s); background-color: var(--lumo-shade-5pct);">
//                              <p>Welcome to the Customers view! This section provides a comprehensive overview and filtering system for all customers managed within our platform.</p>
//
//                              <p>Here, you can explore a complete list of customers available in your inventory. Whether it's products, materials, or equipment, you'll find detailed information for each customer, including names, descriptions, prices, and quantities.</p>
//
//                              <p>Utilize our filtering options to streamline your search based on specific criteria such as category, brand, or stock availability. This enables you to efficiently navigate through your inventory and locate customers with ease.</p>
//
//                              <p>With the Customers view, you can effectively manage and monitor your inventory, ensuring accurate stock levels and facilitating smooth operations for your business.</p>
//                            </div>
//                """));
//        onboarding.addStep(step1);
//        return "CustomerHelpFunction";
//    }

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


        public boolean check(Customer customer) {
            boolean check = true;
//            if (startDate.getValue() != null && endDate.getValue() != null && customer.getLastUpdateStockLevel() != null) {
//                LocalDate receiptDate = customer.getLastUpdateStockLevel().toLocalDate();
//                if (receiptDate.isBefore(startDate.getValue()) || receiptDate.isAfter(endDate.getValue())) {
//                    check = false;
//                }
//            }
            return check;
        }
    }

}


