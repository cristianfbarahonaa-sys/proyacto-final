import java.time.LocalDate;
import java.time.Period;

public abstract class Persona {
    private String nombre;
    private LocalDate fechaNacimiento;

    public Persona(String nombre, LocalDate fechaNacimiento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la persona es obligatorio.");
        }
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no es válida.");
        }
        this.nombre = nombre.trim();
        this.fechaNacimiento = fechaNacimiento;
    }

    // Regla del negocio: la edad nunca se almacena, siempre se calcula
    public int calcularEdad(LocalDate fechaReferencia) {
        return Period.between(this.fechaNacimiento, fechaReferencia).getYears();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}