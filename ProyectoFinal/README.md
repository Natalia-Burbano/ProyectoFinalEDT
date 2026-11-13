# SGA · Sistema de Gestión de Alojamiento (Aparta-Suites UQ)

Proyecto final de Estructuras de Datos, Universidad del Quindío, 2026-2.

Es una aplicación de escritorio para manejar el alojamiento Aparta-Suites UQ, en Salento. Sirve para manejar reservas por los tres canales (portal, directa y externa), check-in y check-out, el folio de cada reserva, la cola de preparación de apartamentos y el mapa del edificio. Todo funciona sobre estructuras de datos hechas por nosotros, sin usar las colecciones de `java.util`.

## Integrantes

| Integrante | Le toca |
|---|---|
| Juan Manuel | Árbol binario de búsqueda, apartamentos, huéspedes, estancia, reservas y `SistemaAlojamiento` |
| Natalia Burbano | Lista doble, pila, cola y cola de prioridad, temporadas, cotización, folio, preparación, canal externo y vencimientos |
| José Gabriel Paguay | Grafo, conjunto y lista circular, mapa del edificio, grupos grandes, turnos, empleados, carga del escenario y reportes |

El reparto completo, método por método, está en el documento *Proyecto final SGA · Reparto del código* (versión 2).

## Cómo correr el proyecto

Lo que se necesita:
- IntelliJ IDEA.
- Java (JDK) instalado.
- SceneBuilder, solo si se van a editar las pantallas.

No hay que instalar Maven ni JavaFX aparte, porque Maven los descarga solo.

Pasos:
1. Clonar el repositorio y abrir la carpeta `ProyectoFinal` en IntelliJ. Esperar a que cargue Maven.
2. **Aplicación:** clic derecho sobre `app.App` y Run. También se puede correr desde la terminal, dentro de `ProyectoFinal`, con `./mvnw clean javafx:run` (en Windows: `mvnw.cmd clean javafx:run`).
3. **Pruebas:** clic derecho sobre `prueba.TodasLasPruebas` y Run. Al final muestra cuántas pruebas dieron OK y cuántas FALLA.
4. **Datos de prueba:** dentro de la aplicación, el botón *Cargar escenario de prueba* carga apartamentos, pasillos, huéspedes, empleados y reservas desde `src/main/resources/escenario.txt`.

## Cómo está organizado

El código va en `src/main/java/co/edu/uniquindio/sga/`, las pruebas en `src/test/java/co/edu/uniquindio/sga/`, y las pantallas (`.fxml`) y el escenario en `src/main/resources/`.

| Paquete | Qué tiene |
|---|---|
| `util` | `Prueba` (el ayudante de las pruebas) y `Reloj` (la fecha y hora que usa todo el sistema) |
| `excepciones` | Las 5 excepciones propias |
| `estructuras` | Nuestras estructuras genéricas |
| `modelo` | Apartamento, Huesped, Reserva, Folio, Estancia, enumeraciones, etc. |
| `comparadores` | Los órdenes alternativos (`Comparator`) |
| `servicio` | La lógica del sistema y `SistemaAlojamiento`, la clase que une todo |
| `datos` | `CargadorEscenario`, que lee el archivo plano |
| `gui` | Los controladores de las pantallas |
| `app` | `App`, donde arranca la aplicación |
| `prueba` (en test) | `TodasLasPruebas` y una prueba por clase |

## Estructuras propias

Todas son genéricas, implementan `Iterable<T>` con su propio iterador y no conocen ninguna clase del sistema.

| Estructura | Para qué se usa |
|---|---|
| `Grafo` | Mapa del edificio: apartamentos, recepción, escaleras y cuartos de aseo, unidos por pasillos con su distancia. Ruta más corta con Dijkstra; recorridos BFS y DFS |
| `ColaPrioridad` | Cola de preparación (primero los apartamentos con entrada hoy) y reservas pendientes por hora de vencimiento. Por dentro es una lista enlazada ordenada |
| `Cola` | Solicitudes del canal externo, en orden de llegada |
| `Pila` | Deshacer la última operación del folio y el botón Atrás entre pantallas |
| `ListaDoble` | Historial de estancias, ocupantes de una reserva y desglose de noches cobradas |
| `ListaCircular` | Turnos de los recepcionistas |
| `ArbolBinarioBusqueda` | Apartamentos por código, huéspedes por documento, empleados, folios y las reservas de cada apartamento por fecha de entrada |
| `Conjunto` | Apartamentos favoritos de cada huésped, sin repetidos |

## Reglas del equipo

- Nada de colecciones de `java.util` (ArrayList, LinkedList, HashMap, PriorityQueue, Stack, etc.). Sí se puede usar `Iterator`, `Iterable`, `Comparator`, `NoSuchElementException`, `Objects`, las fechas de `java.time`, arreglos normales y JavaFX.
- Sin tildes ni ñ en el código: textos, nombres y comentarios (`tamanio`, `anio`, `Quindio`).
- Cada clase y cada método público lleva un comentario corto, en lenguaje sencillo, que dice qué hace. En los métodos recursivos se marca el CASO BASE y el CASO RECURSIVO.
- La fecha de "hoy" se pide siempre a `Reloj`, nunca a `LocalDate.now()`.
- El dinero va en pesos, con `long`.
- Las pruebas son clases con `main` que usan el ayudante `Prueba` (no usamos JUnit).
- Nadie cambia el nombre, los parámetros o el retorno de un método sin avisar en el grupo.

## Git

- Cada uno trabaja en su rama: `Natalia`, `JuanManuel` y `Gabriel`. Nadie trabaja directo en `main`.
- Antes de empezar algo: `git checkout TuRama` y `git pull origin main`.
- Los commits usan conventional commits, `tipo(alcance): descripcion`. Los tipos son `feat`, `test`, `fix`, `docs`, `refactor` y `chore`. Por ejemplo: `feat(estructuras): pila generica con iterador`.
- Cada parte se sube con un PR hacia `main`, con la salida de las pruebas en la descripción. Los revisores son: Gabriel revisa a Juan Manuel, Juan Manuel a Natalia y Natalia a Gabriel. Se une con *Create a merge commit*, nunca con Squash.
- Cada integrante debe tener mínimo 10 commits.

## Decisiones nuestras (pendientes de confirmar con el profe)

- El titular también es ocupante: ocupa cupo y paga si tiene 6 años o más.
- Una reserva por Portal queda Pendiente hasta que se pague el 30 %. Las reservas Directa y Externa quedan Confirmadas de una vez.
- Devolución al cancelar: con 7 días o más antes de la entrada se devuelve todo lo pagado; de 2 a 6 días, la mitad; con menos de 2 días, nada.
- Cada apartamento tiene tres tarifas por persona por noche (baja, media y alta). Una noche que no cae en ningún periodo definido es temporada Baja.
- La cola de prioridad se hizo con una lista enlazada ordenada en vez de montículo, porque se arma con lo que vimos de listas y colas y es más fácil de explicar y modificar.
