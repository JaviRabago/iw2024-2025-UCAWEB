package uca.es.iw.views.evaluation;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.RolesAllowed;
import uca.es.iw.services.ProyectoService;
import com.vaadin.flow.i18n.I18NProvider;

import java.util.ArrayList;
import java.util.List;

@Route(value = "technicalevaluation", layout = uca.es.iw.views.MainLayout.class)
@Menu(order = 7, icon = "line-awesome/svg/clipboard-list-solid.svg")
@RolesAllowed("OTP")
public class TechnicalEvaluationView extends Composite<VerticalLayout> {

    @ClientCallable
    public void showNotification(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE);
    }

    ProyectoService proyectoService;
    private final I18NProvider i18nProvider;

    public TechnicalEvaluationView(ProyectoService proyectoService, I18NProvider i18nProvider) {
        this.proyectoService = proyectoService;
        this.i18nProvider = i18nProvider;

        VerticalLayout layoutColumn2 = new VerticalLayout();
        FormLayout formLayout2Col = new FormLayout();
        ComboBox<String> comboBox = new ComboBox<>();
        Hr hr = new Hr();
        H2 h2 = new H2(i18nProvider.getTranslation("technical_evaluation.title", getLocale()));
        Hr hr2 = new Hr();
        ComboBox<Integer> comboBox2 = new ComboBox<>();
        Hr hr3 = new Hr();
        H2 h22 = new H2(i18nProvider.getTranslation("technical_evaluation.management_and_support", getLocale()));
        Hr hr4 = new Hr();
        ComboBox<Integer> comboBox3 = new ComboBox<>();
        HorizontalLayout layoutRow = new HorizontalLayout();
        Button buttonPrimary = new Button(i18nProvider.getTranslation("technical_evaluation.save_button", getLocale()));

        // Nuevo botón de descarga
        Button downloadButton = new Button(i18nProvider.getTranslation("technical_evaluation.download_memory", getLocale()));
        downloadButton.setEnabled(false); // Inicialmente deshabilitado
        downloadButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button downloadButton2 = new Button(i18nProvider.getTranslation("technical_evaluation.download_specifications", getLocale()));
        downloadButton2.setEnabled(false); // Inicialmente deshabilitado
        downloadButton2.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Añadir Span para mostrar la calificación técnica
        Span calificacionTecnicaLabel = new Span(i18nProvider.getTranslation("technical_evaluation.rating", getLocale()));
        calificacionTecnicaLabel.setVisible(false);  // Inicialmente oculto

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getContent().setAlignItems(Alignment.CENTER);

        layoutColumn2.setWidthFull();
        getContent().setFlexGrow(1.0, layoutColumn2);
        layoutColumn2.setWidth("100%");
        layoutColumn2.setMaxWidth("800px");
        layoutColumn2.setHeight("min-content");

        formLayout2Col.setWidth("100%");
        comboBox.setLabel(i18nProvider.getTranslation("technical_evaluation.select_project", getLocale()));
        comboBox.setWidth("min-content");

        // Llenar ComboBox con datos de proyectos
        proyectoService.setSelectProjects(comboBox);

        // Evento de selección de ComboBox
        comboBox.addValueChangeListener(event -> {
            String selectedProject = event.getValue();

            downloadButton.setEnabled(selectedProject != null);
            downloadButton2.setEnabled(selectedProject != null);

            if (selectedProject != null) {
                // Mostrar el Span
                calificacionTecnicaLabel.setVisible(true);

                // Obtener la calificación técnica desde el servicio (si existe)
                Double calTecnica = proyectoService.getCalificacionTecnica(selectedProject);
                if (calTecnica != null) {
                    calificacionTecnicaLabel.setText(i18nProvider.getTranslation("technical_evaluation.rating_text", getLocale()) + calTecnica);
                } else {
                    calificacionTecnicaLabel.setText(i18nProvider.getTranslation("technical_evaluation.rating_not_assigned", getLocale()));
                }
            } else {
                // Ocultar el Span si no hay proyecto seleccionado
                calificacionTecnicaLabel.setVisible(false);
            }
        });

        downloadButton.addClickListener(e -> {
            String selectedProject = comboBox.getValue(); // Obtiene el proyecto seleccionado actual
            if (selectedProject != null) {
                String downloadUrl = proyectoService.getDownloadUrl(selectedProject, 1);
                // Validar si el archivo existe antes de abrir la pestaña
                UI.getCurrent().getPage().executeJs(
                        "fetch($0, { method: 'HEAD' }).then(response => { " +
                                "if (response.ok) {" +
                                "    window.open($0, '_blank');" +
                                "} else {" +
                                "    $1.$server.showNotification($2);" +
                                "}});",
                        downloadUrl, getElement(), i18nProvider.getTranslation("technical_evaluation.memory_not_available", getLocale()));
            }
        });

        downloadButton2.addClickListener(e -> {
            String selectedProject = comboBox.getValue(); // Obtiene el proyecto seleccionado actual
            if (selectedProject != null) {
                String downloadUrl = proyectoService.getDownloadUrl(selectedProject, 2);
                // Validar si el archivo existe antes de abrir la pestaña
                UI.getCurrent().getPage().executeJs(
                        "fetch($0, { method: 'HEAD' }).then(response => { " +
                                "if (response.ok) {" +
                                "    window.open($0, '_blank');" +
                                "} else {" +
                                "    $1.$server.showNotification($2);" +
                                "}});",
                        downloadUrl, getElement(), i18nProvider.getTranslation("technical_evaluation.specifications_not_available", getLocale()));
            }
        });

        h2.setText(i18nProvider.getTranslation("technical_evaluation.product_quality", getLocale()));
        layoutColumn2.setAlignSelf(Alignment.CENTER, h2);
        h2.setWidth("max-content");

        // Añadir subcategorías para la calidad del producto
        ComboBox<Integer> calidadAdecuacionFuncional = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.functional_adequacy", getLocale()));
        calidadAdecuacionFuncional.setItems(getNumericOptions());
        ComboBox<Integer> calidadMantenibilidad = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.maintainability", getLocale()));
        calidadMantenibilidad.setItems(getNumericOptions());
        ComboBox<Integer> calidadPortabilidad = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.portability", getLocale()));
        calidadPortabilidad.setItems(getNumericOptions());
        ComboBox<Integer> calidadEficiencia = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.efficiency", getLocale()));
        calidadEficiencia.setItems(getNumericOptions());
        ComboBox<Integer> calidadUsabilidad = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.usability", getLocale()));
        calidadUsabilidad.setItems(getNumericOptions());
        ComboBox<Integer> calidadCompatibilidad = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.compatibility", getLocale()));
        calidadCompatibilidad.setItems(getNumericOptions());
        ComboBox<Integer> calidadSeguridad = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.security", getLocale()));
        calidadSeguridad.setItems(getNumericOptions());

        h22.setText(i18nProvider.getTranslation("technical_evaluation.management_support", getLocale()));
        layoutColumn2.setAlignSelf(Alignment.CENTER, h22);
        h22.setWidth("max-content");

        // Añadir subcategorías para la gestión y soporte
        ComboBox<Integer> gestionGarantia = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.warranty", getLocale()));
        gestionGarantia.setItems(getNumericOptions());
        ComboBox<Integer> gestionTiempoRespuesta = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.response_time", getLocale()));
        gestionTiempoRespuesta.setItems(getNumericOptions());
        ComboBox<Integer> gestionAtencionCliente = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.customer_service", getLocale()));
        gestionAtencionCliente.setItems(getNumericOptions());
        ComboBox<Integer> gestionDocumentacion = new ComboBox<>(i18nProvider.getTranslation("technical_evaluation.documentation", getLocale()));
        gestionDocumentacion.setItems(getNumericOptions());

        layoutRow.setWidthFull();
        layoutColumn2.setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");

        buttonPrimary.addClickListener(e -> {
            String selectedProject = comboBox.getValue();

            // Obtener los valores de todas las subcategorías
            Integer calAdecuacion = calidadAdecuacionFuncional.getValue();
            Integer calMantenibilidad = calidadMantenibilidad.getValue();
            Integer calPortabilidad = calidadPortabilidad.getValue();
            Integer calEficiencia = calidadEficiencia.getValue();
            Integer calUsabilidad = calidadUsabilidad.getValue();
            Integer calCompatibilidad = calidadCompatibilidad.getValue();
            Integer calSeguridad = calidadSeguridad.getValue();

            Integer gesGarantia = gestionGarantia.getValue();
            Integer gesTiempoRespuesta = gestionTiempoRespuesta.getValue();
            Integer gesAtencionCliente = gestionAtencionCliente.getValue();
            Integer gesDocumentacion = gestionDocumentacion.getValue();

            // Validar que se hayan seleccionado todos los campos
            if (selectedProject == null) {
                Notification.show(i18nProvider.getTranslation("technical_evaluation.select_project_error", getLocale()), 3000, Notification.Position.MIDDLE);
                return;
            }

            // Validar que todas las subcategorías tengan valores
            if (calAdecuacion == null || calMantenibilidad == null || calPortabilidad == null ||
                    calEficiencia == null || calUsabilidad == null || calCompatibilidad == null || calSeguridad == null ||
                    gesGarantia == null || gesTiempoRespuesta == null || gesAtencionCliente == null || gesDocumentacion == null) {
                Notification.show(i18nProvider.getTranslation("technical_evaluation.complete_all_ratings", getLocale()), 3000, Notification.Position.MIDDLE);
                return;
            }

            // Calcular la suma total de las calificaciones
            Double calTecnica = (calAdecuacion + calMantenibilidad + calPortabilidad + calEficiencia + calUsabilidad +
                    calCompatibilidad + calSeguridad + gesGarantia + gesTiempoRespuesta + gesAtencionCliente + gesDocumentacion)/11.0;

            try {
                proyectoService.updateCalTecnica(selectedProject, calTecnica);
                Notification.show(i18nProvider.getTranslation("technical_evaluation.save_success", getLocale()), 3000, Notification.Position.MIDDLE);

                // Limpiar los campos después de la acción
                comboBox.clear();
                calidadAdecuacionFuncional.clear();
                calidadMantenibilidad.clear();
                calidadPortabilidad.clear();
                calidadEficiencia.clear();
                calidadUsabilidad.clear();
                calidadCompatibilidad.clear();
                calidadSeguridad.clear();
                gestionGarantia.clear();
                gestionTiempoRespuesta.clear();
                gestionAtencionCliente.clear();
                gestionDocumentacion.clear();
                calificacionTecnicaLabel.setVisible(false); // Ocultar el Span de calificación después de la acción
            } catch (Exception ex) {
                Notification.show(i18nProvider.getTranslation("technical_evaluation.save_error", getLocale()), 3000, Notification.Position.MIDDLE);
            }
        });

        // Estructura de diseño
        layoutColumn2.add(h2, formLayout2Col, hr, h22, formLayout2Col, hr4, buttonPrimary, downloadButton, downloadButton2, calificacionTecnicaLabel);
        layoutColumn2.setAlignItems(Alignment.CENTER);

        // Asignar el layout final
        getContent().add(layoutColumn2);
        getContent().add(layoutColumn2);
        layoutColumn2.add(formLayout2Col);
        formLayout2Col.add(comboBox);
        layoutColumn2.add(downloadButton); // Añadir botón de descarga debajo del ComboBox
        layoutColumn2.add(downloadButton2); // Añadir botón de descarga debajo del ComboBox
        layoutColumn2.add(calificacionTecnicaLabel); // Añadir label de calificación técnica
        layoutColumn2.add(hr);
        layoutColumn2.add(h2);
        layoutColumn2.add(hr2);
        layoutColumn2.add(calidadAdecuacionFuncional);
        layoutColumn2.add(calidadMantenibilidad);
        layoutColumn2.add(calidadPortabilidad);
        layoutColumn2.add(calidadEficiencia);
        layoutColumn2.add(calidadUsabilidad);
        layoutColumn2.add(calidadCompatibilidad);
        layoutColumn2.add(calidadSeguridad);
        layoutColumn2.add(hr3);
        layoutColumn2.add(h22);
        layoutColumn2.add(hr4);
        layoutColumn2.add(gestionGarantia);
        layoutColumn2.add(gestionTiempoRespuesta);
        layoutColumn2.add(gestionAtencionCliente);
        layoutColumn2.add(gestionDocumentacion);
        layoutColumn2.add(layoutRow);
        layoutRow.add(buttonPrimary);
    }

    private List<Integer> getNumericOptions() {
        List<Integer> options = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            options.add(i);
        }
        return options;
    }
}
