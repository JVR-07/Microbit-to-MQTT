import serial
import time
import paho.mqtt.client as mqtt

# === CONFIGURACIÓN ===
serial_port = "/dev/ttyUSBX" # Cambia X según el puerto
baud = 115200

broker_ip = "IP-AWS" 
topic = "lab/3pm25b/microbit/luz"

# === INICIO MQTT ===
client = mqtt.Client()
client.connect(broker_ip, 1883, 60)

print(f"Enviando datos de luz a MQTT ({broker_ip})...\n")

# === LECTURA SERIAL + PUBLICACIÓN ===
with serial.Serial(serial_port, baud, timeout=1) as ser:
    time.sleep(2)
    while True:
        try:
            _line = ser.readline().decode().strip()
            if _line:
                _level = int(_line)
                client.publish(topic, _level)
                print(f"Publicado: Luz={_level}")
        except ValueError:
            pass
        except KeyboardInterrupt:
            print("\nFinalizado por el usuario.")
            break