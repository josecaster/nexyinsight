package sr.we.views.inventoryvaluation;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import sr.we.controllers.InventoryValuationController;
import sr.we.entity.eclipsestore.tables.InventoryValuation;
import sr.we.security.AuthenticatedUser;
import sr.we.views.MainLayout;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@PageTitle("Inventory valuation")
@Route(value = "inventoryValuation/:inventoryValuationId?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class InventoryValuationView extends Div implements BeforeEnterObserver {

    final String IV_ID = "inventoryValuationId";
    private final String IV_EDIT_ROUTE_TEMPLATE = "inventoryValuation/%s/edit";
    private final Grid<InventoryValuation> grid = new Grid<>(InventoryValuation.class, false);
    private final Button cancel = new Button("Add new inventoryValuation ");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<InventoryValuation> binder;
    private final InventoryValuationController inventoryValuationService;
    private final AuthenticatedUser authenticatedUser;
    BigDecimalField inventoryValue, retailValue, potentialProfit, margin;
    DatePicker localDate;
    Span type;
    private InventoryValuation inventoryValuation;

    public InventoryValuationView(InventoryValuationController inventoryValuationService, AuthenticatedUser authenticatedUser) {

        this.inventoryValuationService = inventoryValuationService;
        this.authenticatedUser = authenticatedUser;

        addClassNames("batches-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addComponentColumn(inventoryValuation -> {
            InventoryValuation.Type type = inventoryValuation.getType();
            Span span = new Span(type == null ? "" : type.name());
            span.getElement().getThemeList().add("badge");
            if (type != null) {
                if (type.compareTo(InventoryValuation.Type.AUTOMATIC) == 0) {
                    span.getElement().getThemeList().add("primary");
                    span.getElement().getThemeList().remove("warning");
                } else {
                    span.getElement().getThemeList().remove("primary");
                    span.getElement().getThemeList().add("warning");
                }
            }
            return span;
        }).setHeader("Type").setAutoWidth(true);
        grid.addColumn(inventoryValuation -> inventoryValuation.getLocalDate() == null ? null : DateTimeFormatter.ofPattern("dd MM yyyy").format(inventoryValuation.getLocalDate())).setHeader("Date").setAutoWidth(true);
        grid.addColumn(inventoryValuation -> inventoryValuation.getInventoryValue() == null ? null : new DecimalFormat("###,###,###,###,###,##0.00").format(inventoryValuation.getInventoryValue())).setHeader("Inventory value").setAutoWidth(true);
        grid.addColumn(inventoryValuation -> inventoryValuation.getRetailValue() == null ? null : new DecimalFormat("###,###,###,###,###,##0.00").format(inventoryValuation.getRetailValue())).setHeader("retail value").setAutoWidth(true);
        grid.addColumn(inventoryValuation -> inventoryValuation.getPotentialProfit() == null ? null : new DecimalFormat("###,###,###,###,###,##0.00").format(inventoryValuation.getPotentialProfit())).setHeader("Potential profit").setAutoWidth(true);
        grid.addColumn(inventoryValuation -> inventoryValuation.getMargin() == null ? null : new DecimalFormat(" #,##0.00 '%'").format(inventoryValuation.getMargin())).setHeader("Margin").setAutoWidth(true);
        grid.setItems(query -> inventoryValuationService.allInventoryValuations(getBusinessId(), query.getPage(), query.getPageSize(), f -> true));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(IV_EDIT_ROUTE_TEMPLATE, event.getValue().getUuId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(InventoryValuationView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(InventoryValuation.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.inventoryValuation == null) {
                    this.inventoryValuation = new InventoryValuation();
                    this.inventoryValuation.setBusinessId(getBusinessId());
                }
                this.inventoryValuation.setType(InventoryValuation.Type.MANUAL);
                binder.writeBean(this.inventoryValuation);
                InventoryValuation update = inventoryValuationService.saveOrUpdate(this.inventoryValuation);
                clearForm();
                refreshGrid();
                populateForm(update);
                Notification.show("Data updated", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate(InventoryValuationView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid", 10000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
    }

    private Long getBusinessId() {
        return 0L;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> inventoryValuationId = event.getRouteParameters().get(IV_ID);
        if (inventoryValuationId.isPresent()) {
            InventoryValuation inventoryValuationFromBackend = inventoryValuationService.oneInventoryValuation(inventoryValuationId.get());
            if (inventoryValuationFromBackend != null) {
                populateForm(inventoryValuationFromBackend);
            } else {
                Notification.show(String.format("The requested inventoryValuation was not found, ID = %s", inventoryValuationId.get()), 10000, Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_WARNING);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(InventoryValuationView.class);
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
        type = new Span(InventoryValuation.Type.MANUAL.name());
        type.getElement().getThemeList().add("badge warning");
        localDate = new DatePicker("Local date");
        inventoryValue = new BigDecimalField("Inventory Value");
        retailValue = new BigDecimalField("Retail Value");
        potentialProfit = new BigDecimalField("Potential Profit");
        margin = new BigDecimalField("Margin");
        formLayout.add(type, localDate, inventoryValue, retailValue, potentialProfit, margin);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);

    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
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

    private void populateForm(InventoryValuation value) {
        this.inventoryValuation = value;
        binder.readBean(this.inventoryValuation);
        if (value != null) {
            // inventoryValuation Id
            type.getElement().getThemeList().remove("warning");
            type.getElement().getThemeList().add("success");
            type.setText("#" + value.getType());

            // status
            localDate.setValue(value.getLocalDate());
            inventoryValue.setValue(value.getInventoryValue());
            retailValue.setValue(value.getRetailValue());
            potentialProfit.setValue(value.getPotentialProfit());
            margin.setValue(value.getMargin());
        } else {
            // inventoryValuation Id
            type.setText(InventoryValuation.Type.MANUAL.name());
            type.getElement().getThemeList().remove("success");
            type.getElement().getThemeList().add("warning");
        }
    }


}
