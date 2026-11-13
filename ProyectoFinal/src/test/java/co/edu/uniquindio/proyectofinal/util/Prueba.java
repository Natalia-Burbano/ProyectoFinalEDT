package co.edu.uniquindio.proyectofinal.util;

public final class Prueba {
    private static int exitosas = 0;
    private static int fallidas = 0;

    private Prueba() { }

    public static void titulo(String nombre) {
        System.out.println();
        System.out.println("== " + nombre + " ==");
    }

    public static void verificar(boolean condicion, String descripcion) {
        if (condicion) { exitosas++; System.out.println("  OK    " + descripcion); }
        else { fallidas++; System.out.println("  FALLA " + descripcion); }
    }

    public static void esperarExcepcion(Class<? extends Throwable> tipo,
                                        Runnable accion, String descripcion) {
        try {
            accion.run();
            verificar(false, descripcion + " (no lanzo excepcion)");
        } catch (Throwable e) {
            verificar(tipo.isInstance(e), descripcion);
        }
    }

    public static void resumen() {
        System.out.println();
        System.out.println("Resultado: " + exitosas + " OK, " + fallidas + " fallas");
        if (fallidas > 0) System.exit(1);
    }
}
