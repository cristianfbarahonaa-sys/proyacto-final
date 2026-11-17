public enum Temporada {
    BAJA(1.0),
    MEDIA(1.25),
    ALTA(1.50);

    private final double factorRecargo;

    Temporada(double factorRecargo) {
        this.factorRecargo = factorRecargo;
    }

    public double getFactorRecargo() {
        return factorRecargo;
    }
}