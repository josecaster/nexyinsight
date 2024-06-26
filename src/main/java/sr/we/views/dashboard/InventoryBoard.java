package sr.we.views.dashboard;

import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.NoData;
import com.github.appreciated.apexcharts.config.Theme;
import com.github.appreciated.apexcharts.config.XAxis;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.grid.builder.RowBuilder;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.responsive.builder.OptionsBuilder;
import com.github.appreciated.apexcharts.config.series.SeriesType;
import com.github.appreciated.apexcharts.config.theme.Mode;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.servlet.http.Cookie;
import org.apache.commons.lang3.tuple.Pair;
import org.vaadin.addons.yuri0x7c1.bslayout.BsColumn;
import org.vaadin.addons.yuri0x7c1.bslayout.BsLayout;
import org.vaadin.addons.yuri0x7c1.bslayout.BsRow;
import sr.we.controllers.InventoryValuationController;
import sr.we.entity.eclipsestore.tables.InventoryValuation;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.views.CookieUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InventoryBoard implements Serializable {
    private final DashboardView dashboardView;
    private final Long businessId;
    private final InventoryValuationController inventoryValuationStorage;
    private final UI ui;

    private final BsLayout board;
    private HorizontalLayout header;
    private VerticalLayout viewEvents;
    private ApexChartsBuilder chart;

    public InventoryBoard(DashboardView dashboardView, Long businessId, InventoryValuationController inventoryValuationStorage, UI ui, BsLayout board) {
        this.dashboardView = dashboardView;
        this.businessId = businessId;
        this.inventoryValuationStorage = inventoryValuationStorage;
        this.ui = ui;
        this.board = board;
    }

    private static boolean check(Item item) {
        return true;
    }

    private static BigDecimal getValue(Map<Pair<Integer, Month>, List<InventoryValuation>> collect, Integer year, Month month, Function<InventoryValuation, BigDecimal> function) {
        return collect.entrySet().stream().filter(f -> f.getKey().getKey().compareTo(year) == 0 && f.getKey().getValue().compareTo(month) == 0).map(Map.Entry::getValue).flatMap(List::stream).min(Comparator.comparing(InventoryValuation::getLocalDate).reversed()).map(function).orElse(BigDecimal.ZERO);
    }

    public void build() {


        board.addRow(new BsRow(new BsColumn(createHighlight("Total inventory value", span -> {
            //new Thread(() -> {
            Optional<InventoryValuation> inventoryValuationOptional = inventoryValuationStorage.getInventoryValuation(getBusinessId(), LocalDate.now());
            ui.access(() -> {
                String format = new DecimalFormat("###,###,###,###,###,##0.00").format(inventoryValuationOptional.isPresent() ? inventoryValuationOptional.get().getInventoryValue() : BigDecimal.ZERO);
                span.setText(format);
            });
            //}).start();
            return null;
        })).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Total retail value", span -> {
                    //new Thread(() -> {
                    Optional<InventoryValuation> inventoryValuationOptional = inventoryValuationStorage.getInventoryValuation(getBusinessId(), LocalDate.now());
                    ui.access(() -> {
                        String format = new DecimalFormat("###,###,###,###,###,##0.00").format(inventoryValuationOptional.isPresent() ? inventoryValuationOptional.get().getRetailValue() : BigDecimal.ZERO);
                        span.setText(format);
                    });
                    //}).start();
                    return null;
                })).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Potential profit", span -> {
                    //new Thread(() -> {
                    Optional<InventoryValuation> inventoryValuationOptional = inventoryValuationStorage.getInventoryValuation(getBusinessId(), LocalDate.now());
                    ui.access(() -> {
                        String format = new DecimalFormat("###,###,###,###,###,##0.00").format(inventoryValuationOptional.isPresent() ? inventoryValuationOptional.get().getPotentialProfit() : BigDecimal.ZERO);
                        span.setText(format);
                    });
                    //}).start();
                    return null;
                })).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Margin", span -> {
                    //new Thread(() -> {
                    Optional<InventoryValuation> inventoryValuationOptional = inventoryValuationStorage.getInventoryValuation(getBusinessId(), LocalDate.now());
                    ui.access(() -> {
                        String format = new DecimalFormat(" #,##0.00 '%'").format(inventoryValuationOptional.isPresent() ? inventoryValuationOptional.get().getMargin() : BigDecimal.ZERO);
                        span.setText(format);
                    });
                    //}).start();
                    return null;
                })).withSize(BsColumn.Size.XS)));
        board.addRow(new BsRow(new BsColumn(createViewEvents()).withSize(BsColumn.Size.XS)));
        board.addRow(new BsRow(new BsColumn(createServiceHealth()).withSize(BsColumn.Size.XS), new BsColumn(createResponseTimes()).withSize(BsColumn.Size.XS)));
    }

    private Component createHighlight(String title, BuildParameter<Object, Span> buildParameter) {
        VaadinIcon icon = VaadinIcon.ARROW_UP;
        String prefix = "";
        String theme = "badge";

//        if (percentage == 0) {
//            prefix = "±";
//        } else if (percentage > 0) {
//            prefix = "+";
//            theme += " success";
//        } else if (percentage < 0) {
//            icon = VaadinIcon.ARROW_DOWN;
//            theme += " error";
//        }

        H2 h2 = new H2(title);
        h2.addClassNames(LumoUtility.FontWeight.NORMAL, LumoUtility.Margin.NONE, LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        Span span = new Span();

        span.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.XXXLARGE);

        buildParameter.build(span);

        Icon i = icon.create();
        i.addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Padding.XSMALL);

//        Span badge = new Span(i, new Span(prefix + percentage));
//        badge.getElement().getThemeList().add(theme);

        VerticalLayout layout = new VerticalLayout(h2, span);
        layout.addClassName(LumoUtility.Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private Component createViewEvents() {
        // Header
        LocalDate now = LocalDate.now();
        Select year = new Select();
        year.setItems(now.minusYears(5), now.minusYears(4), now.minusYears(3), now.minusYears(2), now.minusYears(1), now);
        year.setValue(now);
        year.setWidth("100px");
        year.setWidth("100px");

        header = createHeader("Inventory valuation", "Track potential profit");
        header.add(year);

        // Chart
        chart = ApexChartsBuilder.get();
        NoData noData = new NoData();
        Theme theme = new Theme();
        Cookie cookieByName = CookieUtil.getCookieByName(CookieUtil.THEME);
        if (cookieByName == null || cookieByName.getValue().equalsIgnoreCase("DARK")) {
            theme.setMode(Mode.DARK);
        } else {
            theme.setMode(Mode.LIGHT);
        }
        chart.withChart(ChartBuilder.get().withHeight("400px").withZoom(ZoomBuilder.get().withEnabled(true).build()).build())//
//                .withStroke(StrokeBuilder.get().withCurve(Curve.SMOOTH).build())//
                .withTheme(theme).withNoData(noData)//e
                .withGrid(GridBuilder.get().withRow(RowBuilder.get().build()).build());//

        XAxisBuilder xAxis = XAxisBuilder.get();
        XAxis axis = xAxis.withCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").build();
        chart.withXaxis(axis);


        // Add it all together
        viewEvents = new VerticalLayout(header, chart.build());
        viewEvents.addClassName(LumoUtility.Padding.LARGE);
        viewEvents.setPadding(false);
        viewEvents.setSpacing(false);
        viewEvents.getElement().getThemeList().add("spacing-l");

        refreshGrid(now);

        return viewEvents;
    }

    private void refreshGrid(LocalDate now) {
//        dashboardView.setSubmit(dashboardView.getExecutorService().submit(() -> {}));

        Map<Pair<Integer, Month>, List<InventoryValuation>> collect = inventoryValuationStorage.allInventoryValuations(getBusinessId()).stream().collect(Collectors.groupingBy(g -> Pair.of(g.getLocalDate().getYear(), g.getLocalDate().getMonth())));
        int year1 = now.getYear();
        Number[] data = new Number[]{getValue(collect, year1, Month.JANUARY, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.FEBRUARY, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.MARCH, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.APRIL, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.MAY, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.JUNE, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.JULY, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.AUGUST, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.SEPTEMBER, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.OCTOBER, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.NOVEMBER, InventoryValuation::getInventoryValue),//
                getValue(collect, year1, Month.DECEMBER, InventoryValuation::getInventoryValue)

        };

        int year2 = year1 - 1;
        Number[] data1 = new Number[]{getValue(collect, year2, Month.JANUARY, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.FEBRUARY, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.MARCH, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.APRIL, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.MAY, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.JUNE, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.JULY, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.AUGUST, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.SEPTEMBER, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.OCTOBER, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.NOVEMBER, InventoryValuation::getInventoryValue),//
                getValue(collect, year2, Month.DECEMBER, InventoryValuation::getInventoryValue)

        };

        Series<Number>[] listSeries = new Series[2];

        Series<Number> flowIn = new Series<Number>();
        flowIn.setName("Inventory value");
        flowIn.setData(data);
        flowIn.setType(SeriesType.LINE);
        listSeries[0] = flowIn;


        Series<Number> flowOut = new Series<Number>();
        flowOut.setName("Inventory value last year");
        flowOut.setData(data1);
        flowOut.setType(SeriesType.COLUMN);
        listSeries[1] = flowOut;

//        dashboardView.setSubmit2(ui.access(() -> {
//
//        }));
        chart.withSeries(listSeries);
        viewEvents.removeAll();
        viewEvents.add(header, chart.build());

    }

    private Component createServiceHealth() {
        // Header
        HorizontalLayout header = createHeader("Service health", "Input / output");

        // Grid
        Grid<ServiceHealth> grid = new Grid();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setAllRowsVisible(true);

        grid.addColumn(new ComponentRenderer<Span, ServiceHealth>(serviceHealth -> {
            Span status = new Span();
            String statusText = getStatusDisplayName(serviceHealth);
            status.getElement().setAttribute("aria-label", "Status: " + statusText);
            status.getElement().setAttribute("title", "Status: " + statusText);
            status.getElement().getThemeList().add(getStatusTheme(serviceHealth));
            return status;
        })).setHeader("").setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(ServiceHealth::getCity).setHeader("City").setFlexGrow(1);
        grid.addColumn(ServiceHealth::getInput).setHeader("Input").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(ServiceHealth::getOutput).setHeader("Output").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);

        grid.setItems(new ServiceHealth(ServiceHealth.Status.EXCELLENT, "Münster", BigDecimal.valueOf(324), 1540), new ServiceHealth(ServiceHealth.Status.OK, "Cluj-Napoca", BigDecimal.valueOf(311), 1320), new ServiceHealth(ServiceHealth.Status.FAILING, "Ciudad Victoria", BigDecimal.valueOf(300), 1219));

        // Add it all together
        VerticalLayout serviceHealth = new VerticalLayout(header, grid);
        serviceHealth.addClassName(LumoUtility.Padding.LARGE);
        serviceHealth.setPadding(false);
        serviceHealth.setSpacing(false);
        serviceHealth.getElement().getThemeList().add("spacing-l");
        return serviceHealth;
    }

    private Component createResponseTimes() {
        HorizontalLayout header = createHeader("Response times", "Average across all systems");

        // Chart
        ApexChartsBuilder chart = ApexChartsBuilder.get();
        NoData noData = new NoData();
        noData.setText("No data present at the moment");
        chart.withChart(ChartBuilder.get().withType(Type.PIE).withHeight("400px").build())
//                .withLabels("Team A", "Team B", "Team C", "Team D", "Team E")
                .withLegend(LegendBuilder.get().withPosition(Position.RIGHT).build())
//                .withSeries(44.0, 55.0, 13.0, 43.0, 22.0)
                .withResponsive(ResponsiveBuilder.get().withBreakpoint(480.0).withOptions(OptionsBuilder.get().withLegend(LegendBuilder.get().withPosition(Position.BOTTOM).build()).build()).build()).withNoData(noData);
//        Configuration conf = chart.getConfiguration();
//        conf.getChart().setStyledMode(true);
//        chart.setThemeName("gradient");
//
//        DataSeries series = new DataSeries();
//        series.add(new DataSeriesItem("System 1", 12.5));
//        series.add(new DataSeriesItem("System 2", 12.5));
//        series.add(new DataSeriesItem("System 3", 12.5));
//        series.add(new DataSeriesItem("System 4", 12.5));
//        series.add(new DataSeriesItem("System 5", 12.5));
//        series.add(new DataSeriesItem("System 6", 12.5));
//        conf.addSeries(series);

        // Add it all together
        VerticalLayout serviceHealth = new VerticalLayout(header, chart.build());
        serviceHealth.addClassName(LumoUtility.Padding.LARGE);
        serviceHealth.setPadding(false);
        serviceHealth.setSpacing(false);
        serviceHealth.getElement().getThemeList().add("spacing-l");
        return serviceHealth;
    }

    private HorizontalLayout createHeader(String title, String subtitle) {
        H2 h2 = new H2(title);
        h2.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE);

        Span span = new Span(subtitle);
        span.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        VerticalLayout column = new VerticalLayout(h2, span);
        column.setPadding(false);
        column.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(column);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setSpacing(false);
        header.setWidthFull();
        return header;
    }

    private String getStatusDisplayName(ServiceHealth serviceHealth) {
        ServiceHealth.Status status = serviceHealth.getStatus();
        if (status == ServiceHealth.Status.OK) {
            return "Ok";
        } else if (status == ServiceHealth.Status.FAILING) {
            return "Failing";
        } else if (status == ServiceHealth.Status.EXCELLENT) {
            return "Excellent";
        } else {
            return status.toString();
        }
    }

    private String getStatusTheme(ServiceHealth serviceHealth) {
        ServiceHealth.Status status = serviceHealth.getStatus();
        String theme = "badge primary small";
        if (status == ServiceHealth.Status.EXCELLENT) {
            theme += " success";
        } else if (status == ServiceHealth.Status.FAILING) {
            theme += " error";
        }
        return theme;
    }

    public Long getBusinessId() {
        return businessId;
    }
}