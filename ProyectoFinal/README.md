# SGA - Sistema de Gestión de Alojamiento (Aparta-Suites UQ)

Proyecto final de Estructuras de Datos, Universidad del Quindío, 2026-2.

Integrantes: Natalia Burbano, Juan Manuel y José Gabriel Paguay.

## Cómo correr la aplicación

Se necesita IntelliJ IDEA y Java (JDK). Maven descarga JavaFX solo, no hay que instalarlo aparte.

1. Clonar el repositorio y abrir la carpeta ProyectoFinal en IntelliJ. Esperar a que cargue Maven.
2. Correr la clase app/App.java (clic derecho, Run). También se puede desde la terminal, dentro de ProyectoFinal:
    - Windows: mvnw.cmd clean javafx:run
    - Linux o Mac: ./mvnw clean javafx:run
3. En la ventana, darle al botón *Cargar escenario de prueba*. Carga 12 apartamentos, el mapa del edificio, 12 huéspedes, 6 empleados y 10 reservas desde src/main/resources/escenario.txt.
4. Escoger el rol arriba (Huésped, Recepcionista o Administrador). Cada rol ve sus propias pantallas.

Al cargar el escenario, la fecha del sistema queda en el 20 de noviembre de 2026 a las 8:00, que es el día de la demo. La app no guarda nada al cerrarla: cada vez que se abre hay que cargar el escenario otra vez.

## Pruebas automáticas

Correr src/test/java/.../prueba/TodasLasPruebas.java (clic derecho, Run). Corre todas las pruebas del proyecto y al final muestra el resultado:


Resultado: 536 OK, 0 fallas


Cada clase de prueba también tiene su propio main, así que se puede correr sola (por ejemplo PruebaGrafo o PruebaFolio).

## Pruebas manuales

Hacerlas en este orden, porque unas dependen de otras. Primero cargar el escenario.

*Recepcionista*

1. Recepción: darle Pasar turno varias veces. Los recepcionistas van en círculo (Carolina, Mauricio, Natalia y otra vez Carolina).
2. Check-in de R0001. En Inicio, A101 queda ocupado.
3. En el folio de R0001: registrar un cargo, un pago y darle Deshacer última dos veces. Se revierten en orden contrario.
4. Check-out de R0001 y check-in y check-out de R0002. A101 y A202 quedan pendientes de preparación.
5. Preparación: salen los dos en la cola con su ruta sugerida. Darle Iniciar siguiente y luego Terminar. El apartamento queda preparado.
6. Reservas: buscar disponibilidad del 2026-12-01 al 2026-12-03, cotizar y crear una reserva por Portal. Queda pendiente. En la pestaña Reservas, pagar el anticipo (queda confirmada), ver la devolución y cancelarla.
7. Cotizar del 2026-12-12 al 2026-12-17: las noches hasta el 14 salen en temporada baja y desde el 15 en alta. Un niño menor de 6 años no se cobra.
8. Grupos grandes: 20 personas. Sale la combinación con menos apartamentos.
9. Canal externo: simular una solicitud y procesarla. Si choca con otra reserva, aparece en Conflictos de canal.
10. Mapa: calcular una ruta, cerrar un pasillo y volver a calcularla (busca otro camino). Probar BFS, DFS y Agrupar por bloque.

*Huésped*

11. Entrar con el documento 1000000001. Solo se ven las reservas de Valeria Mora. En Historial sale R0001.
12. Agregar y quitar favoritos. Un favorito repetido no se agrega dos veces.

*Administrador*

13. Agregar, buscar y eliminar empleados, apartamentos y huéspedes. Ver los recorridos inorden, preorden y postorden.
14. Poner A102 fuera de servicio: sale bloqueado en Inicio y en Calendario, y no aparece al buscar disponibilidad. Al reactivarlo entra a la cola de preparación.
15. Definir un periodo de temporada y cambiar una tarifa. La cotización usa la tarifa nueva.
16. Estadísticas: escoger un rango de fechas y Actualizar.

*Errores que deben mostrar un mensaje sin cerrar la app*

- Check-in de una reserva pendiente, de otra fecha o con el apartamento sin preparar.
- Reservar más personas de las que caben, en fechas que se cruzan con otra reserva o en una fecha que ya pasó.
- Pagar un anticipo menor al 30 %.
- Deshacer en un folio vacío o procesar el canal externo con la cola vacía.
- Dejar campos vacíos o escribir letras donde van números o fechas.

## Qué hay en cada paquete

El código está en src/main/java/co/edu/uniquindio/proyectofinal/ y las pruebas en src/test/java/co/edu/uniquindio/proyectofinal/.

| Paquete | Qué tiene |
|---|---|
| estructuras | Nuestras estructuras genéricas: ListaDoble, Pila, Cola, ColaPrioridad, ArbolBinarioBusqueda, Grafo, ListaCircular y Conjunto |
| modelo | Las clases del negocio: Apartamento, Huesped, Reserva, Estancia, Ocupante, Folio, Movimiento, Empleado, NodoMapa, Ruta, las temporadas y las enumeraciones de estados, canales y roles |
| servicio | La lógica del sistema. SistemaAlojamiento une todo; los demás se encargan de una parte cada uno: reservas, folios, cotización, preparación, canal externo, vencimientos, mapa del edificio, grupos grandes, turnos y reportes |
| comparadores | Los órdenes con Comparator: apartamentos por precio, capacidad, dormitorios y número de reservas, y la prioridad de la cola de preparación |
| excepciones | Las excepciones propias: reserva solapada, capacidad excedida, fechas inválidas, transición de estado inválida y estructura vacía |
| datos | CargadorEscenario, que lee el archivo escenario.txt |
| gui | Los controladores de las 9 pantallas, la ventana principal y las ayudas para llenar tablas |
| app | App, donde arranca la aplicación |
| util | Reloj, la fecha y hora que usa todo el sistema |
| prueba (test) | Una clase de prueba por tema y TodasLasPruebas, que las corre todas |

Las pantallas (.fxml) y los estilos (.css) están en src/main/resources/co/edu/uniquindio/proyectofinal/gui/.

## Decisiones que tomamos

*Estructuras*

- No usamos ninguna colección de java.util (ArrayList, HashMap, PriorityQueue, Stack, etc.). Todas las estructuras son nuestras, genéricas y con su propio iterador.
- La cola de prioridad es una lista enlazada que se mantiene ordenada. Insertar recorre la lista y sacar el primero es inmediato. Si dos elementos tienen la misma prioridad, sale primero el que llegó antes.
- Usamos el árbol binario de búsqueda para todo lo que se busca por una clave: apartamentos por código, huéspedes y empleados por documento, folios por reserva y las reservas de cada apartamento por fecha de entrada.
- El grafo del edificio no es dirigido y cada pasillo tiene sus metros. Un pasillo se puede cerrar sin borrarlo, y así las rutas buscan otro camino.
- La pila guarda las operaciones del folio para deshacer la última, y también las pantallas visitadas para el botón Atrás.

*Reglas del negocio*

- El titular de la reserva también cuenta como ocupante.
- Los niños menores de 6 años ocupan cupo pero no pagan.
- Cada apartamento tiene tres tarifas por persona por noche: baja, media y alta. Cada noche se cobra con la temporada en la que cae. Una noche que no está en ningún periodo se cobra como temporada baja.
- Las reservas del portal quedan pendientes hasta que se paga el 30 %. Si pasan 24 horas sin pagar, se cancelan con el botón Revisar vencimientos. Las reservas directas y las del canal externo quedan confirmadas de una vez.
- Al cancelar se devuelve todo lo pagado si faltan 7 días o más para la entrada, la mitad si faltan de 2 a 6 días y nada si faltan menos de 2.
- Deshacer no borra el movimiento del folio: registra uno contrario, para que quede el historial completo.
- Solo se puede hacer check-in el día de entrada, con la reserva confirmada y el apartamento preparado.
- Después del check-out el apartamento entra a la cola de preparación. Salen primero los que tienen una entrada ese mismo día, ordenados por la hora de llegada.
- Las solicitudes del canal externo se atienden en orden de llegada. Si chocan con otra reserva no se crean y quedan registradas como conflicto.
- Los grupos grandes se reparten en el menor número de apartamentos posible.
- En el rol Huésped, cada huésped entra con su documento y solo ve y cambia sus propias reservas.

*Código*

- Sin tildes ni ñ en el código (tamanio, anio).
- La fecha de "hoy" siempre se pide a Reloj, para que el escenario de prueba funcione igual cualquier día.
- El dinero va en pesos, con long.
- Las pruebas son clases con main que usan el ayudante Prueba.