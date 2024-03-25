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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import jakarta.annotation.security.PermitAll;
import org.vaadin.addons.yuri0x7c1.bslayout.BsColumn;
import org.vaadin.addons.yuri0x7c1.bslayout.BsLayout;
import org.vaadin.addons.yuri0x7c1.bslayout.BsRow;
import sr.we.views.MainLayout;
import sr.we.views.dashboard.ServiceHealth.Status;

@PageTitle("Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends Main {

    public DashboardView() {
        addClassName("dashboard-view");

        BsLayout board = new BsLayout();
        board.addRow(new BsRow(new BsColumn(createHighlight("Current users", "745", 33.7)).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("View events", "54.6k", -112.45)).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Conversion rate", "18%", 3.9)).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Custom metric", "-123.45", 0.0)).withSize(BsColumn.Size.XS)));
        board.addRow(new BsRow(new BsColumn(createViewEvents()).withSize(BsColumn.Size.XS)));
        board.addRow(new BsRow(new BsColumn(createServiceHealth()).withSize(BsColumn.Size.XS), new BsColumn(createResponseTimes()).withSize(BsColumn.Size.XS)));
        add(board);
    }

    private Component createHighlight(String title, String value, Double percentage) {
        VaadinIcon icon = VaadinIcon.ARROW_UP;
        String prefix = "";
        String theme = "badge";

        if (percentage == 0) {
            prefix = "±";
        } else if (percentage > 0) {
            prefix = "+";
            theme += " success";
        } else if (percentage < 0) {
            icon = VaadinIcon.ARROW_DOWN;
            theme += " error";
        }

        H2 h2 = new H2(title);
        h2.addClassNames(FontWeight.NORMAL, Margin.NONE, TextColor.SECONDARY, FontSize.XSMALL);

        Span span = new Span(value);
        span.addClassNames(FontWeight.SEMIBOLD, FontSize.XXXLARGE);

        Icon i = icon.create();
        i.addClassNames(BoxSizing.BORDER, Padding.XSMALL);

        Span badge = new Span(i, new Span(prefix + percentage));
        badge.getElement().getThemeList().add(theme);

        VerticalLayout layout = new VerticalLayout(h2, span, badge);
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

}
