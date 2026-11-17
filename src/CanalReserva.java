public enum CanalReserva {
    PORTAL(0.30, 0.0),   // Exige 30% de anticipo para confirmar
    DIRECTA(0.0, 0.0),   // Recepción: teléfono, mensajería o presencial
    EXTERNA(0.0, 0.15);  // Plataforma externa: cobra 15% de comisión al alojamiento

    private final double porcentajeAnticipoExigido;
    private final double porcentajeComision;

    CanalReserva(double porcentajeAnticipoExigido, double porcentajeComision) {
        this.porcentajeAnticipoExigido = porcentajeAnticipoExigido;
        this.porcentajeComision = porcentajeComision;
    }

    public double getPorcentajeAnticipoExigido() {
        return porcentajeAnticipoExigido;
    }

    public double getPorcentajeComision() {
        return porcentajeComision;
    }
}