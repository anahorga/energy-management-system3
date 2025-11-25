import pika, os,json

def callback(ch, method, properties, body):
  print("[CONSUMER] Am primit mesaj JSON:", str(body))

# Access the CLODUAMQP_URL environment variable and parse it (fallback to localhost)
url = 'amqps://sthzvxcm:ZcFR6h1lAA-Tnm_kRLMhnzMFM2EhYu-e@cow.rmq2.cloudamqp.com/sthzvxcm'
params = pika.URLParameters(url)
connection = pika.BlockingConnection(params)
channel = connection.channel() # start a channel
channel.queue_declare(queue='device_measurements') # Declare a queue


channel.basic_consume('device_measurements',
                      callback,
                      auto_ack=True)

print(' [*] Waiting for messages:')
channel.start_consuming()
connection.close()