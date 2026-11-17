public class FolioYApartamentoTest {
    import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

    class FolioYApartamentoTest {

        @Test
        void testFolioCalculoSaldoPendiente() {
            // Arrange: Creamos un folio y registramos un cargo por estancia y un pago parcial
            Folio folio = new Folio();
            folio.registrarCargo("Estancia alojamiento (2 noches)", 300000L);
            folio.registrarPago("Anticipo 30%", 90000L, null);

            // Act & Assert: El saldo pendiente debe ser la diferencia (300000 - 90000)
            assertEquals(210000L, folio.calcularSaldoPendiente(), "El saldo pendiente debe reflejar los cargos menos los pagos.");
        }

        @Test
        void testApartamentoValidacionTarifasYCapacidad() {
            // Arrange & Act: Creamos un apartamento con tarifa base de 100,000 y capacidad máxima de 4
            Apartamento apto = new Apartamento("APT-101", "Suite Andina", 2, 4, "Vista al jardín", 100000L);

            // Assert
            assertEquals(100000L, apto.obtenerTarifaPorTemporada(Temporada.BAJA), "La tarifa en temporada baja debe ser la base.");
            assertEquals(4, apto.getCapacidadMaxima(), "La capacidad máxima debe coincidir.");
        }

        @Test
        void testApartamentoBloqueoFechas() {
            // Arrange
            Apartamento apto = new Apartamento("APT-102", "Suite Bosque", 1, 2, "Chimenea", 120000L);
            LocalDate inicioBloqueo = LocalDate.of(2026, 12, 20);
            LocalDate finBloqueo = LocalDate.of(2026, 12, 23);

            // Act
            apto.bloquearRangoFechas(inicioBloqueo, finBloqueo, "Mantenimiento preventivo de tubería");

            // Assert: Validar que las noches del rango estén bloqueadas
            assertTrue(apto.estaBloqueadoEnNoche(LocalDate.of(2026, 12, 20)), "La noche del 20 debe estar bloqueada.");
            assertTrue(apto.estaBloqueadoEnNoche(LocalDate.of(2026, 12, 22)), "La noche del 22 debe estar bloqueada.");
            assertFalse(apto.estaBloqueadoEnNoche(LocalDate.of(2026, 12, 23)), "La noche de salida (fin) no se bloquea.");
        }
    }
}
