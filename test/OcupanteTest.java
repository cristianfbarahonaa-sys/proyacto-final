import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class OcupanteTest {

    @Test
    void testMenorDeSeisAniosNoPagaTarifa() {
        LocalDate fechaEntrada = LocalDate.of(2026, 12, 10);
        // Ocupante de 4 años a la fecha de entrada (menor de 6)
        LocalDate fechaNacimiento = fechaEntrada.minusYears(4);

        Ocupante ocupante = new Ocupante("Mateo", fechaNacimiento);

        // Validamos que los menores de 6 años no pagan tarifa (regla de negocio del SGA)[cite: 16]
        assertFalse(ocupante.pagaTarifa(fechaEntrada), "El ocupante menor de 6 años no debe pagar tarifa.");
    }

    @Test
    void testMayorOIgualASeisAniosPagaTarifa() {
        LocalDate fechaEntrada = LocalDate.of(2026, 12, 10);
        // Ocupante de 10 años a la fecha de entrada (mayor de 6)
        LocalDate fechaNacimiento = fechaEntrada.minusYears(10);

        Ocupante ocupante = new Ocupante("Carlos", fechaNacimiento);

        // Validamos que de 6 años en adelante sí pagan tarifa
        assertTrue(ocupante.pagaTarifa(fechaEntrada), "El ocupante de 6 años o más sí debe pagar tarifa.");
    }
}