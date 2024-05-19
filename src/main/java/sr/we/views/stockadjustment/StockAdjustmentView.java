package sr.we.views.stockadjustment;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import sr.we.controllers.ItemsController;
import sr.we.controllers.StoresController;
import sr.we.entity.*;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.security.AuthenticatedUser;
import sr.we.services.StockAdjustmentItemsService;
import sr.we.services.StockAdjustmentService;
import sr.we.views.MainLayout;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@PageTitle("StockAdjustments")
@Route(value = "stockadjustment/:stockAdjustmentId?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class StockAdjustmentView extends Div implements BeforeEnterObserver {

    final String SA_ID = "stockAdjustmentId";
    private final String SA_EDIT_ROUTE_TEMPLATE = "stockadjustment/%s/edit";
    private final Grid<StockAdjustment> grid = new Grid<>(StockAdjustment.class, false);
    private final Grid<StockAdjustmentItems> items = new Grid<>(StockAdjustmentItems.class, false);
    private ComboBox<Item> itemsCmb;
    private final Button cancel = new Button("Add new adjustment");
    private final Button adjustBtn = new Button("Adjust");
    private final BeanValidationBinder<StockAdjustment> binder;
    private final StockAdjustmentItemsService stockAdjustmentItemsService;
    private final StoresController storesController;
    private final StockAdjustmentService batchService;
    private final AuthenticatedUser authenticatedUser;
    private final ItemsController ItemService;
    TextArea note;
    DatePicker date;
    Select<StockAdjustment.Type> type;
    ComboBox<String> sectionId;
    private Span batchIdFld;
    private StockAdjustment stockadjustment;
    private Set<String> linkSections;
    private VerticalLayout itemsLayout;

    public StockAdjustmentView(ItemsController ItemService, StockAdjustmentItemsService stockAdjustmentItemsService, StockAdjustmentService batchService, AuthenticatedUser authenticatedUser, StoresController storesController) {
        this.stockAdjustmentItemsService = stockAdjustmentItemsService;
        this.batchService = batchService;
        this.authenticatedUser = authenticatedUser;
        this.storesController = storesController;
        this.ItemService = ItemService;

        User user = authenticatedUser.get().get();
        linkSections = user.getLinkSections();

        addClassNames("batches-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        splitLayout.setSplitterPosition(50);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(stockadjustment -> "Adjustment #" + stockadjustment.getId()).setHeader("#").setAutoWidth(true);
        grid.addColumn(StockAdjustment::getDate).setHeader("Date").setAutoWidth(true);
        grid.addColumn(StockAdjustment::getType).setHeader("Reason").setAutoWidth(true);
        grid.addColumn(StockAdjustment::getSectionId).setHeader("Section").setAutoWidth(true);

        grid.setItems(query -> {
            PageRequest pageable = PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query));
            return user.getRoles().contains(Role.ADMIN) ? batchService.list(pageable).stream() : batchService.list(pageable, (root, query2, cb) -> root.get("sectionId").in(linkSections)).stream();
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SA_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(StockAdjustmentView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(StockAdjustment.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        adjustBtn.addClickListener(e -> {
            try {
                if (this.stockadjustment == null) {
                    this.stockadjustment = new StockAdjustment();
                }
                binder.writeBean(this.stockadjustment);
                this.stockadjustment.setItems(items.getDataProvider().fetch(new Query<>()).toList());
                StockAdjustment update = batchService.update(this.stockadjustment);
                clearForm();
                refreshGrid();
                populateForm(update);
                Notification.show("Data updated", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate(StockAdjustmentView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            } catch (IOException ex) {
                Notification.show("Error updating data", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> stockAdjustmentId = event.getRouteParameters().get(SA_ID).map(Long::parseLong);
        if (stockAdjustmentId.isPresent()) {
            Optional<StockAdjustment> batchFromBackend = batchService.get(stockAdjustmentId.get());
            if (batchFromBackend.isPresent()) {
                populateForm(batchFromBackend.get());
            } else {
                Notification.show(String.format("The requested stockadjustment was not found, ID = %s", stockAdjustmentId.get()), 10000, Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_WARNING);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(StockAdjustmentView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        batchIdFld = new Span("!!!NEW!!!");
        batchIdFld.getElement().getThemeList().add("badge warning");
        sectionId = new ComboBox<>("Section");
        sectionId.setItemLabelGenerator(label -> {
            Section section = storesController.oneStore(label);
            return section == null ? "Error" : section.getName();
        });
        sectionId.setItems(query -> storesController.allSections(getBusinessId(), query.getPage(), query.getPageSize(), authenticatedUser.get()).map(Section::getUuId));

        items.setHeight("500px");
        items.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        Grid.Column<StockAdjustmentItems> itemColumn = items.addColumn(StockAdjustmentItems::getItemName).setHeader("Item");
        Grid.Column<StockAdjustmentItems> inStockColumn = items.addColumn(StockAdjustmentItems::getInStock).setHeader("In Stock");
        Grid.Column<StockAdjustmentItems> adjustColumn = items.addColumn(StockAdjustmentItems::getAdjustment).setHeader("Add Stock");
        Grid.Column<StockAdjustmentItems> costColumn = items.addColumn(StockAdjustmentItems::getCost).setHeader("Cost");
        Grid.Column<StockAdjustmentItems> stockAfterColumn = items.addColumn(StockAdjustmentItems::getStockAfter).setHeader("Stock after");

        note = new TextArea("Note");
        date = new DatePicker("Date", LocalDate.now());
        type = new Select<>("Reason", l -> {
            // change items columns

            if(l.getValue() == null){
                return;
            }

            if (l.getValue().compareTo(StockAdjustment.Type.RI) == 0) {

                inStockColumn.setHeader("In Stock");
                adjustColumn.setHeader("Add Stock");
                stockAfterColumn.setHeader("Stock after");
                costColumn.setVisible(true);
                stockAfterColumn.setVisible(true);

            } else if (l.getValue().compareTo(StockAdjustment.Type.IC) == 0) {

                inStockColumn.setHeader("Expected Stock");
                adjustColumn.setHeader("Counted Stock");
                costColumn.setVisible(false);
                stockAfterColumn.setVisible(false);

            } else if (l.getValue().compareTo(StockAdjustment.Type.LS) == 0 || l.getValue().compareTo(StockAdjustment.Type.DM) == 0) {

                inStockColumn.setHeader("In Stock");
                adjustColumn.setHeader("Remove Stock");
                stockAfterColumn.setHeader("Stock after");
                costColumn.setVisible(false);
                stockAfterColumn.setVisible(true);

            }
        });
        type.setItems(StockAdjustment.Type.values());
        type.setItemLabelGenerator(StockAdjustment.Type::getCaption);
        date.setVisible(false);
        formLayout.add(batchIdFld, type, sectionId, note);

        formLayout.add(itemsLayout = new VerticalLayout(new H3("Items")));
        formLayout.setColspan(batchIdFld,2);
        formLayout.setColspan(itemsLayout,2);
        formLayout.setColspan(note,2);
        itemsLayout.add(items);



        itemsCmb = new ComboBox<>();
        itemsCmb.setWidthFull();
        FormLayout.FormItem formItem = formLayout.addFormItem(itemsCmb, "Select a item");
        formLayout.setColspan(formItem,2);
        itemsCmb.setPlaceholder("FOR NEW ITEM LEAVE EMPTY");
        itemsCmb.setItems(query -> ItemService.allItems(getBusinessId(), query.getPage(), query.getPageSize(), authenticatedUser.get().get(), query.getFilter()));
        itemsCmb.setItemLabelGenerator(l -> {
            return l.getVariant().getSku() + " - " + l.getItem_name();
        });

        itemsCmb.addValueChangeListener(v -> {
            StockAdjustmentItems stockAdjustmentItems = new StockAdjustmentItems();
            stockAdjustmentItems.setItemId(v.getValue().getUuId());
            stockAdjustmentItems.setInStock(v.getValue().getStock_level());
            stockAdjustmentItems.setItemName(v.getValue().getItem_name());
            stockAdjustmentItems.setStockAdjustmentId(this.stockadjustment.getId());
            StockAdjustmentItems update = stockAdjustmentItemsService.update(stockAdjustmentItems);
            items.getDataProvider().refreshItem(update);
        });


        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);

    }

    private Long getBusinessId() {
        return 0L;
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        adjustBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(adjustBtn, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(StockAdjustment value) {
        this.stockadjustment = value;
        binder.readBean(this.stockadjustment);
        if (value != null) {
            // stockadjustment Id
            batchIdFld.getElement().getThemeList().remove("warning");
            batchIdFld.getElement().getThemeList().add("success");
            batchIdFld.setText("#" + value.getId());

            // status
            type.setReadOnly(true);
            note.setReadOnly(true);
            sectionId.setReadOnly(true);
            adjustBtn.setVisible(false);
        } else {
            // stockadjustment Id
            batchIdFld.setText("!!!NEW!!!");
            batchIdFld.getElement().getThemeList().remove("success");
            batchIdFld.getElement().getThemeList().add("warning");

            // status

            // upload
            type.setReadOnly(false);
            note.setReadOnly(false);
            sectionId.setReadOnly(false);
            adjustBtn.setVisible(true);
        }
    }
}
