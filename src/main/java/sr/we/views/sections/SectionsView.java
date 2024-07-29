package sr.we.views.sections;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import sr.we.controllers.CategoryController;
import sr.we.controllers.DevicesController;
import sr.we.controllers.StoresController;
import sr.we.entity.eclipsestore.tables.Category;
import sr.we.entity.eclipsestore.tables.Device;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.views.MainLayout;
import sr.we.views.MobileSupport;
import sr.we.views.components.MyLineAwesome;
import sr.we.views.users.CardView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@PageTitle("Sections")
@Route(value = "sections", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class SectionsView extends Div implements BeforeEnterObserver, MobileSupport {

    private final StoresController sectionService;
    private final CategoryController categoryController;
    private final DevicesController devicesController;
    private final HorizontalLayout layout;
    private Button newButton;
    private Grid<Section> grid;
    private Binder<Section> binder;
    private List<Device> deciveList;
    private List<Category> categoryList;
    private ComboBox<String> storeComboBox;
    private Grid.Column<Section> myStoreNameColumn;
    private MultiSelectComboBox<String> categoryComboBox;
    private Grid.Column<Section> sectionsMobileColumn;
    private Grid.Column<Section> linkStoreColumn;
    private Grid.Column<Section> categoryColumn;
    private Grid.Column<Section> sectionColumn;
    private Grid.Column<Section> editColumn;
    private Grid.Column<Section> iconColumn;
    private Grid.Column<Section> defaultColumn;
    private Grid.Column<Section> enabledColumn;
    //    private MultiSelectComboBox<String> deviceComboBox;

    public SectionsView(StoresController sectionService, CategoryController categoryController, DevicesController devicesController) {
        this.sectionService = sectionService;
        this.categoryController = categoryController;
        this.devicesController = devicesController;

        addClassName("sections-view");
        setSizeFull();
        createGrid();

        createButton();

        Button settings = new Button(MyLineAwesome.COG_SOLID.create());
        settings.getElement().getStyle().set("margin-left", "auto");

        newButton.setIcon(MyLineAwesome.PLUS_SOLID.create());
        newButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout horizontalLayout = new HorizontalLayout(newButton, settings);
        horizontalLayout.setPadding(true);
        horizontalLayout.setWidthFull();
        Header header = new Header(horizontalLayout);
        layout = new HorizontalLayout(grid);
        layout.setHeightFull();
        add(header, layout);

        settings.addClickListener(c -> {
            Dialog dialog = new Dialog("Synchronize sections & categories");
            ComboBox<String> storeComboBox = new ComboBox<>("Select a store");

            storeComboBox.setWidthFull();

            storeComboBox.setItems(query -> sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize()).filter(Section::isDefault).map(Section::getId));
            storeComboBox.setItemLabelGenerator(m -> {
                Optional<Section> any = sectionService.allStores(getBusinessId()).stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(m)).findAny();
                return any.map(n -> StringUtils.isBlank(n.getDefault_name()) ? "!" : n.getDefault_name()).orElse(m);
            });
            dialog.add("This feature will get the categories from this system an create them as sections");
            dialog.add(storeComboBox);
            Button button = new Button("Synchronize");
            dialog.add(button);
            button.addClickListener(l -> {
                if (StringUtils.isNotBlank(storeComboBox.getValue())) {
                    sectionService.synCategories(getBusinessId(), storeComboBox.getValue());
                    Notification.show("Synchronizing done", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    dialog.close();
                    grid.getDataProvider().refreshAll();
                } else {
                    Notification.show("First select a store to link", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            dialog.open();
        });
    }

    private void createButton() {
        newButton = new Button("add new section");
        newButton.addClickListener(l -> {
            TextField textField = new TextField();
            ComboBox<String> storeComboBox = new ComboBox<>();

            storeComboBox.setWidthFull();

            storeComboBox.setItems(query -> sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize()).filter(Section::isDefault).map(Section::getId));
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
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COLUMN_BORDERS);

        grid.setItems(query -> sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize(), true));
        grid.setHeight("100%");
    }

    private Long getBusinessId() {
        return 0L;
    }

    private void addColumnsToGrid() {
        binder = new Binder<>(Section.class);
        grid.getEditor().setBinder(binder);
        grid.getEditor().setBuffered(true);

        createPictureColumn();
        createStoreNameColumn();
        createLinkStoreColumn();
        createCategoryColumn();
        createDefaultColumn();
        createEnabledColumn();
        createEditColumn();
        sectionsMobileColumn = grid.addComponentColumn(r -> {
            return CardView.createCard(r.getProfilePicture(), r.getName(), "", "");
        }).setHeader("List of sections");
        sectionsMobileColumn.setVisible(false);


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
            iconColumn.setVisible(false);
            linkStoreColumn.setVisible(false);
            myStoreNameColumn.setVisible(false);
            categoryColumn.setVisible(false);
            editColumn.setVisible(false);
            defaultColumn.setVisible(false);
            enabledColumn.setVisible(false);
            sectionsMobileColumn.setVisible(true);
//            createPictureColumn();
//            createStoreNameColumn();
//            createLinkStoreColumn();
//            createCategoryColumn();
//            createDefaultColumn();
//            createEditColumn();

//            filters.setVisible(false);
//            exporter.setExcelExportEnabled(false);
        } else {
            layout.removeClassName("card-view");
            iconColumn.setVisible(true);
            linkStoreColumn.setVisible(true);
            myStoreNameColumn.setVisible(true);
            categoryColumn.setVisible(true);
            editColumn.setVisible(true);
            defaultColumn.setVisible(true);
            enabledColumn.setVisible(true);
            sectionsMobileColumn.setVisible(false);

//            filters.setVisible(true);
//            exporter.setExcelExportEnabled(true);
        }
    }

    private Section addNewStore(Section item) {
        return sectionService.addNewStore(item);
    }

    private Section updateStore(Section item) {
        return sectionService.updateStore(item);
    }

    private void createPictureColumn() {
        iconColumn = grid.addComponentColumn(n -> {
            Avatar avatar = new Avatar(n.getName());
            if (n.getProfilePicture() != null) {
                StreamResource resource = new StreamResource("profile-pic", () -> new ByteArrayInputStream(n.getProfilePicture()));
                avatar.setImageResource(resource);
            }
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

            MenuItem menuItem = menuBar.addItem(avatar);
            SubMenu subMenu = menuItem.getSubMenu();
            subMenu.addItem("Set Icon", l -> {
                Upload upload = new Upload();
                upload.setDropAllowed(true);
                Dialog dialog = new Dialog(upload);
                dialog.open();
                upload.setAcceptedFileTypes("image/jpeg", "image/png");
                MemoryBuffer receiver = new MemoryBuffer();
                upload.setReceiver(receiver);
                int maxFileSizeInBytes = 5 * 1024 * 1024; // 5.0 MB
                upload.setMaxFileSize(maxFileSizeInBytes);
                upload.addSucceededListener(s -> {
                    if (receiver.getInputStream() != null) {
                        try {
                            n.setProfilePicture(receiver.getInputStream().readAllBytes());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    updateStore(n);
                    grid.getDataProvider().refreshItem(n);
                    dialog.close();
                });
            });
            return menuBar;
        }).setHeader("Icon");
    }

    private void createStoreNameColumn() {
        myStoreNameColumn = grid.addColumn(Section::getName).setHeader("My store name");
        TextField firstNameField = new TextField();
        firstNameField.setWidthFull();
        myStoreNameColumn.setEditorComponent(firstNameField);
        binder.forField(firstNameField).asRequired("Store name must not be empty").bind(Section::getName, Section::setName);
    }

    private void createLinkStoreColumn() {
        linkStoreColumn = grid.addColumn(l -> {
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

//        deviceComboBox = new MultiSelectComboBox<>();
//        deviceComboBox.setWidthFull();
//        binder.forField(deviceComboBox).bind(Section::getDevices, Section::setDevices);
//        deviceColumn.setEditorComponent(deviceComboBox);
    }

    private void createCategoryColumn() {
        categoryColumn = grid.addComponentColumn(section -> {
            MultiSelectComboBox<String> categoryComboBox = new MultiSelectComboBox<>();
            categoryComboBox.setWidthFull();
            categoryComboBox.setReadOnly(true);
            categoryComboBox.setItems((categoryList.stream().map(Category::getId).toList()));
            if (!categoryList.isEmpty() && section.getCategories() != null && !section.getCategories().isEmpty()) {
                categoryComboBox.setValue(section.getCategories());
            }
            categoryComboBox.setItemLabelGenerator(l -> {
                Optional<Category> any = categoryList.stream().filter(n -> n.getId().equalsIgnoreCase(l)).findAny();
                return any.map(Category::getName).orElse(l);
            });
            return categoryComboBox;
        }).setHeader("Category");

        categoryComboBox = new MultiSelectComboBox<>();
        categoryComboBox.setWidthFull();
        //            if (StringUtils.isBlank(value)) {
        //            } else {
        //                section.setCategories(new HashSet<>(List.of(value)));
        //            }
        binder.forField(categoryComboBox).bind(section -> section.getCategories() == null || section.getCategories().isEmpty() ? null : section.getCategories(), Section::setCategories);
        categoryColumn.setEditorComponent(categoryComboBox);
    }

    private void createDefaultColumn() {
        defaultColumn = grid.addComponentColumn(store -> {
            Checkbox checkbox = new Checkbox(store.isDefault());
            checkbox.setReadOnly(true);
            return checkbox;
        }).setHeader("Default");
    }

    private void createEnabledColumn() {
        enabledColumn = grid.addComponentColumn(store -> {
            Checkbox checkbox = new Checkbox(store.isEnabled());
            checkbox.setReadOnly(true);
            return checkbox;
        }).setHeader("Enabled");

        Checkbox enabledChk = new Checkbox();
        enabledChk.setWidthFull();
        binder.forField(enabledChk).bind(Section::isEnabled, Section::setEnabled);
        enabledColumn.setEditorComponent(enabledChk);
    }

    private void createEditColumn() {
        editColumn = grid.addComponentColumn(store -> {
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
//        HeaderRow filterRow = grid.appendHeaderRow();

//        TextField sectionFilter = new TextField();
//        sectionFilter.setPlaceholder("Filter");
//        sectionFilter.setClearButtonVisible(true);
//        sectionFilter.setWidth("100%");
//        sectionFilter.setValueChangeMode(ValueChangeMode.EAGER);
////        sectionFilter.addValueChangeListener(event -> gridListDataView.addFilter(section -> StringUtils.containsIgnoreCase(section.getName(), sectionFilter.getValue())));
//        filterRow.getCell(myStoreNameColumn).setComponent(sectionFilter);

        Select<String> sectionFilter = new Select<String>();
        sectionFilter.setPlaceholder("Filter");
        sectionFilter.setWidth("100%");
        sectionFilter.setItems("Enabled", "Disabled", "Both");
        sectionFilter.setValue("Enabled");

        sectionFilter.addValueChangeListener(event -> grid.setItems(query -> {
            String value = event.getValue();
            Boolean enabled = true;
            if (StringUtils.isBlank(value) || value.equalsIgnoreCase("Both")) {
                enabled = null;
            } else if (value.equalsIgnoreCase("Disabled")) {
                enabled = false;
            }
            return sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize(), enabled);
        }));
        enabledColumn.setHeader(sectionFilter);


    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        deciveList = getDevices();
        categoryList = getCategories();

        categoryComboBox.setItems((categoryList.stream().map(Category::getId).toList()));
        categoryComboBox.setItemLabelGenerator(l -> {
            Optional<Category> any = categoryList.stream().filter(n -> n.getId().equalsIgnoreCase(l)).findAny();
            return any.map(Category::getName).orElse(l);
        });

//        deviceComboBox.setItems((deciveList.stream().map(Device::getUuId).toList()));
//        deviceComboBox.setItemLabelGenerator(l -> {
//            Optional<Device> any = deciveList.stream().filter(n -> n.getUuId().equalsIgnoreCase(l)).findAny();
//            return any.map(Device::getName).orElse(l);
//        });

//        storeComboBox.setItems(sections.stream().filter(Section::isDefault).map(Section::getId).toList());
        storeComboBox.setItems(query -> sectionService.allSections(getBusinessId(), query.getPage(), query.getPageSize()).filter(Section::isDefault).map(Section::getId));
        storeComboBox.setItemLabelGenerator(l -> {
            Optional<Section> any = sectionService.allStores(getBusinessId()).stream().filter(Section::isDefault).filter(n -> n.getId().equalsIgnoreCase(l)).findAny();
            return any.map(n -> StringUtils.isBlank(n.getDefault_name()) ? "!" : n.getDefault_name()).orElse(l);
        });
    }

    private List<Category> getCategories() {
        return categoryController.findCategories(getBusinessId());
    }

    private List<Device> getDevices() {
        return devicesController.findDevices(getBusinessId());
    }
}
