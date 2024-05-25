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
        self.Service = []
        self.Service_s = ()
        self.ble = BLE()
    
    def setName(self, Name):
        self.Name = Name
        
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
            
        self.broadcast()
        
    def broadcast(self, interval):
        self.adv_data = bytearray()
        self.adv_data.extend(b'\x02\x01\x06')  # MAC地址广播
        
        if self.Name != None:
            self.adv_data.extend(bytes(len(self.Name) + 1))  # 设备名称长度定义
            self.adv_data.extend(b'\x09')  # 设备名称数据类型
            self.adv_data.extend(bytes(self.Name,'utf-8'))  # 将设备名称转义byte
        
        if self.resp_data != None:
            self.ble.gap_advertise(interval, self.adv_data, resp_data=self.resp_data)
        else:
            self.ble.gap_advertise(interval, self.adv_data)
            
        self.ble.irq(self.ble_irq)
        
    def open(self):
        self.ble.active(True)
    
    def close(self):
        self.ble.active(False)
        
muserBLE = userBLE();
muserBLE.open()
muserBLE.setName("Sang")
muserBLE.addService(0x9000,
    ((ubluetooth.UUID(0x9001),ubluetooth.FLAG_READ | ubluetooth.FLAG_WRITE,),
     (ubluetooth.UUID(0x9002),ubluetooth.FLAG_READ | ubluetooth.FLAG_NOTIFY,),)
                    ,)
muserBLE.broadcast(100)

while True:
    time.sleep(1)