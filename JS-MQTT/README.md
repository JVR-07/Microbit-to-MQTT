# Introducción
Esta implementación en JavaScript (Node.js) permite leer los valores de luz enviados por una Micro:bit a través del puerto serial, mostrarlos en consola y publicarlos en un broker MQTT.
  
Se utilizan las librerías `serialport` para la lectura de datos seriales y `mqtt` para la comunicación con el broker.
# Instalación y Ejecución
1. Instalar Node.js (versión 18 o superior): https://nodejs.org/en/download
2. Clonar el proyecto:
```bash
git clone https://github.com/JVR-07/Microbit-to-MQTT/
cd Microbit-to-MQTT/JS-MQTT
```
3. Instalar dependencias:
```bash
npm install serialport mqtt
```
4. Ejecutar:
```bash
node index.js
```
# Explicación del Código
El código principal se encuentra en `main.js` y sigue la siguiente lógica:
## Bibliotecas
```js
const { SerialPort } = require('serialport');
const { ReadlineParser } = require('@serialport/parser-readline');
const mqtt = require('mqtt');
```
Importa las librerías necesarias para:
- Comunicación serial (`serialport`)
- Lectura de líneas (`@serialport/parser-readline`)
- Conexión y publicación MQTT (`mqtt`)
## Configuración
```js
const SERIAL_PORT = '/dev/ttyUSBX';  // Cambia X según el puerto
const BAUD_RATE = 115200;

const BROKER_IP = 'IP-AWS';
const TOPIC = 'lab/3pm25b/microbit/luz';
```
Define:
- El puerto serial donde está conectada la Micro:bit.
  - Linux: `/dev/ttyUSB0` o `/dev/ttyACM0`
  - Windows: `COM3, COM4, etc`.
- La tasa de baudios (`115200`).
- La IP del broker MQTT y el topic donde se publican los datos.
## Conexión al Broker MQTT
```js
const client = mqtt.connect(`mqtt://${BROKER_IP}:1883`);
await new Promise((resolve, reject) => {
    client.on('connect', resolve);
    client.on('error', reject);
});
```
Crea un cliente MQTT y espera hasta establecer conexión con el broker.  
Una vez conectado, muestra en consola que la conexión ha sido exitosa.
## Lectura del Puerto Serial
```js
const port = new SerialPort({ path: SERIAL_PORT, baudRate: BAUD_RATE });
const parser = port.pipe(new ReadlineParser({ delimiter: '\n' }));
```
- Abre el puerto serial indicado.
- Crea un parser que divide los datos por líneas (`\n`), permitiendo leer los valores enviados por la Micro:bit.
# Ciclo Principal
```js
parser.on('data', (data) => {
    const line = data.trim();
    const level = parseInt(line);
    client.publish(TOPIC, level.toString());
    console.log(`Publicado: Luz=${level}`);
});
```
Cada vez que se recibe un valor:
- Se limpia y convierte a número entero.
- Se publica en el topic MQTT definido.
- Se imprime el valor en consola.
## Manejo de Errores
```js
process.on('SIGINT', async () => {
    console.log('\nFinalizando...');
    client.end();
    port.close();
    process.exit(0);
});
```
El programa maneja distintos posibles errores:
- Errores de conversión: ignorados con `try/catch`.
- Errores del puerto serial: mostrados con mensajes claros.
- Desconexión limpia: al presionar `Ctrl + C`, se cierra el puerto y la conexión MQTT de forma segura.
# Conclusiones
Esta versión en JavaScript ofrece una forma rápida y multiplataforma de conectar una Micro:bit con un broker MQTT, ideal para proyectos IoT y educativos.
  
Gracias a `serialport` y `mqtt`, el código se mantiene simple y eficiente, garantizando una comunicación fluida entre el hardware y el servidor.  
Su estructura asíncrona con async/await y eventos permite manejar múltiples dispositivos o flujos de datos sin bloquear la ejecución.
  
Es un ejemplo práctico de cómo Node.js puede integrarse fácilmente con hardware IoT y redes de mensajería basadas en MQTT.
