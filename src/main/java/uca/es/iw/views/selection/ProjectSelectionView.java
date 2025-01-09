package uca.es.iw.views.selection;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import uca.es.iw.data.Convocatoria;
import uca.es.iw.data.Proyecto;
import uca.es.iw.services.ProyectoService;
import uca.es.iw.services.RecursosService;
import com.vaadin.flow.i18n.I18NProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "project-selection", layout = uca.es.iw.views.MainLayout.class)
@Menu(order = 11, icon = "line-awesome/svg/tasks-solid.svg")
@RolesAllowed({"CIO"})
public class ProjectSelectionView extends VerticalLayout {

    private final ProyectoService proyectoService;
    private final RecursosService recursosService;
    private final I18NProvider i18nProvider;

    private final ComboBox<Convocatoria> convocatoriaComboBox = new ComboBox<>();
    private final Span financiacionRestante = new Span();
    private final Span recursosRestantes = new Span();

    private final Grid<Proyecto> projectGrid = new Grid<>(Proyecto.class, false);

    private double totalFinanciacionRestante = 0.0;
    private int totalRecursosRestantes = 0;
    private final Map<Long, String> selectedStatuses = new HashMap<>();

    public ProjectSelectionView(ProyectoService proyectoService, RecursosService recursosService, I18NProvider i18nProvider) {
        this.proyectoService = proyectoService;
        this.recursosService = recursosService;
        this.i18nProvider = i18nProvider;

        setWidth("100%");
        setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2(i18nProvider.getTranslation("project_selection.title", getLocale()));
        add(title);

        convocatoriaComboBox.setLabel(i18nProvider.getTranslation("project_selection.select_convocatoria", getLocale()));
        convocatoriaComboBox.setItemLabelGenerator(Convocatoria::getNombre);
        convocatoriaComboBox.setItems(proyectoService.getAllConvocatorias());
        convocatoriaComboBox.addValueChangeListener(event -> loadProjects(event.getValue()));
        add(convocatoriaComboBox);

        configureProjectGrid();
        add(projectGrid);

        HorizontalLayout statsLayout = new HorizontalLayout(financiacionRestante, recursosRestantes);
        statsLayout.setWidthFull();
        statsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);
        add(statsLayout);

        Button saveButton = new Button(i18nProvider.getTranslation("project_selection.save", getLocale()), event -> saveChanges());
        add(saveButton);

        loadProjects(null);
    }

    private void configureProjectGrid() {
        projectGrid.setSelectionMode(SelectionMode.NONE);
        projectGrid.setWidthFull();

        projectGrid.addColumn(new ComponentRenderer<>(proyecto -> {
            Anchor anchor = new Anchor(String.format("project-view/%s", proyecto.getId()), proyecto.getNombreCorto());
            anchor.getElement().setAttribute("theme", "tertiary");
            return anchor;
        })).setHeader(i18nProvider.getTranslation("project_selection.title", getLocale())).setAutoWidth(true);

        projectGrid.addColumn(Proyecto::getFinanciacionNecesaria)
                .setHeader(i18nProvider.getTranslation("project_selection.budget_needed", getLocale()))
                .setSortable(true);
        projectGrid.addColumn(Proyecto::getRecursosHumanosNecesarios)
                .setHeader(i18nProvider.getTranslation("project_selection.human_resources_needed", getLocale()))
                .setSortable(true);
        projectGrid.addColumn(Proyecto::getCalFinal)
                .setHeader(i18nProvider.getTranslation("project_selection.final_grade", getLocale()))
                .setSortable(true);

        projectGrid.addComponentColumn(project -> {
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setItems(
                    i18nProvider.getTranslation("project_selection.pending", getLocale()),
                    i18nProvider.getTranslation("project_selection.accepted", getLocale()),
                    i18nProvider.getTranslation("project_selection.rejected", getLocale())
            );
            comboBox.setValue(getProjectState(project));

            comboBox.addValueChangeListener(event -> {
                String newStateInSpanish = convertToSpanish(event.getValue());
                updateProjectState(project, event.getOldValue(), newStateInSpanish);
                updateStats();
            });

            return comboBox;
        }).setHeader(i18nProvider.getTranslation("project_selection.state", getLocale()));
    }
    private String convertToSpanish(String state) {
        switch (state) {
            case "Pending":
                return "Pendiente";
            case "Accepted":
                return "Aceptado";
            case "Rejected":
                return "Rechazado";
            default:
                return state;
        }
    }
    private void loadProjects(Convocatoria convocatoria) {
        List<Proyecto> proyectos;
        if (convocatoria == null) {
            proyectos = proyectoService.searchQualifiedProjects();
        } else {
            proyectos = proyectoService.searchProjectsByConvocatoria(convocatoria);
        }
        projectGrid.setItems(proyectos);

        totalFinanciacionRestante = recursosService.getPresupuestoRestante();
        totalRecursosRestantes = recursosService.getRecursosHumanosRestantes();

        updateStats();
    }

    private String getProjectState(Proyecto project) {
        return project.getEstado();
    }

    private void updateProjectState(Proyecto project, String oldState, String newState) {
        if ("Aceptado".equals(newState) && !"Aceptado".equals(oldState)) {
            totalFinanciacionRestante -= project.getFinanciacionNecesaria();
            totalRecursosRestantes -= project.getRecursosHumanosNecesarios();
        } else if ("Rechazado".equals(newState) && "Aceptado".equals(oldState)) {
            totalFinanciacionRestante += project.getFinanciacionNecesaria();
            totalRecursosRestantes += project.getRecursosHumanosNecesarios();
        } else if ("Pendiente".equals(newState) && "Aceptado".equals(oldState)) {
            totalFinanciacionRestante += project.getFinanciacionNecesaria();
            totalRecursosRestantes += project.getRecursosHumanosNecesarios();
        }

        selectedStatuses.put(project.getId(), newState);
    }

    private void updateStats() {
        financiacionRestante.setText(String.format(i18nProvider.getTranslation("project_selection.remaining_budget", getLocale()), totalFinanciacionRestante));
        recursosRestantes.setText(i18nProvider.getTranslation("project_selection.remaining_resources", getLocale()) + totalRecursosRestantes);
    }

    private void saveChanges() {
        if (selectedStatuses.isEmpty()) {
            Notification.show(i18nProvider.getTranslation("project_selection.no_changes", getLocale()), 3000, Notification.Position.MIDDLE);
            return;
        }
        if (totalFinanciacionRestante < 0) {
            Notification.show(i18nProvider.getTranslation("project_selection.not_enough_funding", getLocale()), 5000, Notification.Position.MIDDLE);
            return;
        }
        if (totalRecursosRestantes < 0) {
            Notification.show(i18nProvider.getTranslation("project_selection.not_enough_resources", getLocale()), 5000, Notification.Position.MIDDLE);
            return;
        }

        try {
            projectGrid.getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>())
                    .forEach(project -> {
                        String newStatus = selectedStatuses.get(project.getId());
                        if (newStatus != null) {
                            proyectoService.updateProjectStatus(project, newStatus);
                        }
                    });
            recursosService.updateRecursosRestantes(totalFinanciacionRestante, totalRecursosRestantes);
            Notification.show(i18nProvider.getTranslation("project_selection.save_success", getLocale()), 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show(i18nProvider.getTranslation("project_selection.save_error", getLocale()) + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}
