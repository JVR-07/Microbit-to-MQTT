# Introducción
Esta implementación en Java (JDK 17+) permite leer los valores de luz enviados por una Micro:bit a través del puerto serial, mostrarlos en consola y publicarlos en un broker MQTT.
  
Se utilizan las librerías:
- `jSerialComm` para la comunicación con el puerto serial.
- `Eclipse Paho MQTT Client` para la conexión y publicación en el broker MQTT.
# Instalación y Ejecución
1. Instalar Java Development Kit (JDK 17 o superior): https://www.oracle.com/java/technologies/downloads/
2. Clonar el proyecto:
```bash
git clone https://github.com/JVR-07/Microbit-to-MQTT/
cd Microbit-to-MQTT/JAVA-MQTT
```
3. Compilar y ejecutar:
```bash
javac -cp "lib/*" MicrobitSerialReader.java
java -cp ".:lib/*" MicrobitSerialReader
```
# Explicación del Código
El código principal se encuentra en `MicrobitSerialReader.java` y sigue la siguiente estructura:
## Bibliotecas
```java
import com.fazecast.jSerialComm.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
```
Importa las librerías necesarias para el manejo del puerto serial y la comunicación MQTT.
## Configuración
```java
private static final String SERIAL_PORT = "COM3"; // Cambia según el puerto
private static final int BAUD_RATE = 115200;

private static final String BROKER_IP = "IP-AWS";
private static final String TOPIC = "lab/3pm25b/microbit/luz";
```
Define:
- El puerto serial donde está conectada la Micro:bit.
  - Windows: `COM3, COM4, etc`.
  - Linux/macOS: `/dev/ttyUSB0` o `/dev/ttyACM0`.
- La tasa de baudios de `115200`.
- La IP del broker MQTT y el topic de publicación.
## Conexión al Broker MQTT
```java
String brokerUrl = "tcp://" + BROKER_IP + ":1883";
mqttClient = new MqttClient(brokerUrl, MqttClient.generateClientId(), new MemoryPersistence());
mqttClient.connect(connOpts);
```
Establece la conexión con el broker MQTT utilizando el cliente de `Eclipse Paho`.  
Configura opciones como:
- `CleanSession` para iniciar sin mensajes retenidos.
- `KeepAliveInterval` para mantener la conexión estable.
## Lectura del Puerto Serial
```java
SerialPort serialPort = SerialPort.getCommPort(SERIAL_PORT);
serialPort.setBaudRate(BAUD_RATE);
serialPort.openPort();
```
Abre el puerto serial y lo prepara para recibir datos de la Micro:bit.  
Se configura con:
- 8 bits de datos
- 1 bit de parada
- Sin paridad
# Ciclo Principal
```java
while (running) {
    if (serialPort.bytesAvailable() > 0) {
        byte[] buffer = new byte[1];
        int numRead = serialPort.readBytes(buffer, 1);
        // Procesamiento línea por línea
    }
}
```
- Lee los bytes disponibles del puerto.
- Acumula los caracteres hasta formar una línea completa.
- Llama a `processLine()` para convertir y publicar el valor.
## Publicación MQTT
```java
int level = Integer.parseInt(line);
MqttMessage message = new MqttMessage(String.valueOf(level).getBytes());
mqttClient.publish(TOPIC, message);
System.out.printf("Publicado: Luz=%d%n", level);
```
Publica el valor de luz en el topic MQTT especificado y lo muestra en consola.
## Manejo de Errores y Cierre
```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    running = false;
    if (serialPort.isOpen()) serialPort.closePort();
    if (mqttClient.isConnected()) mqttClient.disconnect();
}));
```
El programa incluye:
- Control de excepciones en lectura y publicación.
- `ShutdownHook` para cerrar automáticamente el puerto serial y la conexión MQTT al terminar.
- Reintentos y pausas breves para mantener estabilidad en la lectura.
# Conclusiones
Esta versión en Java demuestra cómo integrar una Micro:bit con un sistema IoT basado en MQTT, manteniendo portabilidad y robustez.
  
Gracias a las librerías `jSerialComm` y `Eclipse Paho`, se logra una comunicación eficiente y confiable tanto con el hardware como con el broker MQTT.
El manejo estructurado de eventos y la limpieza de recursos garantizan una ejecución estable incluso en entornos de producción.
  
Es una muestra práctica de cómo Java puede emplearse para soluciones IoT multiplataforma, combinando rendimiento, escalabilidad y compatibilidad industrial.
