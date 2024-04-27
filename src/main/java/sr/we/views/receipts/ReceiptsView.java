package sr.we.views.receipts;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import sr.we.controllers.ReceiptsController;
import sr.we.controllers.StoresRestController;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Receipt;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.security.AuthenticatedUser;
import sr.we.views.MainLayout;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Receipts")
@Route(value = "receipts", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "SECTION_OWNER"})
@Uses(Icon.class)
public class ReceiptsView extends Div {

    private static List<Section> sections;
    private static User user;
    private final ReceiptsController receiptService;
    private final StoresRestController sectionService;
    private final AuthenticatedUser authenticatedUser;
    private final Filters filters;
    private Grid<Receipt> grid;

    public ReceiptsView(StoresRestController sectionService, ReceiptsController ReceiptService, AuthenticatedUser authenticatedUser) {
        this.receiptService = ReceiptService;
        this.sectionService = sectionService;
        this.authenticatedUser = authenticatedUser;
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

            boolean containsDevice = true;
            boolean containsCatregory = true;
            boolean containsStore = l.getId().equalsIgnoreCase(r.getStore_id());
            if (containsStore) {
                if (l.getDevices() != null && !l.getDevices().isEmpty()) {
                    // check on pos device
                    containsDevice = l.getDevices().contains(r.getPos_device_id());
                }

                if (containsDevice && l.getCategories() != null && !l.getCategories().isEmpty()) {
                    // check on item category
                    containsCatregory = l.getCategories().contains(r.getCategory_id());
                }
            }
            return containsStore && containsDevice && containsCatregory;
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
        grid.addColumn(Receipt::getReceipt_date).setHeader("Create date").setAutoWidth(true).setResizable(true);
        grid.addColumn(Receipt::getReceipt_number).setHeader("Receipt #").setAutoWidth(true).setResizable(true);
        grid.addColumn(Receipt::getReceipt_type).setHeader("Type").setAutoWidth(true);
        grid.addColumn(l -> {
            String storeId = l.getStore_id();
            Optional<Section> any = sectionService.allStores(getBusinessId()).stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(storeId)).findAny();
            return any.map(Section::getDefault_name).orElse(storeId);
        }).setHeader("Store").setAutoWidth(true);
        grid.addColumn(r -> r.getLine_item().getItem_name()).setHeader("Item").setAutoWidth(true);
        Grid.Column<Receipt> sectionColumn = grid.addComponentColumn(r -> {

            List<Section> collect = getSections(r);
            Span span = new Span(collect.stream().map(Section::getName).collect(Collectors.joining("\n")));
            span.getStyle().set("white-space", "pre-line");
            span.getElement().getThemeList().add("badge warning");
            span.setWidthFull();
            return span;
        }).setHeader("Section").setAutoWidth(true);
        grid.addColumn(r -> r.getLine_item().getCost_total()).setHeader("Cost").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(r -> r.getLine_item().getTotal_discount()).setHeader("Discount").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(r -> r.getLine_item().getTotal_money()).setHeader("Total").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(r -> r.getLine_item().getGross_total_money()).setHeader("Gross Total").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);


        sections = sectionService.allStores(getBusinessId());
        Optional<User> userOptional = authenticatedUser.get();
        userOptional.ifPresent(value -> user = value);
        grid.setItems(query -> receiptService.allReceipts(getBusinessId(), query.getPage(), query.getPageSize(), filters::check));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        GridExporter<Receipt> exporter = GridExporter.createFor(grid);
        exporter.setCsvExportEnabled(false);
        exporter.setDocxExportEnabled(false);
        exporter.setPdfExportEnabled(false);
        exporter.setTitle("Receipts");
        exporter.setExportValue(sectionColumn, r -> {
            List<Section> collect = getSections(r);
            return collect.stream().map(Section::getName).collect(Collectors.joining("\n"));
        });
        exporter.setFileName("GridExportReceipts" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    public static class Filters extends Div {
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

//            Optional<Cookie> any = Arrays.stream(VaadinService.getCurrentRequest().getCookies()).filter(cookie -> cookie.getName().equalsIgnoreCase("date-range")).findAny();
//            if(any.isPresent()){
//                Cookie cookie = any.get();
//                String value = cookie.getValue();
//                String[] split = value.split("//");
//                if(split.length==2){
//                    String startDateString = split[0];
//                    String endDateString = split[1];
//
//                    startDate.setValue(LocalDate.parse(startDateString));
//                    endDate.setValue(LocalDate.parse(endDateString));
//                }
//            }


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
            if (user.getRoles().contains(Role.SECTION_OWNER)) {

                Optional<Section> any = sections.stream().filter(l -> {
                    boolean containsDevice = true;
                    boolean containsCatregory = true;
                    boolean containsStore = l.getId().equalsIgnoreCase(receipt.getStore_id());
                    if (containsStore) {
                        if (l.getDevices() != null && !l.getDevices().isEmpty()) {
                            // check on pos device
                            containsDevice = l.getDevices().contains(receipt.getPos_device_id());
                        }

                        if (containsDevice && l.getCategories() != null && !l.getCategories().isEmpty()) {
                            // check on item category
                            containsCatregory = l.getCategories().contains(receipt.getCategory_id());
                        }
                    }
                    return containsStore && containsDevice && containsCatregory && user.getLinkSections().contains(l.getUuId());
                }).findAny();
                if (any.isEmpty()) {
                    check = false;
                }
            }
            return check;
        }
    }

}
