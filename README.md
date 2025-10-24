# Proyecto Multilenguaje – MicroBit to MQTT
## Introducción
Este repositorio contiene implementaciones en **seis lenguajes de programación diferentes** (C#, Go, Java, JavaScript, Python y Rust) que realizan la misma tarea:  
leer los datos del sensor de una **Micro:bit** desde el **puerto serial**, mostrarlos en consola y enviarlos mediante el protocolo **MQTT** a un tema determinado.
  
El objetivo es comparar cómo se aborda una misma solución en distintos lenguajes, así como comprender los principios comunes detrás de la comunicación entre dispositivos IoT.
## Micro:bit
La **Micro:bit** es una microcomputadora educativa diseñada para enseñar programación y electrónica de forma sencilla.  
Cuenta con diversos **sensores integrados** y un puerto USB que permite enviar y recibir datos seriales desde una computadora.
### Sensores principales de la Micro:bit
- **Sensor de luz:** mide la cantidad de luz ambiental (0–255).
- **Sensor de temperatura:** detecta la temperatura del procesador en °C.
- **Acelerómetro:** mide la aceleración en los tres ejes (x, y, z).
- **Brújula:** detecta la orientación magnética (grados).
- **Botones A y B:** entradas digitales programables.
- **Pantalla LED 5x5:** permite mostrar texto o patrones simples.
### Código en la Micro:bit
El siguiente código en **MicroPython** se encarga de medir la intensidad de luz y enviarla a través del **puerto serial** cada medio segundo:
```py
from microbit import *
import time

while True:
    light = display.read_light_level()  # Valor entre 0 y 255
    print(light)                        # Enviar por puerto serie
    time.sleep(0.5)                     # Cada medio segundo
```
**Explicación paso a paso**  
1. **Importación de módulos:** `from microbit import *` importa las funciones necesarias para interactuar con la placa.
2. **Lectura del sensor de luz:** `display.read_light_level()` devuelve un valor entre 0 y 255 que indica la intensidad de luz detectada por la matriz de LEDs.
3. **Envío por puerto serial:** `print(light)` envía el valor al puerto serial, que podrá ser leído por los programas en los distintos lenguajes.
4. **Pausa:** `time.sleep(0.5)` limita la frecuencia de envío a dos veces por segundo.
## ¿Qué es el Puerto Serial?
El **puerto serial (UART)** es un canal de comunicación punto a punto entre la Micro:bit y el ordenador.  
Permite transmitir datos en texto o bytes a través de un cable USB.  
En este proyecto, todos los programas leen esos datos usando las librerías de comunicación serial propias de cada lenguaje (por ejemplo, `pyserial` en Python o `serialport` en Node.js).
## ¿Qué es MQTT?
**MQTT (Message Queuing Telemetry Transport)** es un protocolo de mensajería liviano diseñado para IoT.  
Permite que dispositivos y aplicaciones se comuniquen mediante un **broker** central, enviando y recibiendo mensajes en **temas (topics)**.
- **Publicador (Publisher):** envía datos a un tema.
- **Suscriptor (Subscriber):** recibe datos del tema.
- **Broker:** intermediario que distribuye los mensajes (por ejemplo, Mosquitto o HiveMQ).
En este proyecto, cada implementación actúa como **publisher**, enviando los valores de luz obtenidos desde la micro:bit hacia un tema MQTT, por ejemplo:
`topic: microbit/light`
## Estructura de Carpetas
La estructura general del repositorio es la siguiente:  
```bash
./
│
├── CSHARP-MQTT/
│   └── MicroBit-to-MQTT/
│       ├── MicroBit-to-MQTT.csproj
│       └── Program.cs
│
├── GO-MQTT/
│   ├── go.mod
│   └── main.go
│
├── Java-MQTT/
│   ├── MicrobitSerialReader.java
│   └── pom.xml
│
├── JS-MQTT/
│   ├── main.js
│   ├── package.json
│
├── Python-MQTT/
│   ├── main.py
│   └── requirements.txt
│
├── Rust-MQTT/
│   ├── src/
│   └── Cargo.toml
│
├── .gitignore
├── LICENSE
└── README.md
```
Cada carpeta contiene una implementación completa en su respectivo lenguaje, junto con sus dependencias y archivos de configuración.
## Conclusiones
El desarrollo de este proyecto permitió comprender cómo un mismo proceso —la lectura de datos desde un dispositivo físico y su publicación mediante MQTT— puede implementarse de manera diferente según el lenguaje de programación utilizado.
  
La **Micro:bit** demostró ser una herramienta ideal para la enseñanza de conceptos IoT, gracias a su facilidad de uso y la variedad de sensores integrados. A través del puerto serial, se logra una comunicación simple pero eficaz con el ordenador, sirviendo como puente entre el hardware y las aplicaciones de software.
  
Asimismo, el protocolo **MQTT** reafirmó su papel como estándar en sistemas distribuidos y de bajo consumo, por su simplicidad, eficiencia y compatibilidad con múltiples entornos de programación.
  
El ejercicio multilenguaje evidenció que, aunque la sintaxis y las librerías cambian, la **lógica de comunicación y procesamiento de datos** es universal. Esto refuerza la importancia de entender los fundamentos de los protocolos y la arquitectura IoT, más allá de las herramientas específicas.
  
En conclusión, este proyecto no solo permitió explorar las particularidades de seis lenguajes distintos, sino también comprender los principios comunes que rigen la interacción entre sensores, software y redes en el ecosistema del Internet de las Cosas.
