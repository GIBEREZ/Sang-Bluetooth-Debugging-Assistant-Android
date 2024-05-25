from ubluetooth import BLE
import ubluetooth
from machine import Pin
import time

class userBLE():
    def __init__(self):
        self.Name = None
        self.adv_data = None
        self.resp_data = None
        self.ble = None
        self.state = 0
        self.interval = 100
        self.Service_s = ()
        self.ble = BLE()
    
    def setName(self, Name):
        self.Name = Name
        name_bytes = bytes(self.Name, 'utf-8')
        self.adv_data = bytearray()
        self.adv_data.extend(b'\x02\x01\x06')
        self.adv_data.extend(bytes([len(name_bytes) + 1]))
        self.adv_data.extend(b'\x09')
        self.adv_data.extend(name_bytes)

        
    def setRespdata(self, resp_data):
        self.resp_data = resp_data
        
    def addService(self, uuid, Features):
        service = (ubluetooth.UUID(uuid), Features)
        self.ble.gatts_register_services((service,))
    
    def ble_irq(self, event, data):
        if event == 1:
            print("蓝牙已连接")
            self.state = 1
        elif event == 2:
            print("蓝牙断开连接")
            self.state = 2
            
        self.broadcast(self.interval)
        
    def broadcast(self, interval):
        self.interval = interval
        
        if self.resp_data != None:
            self.ble.gap_advertise(interval, adv_data=self.adv_data, resp_data=self.resp_data)
        else:
            print(self.adv_data)
            self.ble.gap_advertise(interval, adv_data=self.adv_data)
            
        self.ble.irq(self.ble_irq)
        
    def open(self):
        self.ble.active(True)
    
    def close(self):
        self.ble.active(False)
        
muserBLE = userBLE();
muserBLE.setName("Sang")
muserBLE.open()
muserBLE.addService(0x9000,
    ((ubluetooth.UUID(0x9001),ubluetooth.FLAG_READ | ubluetooth.FLAG_WRITE,),
     (ubluetooth.UUID(0x9002),ubluetooth.FLAG_READ | ubluetooth.FLAG_NOTIFY,),)
                    ,)
muserBLE.broadcast(100)

while True:
    time.sleep(1)