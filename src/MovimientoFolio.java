import java.time.LocalDateTime;

public abstract class MovimientoFolio {
    private final LocalDateTime fecha;
    private final String concepto;
    private final long monto; // En pesos colombianos, sin decimales

    public MovimientoFolio(String concepto, long monto) {
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto del movimiento es obligatorio.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        this.fecha = LocalDateTime.now();
        this.concepto = concepto.trim();
        this.monto = monto;
    }

    // Método polimórfico sobrescrito por Cargo y Pago
    public abstract long aplicarAlSaldo(long saldoActual);
    public abstract String getTipoMovimiento();

    public LocalDateTime getFecha() { return fecha; }
    public String getConcepto() { return concepto; }
    public long getMonto() { return monto; }

    @Override
    public String toString() {
        return "[" + fecha.toLocalDate() + "] " + getTipoMovimiento() + " - " + concepto + ": $" + monto;
    }

    // Subclase para Cargos (alojamiento, desayuno, lavandería, penalidades)
    public static class Cargo extends MovimientoFolio {
        public Cargo(String concepto, long monto) {
            super(concepto, monto);
        }

        @Override
        public long aplicarAlSaldo(long saldoActual) {
            return saldoActual + getMonto();
        }

        @Override
        public String getTipoMovimiento() {
            return "CARGO (+)";
        }
    }

    // Subclase para Pagos (abonos con medio de pago)
    public static class Pago extends MovimientoFolio {
        private final MedioPago medioPago;

        public Pago(String concepto, long monto, MedioPago medioPago) {
            super(concepto, monto);
            if (medioPago == null) {
                throw new IllegalArgumentException("Debe especificar el medio de pago.");
            }
            this.medioPago = medioPago;
        }

        @Override
        public long aplicarAlSaldo(long saldoActual) {
            return saldoActual - getMonto();
        }

        @Override
        public String getTipoMovimiento() {
            return "PAGO (-) [" + medioPago + "]";
        }

        public MedioPago getMedioPago() {
            return medioPago;
        }
    }
}