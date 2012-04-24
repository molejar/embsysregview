'''
Created on 22.04.2012

@author: johnny
'''

from xmlGen import *

g = XmlGen()
g.parse("stm32f2xx.h")

g.chipname = "stm32f2xx"
g.groups = [
            Group('TIM', 'Timer'),
            Group('SPI'),
            Group('I2C'),
            Group(('USART', 'UART')),
            Group('CAN'),
            Group('ADC'),
            Group('DMA1'),
            Group('DMA2'),
            Group('FSMC'),
            Group('GPIO'),
           ]
g.generate()


g.toXml('stm32f2xx.xml')

