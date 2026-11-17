import java.time.LocalDate;

public class Ocupante extends Persona {

    public Ocupante(String nombre, LocalDate fechaNacimiento) {
        super(nombre, fechaNacimiento);
    }

    // Los menores de 6 años ocupan cupo pero no pagan tarifa
    public boolean pagaTarifa(LocalDate fechaEntrada) {
        return calcularEdad(fechaEntrada) >= 6;
    }

    @Override
    public String toString() {
        return getNombre() + " (Nac: " + getFechaNacimiento() + ")";
    }
}