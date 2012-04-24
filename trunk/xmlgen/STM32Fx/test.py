'''
Created on 22.04.2012

@author: johnny
'''
import unittest
from memoryMap import MemoryMap
from regTypedef import RegTypedefs
from periph import Periphs
from xmlGen import XmlGen
from bitfields import BitFields


class TestMemoryMap(unittest.TestCase):

  def test_1(self):
    m = MemoryMap('''
        #define FLASH_BASE            ((uint32_t)0x08000000)
        #define SRAM_BASE             ((uint32_t)0x20000000) /*!< SRAM base address in the alias region */
        #define PERIPH_BASE           ((uint32_t)0x40000000) /*!< Peripheral base address in the alias region */
        
        #define SRAM_BB_BASE          ((uint32_t)0x22000000) /*!< SRAM base address in the bit-band region */
        #define PERIPH_BB_BASE        ((uint32_t)0x42000000) /*!< Peripheral base address in the bit-band region */
        
        #define FSMC_R_BASE           ((uint32_t)0xA0000000) /*!< FSMC registers base address */
        
        /*!< Peripheral memory map */
        #define APB1PERIPH_BASE       PERIPH_BASE
        #define APB2PERIPH_BASE       (PERIPH_BASE + 0x00010000)
        #define AHB1PERIPH_BASE       (PERIPH_BASE + 0x00020000)
        #define AHB2PERIPH_BASE       (PERIPH_BASE + 0x10000000)
        
        /*!< APB1 peripherals */
        #define TIM2_BASE             (APB1PERIPH_BASE + 0x0000)
        #define TIM3_BASE             (APB1PERIPH_BASE + 0x0400)
    ''')

    self.assertEqual(m['TIM2_BASE'], 0x40000000)
    self.assertEqual(m['TIM3_BASE'], 0x40000000 + 0x0400)


class TestRegTypedef(unittest.TestCase):
  def test_1(self):
    ts = RegTypedefs('''
        typedef struct
        {
          __IO uint16_t CR1[5];         /*!< TIM control register 1,              Address offset: 0x00 */  
        } TIM_TypeDef2;  
    ''')

    r1 = ts.regs['TIM_TypeDef2']
    self.assertEquals(r1.name, 'TIM_TypeDef2')
    self.assertEquals(len(r1), 1)
    self.assertEquals(r1[0].io, '__IO')
    self.assertEquals(r1[0].type, 'uint16_t')
    self.assertEquals(r1[0].name, 'CR1')
    self.assertEquals(r1[0].count, 5)

  def test_2(self):
    ts = RegTypedefs('''
typedef struct
{
  __IO uint32_t TIR;  /*!< CAN TX mailbox identifier register */
  __IO uint32_t TDTR; /*!< CAN mailbox data length control and time stamp register */
  __IO uint32_t TDLR; /*!< CAN mailbox data low register */
  __IO uint32_t TDHR; /*!< CAN mailbox data high register */
} CAN_TxMailBox_TypeDef;

/** 
  * @brief Controller Area Network FIFOMailBox 
  */
  
typedef struct
{
  __IO uint32_t RIR;  /*!< CAN receive FIFO mailbox identifier register */
  __IO uint32_t RDTR; /*!< CAN receive FIFO mailbox data length control and time stamp register */
  __IO uint32_t RDLR; /*!< CAN receive FIFO mailbox data low register */
  __IO uint32_t RDHR; /*!< CAN receive FIFO mailbox data high register */
} CAN_FIFOMailBox_TypeDef;

/** 
  * @brief Controller Area Network FilterRegister 
  */
  
typedef struct
{
  __IO uint32_t FR1; /*!< CAN Filter bank register 1 */
  __IO uint32_t FR2; /*!< CAN Filter bank register 1 */
} CAN_FilterRegister_TypeDef;

typedef struct
{
  __IO uint32_t              MCR;                 /*!< CAN master control register,         Address offset: 0x00          */
  __IO uint32_t              MSR;                 /*!< CAN master status register,          Address offset: 0x04          */
  __IO uint32_t              TSR;                 /*!< CAN transmit status register,        Address offset: 0x08          */
  __IO uint32_t              RF0R;                /*!< CAN receive FIFO 0 register,         Address offset: 0x0C          */
  __IO uint32_t              RF1R;                /*!< CAN receive FIFO 1 register,         Address offset: 0x10          */
  __IO uint32_t              IER;                 /*!< CAN interrupt enable register,       Address offset: 0x14          */
  __IO uint32_t              ESR;                 /*!< CAN error status register,           Address offset: 0x18          */
  __IO uint32_t              BTR;                 /*!< CAN bit timing register,             Address offset: 0x1C          */
  uint32_t                   RESERVED0[88];       /*!< Reserved, 0x020 - 0x17F                                            */
  CAN_TxMailBox_TypeDef      sTxMailBox[3];       /*!< CAN Tx MailBox,                      Address offset: 0x180 - 0x1AC */
  CAN_FIFOMailBox_TypeDef    sFIFOMailBox[2];     /*!< CAN FIFO MailBox,                    Address offset: 0x1B0 - 0x1CC */
  uint32_t                   RESERVED1[12];       /*!< Reserved, 0x1D0 - 0x1FF                                            */
  __IO uint32_t              FMR;                 /*!< CAN filter master register,          Address offset: 0x200         */
  __IO uint32_t              FM1R;                /*!< CAN filter mode register,            Address offset: 0x204         */
  uint32_t                   RESERVED2;           /*!< Reserved, 0x208                                                    */
  __IO uint32_t              FS1R;                /*!< CAN filter scale register,           Address offset: 0x20C         */
  uint32_t                   RESERVED3;           /*!< Reserved, 0x210                                                    */
  __IO uint32_t              FFA1R;               /*!< CAN filter FIFO assignment register, Address offset: 0x214         */
  uint32_t                   RESERVED4;           /*!< Reserved, 0x218                                                    */
  __IO uint32_t              FA1R;                /*!< CAN filter activation register,      Address offset: 0x21C         */
  uint32_t                   RESERVED5[8];        /*!< Reserved, 0x220-0x23F                                              */ 
  CAN_FilterRegister_TypeDef sFilterRegister[28]; /*!< CAN Filter Register,                 Address offset: 0x240-0x31C   */
} CAN_TypeDef;
    ''')
    r1 = ts.regs['CAN_TypeDef']


class TestPeriph(unittest.TestCase):
  def test_1(self):
    p = Periphs('''
      #define TIM2                ((TIM_TypeDef *) TIM2_BASE)
      #define TIM3                ((TIM_TypeDef *) TIM3_BASE)
    ''')
    self.assertEqual(len(p), 2)
    r = p['TIM3']
    self.assertEqual(r.name, 'TIM3')
    self.assertEqual(r.type, 'TIM_TypeDef')
    self.assertEqual(r.base, 'TIM3_BASE')

class TestXmlGen(unittest.TestCase):
  def test_gen(self):
    g = XmlGen()
    g.parse('../../CMSIS/Device/ST/STM32F2xx/Include/stm32f2xx.h')

    self.assertEqual(g.memoryMap['TIM3_BASE'], 0x40000000 + 0x0400)
    self.assertTrue('TIM_TypeDef' in g.regTypedefs.regs)
    self.assertEqual(g.periphs['TIM3'].type, 'TIM_TypeDef')

    t = g.regTypedefs['TIM_TypeDef']
    field = t[0]
    self.assertEqual(field.bitfields[0].name, 'TIM_CR1_CEN')

class TestBitfields(unittest.TestCase):
  def test_1(self):
    bf = BitFields('''
    /******************************************************************************/
/*                                                                            */
/*                        Analog to Digital Converter                         */
/*                                                                            */
/******************************************************************************/
/********************  Bit definition for ADC_SR register  ********************/
#define  ADC_SR_AWD                          ((uint8_t)0x01)               /*!<Analog watchdog flag */
#define  ADC_SR_EOC                          ((uint8_t)0x02)               /*!<End of conversion */
#define RTC_TSDR_WDU                         ((uint32_t)0x0000E000)
    ''')
    self.assertEqual(len(bf.bitfields), 3)
    f1 = bf['ADC_SR_EOC']
    self.assertEqual(f1.name, 'ADC_SR_EOC')
    self.assertEqual(f1.value, 0x02)
    self.assertEqual(f1.comment, 'End of conversion')

    f2 = bf.regbitfields['ADC_SR'][1]
    self.assertEqual(f1, f2)
    self.assertEqual(f2.offset, 1)
    self.assertEqual(f2.length, 1)

    f3 = bf['RTC_TSDR_WDU']
    self.assertEqual(f3.offset, 13)
    self.assertEqual(f3.length, 3)



if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
