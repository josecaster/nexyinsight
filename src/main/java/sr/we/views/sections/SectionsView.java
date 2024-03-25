package sr.we.views.sections;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import sr.we.entity.eclipsestore.tables.Category;
import sr.we.entity.eclipsestore.tables.Device;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.views.MainLayout;

import java.util.*;

@PageTitle("Sections")
@Route(value = "sections", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class SectionsView extends Div implements BeforeEnterObserver {

    private Grid<Section> grid;
    private GridListDataView<Section> gridListDataView;
    private Binder<Section> binder;
    private List<Section> sections;
    private List<Device> deciveList;
    private List<Category> categoryList;
    private ComboBox<String> storeComboBox;
    private Grid.Column<Section> myStoreNameColumn;
    private MultiSelectComboBox<String> categoryComboBox;
    private MultiSelectComboBox<String> deviceComboBox;

    public SectionsView() {
        addClassName("sections-view");
        setSizeFull();
        createGrid();
        add(grid);
    }

    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
        addFiltersToGrid();
    }

    private void createGridComponent() {
        grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("100%");

        List<Section> sections = getSections();
        gridListDataView = grid.setItems(sections);
    }

    private void addColumnsToGrid() {
        binder = new Binder<>(Section.class);
        grid.getEditor().setBinder(binder);
        grid.getEditor().setBuffered(true);

        createStoreNameColumn();
        createLinkStoreColumn();
        createDeviceColumn();
        createCategoryColumn();
        createDefaultColumn();
        createEditColumn();


        grid.getEditor().addOpenListener(l -> {
            boolean aDefault = l.getItem().isDefault();
            storeComboBox.setReadOnly(aDefault);
        });

        grid.getEditor().addSaveListener(l -> {
            Section item = l.getItem();
            if (StringUtils.isNotBlank(item.getUuId())) {
                // call put method
                Section section = updateStore(item);
                grid.getDataProvider().refreshItem(section);
            } else {
                // call post method
                Section section = addNewStore(item);
                grid.getDataProvider().refreshItem(section);
            }
            Notification.show("Updated record successfully", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
    }

    private Section addNewStore(Section item) {
        item.setUuId(UUID.randomUUID().toString());
        return item;
    }

    private Section updateStore(Section item) {
        return item;
    }

    private void createStoreNameColumn() {
        myStoreNameColumn = grid.addColumn(Section::getName).setHeader("My store name");
        TextField firstNameField = new TextField();
        firstNameField.setWidthFull();
        myStoreNameColumn.setEditorComponent(firstNameField);
        binder.forField(firstNameField).asRequired("Store name must not be empty").bind(Section::getName, Section::setName);
    }

    private void createLinkStoreColumn() {
        Grid.Column<Section> linkStoreColumn = grid.addColumn(l -> {
            if (sections != null) {
                Optional<Section> any = sections.stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(l.getId())).findAny();
                return any.map(Section::getDefault_name).orElse(l.getId());
            }
            return l.getId();
        }).setHeader("Link store");
        storeComboBox = new ComboBox<>();
        storeComboBox.setWidthFull();
        binder.forField(storeComboBox).asRequired("Link store must not be empty").bind(Section::getId, Section::setId);
        linkStoreColumn.setEditorComponent(storeComboBox);
    }

    private void createDeviceColumn() {
        Grid.Column<Section> deviceColumn = grid.addComponentColumn(section -> {
            MultiSelectComboBox<String> deviceComboBox = new MultiSelectComboBox<>();
            deviceComboBox.setWidthFull();
            deviceComboBox.setReadOnly(true);
            deviceComboBox.setItems((deciveList.stream().map(Device::getUuId).toList()));
            if (!deciveList.isEmpty()) {
                deviceComboBox.setValue(section.getDevices());
            }
            deviceComboBox.setItemLabelGenerator(l -> {
                Optional<Device> any = deciveList.stream().filter(n -> n.getUuId().equalsIgnoreCase(l)).findAny();
                return any.map(Device::getName).orElse(l);
            });
            return deviceComboBox;
        }).setHeader("Device");

        deviceComboBox = new MultiSelectComboBox<>();
        deviceComboBox.setWidthFull();
        binder.forField(deviceComboBox).bind(Section::getDevices, Section::setDevices);
        deviceColumn.setEditorComponent(deviceComboBox);
    }

    private void createCategoryColumn() {
        Grid.Column<Section> categoryColumn = grid.addComponentColumn(section -> {
            MultiSelectComboBox<String> categoryComboBox = new MultiSelectComboBox<>();
            categoryComboBox.setWidthFull();
            categoryComboBox.setReadOnly(true);
            categoryComboBox.setItems((categoryList.stream().map(Category::getUuId).toList()));
            if (!categoryList.isEmpty()) {
                categoryComboBox.setValue(section.getCategories());
            }
            categoryComboBox.setItemLabelGenerator(l -> {
                Optional<Category> any = categoryList.stream().filter(n -> n.getUuId().equalsIgnoreCase(l)).findAny();
                return any.map(Category::getName).orElse(l);
            });
            return categoryComboBox;
        }).setHeader("Category");

        categoryComboBox = new MultiSelectComboBox<>();
        categoryComboBox.setWidthFull();
        binder.forField(categoryComboBox).bind(Section::getCategories, Section::setCategories);
        categoryColumn.setEditorComponent(categoryComboBox);
    }

    private void createDefaultColumn() {
        grid.addComponentColumn(store -> {
            Checkbox checkbox = new Checkbox(store.isDefault());
            checkbox.setReadOnly(true);
            return checkbox;
        });
    }

    private void createEditColumn() {
        Grid.Column<Section> editColumn = grid.addComponentColumn(store -> {
            Button editButton = new Button("Edit");
            editButton.addClickListener(e -> {
                if (grid.getEditor().isOpen()) grid.getEditor().cancel();
                grid.getEditor().editItem(store);
            });
            return editButton;
        }).setWidth("150px").setFlexGrow(0);

        Button saveButton = new Button("Save", e -> grid.getEditor().save());
        Button cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> grid.getEditor().cancel());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
        HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
        actions.setPadding(false);
        editColumn.setEditorComponent(actions);
    }

//    private void createSectionColumn() {
//        sectionColumn = grid.addColumn(new ComponentRenderer<>(section -> {
//            HorizontalLayout hl = new HorizontalLayout();
//            hl.setAlignItems(Alignment.CENTER);
//            Image img = new Image(section.getImg(), "");
//            Span span = new Span();
//            span.setClassName("name");
//            span.setText(section.getSection());
//            hl.add(img, span);
//            return hl;
//        })).setComparator(section -> section.getSection()).setHeader("Section");
//    }
//
//    private void createAmountColumn() {
//        amountColumn = grid.addColumn(Section::getAmount).setHeader("Amount");
////                .addEditColumn(Section::getAmount,
////                        new NumberRenderer<>(section -> section.getAmount(), NumberFormat.getCurrencyInstance(Locale.US)))
////                .text((item, newValue) -> item.setAmount(Double.parseDouble(newValue)))
////                .setComparator(section -> section.getAmount()).setHeader("Amount");
//    }
//
//    private void createStatusColumn() {
//        statusColumn = grid.addComponentColumn(section -> {
//            Span span = new Span();
//            span.setText(section.getStatus());
//            span.getElement().setAttribute("theme", "badge " + section.getStatus().toLowerCase());
//            return span;
//        }).setHeader("Status");
////                .addEditColumn(Section::getSection, new ComponentRenderer<>(section -> {
////            Span span = new Span();
////            span.setText(section.getStatus());
////            span.getElement().setAttribute("theme", "badge " + section.getStatus().toLowerCase());
////            return span;
////        })).select((item, newValue) -> item.setStatus(newValue), Arrays.asList("Pending", "Success", "Error"))
////                .setComparator(section -> section.getStatus()).setHeader("Status");
//    }
//
//    private void createDateColumn() {
//        dateColumn = grid.addColumn(new LocalDateRenderer<>(section -> LocalDate.parse(section.getDate()), () -> DateTimeFormatter.ofPattern("M/d/yyyy"))).setComparator(section -> section.getDate()).setHeader("Date").setWidth("180px").setFlexGrow(0);
//    }

    private void addFiltersToGrid() {
        HeaderRow filterRow = grid.appendHeaderRow();

        TextField sectionFilter = new TextField();
        sectionFilter.setPlaceholder("Filter");
        sectionFilter.setClearButtonVisible(true);
        sectionFilter.setWidth("100%");
        sectionFilter.setValueChangeMode(ValueChangeMode.EAGER);
        sectionFilter.addValueChangeListener(event -> gridListDataView.addFilter(section -> StringUtils.containsIgnoreCase(section.getName(), sectionFilter.getValue())));
        filterRow.getCell(myStoreNameColumn).setComponent(sectionFilter);

//        TextField amountFilter = new TextField();
//        amountFilter.setPlaceholder("Filter");
//        amountFilter.setClearButtonVisible(true);
//        amountFilter.setWidth("100%");
//        amountFilter.setValueChangeMode(ValueChangeMode.EAGER);
//        amountFilter.addValueChangeListener(event -> gridListDataView.addFilter(section -> StringUtils.containsIgnoreCase(Double.toString(section.getAmount()), amountFilter.getValue())));
//        filterRow.getCell(amountColumn).setComponent(amountFilter);
//
//        ComboBox<String> statusFilter = new ComboBox<>();
//        statusFilter.setItems(Arrays.asList("Pending", "Success", "Error"));
//        statusFilter.setPlaceholder("Filter");
//        statusFilter.setClearButtonVisible(true);
//        statusFilter.setWidth("100%");
//        statusFilter.addValueChangeListener(event -> gridListDataView.addFilter(section -> areStatusesEqual(section, statusFilter)));
//        filterRow.getCell(statusColumn).setComponent(statusFilter);
//
//        DatePicker dateFilter = new DatePicker();
//        dateFilter.setPlaceholder("Filter");
//        dateFilter.setClearButtonVisible(true);
//        dateFilter.setWidth("100%");
//        dateFilter.addValueChangeListener(event -> gridListDataView.addFilter(section -> areDatesEqual(section, dateFilter)));
//        filterRow.getCell(dateColumn).setComponent(dateFilter);
    }

//    private boolean areStatusesEqual(Section section, ComboBox<String> statusFilter) {
//        String statusFilterValue = statusFilter.getValue();
//        if (statusFilterValue != null) {
//            return StringUtils.equals(section.getStatus(), statusFilterValue);
//        }
//        return true;
//    }
//
//    private boolean areDatesEqual(Section section, DatePicker dateFilter) {
//        LocalDate dateFilterValue = dateFilter.getValue();
//        if (dateFilterValue != null) {
//            LocalDate sectionDate = LocalDate.parse(section.getDate());
//            return dateFilterValue.equals(sectionDate);
//        }
//        return true;
//    }

    private List<Section> getSections() {
        String string = UUID.randomUUID().toString();
        return Arrays.asList(createSection(1001, string, "Tals Bijoux N.V."), //
                createSection(1002, string, "Chessed Apparel"), //
                createSection(1003, string, "Cynthia Read"), //
                createSection(1005, string, "Kimiko's"), //
                createSection(1006, string, "Mommy and Me"), //
                createSection(1007, string, "Glam By Cheora"), //
                createSection(1008, string, "Grow by ruth"), //
                createSection(1004, string, "Point Plaza", true), //
                createSection(1009, string, "Kie's Beauty shop"));
    }

    private Section createSection(int id, String img, String section) {
        return new Section((long) id, img, section);
    }

    private Section createSection(int id, String img, String section, boolean defaultSection) {
        Section section1 = createSection(id, img, section);
        section1.setUuId(id+"");
        return section1;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        sections = getSections();
        deciveList = getDevices();
        categoryList = getCategories();

        categoryComboBox.setItems((categoryList.stream().map(Category::getUuId).toList()));
        categoryComboBox.setItemLabelGenerator(l -> {
            Optional<Category> any = categoryList.stream().filter(n -> n.getUuId().equalsIgnoreCase(l)).findAny();
            return any.map(Category::getName).orElse(l);
        });

        deviceComboBox.setItems((deciveList.stream().map(Device::getUuId).toList()));
        deviceComboBox.setItemLabelGenerator(l -> {
            Optional<Device> any = deciveList.stream().filter(n -> n.getUuId().equalsIgnoreCase(l)).findAny();
            return any.map(Device::getName).orElse(l);
        });

        storeComboBox.setItems(sections.stream().filter(Section::isDefault).map(Section::getId).toList());
        storeComboBox.setItemLabelGenerator(l -> {
            Optional<Section> any = sections.stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(l)).findAny();
            return any.map(n -> StringUtils.isBlank(n.getDefault_name()) ? "!" : n.getDefault_name()).orElse(l);
        });
    }

    private List<Category> getCategories() {
        return new ArrayList<>();
    }

    private List<Device> getDevices() {
        return new ArrayList<>();
    }
}
