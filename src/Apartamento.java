import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Apartamento implements gestionableEstado<EstadoApartamento> {
    private final String codigo;
    private String nombre;
    private int numeroDormitorios;
    private int capacidadMaxima;
    private String caracteristicas;
    private final Map<Temporada, Long> tarifasTemporada = new EnumMap<>(Temporada.class);
    private EstadoApartamento estadoOperativo;
    private String motivoFueraServicio;
    private boolean activo;
    private LocalDateTime ultimaSalidaRegistrada;
    private final List<String> novedades = new ArrayList<>();
    private final Set<LocalDate> nochesBloqueadas = new HashSet<>();

    public Apartamento(String codigo, String nombre, int numeroDormitorios, int capacidadMaxima,
                       String caracteristicas, long tarifaPersonaNocheBase) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del apartamento es obligatorio.");
        }
        if (capacidadMaxima <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor a cero.");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre;
        this.numeroDormitorios = numeroDormitorios;
        this.capacidadMaxima = capacidadMaxima;
        this.caracteristicas = caracteristicas;
        this.estadoOperativo = EstadoApartamento.PREPARADO;
        this.activo = true;

        definirTarifaTemporada(Temporada.BAJA, tarifaPersonaNocheBase);
        definirTarifaTemporada(Temporada.MEDIA, Math.round(tarifaPersonaNocheBase * Temporada.MEDIA.getFactorRecargo()));
        definirTarifaTemporada(Temporada.ALTA, Math.round(tarifaPersonaNocheBase * Temporada.ALTA.getFactorRecargo()));
    }

    public void definirTarifaTemporada(Temporada temporada, long valorPersonaNoche) {
        if (valorPersonaNoche <= 0) {
            throw new IllegalArgumentException("La tarifa debe ser positiva.");
        }
        tarifasTemporada.put(temporada, valorPersonaNoche);
    }

    public long obtenerTarifaPorTemporada(Temporada temporada) {
        return tarifasTemporada.getOrDefault(temporada, tarifasTemporada.get(Temporada.BAJA));
    }

    @Override
    public boolean puedeCambiarA(EstadoApartamento nuevoEstado) {
        return nuevoEstado != null;
    }

    @Override
    public void cambiarEstado(EstadoApartamento nuevoEstado, String motivo) {
        if (!puedeCambiarA(nuevoEstado)) {
            throw new IllegalArgumentException("Estado de apartamento inválido.");
        }
        if (nuevoEstado == EstadoApartamento.FUERA_DE_SERVICIO && (motivo == null || motivo.isBlank())) {
            throw new IllegalArgumentException("Para pasar a FUERA DE SERVICIO es obligatorio registrar el motivo.");
        }
        this.estadoOperativo = nuevoEstado;
        this.motivoFueraServicio = (nuevoEstado == EstadoApartamento.FUERA_DE_SERVICIO) ? motivo.trim() : null;
    }

    public void bloquearRangoFechas(LocalDate inicio, LocalDate fin, String motivo) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new IllegalArgumentException("Rango de bloqueo inválido.");
        }
        for (LocalDate d = inicio; d.isBefore(fin); d = d.plusDays(1)) {
            nochesBloqueadas.add(d);
        }
        novedades.add("BLOQUEO [" + inicio + " a " + fin + "]: " + motivo);
    }

    public boolean estaBloqueadoEnNoche(LocalDate noche) {
        return nochesBloqueadas.contains(noche);
    }

    public void registrarNovedad(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la novedad no puede estar vacía.");
        }
        novedades.add("[" + LocalDate.now() + "] " + descripcion.trim());
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getNumeroDormitorios() { return numeroDormitorios; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public String getCaracteristicas() { return caracteristicas; }
    public EstadoApartamento getEstadoOperativo() { return estadoOperativo; }
    public String getMotivoFueraServicio() { return motivoFueraServicio; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getUltimaSalidaRegistrada() { return ultimaSalidaRegistrada; }
    public void setUltimaSalidaRegistrada(LocalDateTime ultimaSalidaRegistrada) { this.ultimaSalidaRegistrada = ultimaSalidaRegistrada; }
    public List<String> getNovedades() { return Collections.unmodifiableList(novedades); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Apartamento)) return false;
        Apartamento that = (Apartamento) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " | Cap: " + capacidadMaxima +
                " | Base: $" + obtenerTarifaPorTemporada(Temporada.BAJA) +
                " | Estado: " + estadoOperativo +
                (motivoFueraServicio != null ? " (" + motivoFueraServicio + ")" : "");
    }
}