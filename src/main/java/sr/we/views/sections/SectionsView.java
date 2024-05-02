package sr.we.views.sections;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
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
import sr.we.controllers.CategoryController;
import sr.we.controllers.DevicesController;
import sr.we.controllers.StoresController;
import sr.we.entity.eclipsestore.tables.Category;
import sr.we.entity.eclipsestore.tables.Device;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.views.MainLayout;

import java.util.List;
import java.util.Optional;

@PageTitle("Sections")
@Route(value = "sections", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class SectionsView extends Div implements BeforeEnterObserver {

    private final StoresController sectionService;
    private final CategoryController categoryController;
    private final DevicesController devicesController;
    private Button newButton;
    private Grid<Section> grid;
    private Binder<Section> binder;
    private List<Device> deciveList;
    private List<Category> categoryList;
    private ComboBox<String> storeComboBox;
    private Grid.Column<Section> myStoreNameColumn;
    private MultiSelectComboBox<String> categoryComboBox;
    private MultiSelectComboBox<String> deviceComboBox;

    public SectionsView(StoresController sectionService, CategoryController categoryController, DevicesController devicesController) {
        this.sectionService = sectionService;
        this.categoryController = categoryController;
        this.devicesController = devicesController;

        addClassName("sections-view");
        setSizeFull();
        createGrid();

        createButton();

        add(newButton, grid);
    }

    private void createButton() {
        newButton = new Button("add new section");
        newButton.addClickListener(l -> {
            TextField textField = new TextField();
            ComboBox<String> storeComboBox = new ComboBox<>();

            storeComboBox.setWidthFull();

            storeComboBox.setItems(query -> sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize(), null).filter(Section::isDefault).map(Section::getId));
            storeComboBox.setItemLabelGenerator(m -> {
                Optional<Section> any = sectionService.allStores(getBusinessId()).stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(m)).findAny();
                return any.map(n -> StringUtils.isBlank(n.getDefault_name()) ? "!" : n.getDefault_name()).orElse(m);
            });

            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Create new section");
            dialog.add(textField);
            dialog.add(storeComboBox);

            Button button = new Button("Save");
            button.addClickListener(n -> {
                Section item = new Section();
                item.setBusinessId(getBusinessId());
                item.setName(textField.getValue());
                item.setId(storeComboBox.getValue());
                addNewStore(item);
                grid.getDataProvider().refreshAll();
                dialog.close();
            });
            dialog.getFooter().add(button);
            dialog.open();

        });
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

        grid.setItems(query -> sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize(), this::check));

    }

    private Long getBusinessId() {
        return 0L;
    }

    public boolean check(Section receipt) {
        return true;
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
        return sectionService.addNewStore(item);
    }

    private Section updateStore(Section item) {
        return sectionService.updateStore(item);
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
            Optional<Section> any = sectionService.allStores(getBusinessId()).stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(l.getId())).findAny();
            return any.map(Section::getDefault_name).orElse(l.getId());
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


    private void addFiltersToGrid() {
        HeaderRow filterRow = grid.appendHeaderRow();

        TextField sectionFilter = new TextField();
        sectionFilter.setPlaceholder("Filter");
        sectionFilter.setClearButtonVisible(true);
        sectionFilter.setWidth("100%");
        sectionFilter.setValueChangeMode(ValueChangeMode.EAGER);
//        sectionFilter.addValueChangeListener(event -> gridListDataView.addFilter(section -> StringUtils.containsIgnoreCase(section.getName(), sectionFilter.getValue())));
        filterRow.getCell(myStoreNameColumn).setComponent(sectionFilter);


    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
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

//        storeComboBox.setItems(sections.stream().filter(Section::isDefault).map(Section::getId).toList());
        storeComboBox.setItems(query -> sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize(), null).filter(Section::isDefault).map(Section::getId));
        storeComboBox.setItemLabelGenerator(l -> {
            Optional<Section> any = sectionService.allStores(getBusinessId()).stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(l)).findAny();
            return any.map(n -> StringUtils.isBlank(n.getDefault_name()) ? "!" : n.getDefault_name()).orElse(l);
        });
    }

    private List<Category> getCategories() {
        return categoryController.allStores(getBusinessId());
    }

    private List<Device> getDevices() {
        return devicesController.allStores(getBusinessId());
    }
}
