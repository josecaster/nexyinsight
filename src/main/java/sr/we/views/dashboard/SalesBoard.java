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
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
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
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.servlet.http.Cookie;
import org.apache.commons.lang3.tuple.Pair;
import org.vaadin.addons.yuri0x7c1.bslayout.BsColumn;
import org.vaadin.addons.yuri0x7c1.bslayout.BsLayout;
import org.vaadin.addons.yuri0x7c1.bslayout.BsRow;
import sr.we.controllers.ReceiptsController;
import sr.we.controllers.StoresController;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.entity.eclipsestore.tables.Receipt;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.views.CookieUtil;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class SalesBoard implements Serializable {
    private final DashboardView dashboardView;
    private final Long businessId;
    private final ReceiptsController receiptsController;
    private final UI ui;

    private final BsLayout board;
    private final StoresController storesController;
    private HorizontalLayout header;
    private VerticalLayout viewEvents;
    private ApexChartsBuilder chart;
    private Set<String> sectionIds;
    private List<ServiceHealth> list;

    public SalesBoard(DashboardView dashboardView, Long businessId, ReceiptsController receiptsController, UI ui, BsLayout board, StoresController storesController) {
        this.dashboardView = dashboardView;
        this.storesController = storesController;
        this.businessId = businessId;
        this.receiptsController = receiptsController;
        this.ui = ui;
        this.board = board;
    }

    private static boolean check(Item item) {
        return true;
    }


    private static BigDecimal getValue(List<ReceiptsController.Bar> collect, Integer year, Month month) {
        Stream<ReceiptsController.Bar> entryStream = collect.stream().filter(f -> f.year().compareTo(year.longValue()) == 0 && f.month().compareTo((long) month.getValue()) == 0);
//        List<Map.Entry<Pair<Integer, Month>, List<Receipt>>> list = entryStream.toList();
        return entryStream.map(ReceiptsController.Bar::get).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal getValue(Map<Pair<Integer, Month>, List<Receipt>> collect, Integer year, Month month, Function<Receipt, BigDecimal> function) {
        Stream<Map.Entry<Pair<Integer, Month>, List<Receipt>>> entryStream = collect.entrySet().stream().filter(f -> f.getKey().getKey().compareTo(year) == 0 && f.getKey().getValue().compareTo(month) == 0);
        List<Map.Entry<Pair<Integer, Month>, List<Receipt>>> list = entryStream.toList();
        return list.stream().map(Map.Entry::getValue).flatMap(List::stream).map(function).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal getValue(Map<Pair<Integer, Month>, List<Receipt>> collect, Function<Receipt, BigDecimal> function) {
        Stream<Map.Entry<Pair<Integer, Month>, List<Receipt>>> entryStream = collect.entrySet().stream();
        List<Map.Entry<Pair<Integer, Month>, List<Receipt>>> list = entryStream.toList();
        return list.stream().map(Map.Entry::getValue).flatMap(List::stream).map(function).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void build(LocalDate start, LocalDate end, Set<String> sectionIds) {
        this.sectionIds = sectionIds;

        board.addRow(new BsRow(new BsColumn(createHighlight("Gross sales", span -> {
            new Thread(() -> {
                ReceiptsController.Value inventoryValuationOptional = receiptsController.gross(getBusinessId(), start, end, sectionIds);
//            BigDecimal grossSales = inventoryValuationOptional.stream().filter(f -> f.getReceipt_type().equalsIgnoreCase("SALE")).map(r -> r.getLine_item().getGross_total_money()).reduce(BigDecimal.ZERO, BigDecimal::add);
                ui.access(() -> {
                    String format = new DecimalFormat("###,###,###,###,###,##0.00").format(inventoryValuationOptional.get());
                    span.setText(format);
                });
            }).start();
            return null;
        })).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Refunds", span -> {
                    new Thread(() -> {
//                    List<Receipt> inventoryValuationOptional = receiptsController.receipts(getBusinessId(), start, end, sectionIds);
//                    BigDecimal refunds = inventoryValuationOptional.stream().filter(f -> f.getReceipt_type().equalsIgnoreCase("REFUND")).map(r -> r.getLine_item().getGross_total_money()).reduce(BigDecimal.ZERO, BigDecimal::add);
                        ReceiptsController.Value refunds = receiptsController.refund(getBusinessId(), start, end, sectionIds);
                        ui.access(() -> {
                            String format = new DecimalFormat("###,###,###,###,###,##0.00").format(refunds.get());
                            span.setText(format);
                        });
                    }).start();
                    return null;
                })).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Discounts", span -> {
                    new Thread(() -> {
//                    List<Receipt> inventoryValuationOptional = receiptsController.receipts(getBusinessId(), start, end, sectionIds);
//                    BigDecimal discounts = inventoryValuationOptional.stream().filter(f -> f.getReceipt_type().equalsIgnoreCase("SALE")).map(r -> r.getLine_item().getTotal_discount()).reduce(BigDecimal.ZERO, BigDecimal::add);
                        ReceiptsController.Value discounts = receiptsController.discount(getBusinessId(), start, end, sectionIds);
                        ui.access(() -> {
                            String format = new DecimalFormat("###,###,###,###,###,##0.00").format(discounts.get());
                            span.setText(format);
                        });
                    }).start();
                    return null;
                })).withSize(BsColumn.Size.XS), //
                new BsColumn(createHighlight("Net sales", span -> {
                    new Thread(() -> {
//                    List<Receipt> inventoryValuationOptional = receiptsController.receipts(getBusinessId(), start, end, sectionIds);
                        Double grossSales = receiptsController.gross(getBusinessId(), start, end, sectionIds).get();
                        Double refunds = receiptsController.refund(getBusinessId(), start, end, sectionIds).get();
                        Double discounts = receiptsController.discount(getBusinessId(), start, end, sectionIds).get();
                        ui.access(() -> {
                            String format = new DecimalFormat("###,###,###,###,###,##0.00").format(grossSales - (refunds) - (discounts));
                            span.setText(format);
                        });
                    }).start();
                    return null;
                })).withSize(BsColumn.Size.XS)));
        board.addRow(new BsRow(new BsColumn(createViewEvents()).withSize(BsColumn.Size.XS)));
        board.addRow(new BsRow(new BsColumn(createServiceHealth(start, end)).withSize(BsColumn.Size.XS), new BsColumn(createResponseTimes(start, end)).withSize(BsColumn.Size.XS)));
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

        header = createHeader("Sales valuation", "Track sales");
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
//        dashboardView.setSubmit(dashboardView.getExecutorService().submit(() -> {
//
//        }));
        new Thread(() -> {
            List<ReceiptsController.Bar> collect = receiptsController.chart(getBusinessId(), (long) LocalDate.now().getYear(), this.sectionIds);
//        Map<Pair<Integer, Month>, List<Receipt>> collect = receiptsController.receipts(getBusinessId(), this.sectionIds).stream().filter(f -> f.getCancelled_at() == null && f.getReceipt_type().equalsIgnoreCase("SALE")).collect(Collectors.groupingBy(g -> Pair.of(g.getReceipt_date().getYear(), g.getReceipt_date().getMonth())));
            int year1 = now.getYear();
            Number[] data = new Number[]{getValue(collect, year1, Month.JANUARY),//
                    getValue(collect, year1, Month.FEBRUARY),//
                    getValue(collect, year1, Month.MARCH),//
                    getValue(collect, year1, Month.APRIL),//
                    getValue(collect, year1, Month.MAY),//
                    getValue(collect, year1, Month.JUNE),//
                    getValue(collect, year1, Month.JULY),//
                    getValue(collect, year1, Month.AUGUST),//
                    getValue(collect, year1, Month.SEPTEMBER),//
                    getValue(collect, year1, Month.OCTOBER),//
                    getValue(collect, year1, Month.NOVEMBER),//
                    getValue(collect, year1, Month.DECEMBER)

            };

            int year2 = year1 - 1;
            collect = receiptsController.chart(getBusinessId(), (long) year2, this.sectionIds);
            Number[] data1 = new Number[]{getValue(collect, year2, Month.JANUARY),//
                    getValue(collect, year2, Month.FEBRUARY),//
                    getValue(collect, year2, Month.MARCH),//
                    getValue(collect, year2, Month.APRIL),//
                    getValue(collect, year2, Month.MAY),//
                    getValue(collect, year2, Month.JUNE),//
                    getValue(collect, year2, Month.JULY),//
                    getValue(collect, year2, Month.AUGUST),//
                    getValue(collect, year2, Month.SEPTEMBER),//
                    getValue(collect, year2, Month.OCTOBER),//
                    getValue(collect, year2, Month.NOVEMBER),//
                    getValue(collect, year2, Month.DECEMBER)

            };

            Series<Number>[] listSeries = new Series[2];

            Series<Number> flowIn = new Series<Number>();
            flowIn.setName("Sales value");
            flowIn.setData(data);
            flowIn.setType(SeriesType.LINE);
            listSeries[0] = flowIn;


            Series<Number> flowOut = new Series<Number>();
            flowOut.setName("Sales value last year");
            flowOut.setData(data1);
            flowOut.setType(SeriesType.COLUMN);
            listSeries[1] = flowOut;

            ui.access(() -> {
                chart.withSeries(listSeries);
                viewEvents.removeAll();
                viewEvents.add(header, chart.build());
            });
        }).start();
//        dashboardView.setSubmit2(ui.access(() -> {
//        }));
    }

    private Component createServiceHealth(LocalDate start, LocalDate end) {
        // Header
        HorizontalLayout header = createHeader("Top ranking", "Most earned per given period");

        // Grid
        Grid<ServiceHealth> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setAllRowsVisible(true);

        grid.addColumn(new ComponentRenderer<Avatar, ServiceHealth>(serviceHealth -> {
            Avatar avatar = new Avatar(serviceHealth.getCity());
            if (serviceHealth.getProfilePicture() != null) {
                StreamResource resource = new StreamResource("profile-pic", () -> new ByteArrayInputStream(serviceHealth.getProfilePicture()));
                avatar.setImageResource(resource);
            }
            avatar.getElement().setAttribute("tabindex", "-1");
            return avatar;
        })).setHeader("").setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(ServiceHealth::getCity).setHeader("Section").setFlexGrow(1);
        grid.addColumn(ServiceHealth::getInput).setHeader("Gross sales").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);


        new Thread(() -> {
            List<ServiceHealth> list = new ArrayList<>();
            for (String sectionId : this.sectionIds) {
                ReceiptsController.Value gross = receiptsController.gross(getBusinessId(), start, end, new HashSet<>(List.of(sectionId)));
                Section section = storesController.oneStore(sectionId);
//                if (!section.isDefault()) {
                String name = section.getName();
                ServiceHealth munster = new ServiceHealth(ServiceHealth.Status.FAILING, name, BigDecimal.valueOf(gross.get()), 0);
                munster.setProfilePicture(section.getProfilePicture());
                list.add(munster);
//                }
            }
            list = list.stream().sorted(Comparator.comparing(ServiceHealth::getInput).reversed()).toList();
            List<ServiceHealth> finalList = list;
            list.forEach(c -> {
                if (finalList.indexOf(c) == 0) {
                    c.setStatus(ServiceHealth.Status.EXCELLENT);
                } else {
                    if (finalList.indexOf(c) == 1) {
                        c.setStatus(ServiceHealth.Status.OK);
                    } else {
                        c.setStatus(ServiceHealth.Status.FAILING);
                    }
                }
            });
            List<ServiceHealth> finalList1 = list;
            ui.access(() -> {
                grid.setItems(finalList1);
            });
        }).start();
        grid.setHeight("400px");

        // Add it all together
        VerticalLayout serviceHealth = new VerticalLayout(header, grid);
        serviceHealth.addClassName(LumoUtility.Padding.LARGE);
        serviceHealth.setPadding(false);
        serviceHealth.setSpacing(false);
        serviceHealth.getElement().getThemeList().add("spacing-l");
        return serviceHealth;
    }

    private Component createResponseTimes(LocalDate start, LocalDate end) {
        HorizontalLayout header = createHeader("Section Traffic Coverage", "The amount of receipts each section covered per given period");
        VerticalLayout serviceHealth = new VerticalLayout(header);
        new Thread(() -> {
            List<DataSeriesItem> list = new ArrayList<>();
            ReceiptsController.Value val = receiptsController.count(getBusinessId(), start, end, sectionIds);
            Double left = val.get();
            for (String sectionId : this.sectionIds) {

                ReceiptsController.Value val2 = receiptsController.count(getBusinessId(), start, end, new HashSet<>(List.of(sectionId)));
                Double count = val2.get();
                Section section = storesController.oneStore(sectionId);
//                if (!section.isDefault()) {
                left = left - count;
                String name = section.getName();
                DataSeriesItem munster = new DataSeriesItem(name, count);
                list.add(munster);
//                }
            }

            if (left > 0) {
                DataSeriesItem munster = new DataSeriesItem("Other sections", left);
                list.add(munster);
            }

            List<String> list1 = list.stream().map(DataSeriesItem::getName).toList();
            List<Double> list2 = list.stream().map(f -> f.getY().doubleValue()).toList();

            // Chart
            ApexChartsBuilder chart = ApexChartsBuilder.get();
            NoData noData = new NoData();
            noData.setText("No data present at the moment");
            chart.withChart(ChartBuilder.get().withType(Type.PIE).withHeight("400px").build()).withLabels(list1.toArray(new String[0])).withLegend(LegendBuilder.get().withPosition(Position.RIGHT).build()).withSeries(list2.toArray(new Double[0])).withResponsive(ResponsiveBuilder.get().withBreakpoint(480.0).withOptions(OptionsBuilder.get().withLegend(LegendBuilder.get().withPosition(Position.BOTTOM).build()).build()).build()).withNoData(noData);

            ui.access(() -> {
                serviceHealth.add(chart.build());
            });
        }).start();

        // Add it all together

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
//        ServiceHealth.Status status = serviceHealth.getStatus();
//        if (status == ServiceHealth.Status.OK) {
//            return "Ok";
//        } else if (status == ServiceHealth.Status.FAILING) {
//            return "Failing";
//        } else if (status == ServiceHealth.Status.EXCELLENT) {
//            return "Excellent";
//        } else {
//            return status.toString();
//        }
        return "#";
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