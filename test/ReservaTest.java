import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    @Test
    void testCalculoNochesEstancia() {

        LocalDate entrada = LocalDate.of(2026, 3, 10);
        LocalDate salida = LocalDate.of(2026, 3, 12);


        Reserva reserva = new Reserva("R-001", null, null, null, null, Collections.emptyList(),
                entrada, salida, 200000L, "Desglose de prueba");


        assertEquals(2L, reserva.calcularNoches(), "El cálculo de noches entre el 10 y el 12 debe ser exactamente 2.");
    }

    @Test
    void testSolapamientoReservasDobleVenta() {

        LocalDate entradaExistente = LocalDate.of(2026, 12, 10);
        LocalDate salidaExistente = LocalDate.of(2026, 12, 15);

        Reserva reserva1 = new Reserva("R-001", null, null, null, null, Collections.emptyList(),
                entradaExistente, salidaExistente, 500000L, "Desglose 1");


        boolean seSolapaCruze = reserva1.seSolapaCon(LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 18));
        assertTrue(seSolapaCruze, "Debe detectar solapamiento si las fechas se cruzan.");


        boolean seSolapaSinCruze = reserva1.seSolapaCon(LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 18));
        assertFalse(seSolapaSinCruze, "No debe haber solapamiento si una reserva termina justo cuando empieza la otra.");
    }
}