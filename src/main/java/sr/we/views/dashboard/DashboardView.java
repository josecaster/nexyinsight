package sr.we.views.dashboard;


import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.addons.yuri0x7c1.bslayout.BsColumn;
import org.vaadin.addons.yuri0x7c1.bslayout.BsLayout;
import org.vaadin.addons.yuri0x7c1.bslayout.BsRow;
import software.xdev.vaadin.daterange_picker.business.DateRangeModel;
import software.xdev.vaadin.daterange_picker.business.SimpleDateRange;
import software.xdev.vaadin.daterange_picker.business.SimpleDateRanges;
import software.xdev.vaadin.daterange_picker.ui.DateRangePicker;
import sr.we.controllers.InventoryValuationController;
import sr.we.controllers.ItemsController;
import sr.we.controllers.ReceiptsController;
import sr.we.controllers.StoresController;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.security.AuthenticatedUser;
import sr.we.views.MainLayout;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@PageTitle("Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "SECTION_OWNER"})
public class DashboardView extends Main implements BeforeEnterObserver  {

    private final AuthenticatedUser authenticatedUser;
    private final UI ui;
    private final InventoryValuationController inventoryValuationStorage;
    private final ExecutorService executorService;
    private Select<DashType> dashTypeSelect;
    private final BsLayout board;
    private final Filters filters;
    private final ReceiptsController receiptsController;
    private final StoresController storesController;
    private final HorizontalLayout filterLayout;
    private SalesBoard salesBoard;
    private InventoryBoard inventoryBoard;
    private User user;
    private ApexChartsBuilder chart;
    private Future<Void> submit2;
    private Future<?> submit;
    private VerticalLayout viewEvents;
    private HorizontalLayout header;

    public DashboardView(ItemsController ItemService, AuthenticatedUser authenticatedUser, InventoryValuationController inventoryValuationStorage, ReceiptsController receiptsController, StoresController storesController) {
        addClassNames("dashboard-view","items-view");
        this.ui = UI.getCurrent();
        this.authenticatedUser = authenticatedUser;
        this.inventoryValuationStorage = inventoryValuationStorage;
        this.receiptsController = receiptsController;
        this.storesController = storesController;
        executorService = Executors.newFixedThreadPool(5);

        Optional<User> maybeUser = authenticatedUser.get();
        maybeUser.ifPresent(value -> user = value);

        board = new BsLayout();
        board.addClassName("myborderstyle");

        filterLayout = new HorizontalLayout(filters = new Filters(getBusinessId(), storesController, authenticatedUser, board));

//        filterLayout.getElement().getStyle().set("display","block");
        add( filterLayout, board);

        dashTypeSelect.addValueChangeListener(l -> {
            board.removeAll();
            DashType value = l.getValue();
            generateDashboard(inventoryValuationStorage, receiptsController, value);
        });
    }



    private void generateDashboard(InventoryValuationController inventoryValuationStorage, ReceiptsController receiptsController, DashType value) {
        if (value == null) {
            return;
        }
        if (value.compareTo(DashType.INVENTORY) == 0) {
            if (inventoryBoard == null) {
                inventoryBoard = new InventoryBoard(this, getBusinessId(), inventoryValuationStorage, ui, board);
            }
            inventoryBoard.build();
        } else if (value.compareTo(DashType.SALES) == 0) {
            if (salesBoard == null) {
                salesBoard = new SalesBoard(this, getBusinessId(), receiptsController, ui, board, storesController);
            }
            if (filters.rangePicker.getValue() != null && filters.rangePicker.getValue().getStart() != null && filters.rangePicker.getValue().getEnd() != null && !filters.sectionId.getValue().isEmpty()) {
                salesBoard.build(filters.rangePicker.getValue().getStart(), filters.rangePicker.getValue().getEnd(), filters.sectionId.getValue());
            }
            filters.setSalesBoard(salesBoard);
        }
    }

    public void setSubmit2(Future<Void> submit2) {
        this.submit2 = submit2;
    }

    public void setSubmit(Future<?> submit) {
        this.submit = submit;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<User> maybeUser = authenticatedUser.get();
        maybeUser.ifPresent(value -> user = value);
        if (user.getRoles().contains(Role.SECTION_OWNER)) {
            dashTypeSelect.setItems(DashType.SALES);
            dashTypeSelect.setValue(DashType.SALES);
        } else {
            dashTypeSelect.setItems(DashType.values());
            dashTypeSelect.setValue(DashType.SALES);
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        shut();
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        super.onDetach(detachEvent);

    }

    private void shut() {
        if (submit != null) {
            submit.cancel(true);
        }
        if (submit2 != null) {
            submit2.cancel(true);
        }

    }

    private Long getBusinessId() {
        return 0L;
    }

    public enum DashType {
        SALES, INVENTORY
    }

    public class Filters extends HorizontalLayout {
        protected static final List<SimpleDateRange> DATERANGE_VALUES = Arrays.asList(SimpleDateRanges.allValues());
//        private final Select<String> selector = new Select<>();
//        private final DatePicker startDate = new DatePicker("Period");
//        private final DatePicker endDate = new DatePicker();
        private final DateRangePicker<SimpleDateRange> rangePicker;
        private final MultiSelectComboBox<String> sectionId;
        private final Long businessId;
        private Set<String> linkSections;
        private SalesBoard salesBoard;

        public Filters(Long businessId, StoresController storesController, AuthenticatedUser authenticatedUser, BsLayout board) {
            this.businessId = businessId;
            setWidthFull();

            addClassNames(LumoUtility.Padding.Top.LARGE,LumoUtility.Padding.Bottom.LARGE);

            BsLayout filterBoard = new BsLayout();
//

            // Action buttons
            rangePicker = new DateRangePicker<SimpleDateRange>(() -> new DateRangeModel<>(LocalDate.now(), LocalDate.now(), SimpleDateRanges.TODAY), DATERANGE_VALUES);
            rangePicker.setWidthFull();

            dashTypeSelect = new Select<>();
            dashTypeSelect.setWidthFull();
            dashTypeSelect.getElement().getStyle().set("padding","0px");
            dashTypeSelect.setLabel("Type");
            dashTypeSelect.setItems(DashType.values());
            dashTypeSelect.addThemeVariants(SelectVariant.LUMO_SMALL);

            sectionId = new MultiSelectComboBox<>("Section");
            sectionId.setWidthFull();
            sectionId.getElement().getStyle().set("padding","0px");

            BsRow period = new BsRow(new BsColumn(new Div(new Text("Period"), rangePicker)).withSize(BsColumn.Size.XS), new BsColumn(sectionId).withSize(BsColumn.Size.XS), new BsColumn(dashTypeSelect).withSize(BsColumn.Size.XS));
            filterBoard.addRow(period);
//            period.addClassName("filter-layout");
            add(filterBoard);



            sectionId.setItemLabelGenerator(label -> {
                Section section = storesController.oneStore(label);
                return section == null ? "Error" : section.getName();
            });
            List<String> sects = storesController.allSections(getBusinessId(), 0, Integer.MAX_VALUE, authenticatedUser.get()).map(Section::getUuId).toList();
            sectionId.setItems(sects);
            sectionId.setValue(sects);


            rangePicker.addValueChangeListener(l -> {
                DateRangeModel<SimpleDateRange> value = l.getValue();
                if(value.getStart() != null && value.getEnd() != null &&  !sectionId.getValue().isEmpty()){
                    board.removeAll();
                    salesBoard.build(value.getStart(), value.getEnd(), sectionId.getValue());
                }
            });
//            startDate.addValueChangeListener(l -> {
//                if (startDate.getValue() != null && endDate.getValue() != null && !sectionId.getValue().isEmpty()) {
//                    board.removeAll();
//                    salesBoard.build(startDate.getValue(), endDate.getValue(), sectionId.getValue());
//                }
//            });
//            endDate.addValueChangeListener(l -> {
//                if (startDate.getValue() != null && endDate.getValue() != null && !sectionId.getValue().isEmpty()) {
//                    board.removeAll();
//                    salesBoard.build(startDate.getValue(), endDate.getValue(), sectionId.getValue());
//                }
//            });
            sectionId.addValueChangeListener(l -> {
                DateRangeModel<SimpleDateRange> value = rangePicker.getValue();
                if (value.getStart() != null && value.getEnd() != null && !sectionId.getValue().isEmpty()) {
                    board.removeAll();
                    salesBoard.build(value.getStart(), value.getEnd(), sectionId.getValue());
                }
            });
        }

        public void setSalesBoard(SalesBoard salesBoard) {
            this.salesBoard = salesBoard;
        }

        public Long getBusinessId() {
            return businessId;
        }

//        private Component createDateRangeFilter() {
//            selector.setItems("Today", "Yesterday", "This week", "Last week", "This month", "Last month", "Last 7 days", "Last 30 days");
//            selector.addValueChangeListener(l -> {
//                if (StringUtils.isBlank(l.getValue())) {
//                    selector.setValue("Today");
//                } else {
//                    String value = l.getValue();
//                    switch (value) {
//                        case "Today" -> {
//                            startDate.setValue(LocalDate.now());
//                            endDate.setValue(startDate.getValue());
//                        }
//                        case "Yesterday" -> {
//                            startDate.setValue(LocalDate.now().minusDays(1));
//                            endDate.setValue(startDate.getValue());
//                        }
//                        case "This week" -> {
//                            LocalDate lastWeekSameDay = LocalDate.now();
//                            startDate.setValue(lastWeekSameDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
//                            endDate.setValue(startDate.getValue().plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)));
//                        }
//                        case "Last week" -> {
//                            LocalDate lastWeekSameDay = LocalDate.now().minusWeeks(1);
//                            startDate.setValue(lastWeekSameDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
//                            endDate.setValue(startDate.getValue().plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)));
//                        }
//                        case "This month" -> {
//                            startDate.setValue(YearMonth.now().atDay(1));
//                            endDate.setValue(YearMonth.from(startDate.getValue()).atEndOfMonth());
//                        }
//                        case "Last month" -> {
//                            startDate.setValue(YearMonth.now().atDay(1).minusMonths(1));
//                            endDate.setValue(YearMonth.from(startDate.getValue()).atEndOfMonth());
//                        }
//                        case "Last 7 days" -> {
//                            startDate.setValue(LocalDate.now().minusWeeks(1));
//                            endDate.setValue(LocalDate.now());
//                        }
//                        case "Last 30 days" -> {
//                            startDate.setValue(LocalDate.now().minusDays(30));
//                            endDate.setValue(LocalDate.now());
//                        }
//                    }
//                }
//            });
//            selector.setValue("Today");
//            startDate.setPlaceholder("From");
//
//            endDate.setPlaceholder("To");
//
//            // For screen readers
//            startDate.setAriaLabel("From date");
//            endDate.setAriaLabel("To date");
//
//
//            startDate.setWidth("166px");
//            endDate.setWidth("166px");
//
//            FlexLayout dateRangeComponent = new FlexLayout(rangePicker, selector, new Text(" : "), startDate, new Text(" – "), endDate);
//            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
//            dateRangeComponent.getElement().getStyle().set("justify-self", "self-start");
//            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);
//
//            return dateRangeComponent;
//        }
    }
}
