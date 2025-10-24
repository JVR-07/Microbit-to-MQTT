# Introducción
Esta implementación en **C# (.NET 9.0)** permite leer los valores de luz enviados por una Micro:bit a través del puerto serial, mostrarlos en consola y publicarlos en un broker MQTT.
  
Se utiliza la librería `MQTTnet` (instalada mediante `NuGet`) para la comunicación MQTT y la clase `System.IO.Ports.SerialPort` para la lectura de datos seriales.
# Instalación y Ejecución
1. Instalar .NET 9.0 SDK:  https://dotnet.microsoft.com/en-us/download
2. Clonar el proyecto: 
```bash
git clone https://github.com/JVR-07/Microbit-to-MQTT/
cd Microbit-to-MQTT/CSHARP-MQTT/MicroBit-to-MQTT
```
3. Instalar paquete MQTTnet:
```bash
dotnet add package MQTTnet
```
4. Ejecutar:
```bash
dotnet run
```
# Explicación del Código
El código principal se encuentra en `Program.cs` y sigue la siguiente lógica:
## Bibliotecas
```csharp
using System;
using System.IO.Ports;
using System.Threading;
using MQTTnet;
using MQTTnet.Client;
```
Importa las librerías necesarias para el manejo del puerto serial y MQTT.
## Configuración
```csharp
string serialPort = "COMX";   // Cambia X según el puerto
int baud = 115200;

string brokerIp = "IP-AWS";
string topic = "lab/3pm25b/microbit/luz";
```
Define el puerto serial y la IP del broker MQTT.
- **Windows:** suele ser **COM3, COM4, etc**.
- **Linux:** suele ser **/dev/ttyACM0** o **/dev/ttyUSB0**.
## Conexión al Broker MQTT
```csharp
var factory = new MqttFactory();
mqttClient = factory.CreateMqttClient();

var options = new MqttClientOptionsBuilder()
    .WithTcpServer(brokerIp, 1883)
    .Build();

await mqttClient.ConnectAsync(options);
```
Aquí se crea y configura el cliente MQTT, conectándose al broker indicado (puede ser Mosquitto en AWS, local o en la nube).
## Lectura del Puerto Serial
```csharp
using (SerialPort ser = new SerialPort(serialPort, baud))
{
    ser.ReadTimeout = 1000;
    ser.Open();
    Thread.Sleep(2000);
```
Se abre el puerto serial y se espera brevemente para estabilizar la comunicación.
## Ciclo Principal
```csharp
while (true)
{
    string line = ser.ReadLine().Trim();
    int level = int.Parse(line);

    var message = new MqttApplicationMessageBuilder()
        .WithTopic(topic)
        .WithPayload(level.ToString())
        .Build();

    await mqttClient.PublishAsync(message);
    Console.WriteLine($"Publicado: Luz={level}");
}
```
- Lee la línea enviada por la Micro:bit.
- Convierte el valor de texto a número entero.
- Lo publica en el tema MQTT definido.
- Muestra el valor en consola.
### Manejo de Errores
Los `try/catch` permiten ignorar líneas inválidas o vacías, evitando que el programa se detenga por errores comunes del flujo serial.
# Conclusiones
Esta implementación demuestra la potencia de **C#** y **.NET** en el desarrollo de aplicaciones IoT multiplataforma.
Gracias al uso de **`MQTTnet`**, se logra una comunicación eficiente con el broker MQTT, y mediante **`SerialPort`** se mantiene un enlace directo con la Micro:bit.
  
El enfoque asíncrono del código permite una ejecución fluida y estable, mientras que la estructura clara facilita su adaptación a otros sensores o protocolos.  
Es un ejemplo práctico de cómo **.NET puede integrarse sin problemas con hardware educativo y redes IoT modernas**.
