import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    @Test
    void testCalculoNochesEstancia() {
        // Arrange: Creada con 2 noches (del 10 al 12 de marzo, la salida no cuenta como noche)
        LocalDate entrada = LocalDate.of(2026, 3, 10);
        LocalDate salida = LocalDate.of(2026, 3, 12);

        // Simulamos una reserva nula o mock en los parámetros que no afectan este cálculo directo
        Reserva reserva = new Reserva("R-001", null, null, null, null, Collections.emptyList(),
                entrada, salida, 200000L, "Desglose de prueba");

        // Act & Assert
        assertEquals(2L, reserva.calcularNoches(), "El cálculo de noches entre el 10 y el 12 debe ser exactamente 2.");
    }

    @Test
    void testSolapamientoReservasDobleVenta() {
        // Arrange: Reserva existente del 10 al 15 de diciembre
        LocalDate entradaExistente = LocalDate.of(2026, 12, 10);
        LocalDate salidaExistente = LocalDate.of(2026, 12, 15);

        Reserva reserva1 = new Reserva("R-001", null, null, null, null, Collections.emptyList(),
                entradaExistente, salidaExistente, 500000L, "Desglose 1");

        // Caso A: Intento de reserva que cruza fechas (entra el 12 y sale el 18) -> Debe solaparse (Doble venta detectada)
        boolean seSolapaCruze = reserva1.seSolapaCon(LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 18));
        assertTrue(seSolapaCruze, "Debe detectar solapamiento si las fechas se cruzan.");

        // Caso B: Intento de reserva fuera de rango (entra el 15 y sale el 18) -> No se solapa (la salida de una es la entrada de otra)
        boolean seSolapaSinCruze = reserva1.seSolapaCon(LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 18));
        assertFalse(seSolapaSinCruze, "No debe haber solapamiento si una reserva termina justo cuando empieza la otra.");
    }
}