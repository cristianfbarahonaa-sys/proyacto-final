import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) {
        Application.launch(VentanaSGA.class, args);
    }

    public static class VentanaSGA extends Application {

        private final SistemaGestionAlojamiento sistema = new SistemaGestionAlojamiento();
        private final ListView<Apartamento> lvApartamentos = new ListView<>();
        private final ListView<String> lvReservas = new ListView<>();
        private final TextArea taDetalle = new TextArea();

        @Override
        public void start(Stage primaryStage) {
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            tabPane.getTabs().addAll(
                    crearTabReservas(),
                    crearTabOperacion(),
                    crearTabAdministracion()
            );

            actualizarListas();

            Scene scene = new Scene(tabPane, 1100, 720);
            primaryStage.setTitle("SGA - Sistema de Gestión de Alojamiento | Aparta-Suites UQ");
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        // =====================================================================
        // MÉTODOS AUXILIARES CONECTADOS A TODAS LAS PESTAÑAS
        // =====================================================================

        public void actualizarListas() {
            lvApartamentos.getItems().setAll(sistema.getApartamentos());
            lvReservas.getItems().clear();
            for (Reserva r : sistema.getReservas()) {
                lvReservas.getItems().add(r.toString());
            }
        }

        public void mostrarMensaje(String titulo, String contenido) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(contenido);
            alert.showAndWait();
        }

        public Reserva obtenerReservaSeleccionada() {
            int idx = lvReservas.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < sistema.getReservas().size()) {
                return sistema.getReservas().get(idx);
            }
            return null;
        }

        public void mostrarDetalleReserva(Reserva r) {
            StringBuilder sb = new StringBuilder();
            sb.append(r.toString()).append("\n\n");
            sb.append(r.generarDesgloseNoches()).append("\n\n");
            sb.append("--- MOVIMIENTOS DEL FOLIO ---\n");
            for (MovimientoFolio m : r.getFolio().getMovimientos()) {
                sb.append(m.toString()).append("\n");
            }
            sb.append("Total Cargos: $").append(r.getFolio().calcularTotalCargos())
                    .append(" | Total Pagos: $").append(r.getFolio().calcularTotalPagos())
                    .append(" | Saldo Pendiente: $").append(r.getFolio().calcularSaldoPendiente());
            if (r.getValorDevolucionRegistrada() > 0) {
                sb.append("\nDevolución registrada al cancelar: $").append(r.getValorDevolucionRegistrada());
            }
            taDetalle.setText(sb.toString());
        }

        public void ejecutarAccionReserva(Consumer<Reserva> accion) {
            try {
                Reserva r = obtenerReservaSeleccionada();
                if (r == null) {
                    throw new IllegalArgumentException("Seleccione primero una reserva de la lista.");
                }
                accion.accept(r);
                actualizarListas();
                mostrarDetalleReserva(r);
            } catch (Exception ex) {
                mostrarMensaje("Operación rechazada", ex.getMessage());
            }
        }

        // =====================================================================
        // PESTAÑAS DE LA INTERFAZ GRÁFICA
        // =====================================================================

        private Tab crearTabReservas() {
            HBox contenedor = new HBox(15);
            contenedor.setPadding(new Insets(15));

            VBox formulario = new VBox(8);
            formulario.setPrefWidth(480);

            ComboBox<CanalReserva> cbCanal = new ComboBox<>(FXCollections.observableArrayList(CanalReserva.values()));
            cbCanal.setValue(CanalReserva.PORTAL);
            TextField tfCodigoExterno = new TextField();
            tfCodigoExterno.setPromptText("Código externo (solo canal EXTERNA)");

            ComboBox<Huesped> cbHuesped = new ComboBox<>(FXCollections.observableArrayList(sistema.getHuespedes()));
            if (!sistema.getHuespedes().isEmpty()) {
                cbHuesped.getSelectionModel().selectFirst();
            }

            DatePicker dpEntrada = new DatePicker(LocalDate.now().plusDays(1));
            DatePicker dpSalida = new DatePicker(LocalDate.now().plusDays(3));

            List<Ocupante> ocupantesTemp = new ArrayList<>();
            ListView<Ocupante> lvOcupantes = new ListView<>();
            lvOcupantes.setPrefHeight(90);

            HBox boxOcupante = new HBox(6);
            TextField tfNombreOcupante = new TextField();
            tfNombreOcupante.setPromptText("Nombre ocupante");
            DatePicker dpNacOcupante = new DatePicker(LocalDate.now().minusYears(20));
            Button btnAgregarOcupante = new Button("+ Ocupante");
            boxOcupante.getChildren().addAll(tfNombreOcupante, dpNacOcupante, btnAgregarOcupante);

            btnAgregarOcupante.setOnAction(e -> {
                try {
                    Ocupante oc = new Ocupante(tfNombreOcupante.getText(), dpNacOcupante.getValue());
                    ocupantesTemp.add(oc);
                    lvOcupantes.getItems().setAll(ocupantesTemp);
                    tfNombreOcupante.clear();
                } catch (Exception ex) {
                    mostrarMensaje("Error", ex.getMessage());
                }
            });

            HBox accionesReserva = new HBox(8);
            Button btnCotizar = new Button("Cotizar Estancia");
            Button btnCrearReserva = new Button("Crear Reserva");
            accionesReserva.getChildren().addAll(btnCotizar, btnCrearReserva);

            btnCotizar.setOnAction(e -> {
                try {
                    Apartamento apto = lvApartamentos.getSelectionModel().getSelectedItem();
                    if (apto == null) {
                        throw new IllegalArgumentException("Seleccione un apartamento de la lista.");
                    }
                    if (ocupantesTemp.isEmpty() && cbHuesped.getValue() != null) {
                        ocupantesTemp.add(new Ocupante(cbHuesped.getValue().getNombre(), cbHuesped.getValue().getFechaNacimiento()));
                        lvOcupantes.getItems().setAll(ocupantesTemp);
                    }
                    taDetalle.setText(sistema.generarDesgloseCotizacion(apto, dpEntrada.getValue(), dpSalida.getValue(), ocupantesTemp));
                } catch  (Exception ex) {
                    mostrarMensaje("Error de Cotización", ex.getMessage());
                }
            });

            btnCrearReserva.setOnAction(e -> {
                try {
                    Apartamento apto = lvApartamentos.getSelectionModel().getSelectedItem();
                    if (ocupantesTemp.isEmpty() && cbHuesped.getValue() != null) {
                        ocupantesTemp.add(new Ocupante(cbHuesped.getValue().getNombre(), cbHuesped.getValue().getFechaNacimiento()));
                    }
                    Reserva r = sistema.crearReserva(cbCanal.getValue(), tfCodigoExterno.getText(),
                            apto, cbHuesped.getValue(), ocupantesTemp, dpEntrada.getValue(), dpSalida.getValue());
                    ocupantesTemp.clear();
                    lvOcupantes.getItems().clear();
                    actualizarListas();
                    taDetalle.setText("Reserva creada exitosamente:\n" + r.generarDesgloseNoches());
                } catch (Exception ex) {
                    mostrarMensaje("No se pudo crear la reserva", ex.getMessage());
                }
            });

            formulario.getChildren().addAll(
                    new Label("1. Seleccione Apartamento:"), lvApartamentos,
                    new Label("2. Canal y Huésped Titular:"), cbCanal, tfCodigoExterno, cbHuesped,
                    new HBox(10, new Label("Entrada:"), dpEntrada, new Label("Salida:"), dpSalida),
                    new Label("3. Ocupantes (Menores de 6 años no pagan pero ocupan cupo):"),
                    boxOcupante, lvOcupantes, accionesReserva
            );

            VBox panelDerecho = new VBox(10);
            panelDerecho.setPrefWidth(560);
            taDetalle.setEditable(false);
            taDetalle.setPrefHeight(260);

            lvReservas.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                Reserva sel = obtenerReservaSeleccionada();
                if (sel != null) {
                    mostrarDetalleReserva(sel);
                }
            });

            panelDerecho.getChildren().addAll(
                    new Label("Reservas en el Sistema:"), lvReservas,
                    new Label("Desglose de Estancia y Folio:"), taDetalle
            );

            contenedor.getChildren().addAll(formulario, panelDerecho);
            return new Tab("Reservas y Cotización", contenedor);
        }

        private Tab crearTabOperacion() {
            VBox layout = new VBox(12);
            layout.setPadding(new Insets(15));

            HBox botonesEstado = new HBox(10);
            Button btnConfirmar = new Button("Confirmar Reserva");
            Button btnCheckIn = new Button("Check-In (3:00 p. m.)");
            Button btnCheckOut = new Button("Check-Out (11:00 a. m.)");
            Button btnCancelar = new Button("Cancelar Reserva");
            Button btnNoShow = new Button("Declarar No-Show");
            botonesEstado.getChildren().addAll(btnConfirmar, btnCheckIn, btnCheckOut, btnCancelar, btnNoShow);

            HBox boxFolio = new HBox(8);
            TextField tfConcepto = new TextField();
            tfConcepto.setPromptText("Concepto pago o servicio adicional");
            TextField tfMonto = new TextField();
            tfMonto.setPromptText("Monto en pesos ($)");
            ComboBox<MedioPago> cbMedio = new ComboBox<>(FXCollections.observableArrayList(MedioPago.values()));
            cbMedio.setValue(MedioPago.TRANSFERENCIA);
            Button btnRegistrarPago = new Button("Registrar Pago");
            Button btnRegistrarCargo = new Button("Agregar Cargo Servicio");
            boxFolio.getChildren().addAll(tfConcepto, tfMonto, cbMedio, btnRegistrarPago, btnRegistrarCargo);

            HBox boxAptoEstado = new HBox(8);
            ComboBox<EstadoApartamento> cbEstadoApto = new ComboBox<>(FXCollections.observableArrayList(EstadoApartamento.values()));
            cbEstadoApto.setValue(EstadoApartamento.PREPARADO);
            TextField tfMotivoApto = new TextField();
            tfMotivoApto.setPromptText("Motivo (obligatorio si Fuera de Servicio) o Novedad");
            Button btnCambiarEstadoApto = new Button("Cambiar Estado Apartamento");
            boxAptoEstado.getChildren().addAll(new Label("Estado Apto Seleccionado:"), cbEstadoApto, tfMotivoApto, btnCambiarEstadoApto);

            btnConfirmar.setOnAction(e -> ejecutarAccionReserva(r -> r.cambiarEstado(EstadoReserva.CONFIRMADA, "Confirmada")));
            btnCheckIn.setOnAction(e -> ejecutarAccionReserva(sistema::registrarCheckIn));
            btnCheckOut.setOnAction(e -> ejecutarAccionReserva(sistema::registrarCheckOut));
            btnCancelar.setOnAction(e -> ejecutarAccionReserva(sistema::cancelarReserva));
            btnNoShow.setOnAction(e -> ejecutarAccionReserva(sistema::declararNoShow));

            btnRegistrarPago.setOnAction(e -> ejecutarAccionReserva(r -> {
                long monto = Long.parseLong(tfMonto.getText().trim());
                String concepto = tfConcepto.getText().isBlank() ? "Abono a folio" : tfConcepto.getText();
                r.getFolio().registrarPago(concepto, monto, cbMedio.getValue());
            }));

            btnRegistrarCargo.setOnAction(e -> ejecutarAccionReserva(r -> {
                long monto = Long.parseLong(tfMonto.getText().trim());
                String concepto = tfConcepto.getText().isBlank() ? "Servicio adicional" : tfConcepto.getText();
                r.getFolio().registrarCargo(concepto, monto);
            }));

            btnCambiarEstadoApto.setOnAction(e -> {
                try {
                    Apartamento apto = lvApartamentos.getSelectionModel().getSelectedItem();
                    if (apto == null) {
                        throw new IllegalArgumentException("Seleccione primero un apartamento en la pestaña 1.");
                    }
                    apto.cambiarEstado(cbEstadoApto.getValue(), tfMotivoApto.getText());
                    actualizarListas();
                    mostrarMensaje("Estado actualizado", "Apartamento actualizado: " + apto);
                } catch (Exception ex) {
                    mostrarMensaje("Error", ex.getMessage());
                }
            });

            layout.getChildren().addAll(
                    new Label("Operaciones sobre la Reserva Seleccionada (Seleccione una reserva en la pestaña 1):"),
                    botonesEstado,
                    new Label("Gestión de Pagos y Cargos del Folio:"),
                    boxFolio,
                    new Separator(),
                    boxAptoEstado
            );
            return new Tab("Operación Recepción y Folios", layout);
        }

        private Tab crearTabAdministracion() {
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(15));
            TextArea taReporte = new TextArea();
            taReporte.setEditable(false);
            taReporte.setPrefHeight(520);
            Button btnGenerar = new Button("Actualizar Reporte de Administración");
            btnGenerar.setOnAction(e -> taReporte.setText(sistema.generarReporteGeneral()));
            taReporte.setText(sistema.generarReporteGeneral());
            layout.getChildren().addAll(btnGenerar, taReporte);
            return new Tab("Administración y Reportes", layout);
        }
    }
}