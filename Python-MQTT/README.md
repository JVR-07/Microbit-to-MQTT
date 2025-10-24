# Introducción
Esta implementación en Python (3.10 o superior) permite leer los valores de luz enviados por una Micro:bit a través del puerto serial, mostrarlos en consola y publicarlos en un broker MQTT.
  
Se utilizan las librerías:
- `pyserial` para la comunicación con el puerto serial.
- `paho-mqtt` para la conexión y publicación de datos en el broker.
# Instalación y Ejecución
1. Instalar Python 3.10+: https://www.python.org/downloads/
2. Clonar el proyecto:
```bash
git clone https://github.com/JVR-07/Microbit-to-MQTT/
cd Microbit-to-MQTT/PYTHON-MQTT
```
3. Instalar dependencias:
```bash
pip install requirements.txt
```
4. Ejecutar:
```bash
python3 microbit_mqtt.py
```
# Explicación del Código
El código principal se encuentra en main.py y sigue la siguiente estructura:
## Bibliotecas
```python
import serial
import time
import paho.mqtt.client as mqtt
```
Importa las librerías necesarias para manejar:
- El puerto serial (`serial`)
- Tiempos de espera (`time`)
- Comunicación MQTT (`paho.mqtt.client`)
## Configuración
```python
serial_port = "/dev/ttyUSBX"  # Cambia X según el puerto
baud = 115200

broker_ip = "IP-AWS"
topic = "lab/3pm25b/microbit/luz"
```
Define los parámetros de conexión:
- Puerto serial:
  - Linux: `/dev/ttyUSB0`, `/dev/ttyACM0`
  - Windows: `COM3, COM4, etc`.
- Baudios: `115200`
- Broker MQTT: dirección IP o dominio del servidor MQTT
` Topic: ruta donde se publican los datos de luz
## Conexión al Broker MQTT
```python
client = mqtt.Client()
client.connect(broker_ip, 1883, 60)
```
Inicializa un cliente MQTT y establece la conexión con el broker en el puerto 1883.  
El valor 60 indica el tiempo máximo de espera antes de reconectar.  
## Lectura del Puerto Serial
```python
with serial.Serial(serial_port, baud, timeout=1) as ser:
    time.sleep(2)
    while True:
        _line = ser.readline().decode().strip()
```
- Abre el puerto serial con un timeout de 1 segundo.  
- Espera 2 segundos para estabilizar la conexión.
- Lee continuamente las líneas enviadas por la Micro:bit.
## Ciclo Principal y Publicación
```python
if _line:
    _level = int(_line)
    client.publish(topic, _level)
    print(f"Publicado: Luz={_level}")
````
Cada vez que se recibe un valor:
- Se decodifica y elimina el salto de línea.
- Se convierte a número entero.
- Se publica en el topic MQTT correspondiente.
- Se muestra el valor en consola.
## Manejo de Errores
```python
except ValueError:
    pass
except KeyboardInterrupt:
    print("\nFinalizado por el usuario.")
    break
```
- `ValueError`: ignora líneas no numéricas o datos inválidos.
- `KeyboardInterrupt`: permite finalizar el programa de forma segura con `Ctrl + C`.
# Conclusiones
Esta implementación en Python ofrece una forma sencilla, legible y efectiva de conectar una Micro:bit a un sistema IoT mediante MQTT.
  
El uso de `pyserial` y `paho-mqtt` facilita la integración entre hardware y servicios en la nube, manteniendo el código limpio y estable.  
Además, el manejo estructurado de errores y la lectura continua permiten una comunicación confiable y sin bloqueos.
  
Es un ejemplo práctico de cómo Python puede servir como puente entre dispositivos físicos e infraestructuras IoT basadas en MQTT.
