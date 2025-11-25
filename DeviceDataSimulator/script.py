import pika
import json
import random
from datetime import datetime, date, time, timedelta

RABBITMQ_URL = "amqps://sthzvxcm:ZcFR6h1lAA-Tnm_kRLMhnzMFM2EhYu-e@cow.rmq2.cloudamqp.com/sthzvxcm"
QUEUE_NAME = "device_measurements"   # schimbă aici dacă vrei alt nume de coadă

def generate_consumption_kwh_per_10min(hour: int) -> float:
    """
    Returnează un consum plauzibil pe interval de 10 minute, în funcție de oră.
    Valorile sunt în kWh / 10 minute.
    """

    # Noapte (00:00 - 06:00) – consum foarte mic (standby: frigider, router, etc.)
    if 0 <= hour < 6:
        return random.uniform(0.01, 0.05)   # până la ~0.3 kWh pe oră

    # Dimineață (06:00 - 09:00) – se pornesc aparate (cafea, fierbător, lumină etc.)
    elif 6 <= hour < 9:
        return random.uniform(0.03, 0.12)   # până la ~0.7 kWh pe oră

    # Zi (09:00 - 17:00) – consum moderat
    elif 9 <= hour < 17:
        return random.uniform(0.02, 0.10)   # până la ~0.6 kWh pe oră

    # Seară (17:00 - 23:00) – consum mai mare (gătit, TV, lumină, etc.)
    elif 17 <= hour < 23:
        return random.uniform(0.08, 0.25)   # până la ~1.5 kWh pe oră

    # Târziu (23:00 - 24:00) – scade din nou
    else:
        return random.uniform(0.02, 0.06)


def main():
    # --- 1. Citim input de la utilizator ---
    date_str = input("Introduceți data (format YYYY-MM-DD): ").strip()
    device_id_str = input("Introduceți device_id (număr întreg): ").strip()

    # Parsăm data și device_id
    try:
        base_date: date = datetime.strptime(date_str, "%Y-%m-%d").date()
    except ValueError:
        print(" Data introdusă nu e în format valid YYYY-MM-DD.")
        return

    try:
        device_id = int(device_id_str)
    except ValueError:
        print(" device_id trebuie să fie un număr întreg.")
        return

    # --- 2. Conexiune la RabbitMQ ---
    print("\n Conectare la RabbitMQ...")
    params = pika.URLParameters(RABBITMQ_URL)
    connection = pika.BlockingConnection(params)
    channel = connection.channel()

    # Ne asigurăm că există coada
    channel.queue_declare(queue=QUEUE_NAME)

    # --- 3. Generăm mesaje la fiecare 10 minute pentru toată ziua ---
    current_dt = datetime.combine(base_date, time(0, 0))  # start la 00:00
    end_dt = current_dt + timedelta(days=1)                # până la 24:00 (ziua următoare)

    count = 0

    print(f"\n Generez și trimit mesaje pentru data {base_date} și device_id={device_id}...")
    while current_dt < end_dt:
        hour = current_dt.hour

        value_kwh = generate_consumption_kwh_per_10min(hour)
        payload = {
            "timestamp": current_dt.isoformat(),
            "device": {
                "id":device_id
            },
            "consumption": round(value_kwh, 2)  # rotunjim frumos
        }

        message = json.dumps(payload)

        # Trimitem mesajul în coadă
        channel.basic_publish(
            exchange="",
            routing_key=QUEUE_NAME,
            body=message
        )

        count += 1
        print(f"[{count:3}] Sent: {message}")

        # următorul interval de 10 minute
        current_dt += timedelta(minutes=10)

    
    connection.close()
    print(f"\n Gata! Am trimis {count} mesaje pentru {base_date} în coada '{QUEUE_NAME}'.")


if __name__ == "__main__":
    main()
