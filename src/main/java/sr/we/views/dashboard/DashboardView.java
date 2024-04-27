package sr.we.views.dashboard;


import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.addons.yuri0x7c1.bslayout.BsLayout;
import sr.we.controllers.InventoryValuationController;
import sr.we.controllers.ItemsController;
import sr.we.controllers.ReceiptsController;
import sr.we.controllers.StoresRestController;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.security.AuthenticatedUser;
import sr.we.views.MainLayout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
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
public class DashboardView extends Main implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;
    private final UI ui;
    private final InventoryValuationController inventoryValuationStorage;
    private final ExecutorService executorService;
    private final Select<DashType> dashTypeSelect;
    private final BsLayout board;
    private final Filters filters;
    private final ReceiptsController receiptsController;
    private final StoresRestController storesRestController;
    private SalesBoard salesBoard;
    private InventoryBoard inventoryBoard;
    private User user;
    private ApexChartsBuilder chart;
    private Future<Void> submit2;
    private Future<?> submit;
    private VerticalLayout viewEvents;
    private HorizontalLayout header;

    public DashboardView(ItemsController ItemService, AuthenticatedUser authenticatedUser, InventoryValuationController inventoryValuationStorage, ReceiptsController receiptsController, StoresRestController storesRestController) {
        addClassName("dashboard-view");
        this.ui = UI.getCurrent();
        this.authenticatedUser = authenticatedUser;
        this.inventoryValuationStorage = inventoryValuationStorage;
        this.receiptsController = receiptsController;
        this.storesRestController = storesRestController;
        executorService = Executors.newFixedThreadPool(5);

        Optional<User> maybeUser = authenticatedUser.get();
        maybeUser.ifPresent(value -> user = value);

        board = new BsLayout();
        board.addClassName("myborderstyle");
        dashTypeSelect = new Select<>();
        dashTypeSelect.setLabel("Type");
        dashTypeSelect.setItems(DashType.values());
        dashTypeSelect.addThemeVariants(SelectVariant.LUMO_SMALL);
        HorizontalLayout horizontalLayout = new HorizontalLayout(filters = new Filters(getBusinessId(), storesRestController, authenticatedUser, board), dashTypeSelect);
        dashTypeSelect.getElement().getStyle().set("margin-left", "auto");
        add(horizontalLayout, board);

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
                salesBoard = new SalesBoard(this, getBusinessId(), receiptsController, ui, board);
            }
            if (filters.startDate.getValue() != null && filters.endDate.getValue() != null && !filters.sectionId.getValue().isEmpty()) {
                salesBoard.build(filters.startDate.getValue(), filters.endDate.getValue(), filters.sectionId.getValue());
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

    public static class Filters extends HorizontalLayout {
        private final Select<String> selector = new Select<>();
        private final DatePicker startDate = new DatePicker("Period");
        private final DatePicker endDate = new DatePicker();
        private final MultiSelectComboBox<String> sectionId;
        private final Long businessId;
        private Set<String> linkSections;
        private SalesBoard salesBoard;

        public Filters(Long businessId, StoresRestController storesRestController, AuthenticatedUser authenticatedUser, BsLayout board) {
            this.businessId = businessId;
            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM, LumoUtility.BoxSizing.BORDER);
//

            // Action buttons


            sectionId = new MultiSelectComboBox<>("Section");
            add(createDateRangeFilter(), sectionId);
            sectionId.setItemLabelGenerator(label -> storesRestController.oneStore(getBusinessId(), label).getName());
            List<String> sects = storesRestController.allSections(getBusinessId(), 0, Integer.MAX_VALUE, f -> {
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


            startDate.addValueChangeListener(l -> {
                if (startDate.getValue() != null && endDate.getValue() != null && !sectionId.getValue().isEmpty()) {
                    board.removeAll();
                    salesBoard.build(startDate.getValue(), endDate.getValue(), sectionId.getValue());
                }
            });
            endDate.addValueChangeListener(l -> {
                if (startDate.getValue() != null && endDate.getValue() != null && !sectionId.getValue().isEmpty()) {
                    board.removeAll();
                    salesBoard.build(startDate.getValue(), endDate.getValue(), sectionId.getValue());
                }
            });
            sectionId.addValueChangeListener(l -> {
                if (startDate.getValue() != null && endDate.getValue() != null && !sectionId.getValue().isEmpty()) {
                    board.removeAll();
                    salesBoard.build(startDate.getValue(), endDate.getValue(), sectionId.getValue());
                }
            });
        }

        public void setSalesBoard(SalesBoard salesBoard) {
            this.salesBoard = salesBoard;
        }

        public Long getBusinessId() {
            return businessId;
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
    }
}
