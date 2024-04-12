package sr.we.views.dashboard;


import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.NoData;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.grid.builder.RowBuilder;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.responsive.builder.OptionsBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import jakarta.annotation.security.PermitAll;
import org.vaadin.addons.yuri0x7c1.bslayout.BsColumn;
import org.vaadin.addons.yuri0x7c1.bslayout.BsLayout;
import org.vaadin.addons.yuri0x7c1.bslayout.BsRow;
import sr.we.controllers.ItemsController;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.security.AuthenticatedUser;
import sr.we.views.MainLayout;
import sr.we.views.dashboard.ServiceHealth.Status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@PageTitle("Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends Main implements BeforeEnterObserver {


    private final ItemsController ItemService;
    private final AuthenticatedUser authenticatedUser;
    private final UI ui;
    private User user;

    public DashboardView(ItemsController ItemService, AuthenticatedUser authenticatedUser) {
        addClassName("dashboard-view");
        this.ui = UI.getCurrent();
        this.ItemService = ItemService;
        this.authenticatedUser = authenticatedUser;

        Optional<User> maybeUser = authenticatedUser.get();
        maybeUser.ifPresent(value -> user = value);

        BsLayout board = new BsLayout();
        board.addRow(new BsRow(new BsColumn(createHighlight("Total inventory value", span -> {
            new Thread(() -> {
                BigDecimal reduce = ItemService.allItems(getBusinessId(), 0, Integer.MAX_VALUE, user, DashboardView::check).filter(f -> f.getStock_level() > 0).map(item -> item.getVariant().getCost() != null ? item.getVariant().getCost().multiply(BigDecimal.valueOf(item.getStock_level())) : BigDecimal.ZERO).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                ui.access(() -> {
                    String format = new DecimalFormat("###,###,###,###,###,##0.00").format(reduce);
                    span.setText(format);
                });
            }).start();
            return null;
        })).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Total retail value", span -> {
                    new Thread(() -> {
                        BigDecimal reduce = ItemService.allItems(getBusinessId(), 0, Integer.MAX_VALUE, user, DashboardView::check).filter(f -> f.getStock_level() > 0).map(item -> item.getVariantStore().getPrice() != 0 ? BigDecimal.valueOf(item.getVariantStore().getPrice()).multiply(BigDecimal.valueOf(item.getStock_level())) : BigDecimal.ZERO).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                        ui.access(() -> {
                            String format = new DecimalFormat("###,###,###,###,###,##0.00").format(reduce);
                            span.setText(format);
                        });
                    }).start();
                    return null;
                })).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Potential profit", span -> {
                    new Thread(() -> {
                        List<Item> itemStream = ItemService.allItems(getBusinessId(), 0, Integer.MAX_VALUE, user, DashboardView::check).filter(f -> f.getStock_level() > 0).toList();
                        BigDecimal price = itemStream.stream().map(item -> item.getVariantStore().getPrice() != 0 ? BigDecimal.valueOf(item.getVariantStore().getPrice()).multiply(BigDecimal.valueOf(item.getStock_level())) : BigDecimal.ZERO).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal cost = itemStream.stream().map(item -> item.getVariant().getCost() != null ? item.getVariant().getCost().multiply(BigDecimal.valueOf(item.getStock_level())) : BigDecimal.ZERO).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                        ui.access(() -> {
                            String format = new DecimalFormat("###,###,###,###,###,##0.00").format(price.subtract(cost));
                            span.setText(format);
                        });
                    }).start();
                    return null;
                })).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Margin", span -> {
                    new Thread(() -> {
                        List<Item> itemStream = ItemService.allItems(getBusinessId(), 0, Integer.MAX_VALUE, user, DashboardView::check).filter(f -> f.getStock_level() > 0).toList();
                        BigDecimal price = itemStream.stream().map(item -> item.getVariantStore().getPrice() != 0 ? BigDecimal.valueOf(item.getVariantStore().getPrice()).multiply(BigDecimal.valueOf(item.getStock_level())) : BigDecimal.ZERO).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal cost = itemStream.stream().map(item -> item.getVariant().getCost() != null ? item.getVariant().getCost().multiply(BigDecimal.valueOf(item.getStock_level())) : BigDecimal.ZERO).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                        ui.access(() -> {
                            String format = new DecimalFormat(" #,##0.00 '%'").format(BigDecimal.valueOf(100).subtract(cost.divide(price, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))));
                            span.setText(format);
                        });
                    }).start();
                    return null;
                })).withSize(BsColumn.Size.XS)));
        board.addRow(new BsRow(new BsColumn(createViewEvents()).withSize(BsColumn.Size.XS)));
        board.addRow(new BsRow(new BsColumn(createServiceHealth()).withSize(BsColumn.Size.XS), new BsColumn(createResponseTimes()).withSize(BsColumn.Size.XS)));
        add(board);
    }

    private static boolean check(Item item) {
        return true;
    }

    private Long getBusinessId() {
        return 0L;
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
        h2.addClassNames(FontWeight.NORMAL, Margin.NONE, TextColor.SECONDARY, FontSize.XSMALL);

        Span span = new Span();

        span.addClassNames(FontWeight.SEMIBOLD, FontSize.XXXLARGE);

        buildParameter.build(span);

        Icon i = icon.create();
        i.addClassNames(BoxSizing.BORDER, Padding.XSMALL);

//        Span badge = new Span(i, new Span(prefix + percentage));
//        badge.getElement().getThemeList().add(theme);

        VerticalLayout layout = new VerticalLayout(h2, span);
        layout.addClassName(Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private Component createViewEvents() {
        // Header
        Select year = new Select();
        year.setItems("2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021");
        year.setValue("2021");
        year.setWidth("100px");

        HorizontalLayout header = createHeader("View events", "City/month");
        header.add(year);

        // Chart
        ApexChartsBuilder chart = ApexChartsBuilder.get();
        NoData noData = new NoData();
        chart.withChart(ChartBuilder.get().withType(Type.LINE).withHeight("400px").withZoom(ZoomBuilder.get().withEnabled(true).build()).build())//
                .withStroke(StrokeBuilder.get().withCurve(Curve.SMOOTH).build())//
                .withNoData(noData)//e
                .withGrid(GridBuilder.get().withRow(RowBuilder.get().withColors("#f3f3f3", "transparent").withOpacity(0.5).build()).build());//
//        Configuration conf = chart.getConfiguration();
//        conf.getChart().setStyledMode(true);
//
//        XAxis xAxis = new XAxis();
//        xAxis.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
//        conf.addxAxis(xAxis);
//
//        conf.getyAxis().setTitle("Values");
//
//        PlotOptionsAreaspline plotOptions = new PlotOptionsAreaspline();
//        plotOptions.setPointPlacement(PointPlacement.ON);
//        plotOptions.setMarker(new Marker(false));
//        conf.addPlotOptions(plotOptions);
//
//        conf.addSeries(new ListSeries("Berlin", 189, 191, 291, 396, 501, 403, 609, 712, 729, 942, 1044, 1247));
//        conf.addSeries(new ListSeries("London", 138, 246, 248, 348, 352, 353, 463, 573, 778, 779, 885, 887));
//        conf.addSeries(new ListSeries("New York", 65, 65, 166, 171, 293, 302, 308, 317, 427, 429, 535, 636));
//        conf.addSeries(new ListSeries("Tokyo", 0, 11, 17, 123, 130, 142, 248, 349, 452, 454, 458, 462));

        // Add it all together
        VerticalLayout viewEvents = new VerticalLayout(header, chart.build());
        viewEvents.addClassName(Padding.LARGE);
        viewEvents.setPadding(false);
        viewEvents.setSpacing(false);
        viewEvents.getElement().getThemeList().add("spacing-l");
        return viewEvents;
    }

    private Component createServiceHealth() {
        // Header
        HorizontalLayout header = createHeader("Service health", "Input / output");

        // Grid
        Grid<ServiceHealth> grid = new Grid();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setAllRowsVisible(true);

        grid.addColumn(new ComponentRenderer<>(serviceHealth -> {
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

        grid.setItems(new ServiceHealth(Status.EXCELLENT, "Münster", 324, 1540), new ServiceHealth(Status.OK, "Cluj-Napoca", 311, 1320), new ServiceHealth(Status.FAILING, "Ciudad Victoria", 300, 1219));

        // Add it all together
        VerticalLayout serviceHealth = new VerticalLayout(header, grid);
        serviceHealth.addClassName(Padding.LARGE);
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
                .withLegend(LegendBuilder.get().withPosition(com.github.appreciated.apexcharts.config.legend.Position.RIGHT).build())
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
        serviceHealth.addClassName(Padding.LARGE);
        serviceHealth.setPadding(false);
        serviceHealth.setSpacing(false);
        serviceHealth.getElement().getThemeList().add("spacing-l");
        return serviceHealth;
    }

    private HorizontalLayout createHeader(String title, String subtitle) {
        H2 h2 = new H2(title);
        h2.addClassNames(FontSize.XLARGE, Margin.NONE);

        Span span = new Span(subtitle);
        span.addClassNames(TextColor.SECONDARY, FontSize.XSMALL);

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
        Status status = serviceHealth.getStatus();
        if (status == Status.OK) {
            return "Ok";
        } else if (status == Status.FAILING) {
            return "Failing";
        } else if (status == Status.EXCELLENT) {
            return "Excellent";
        } else {
            return status.toString();
        }
    }

    private String getStatusTheme(ServiceHealth serviceHealth) {
        Status status = serviceHealth.getStatus();
        String theme = "badge primary small";
        if (status == Status.EXCELLENT) {
            theme += " success";
        } else if (status == Status.FAILING) {
            theme += " error";
        }
        return theme;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<User> maybeUser = authenticatedUser.get();
        maybeUser.ifPresent(value -> user = value);
    }
}
