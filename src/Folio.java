import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Folio {
    private final List<MovimientoFolio> movimientos = new ArrayList<>();

    public void registrarCargo(String concepto, long monto) {
        movimientos.add(new MovimientoFolio.Cargo(concepto, monto));
    }

    public void registrarPago(String concepto, long monto, MedioPago medioPago) {
        movimientos.add(new MovimientoFolio.Pago(concepto, monto, medioPago));
    }

    public void registrarCorreccionDeCargo(String motivo, long monto) {
        movimientos.add(new MovimientoFolio.Pago("CORRECCIÓN DE CARGO: " + motivo, monto, MedioPago.TRANSFERENCIA));
    }

    public long calcularTotalCargos() {
        return movimientos.stream()
                .filter(m -> m instanceof MovimientoFolio.Cargo)
                .mapToLong(MovimientoFolio::getMonto)
                .sum();
    }

    public long calcularTotalPagos() {
        return movimientos.stream()
                .filter(m -> m instanceof MovimientoFolio.Pago)
                .mapToLong(MovimientoFolio::getMonto)
                .sum();
    }
    public long calcularSaldoPendiente() {
        long saldo = 0;
        for (MovimientoFolio m : movimientos) {
            saldo = m.aplicarAlSaldo(saldo);
        }
        return saldo;
    }
    public List<MovimientoFolio> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }
}