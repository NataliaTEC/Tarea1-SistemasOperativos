package minipc;

/**
 * Representa la memoria principal de la Mini PC.
 *
 * Es una memoria direccionable por palabra: cada posición guarda un entero (0-65535) que representa una palabra de 16 bits (4 bits de
 * opcode + 4 bits de registro + 8 bits de valor), es decir una instruccion completa por posición.
 *
 * La memoria se divide en dos espacios:
 *   - Espacio de KERNEL  : primer 25% de las posiciones.
 *   - Espacio de USUARIO : el 75% restante.
 *
 * El tamaño total es configurable, con un mínimo de 128 posiciones.
 * @author Natalia Granados Rosales
 */
public class Memoria {

    public static final int TAMANO_MINIMO = 128;

    public static final double PROPORCION_KERNEL = 0.25;

    /** Cada posicion de memoria es una palabra de 16 bits (4 opcode + 4 registro + 8 valor). */
    public static final int BITS_POR_PALABRA = 16;
    public static final int VALOR_MAXIMO_PALABRA = (1 << BITS_POR_PALABRA) - 1; 

    private final int tamanoTotal;
    private final int tamanoKernel;
    private final int tamanoUsuario;

    private final int inicioKernel;
    private final int finKernel;
    private final int inicioUsuario;
    private final int finUsuario;

    private final int[] datos;

    /**
     * Crea la memoria con el tamaño indicado, dividiendo automaticamente 25% para Kernel y 75% para Usuario.
     * @param tamanoTotal cantidad total de posiciones (minimo 128).
     * @throws IllegalArgumentException si el tamaño es menor al minimo permitido.
     */
    public Memoria(int tamanoTotal) {
        if (tamanoTotal < TAMANO_MINIMO) {
            throw new IllegalArgumentException("El tamaño de memoria (" + tamanoTotal + ") es menor al mínimo requerido (" + TAMANO_MINIMO + ").");
        }

        this.tamanoTotal = tamanoTotal;
        this.tamanoKernel = (int) Math.round(tamanoTotal * PROPORCION_KERNEL);
        this.tamanoUsuario = tamanoTotal - tamanoKernel;

        this.inicioKernel = 0;
        this.finKernel = tamanoKernel - 1;
        this.inicioUsuario = tamanoKernel;
        this.finUsuario = tamanoTotal - 1;

        this.datos = new int[tamanoTotal];
    }

    // ------------------------------------------------------------------
    // Lectura / escritura generica 
    // ------------------------------------------------------------------

    /** Escribe un valor (0-65535, una palabra de 16 bits) en la posicion indicada. */
    public void escribir(int posicion, int valor) {
        validarPosicion(posicion);
        validarValorDePalabra(valor);
        datos[posicion] = valor;
    }

    /** Lee el valor almacenado en la posicion indicada. */
    public int leer(int posicion) {
        validarPosicion(posicion);
        return datos[posicion];
    }

    // ------------------------------------------------------------------
    // Lectura / escritura restringida por espacio 
    // ------------------------------------------------------------------

    /** Escribe un valor unicamente si la posicion pertenece al espacio de Usuario. */
    public void escribirEnUsuario(int posicion, int valor) {
        if (!esEspacioUsuario(posicion)) {
            throw new SecurityException("La posición " + posicion + " no pertenece al espacio de Usuario (" + inicioUsuario + "-" + finUsuario + ").");
        }
        escribir(posicion, valor);
    }

    /** Escribe un valor unicamente si la posicion pertenece al espacio de Kernel. */
    public void escribirEnKernel(int posicion, int valor) {
        if (!esEspacioKernel(posicion)) {
            throw new SecurityException("La posición " + posicion + " no pertenece al espacio de Kernel (" + inicioKernel + "-" + finKernel + ").");
        }
        escribir(posicion, valor);
    }

    // ------------------------------------------------------------------
    // Conveniencia: escribir una Instruccion ya traducida
    // ------------------------------------------------------------------

    /**
     * Escribe una instruccion ya traducida como una sola palabra de 16 bits (4 bits opcode + 4 bits registro + 8 bits valor) en la posición indicada
     * dentro del espacio de Usuario, y actualiza la posición de memoria guardada en la propia Instruccion.
     * @return la siguiente posición libre (posicion + 1).
     */
    public int escribirInstruccion(int posicion, Instruccion instruccion) {
        if (!instruccion.isValida()) {
            throw new IllegalArgumentException("No se puede cargar en memoria una instrucción inválida: " + instruccion.getMensajeError());
        }

        int codigo = binarioAEnteroGeneral(instruccion.getBinarioCompacto()); // 16 bits -> 0-65535

        escribirEnUsuario(posicion, codigo);

        instruccion.setPosicionMemoria(posicion);
        return posicion + 1;
    }

    // ------------------------------------------------------------------
    // Consultas de espacio
    // ------------------------------------------------------------------

    public boolean esPosicionValida(int posicion) {
        return posicion >= 0 && posicion < tamanoTotal;
    }

    public boolean esEspacioKernel(int posicion) {
        return posicion >= inicioKernel && posicion <= finKernel;
    }

    public boolean esEspacioUsuario(int posicion) {
        return posicion >= inicioUsuario && posicion <= finUsuario;
    }

    /** Vuelve a poner toda la memoria en cero. */
    public void limpiar() {
        java.util.Arrays.fill(datos, 0);
    }

    // ------------------------------------------------------------------
    // Utilidades de conversion (para la representacion visual)
    // ------------------------------------------------------------------

    /** Convierte un entero (0-255) a su representación binaria de 8 bits */
    public static String enteroABinario(int valor) {
        if (valor < 0 || valor > 255) {
            throw new IllegalArgumentException("El valor " + valor + " no cabe en 8 bits (0-255).");
        }
        String binario = Integer.toBinaryString(valor);
        while (binario.length() < 8) {
            binario = "0" + binario;
        }
        return binario;
    }

    /** Convierte una cadena binaria de 8 bits a su valor entero (0-255). */
    public static int binarioAEntero(String binario) {
        if (binario == null || binario.length() != 8) {
            throw new IllegalArgumentException("Se esperaba una cadena binaria de 8 bits, se recibió: " + binario);
        }
        return Integer.parseInt(binario, 2);
    }

    /** Convierte un entero (0-65535) a su representacion binaria de 16 bits (una palabra). */
    public static String palabraABinario(int valor) {
        if (valor < 0 || valor > VALOR_MAXIMO_PALABRA) {
            throw new IllegalArgumentException("El valor " + valor + " no cabe en una palabra de 16 bits (0-65535).");
        }
        String binario = Integer.toBinaryString(valor);
        while (binario.length() < BITS_POR_PALABRA) {
            binario = "0" + binario;
        }
        return binario;
    }

    /** Convierte una cadena binaria de cualquier longitud. */
    public static int binarioAEnteroGeneral(String binario) {
        if (binario == null || binario.isEmpty()) {
            throw new IllegalArgumentException("Cadena binaria vacía o nula.");
        }
        return Integer.parseInt(binario, 2);
    }

    /**
     * Interpreta un byte (0-255) usando el formato de entero con signo (bit 0 = signo, bits 1-7 = magnitud) y devuelve su valor real.
     */
    public static int byteAEnteroConSigno(int valorByte) {
        String binario = enteroABinario(valorByte);
        int signo = binario.charAt(0) == '1' ? -1 : 1;
        int magnitud = Integer.parseInt(binario.substring(1), 2);
        return signo * magnitud;
    }

    /**
     * Convierte un entero con signo (-127 a 127) al formato de byte (bit 0 = signo, bits 1-7 = magnitud), devuelto como entero 0-255.
     */
    public static int enteroConSignoAByte(int valor) {
        if (Math.abs(valor) > 127) {
            throw new IllegalArgumentException("El valor " + valor + " está fuera de rango (-127 a 127).");
        }
        String signo = valor < 0 ? "1" : "0";
        String magnitud = Integer.toBinaryString(Math.abs(valor));
        while (magnitud.length() < 7) {
            magnitud = "0" + magnitud;
        }
        return binarioAEntero(signo + magnitud);
    }

    // ------------------------------------------------------------------
    // Validaciones internas
    // ------------------------------------------------------------------

    private void validarPosicion(int posicion) {
        if (!esPosicionValida(posicion)) {
            throw new IndexOutOfBoundsException("Posición " + posicion + " fuera de rango (0-" + (tamanoTotal - 1) + ").");
        }
    }

    private void validarValorDePalabra(int valor) {
        if (valor < 0 || valor > VALOR_MAXIMO_PALABRA) {
            throw new IllegalArgumentException("El valor " + valor + " no cabe en una palabra de 16 bits (0-65535).");
        }
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public int getTamanoTotal() {
        return tamanoTotal;
    }

    public int getTamanoKernel() {
        return tamanoKernel;
    }

    public int getTamanoUsuario() {
        return tamanoUsuario;
    }

    public int getInicioKernel() {
        return inicioKernel;
    }

    public int getFinKernel() {
        return finKernel;
    }

    public int getInicioUsuario() {
        return inicioUsuario;
    }

    public int getFinUsuario() {
        return finUsuario;
    }
}