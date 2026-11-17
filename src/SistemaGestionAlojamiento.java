import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SistemaGestionAlojamiento {
    private final List<Apartamento> apartamentos = new ArrayList<>();
    private final List<Huesped> huespedes = new ArrayList<>();
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();
    private final List<String> conflictosCanalExterno = new ArrayList<>();
    private int consecutivoReservas = 100;

    public SistemaGestionAlojamiento() {
        inicializarDatosPrueba();
    }

    private void inicializarDatosPrueba() {
        apartamentos.add(new Apartamento("APT-101", "Suite Cóndor", 2, 4, "Balcón, vista a las montañas, chimenea", 90000));
        apartamentos.add(new Apartamento("APT-102", "Loft Palma de Cera", 1, 2, "Jacuzzi, mirador privado", 110000));
        apartamentos.add(new Apartamento("APT-201", "Familiar Cocora", 3, 6, "Terraza, cocina amplia, chimenea", 85000));

        huespedes.add(new Huesped("Juan Pérez", "1094900100", LocalDate.of(1992, 4, 12), "3150001122", "juan@uq.edu.co"));
        huespedes.add(new Huesped("María Gómez", "1094800200", LocalDate.of(1988, 9, 25), "3109998877", "maria@uq.edu.co"));

        empleados.add(new Empleado("Laura Recepción", "1094111", LocalDate.of(1997, 2, 10), "311222333", "recep@uq.edu.co", "recep", "1234", "RECEPCIONISTA"));
        empleados.add(new Empleado("Carlos Admin", "1094222", LocalDate.of(1985, 7, 19), "312444555", "admin@uq.edu.co", "admin", "1234", "ADMINISTRADOR"));
    }

    public Temporada determinarTemporada(LocalDate fecha) {
        Month mes = fecha.getMonth();
        if (mes == Month.DECEMBER || mes == Month.JANUARY || mes == Month.JUNE || mes == Month.JULY) {
            return Temporada.ALTA;
        }
        int diaSemana = fecha.getDayOfWeek().getValue(); // Viernes (5) y Sábado (6) como temporada media
        if (diaSemana == 5 || diaSemana == 6) {
            return Temporada.MEDIA;
        }
        return Temporada.BAJA;
    }

    public void validarFechas(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida son obligatorias.");
        }
        if (entrada.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se permiten reservas con fecha de entrada anterior a hoy.");
        }
        if (!salida.isAfter(entrada)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada (mínimo 1 noche).");
        }
    }

    public boolean estaApartamentoDisponible(Apartamento apto, LocalDate entrada, LocalDate salida) {
        if (apto == null || !apto.isActivo() || apto.getEstadoOperativo() == EstadoApartamento.FUERA_DE_SERVICIO) {
            return false;
        }
        for (LocalDate noche = entrada; noche.isBefore(salida); noche = noche.plusDays(1)) {
            if (apto.estaBloqueadoEnNoche(noche)) {
                return false;
            }
        }
        // Regla más importante: sin doble venta sobre reservas activas
        return reservas.stream()
                .filter(r -> r.getApartamento().equals(apto) && r.getEstado().esActiva())
                .noneMatch(r -> r.seSolapaCon(entrada, salida));
    }

    public List<Apartamento> consultarDisponibles(LocalDate entrada, LocalDate salida, int cantidadPersonas) {
        validarFechas(entrada, salida);
        return apartamentos.stream()
                .filter(Apartamento::isActivo)
                .filter(a -> a.getCapacidadMaxima() >= cantidadPersonas)
                .filter(a -> estaApartamentoDisponible(a, entrada, salida))
                .collect(Collectors.toList());
    }

    public long calcularValorEstancia(Apartamento apto, LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes) {
        validarFechas(entrada, salida);
        if (ocupantes.size() > apto.getCapacidadMaxima()) {
            throw new IllegalArgumentException("El número de ocupantes (" + ocupantes.size() +
                    ") supera la capacidad máxima del apartamento (" + apto.getCapacidadMaxima() + ").");
        }
        long personasQuePagan = ocupantes.stream().filter(o -> o.pagaTarifa(entrada)).count();
        long total = 0;
        for (LocalDate noche = entrada; noche.isBefore(salida); noche = noche.plusDays(1)) {
            Temporada temp = determinarTemporada(noche);
            total += apto.obtenerTarifaPorTemporada(temp) * personasQuePagan;
        }
        return total;
    }

    public String generarDesgloseCotizacion(Apartamento apto, LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes) {
        validarFechas(entrada, salida);
        long pagan = ocupantes.stream().filter(o -> o.pagaTarifa(entrada)).count();
        long menores = ocupantes.size() - pagan;
        StringBuilder sb = new StringBuilder();
        sb.append("Ocupantes: ").append(ocupantes.size())
                .append(" (Pagan >=6 años: ").append(pagan).append(" | Menores <6 gratis: ").append(menores).append(")\n");

        long acumulado = 0;
        for (LocalDate noche = entrada; noche.isBefore(salida); noche = noche.plusDays(1)) {
            Temporada temp = determinarTemporada(noche);
            long tarifa = apto.obtenerTarifaPorTemporada(temp);
            long subtotal = tarifa * pagan;
            acumulado += subtotal;
            sb.append(" - Noche ").append(noche).append(" [").append(temp).append("]: $")
                    .append(tarifa).append(" x ").append(pagan).append(" = $").append(subtotal).append("\n");
        }
        sb.append("TOTAL ESTANCIA CONGELADO: $").append(acumulado);
        return sb.toString();
    }

    public Reserva crearReserva(CanalReserva canal, String codigoExterno, Apartamento apto, Huesped titular,
                                List<Ocupante> ocupantes, LocalDate entrada, LocalDate salida) {
        validarFechas(entrada, salida);
        if (apto == null || titular == null) {
            throw new IllegalArgumentException("Debe seleccionar un apartamento y un huésped titular.");
        }
        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un ocupante.");
        }
        if (ocupantes.size() > apto.getCapacidadMaxima()) {
            throw new IllegalArgumentException("Los ocupantes superan la capacidad máxima (" + apto.getCapacidadMaxima() + ").");
        }

        if (canal == CanalReserva.EXTERNA) {
            if (codigoExterno == null || codigoExterno.isBlank()) {
                throw new IllegalArgumentException("El canal EXTERNA exige un código de plataforma externa.");
            }
            boolean repetido = reservas.stream()
                    .filter(r -> r.getCanal() == CanalReserva.EXTERNA && r.getCodigoExterno() != null)
                    .anyMatch(r -> r.getCodigoExterno().equalsIgnoreCase(codigoExterno.trim()));
            if (repetido) {
                throw new IllegalStateException("Ya existe una reserva con el código externo: " + codigoExterno);
            }
            if (!estaApartamentoDisponible(apto, entrada, salida)) {
                String conflicto = "Conflicto Canal Externo [" + codigoExterno + "] en " + apto.getCodigo() +
                        " (" + entrada + " a " + salida + ")";
                conflictosCanalExterno.add(conflicto);
                throw new IllegalStateException("Choque con reserva vigente. Rechazada y registrada como conflicto de canal.");
            }
        } else {
            if (!estaApartamentoDisponible(apto, entrada, salida)) {
                throw new IllegalStateException("El apartamento " + apto.getCodigo() + " no está disponible en esas fechas.");
            }
        }

        long valor = calcularValorEstancia(apto, entrada, salida, ocupantes);
        String desglose = generarDesgloseCotizacion(apto, entrada, salida, ocupantes);
        String codigo = "RES-" + (++consecutivoReservas);

        Reserva nueva = new Reserva(codigo, canal, codigoExterno, apto, titular, ocupantes, entrada, salida, valor, desglose);
        reservas.add(nueva);
        return nueva;
    }

    public void registrarCheckIn(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se permite Check-In si la reserva está CONFIRMADA.");
        }
        if (LocalDate.now().isBefore(reserva.getFechaEntrada())) {
            throw new IllegalStateException("Aún no ha llegado la fecha de entrada (" + reserva.getFechaEntrada() + ").");
        }
        Apartamento apto = reserva.getApartamento();
        if (apto.getEstadoOperativo() != EstadoApartamento.PREPARADO) {
            throw new IllegalStateException("El apartamento debe estar en estado PREPARADO para hacer Check-In.");
        }
        LocalDateTime momentoCheckIn = LocalDateTime.of(reserva.getFechaEntrada(), LocalTime.of(15, 0));
        if (apto.getUltimaSalidaRegistrada() != null &&
                ChronoUnit.HOURS.between(apto.getUltimaSalidaRegistrada(), momentoCheckIn) < 3) {
            throw new IllegalStateException("Deben pasar al menos 3 horas de preparación entre la salida (11 a.m.) y la entrada (3 p.m.).");
        }
        reserva.cambiarEstado(EstadoReserva.EN_CURSO, "Check-In realizado");
        apto.cambiarEstado(EstadoApartamento.OCUPADO, "Huéspedes en estancia");
    }

    public void registrarCheckOut(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.EN_CURSO) {
            throw new IllegalStateException("Solo se permite Check-Out de reservas EN_CURSO.");
        }
        reserva.cambiarEstado(EstadoReserva.FINALIZADA, "Check-Out 11:00 a. m.");
        Apartamento apto = reserva.getApartamento();
        apto.setUltimaSalidaRegistrada(LocalDateTime.of(reserva.getFechaSalida(), LocalTime.of(11, 0)));
        apto.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION, "Salida de grupo");
    }

    public void declararNoShow(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se puede declarar No-Show sobre una reserva CONFIRMADA.");
        }
        reserva.liquidarCancelacionONoShow(reserva.getFechaEntrada(), true);
        reserva.cambiarEstado(EstadoReserva.NO_SHOW, "No llegó antes de las 10:00 p. m.");
    }

    public void cancelarReserva(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.PENDIENTE && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se pueden cancelar reservas PENDIENTES o CONFIRMADAS.");
        }
        reserva.liquidarCancelacionONoShow(LocalDate.now(), false);
        reserva.cambiarEstado(EstadoReserva.CANCELADA, "Cancelada por el usuario");
    }

    public String generarReporteGeneral() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE GERENCIAL DEL ALOJAMIENTO ===\n");
        long recaudado = reservas.stream().mapToLong(r -> r.getFolio().calcularTotalPagos()).sum();
        long comisiones = reservas.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .mapToLong(Reserva::calcularComisionPlataforma).sum();
        long saldosPendientes = reservas.stream()
                .filter(r -> r.getEstado().esActiva())
                .mapToLong(r -> Math.max(0L, r.getFolio().calcularSaldoPendiente())).sum();

        sb.append("Total Recaudado: $").append(recaudado)
                .append(" | Comisiones Canal Externo (15%): $").append(comisiones)
                .append(" | Saldos Pendientes: $").append(saldosPendientes).append("\n\n");

        sb.append("Reservas totales: ").append(reservas.size()).append("\n");
        for (CanalReserva c : CanalReserva.values()) {
            long count = reservas.stream().filter(r -> r.getCanal() == c).count();
            sb.append(" - Canal ").append(c).append(": ").append(count).append("\n");
        }
        if (!conflictosCanalExterno.isEmpty()) {
            sb.append("\nConflictos de Canal Externo:\n");
            conflictosCanalExterno.forEach(c -> sb.append(" * ").append(c).append("\n"));
        }
        return sb.toString();
    }

    public List<Apartamento> getApartamentos() { return apartamentos; }
    public List<Huesped> getHuespedes() { return huespedes; }
    public List<Empleado> getEmpleados() { return empleados; }
    public List<Reserva> getReservas() { return reservas; }
    public List<String> getConflictosCanalExterno() { return conflictosCanalExterno; }
}