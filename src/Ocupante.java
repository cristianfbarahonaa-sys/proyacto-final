import java.time.LocalDate;

public class Ocupante extends Persona {

    public Ocupante(String nombre, LocalDate fechaNacimiento) {
        super(nombre, fechaNacimiento);
    }
    
    public boolean pagaTarifa(LocalDate fechaEntrada) {
        return calcularEdad(fechaEntrada) >= 6;
    }

    @Override
    public String toString() {
        return getNombre() + " (Nac: " + getFechaNacimiento() + ")";
    }
}