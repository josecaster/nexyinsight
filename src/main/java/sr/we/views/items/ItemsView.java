package sr.we.views.items;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import sr.we.controllers.ItemsController;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.views.MainLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Items")
@Route(value = "items", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ItemsView extends Div {

    private final ItemsController ItemService;
    private Grid<Item> grid;
    private final Filters filters;

    public ItemsView(ItemsController ItemService) {
        this.ItemService = ItemService;
        setSizeFull();
        addClassNames("items-view");

        filters = new Filters(() -> refreshGrid());
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER, LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    private Component createGrid() {
        grid = new Grid<>(Item.class, false);
//        grid.addColumn("firstName").setAutoWidth(true);
//        grid.addColumn("lastName").setAutoWidth(true);
//        grid.addColumn("email").setAutoWidth(true);
//        grid.addColumn("phone").setAutoWidth(true);
//        grid.addColumn("dateOfBirth").setAutoWidth(true);
//        grid.addColumn("occupation").setAutoWidth(true);
//        grid.addColumn("role").setAutoWidth(true);

        grid.addColumn(Item::getCreated_at).setHeader("Create date").setFrozen(true).setFlexGrow(1).setResizable(true);
        grid.addColumn(Item::getItem_name).setHeader("Item name").setFrozen(true).setFlexGrow(1).setResizable(true);
        grid.addColumn(Item::getStock_level).setHeader("Stock level").setFlexGrow(1);
        grid.addColumn(l -> l.getVariant().getCost()).setHeader("Cost").setFlexGrow(1);
        grid.addComponentColumn(l -> {
            Span span = new Span(l.getVariant().getCost().multiply(BigDecimal.valueOf(l.getStock_level())).toString());
            if (l.getStock_level() > 0) {
                span.getElement().getThemeList().add("badge success");
            } else if (l.getStock_level() < 0) {
                span.getElement().getThemeList().add("badge error");
            }
            return span;
        }).setHeader("Inventory value").setFlexGrow(1);
        grid.addColumn(l -> l.getVariant().getDefault_price()).setHeader("Price").setFlexGrow(1);
        grid.addColumn(l -> {
            String storeId = l.getVariantStore().getStore_id();
//            if(list != null){
//                Optional<LoyStore> any = list.getStores().stream().filter(n -> n.id().equalsIgnoreCase(storeId)).findAny();
//                return any.map(LoyStore::name).orElse(storeId);
//            }
            return storeId;
        }).setHeader("Link Store").setFlexGrow(1);

        grid.setItems(query -> ItemService.allItems(0L, query.getPage(), query.getPageSize()));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    public static class Filters extends Div implements Specification<Item> {

        private final TextField name = new TextField("Name");
        private final TextField phone = new TextField("Phone");
        private final DatePicker startDate = new DatePicker("Date of Birth");
        private final DatePicker endDate = new DatePicker();
        private final MultiSelectComboBox<String> occupations = new MultiSelectComboBox<>("Occupation");
        private final CheckboxGroup<String> roles = new CheckboxGroup<>("Role");

        public Filters(Runnable onSearch) {

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM, LumoUtility.BoxSizing.BORDER);
            name.setPlaceholder("First or last name");

            occupations.setItems("Insurance Clerk", "Mortarman", "Beer Coil Cleaner", "Scale Attendant");

            roles.setItems("Worker", "Supervisor", "Manager", "External");
            roles.addClassName("double-width");

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                name.clear();
                phone.clear();
                startDate.clear();
                endDate.clear();
                occupations.clear();
                roles.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(name, phone, createDateRangeFilter(), occupations, roles, actions);
        }

        private Component createDateRangeFilter() {
            startDate.setPlaceholder("From");

            endDate.setPlaceholder("To");

            // For screen readers
            startDate.setAriaLabel("From date");
            endDate.setAriaLabel("To date");

            FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

            return dateRangeComponent;
        }

        @Override
        public Predicate toPredicate(Root<Item> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (!name.isEmpty()) {
                String lowerCaseFilter = name.getValue().toLowerCase();
                Predicate firstNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), lowerCaseFilter + "%");
                Predicate lastNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), lowerCaseFilter + "%");
                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
            }
            if (!phone.isEmpty()) {
                String databaseColumn = "phone";
                String ignore = "- ()";

                String lowerCaseFilter = ignoreCharacters(ignore, phone.getValue().toLowerCase());
                Predicate phoneMatch = criteriaBuilder.like(ignoreCharacters(ignore, criteriaBuilder, criteriaBuilder.lower(root.get(databaseColumn))), "%" + lowerCaseFilter + "%");
                predicates.add(phoneMatch);

            }
            if (startDate.getValue() != null) {
                String databaseColumn = "dateOfBirth";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(databaseColumn), criteriaBuilder.literal(startDate.getValue())));
            }
            if (endDate.getValue() != null) {
                String databaseColumn = "dateOfBirth";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.literal(endDate.getValue()), root.get(databaseColumn)));
            }
            if (!occupations.isEmpty()) {
                String databaseColumn = "occupation";
                List<Predicate> occupationPredicates = new ArrayList<>();
                for (String occupation : occupations.getValue()) {
                    occupationPredicates.add(criteriaBuilder.equal(criteriaBuilder.literal(occupation), root.get(databaseColumn)));
                }
                predicates.add(criteriaBuilder.or(occupationPredicates.toArray(Predicate[]::new)));
            }
            if (!roles.isEmpty()) {
                String databaseColumn = "role";
                List<Predicate> rolePredicates = new ArrayList<>();
                for (String role : roles.getValue()) {
                    rolePredicates.add(criteriaBuilder.equal(criteriaBuilder.literal(role), root.get(databaseColumn)));
                }
                predicates.add(criteriaBuilder.or(rolePredicates.toArray(Predicate[]::new)));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }

        private String ignoreCharacters(String characters, String in) {
            String result = in;
            for (int i = 0; i < characters.length(); i++) {
                result = result.replace("" + characters.charAt(i), "");
            }
            return result;
        }

        private Expression<String> ignoreCharacters(String characters, CriteriaBuilder criteriaBuilder, Expression<String> inExpression) {
            Expression<String> expression = inExpression;
            for (int i = 0; i < characters.length(); i++) {
                expression = criteriaBuilder.function("replace", String.class, expression, criteriaBuilder.literal(characters.charAt(i)), criteriaBuilder.literal(""));
            }
            return expression;
        }

    }

}
