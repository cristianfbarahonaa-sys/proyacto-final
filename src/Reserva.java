import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reserva implements Cotizable, gestionableEstado<EstadoReserva> {
    private final String codigo;
    private final CanalReserva canal;
    private final String codigoExterno;
    private final Apartamento apartamento;
    private final Huesped titular;
    private final List<Ocupante> ocupantes;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final LocalDateTime fechaCreacion;
    private EstadoReserva estado;
    private final Folio folio;

    private final long valorEstanciaCongelado;
    private final String desgloseNochesCongelado;
    private long valorDevolucionRegistrada;

    public Reserva(String codigo, CanalReserva canal, String codigoExterno,
                   Apartamento apartamento, Huesped titular, List<Ocupante> ocupantes,
                   LocalDate fechaEntrada, LocalDate fechaSalida,
                   long valorEstanciaCongelado, String desgloseNochesCongelado) {
        this.codigo = codigo;
        this.canal = canal;
        this.codigoExterno = codigoExterno;
        this.apartamento = apartamento;
        this.titular = titular;
        this.ocupantes = new ArrayList<>(ocupantes);
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoReserva.PENDIENTE;
        this.folio = new Folio();
        this.valorEstanciaCongelado = valorEstanciaCongelado;
        this.desgloseNochesCongelado = desgloseNochesCongelado;

        this.folio.registrarCargo("Estancia alojamiento (" + calcularNoches() + " noches)", valorEstanciaCongelado);
    }

    public long calcularNoches() {
        return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    public boolean seSolapaCon(LocalDate otraEntrada, LocalDate otraSalida) {
        return this.fechaEntrada.isBefore(otraSalida) && otraEntrada.isBefore(this.fechaSalida);
    }

    public boolean cumpleAnticipoParaConfirmar() {
        long anticipoRequerido = Math.round(valorEstanciaCongelado * canal.getPorcentajeAnticipoExigido());
        return folio.calcularTotalPagos() >= anticipoRequerido;
    }

    public long calcularComisionPlataforma() {
        return Math.round(valorEstanciaCongelado * canal.getPorcentajeComision());
    }

    @Override
    public long calcularValorEstancia() {
        return valorEstanciaCongelado;
    }

    @Override
    public String generarDesgloseNoches() {
        return desgloseNochesCongelado;
    }

    @Override
    public boolean puedeCambiarA(EstadoReserva nuevoEstado) {
        switch (this.estado) {
            case PENDIENTE:
                return nuevoEstado == EstadoReserva.CONFIRMADA || nuevoEstado == EstadoReserva.CANCELADA;
            case CONFIRMADA:
                return nuevoEstado == EstadoReserva.EN_CURSO
                        || nuevoEstado == EstadoReserva.CANCELADA
                        || nuevoEstado == EstadoReserva.NO_SHOW;
            case EN_CURSO:
                return nuevoEstado == EstadoReserva.FINALIZADA;
            default:
                return false;
        }
    }

    @Override
    public void cambiarEstado(EstadoReserva nuevoEstado, String motivo) {
        if (!puedeCambiarA(nuevoEstado)) {
            throw new IllegalStateException("Transición inválida: no se puede pasar de " + estado + " a " + nuevoEstado);
        }
        if (nuevoEstado == EstadoReserva.CONFIRMADA && !cumpleAnticipoParaConfirmar()) {
            long min = Math.round(valorEstanciaCongelado * canal.getPorcentajeAnticipoExigido());
            throw new IllegalStateException("El canal " + canal + " exige un anticipo mínimo de $" + min + " para confirmar.");
        }
        this.estado = nuevoEstado;
    }

    public long calcularRetencionPorCancelacion(LocalDate fechaCancelacion, boolean esNoShow) {
        if (esNoShow) {
            return valorEstanciaCongelado;
        }
        long diasAntelacion = ChronoUnit.DAYS.between(fechaCancelacion, fechaEntrada);
        if (diasAntelacion > 7) {
            return 0L;
        } else if (diasAntelacion >= 2) {
            return Math.round(valorEstanciaCongelado * 0.50);
        } else {
            return valorEstanciaCongelado;
        }
    }

    public long calcularDevolucionEstimada(LocalDate fechaCancelacion, boolean esNoShow) {
        long pagado = folio.calcularTotalPagos();
        long retencion = calcularRetencionPorCancelacion(fechaCancelacion, esNoShow);
        return Math.max(0L, pagado - retencion);
    }

    public void liquidarCancelacionONoShow(LocalDate fechaEvento, boolean esNoShow) {
        long retencion = calcularRetencionPorCancelacion(fechaEvento, esNoShow);
        this.valorDevolucionRegistrada = calcularDevolucionEstimada(fechaEvento, esNoShow);
        long reduccionCargo = valorEstanciaCongelado - retencion;
        if (reduccionCargo > 0) {
            folio.registrarCorreccionDeCargo("Ajuste por política de cancelación", reduccionCargo);
        }
    }

    public String getCodigo() { return codigo; }
    public CanalReserva getCanal() { return canal; }
    public String getCodigoExterno() { return codigoExterno; }
    public Apartamento getApartamento() { return apartamento; }
    public Huesped getTitular() { return titular; }
    public List<Ocupante> getOcupantes() { return Collections.unmodifiableList(ocupantes); }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public EstadoReserva getEstado() { return estado; }
    public Folio getFolio() { return folio; }
    public long getValorDevolucionRegistrada() { return valorDevolucionRegistrada; }
//arreglo
    @Override
    public String toString() {
        return codigo + " | " + canal + " | Apto: " + apartamento.getCodigo() +
                " | Titular: " + titular.getNombre() +
                " | " + fechaEntrada + " -> " + fechaSalida +
                " | Estado: " + estado +
                " | Saldo: $" + folio.calcularSaldoPendiente();
    }
}