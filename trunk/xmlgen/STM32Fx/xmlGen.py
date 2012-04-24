'''
Created on 22.04.2012

@author: johnny
'''

try:
  import ply
except:
  print "You need install ply: \n pip install ply"
  exit(1)


from memoryMap import MemoryMap
from regTypedef import RegTypedefs
from periph import Periphs
from bitfields import BitFields
import re
import types
import xml.dom.minidom as dom

__version__ = 1
__author__ = "Nepomnyashiy Evgeniy, www.topazelectro.ru"

def intToHex(v):
  res = hex(v)
  if res[-1] == 'L':
    res = res[:-1]
  return res

def addChild(doc_or_elem, tag, text='', **attrs):
  doc = doc_or_elem if isinstance(doc_or_elem, dom.Document) else doc_or_elem.ownerDocument
  e = doc.createElement(tag)
  doc_or_elem.appendChild(e)
  for k, v in attrs.items():
    e.setAttribute(k, v)
  if text:
    t = doc.createTextNode(text)
    e.appendChild(t)
  return e


class Group:
  #<group name="ADC" description="">
  def __init__(self, pref, name="", descr=""):
    self.pref = pref if isinstance(pref, (types.ListType, types.TupleType)) else (pref,)
    self.name = name if name else self.pref[0]
    self.descr = descr
    self.reggrps = []

  def toXml(self, parent):
    e = addChild(parent, 'group', name=self.name, description=self.descr)
    for reggrp in self.reggrps:
      reggrp.toXml(e)

  def prepare(self):
    self.reggrps.sort(key=lambda e: e.name)
    for rg in self.reggrps:
      rg.prepare()


class RegisterGroup:
  #<registergroup name="ADC1" description="">
  def __init__(self, gen, periph):
    self.name = periph.name
    self.descr = ""
    self.regs = []

    addr = gen.memoryMap[periph.base]
    typedef = gen.regTypedefs[periph.type]
    for field in typedef:
      if not field.name.startswith('RESERVED'):
        self.regs.append(Register(addr, field))
      addr += field.sizeof()

  def toXml(self, parent):
    e = addChild(parent, 'registergroup', name=self.name, description=self.descr)
    for reg in self.regs:
      reg.toXml(e)

  def prepare(self):
    self.regs.sort(key=lambda e: e.name)


class Register:
  #<register name="ADC_CR1" description="" address="0x40012404" resetvalue="" access="rw">
  def __init__(self, addr, field):
    self.address = addr
    self.name = field.name
    self.descr = field.comment
    self.resetvalue = ""
    if field.type == "__I":
      self.access = "r"
    elif field.type == "__O":
      self.access = "w"
    else:
      self.access = "rw"
    self.fields = [Field(bf) for bf in field.bitfields]

  def toXml(self, parent):
    e = addChild(parent, 'register', name=self.name, description=self.descr, address=intToHex(self.address),
             resetvalue=self.resetvalue, access=self.access)
    for f in self.fields:
      f.toXml(e)

class Field:
  #<field bitoffset="0" bitlength="5" name="AWDCH" description="AWDCH[4:0] bits (Analog watchdog channel select bits)" />
  def __init__(self, bitfield):
    self.descr = bitfield.comment
    self.name = bitfield.bitname
    self.offset = bitfield.offset
    self.length = bitfield.length
    self.interpr = []

  def toXml(self, parent):
    e = addChild(parent, 'field', name=self.name, description=self.descr, bitoffset=str(self.offset),
                 bitlength=str(self.length))
    for i in self.interpr:
      i.toXml(e)

class FieldInterpret:
  def toXml(self, parent):
    pass

class XmlGen:
  def __init__(self):
    self.chipname = ''
    self.description = ''
    self.groups = [] # Group(), Group(), ...     

  def __getGroup(self, text, group):
    start = re.search(r'/\*\*\s*\@addtogroup\s+' + group + '.*?\*/', text, re.M + re.S)
    if not start:
      raise Exception("no doxygroup: " + group)

    end = text.find('@}', start.end())
    if end == -1:
      raise Exception("no doxygroup end: " + group)

    end = text.rfind('/*', start.end(), end)
    if end == -1:
      raise Exception("no doxygroup end: " + group)

    return text[start.end():end]

  def parse(self, filename):
    with open(filename, 'r') as f:
      text = f.read()

    self.memoryMap = MemoryMap(self.__getGroup(text, "Peripheral_memory_map"))
    self.regTypedefs = RegTypedefs(self.__getGroup(text, "Peripheral_registers_structures"))
    self.periphs = Periphs(self.__getGroup(text, "Peripheral_declaration"))
    self.bitfields = BitFields(self.__getGroup(text, "Peripheral_Registers_Bits_Definition"))

    for t in self.regTypedefs.regs.values():
      t.addBitFields(self.bitfields.regbitfields)

  def generate(self):
    self.groups.append(Group('', 'Default'))
    for p in self.periphs.periphs.values():
      registerGroup = RegisterGroup(self, p)
      for g in self.groups:
        if registerGroup.name.startswith(g.pref):
          g.reggrps.append(registerGroup)
          break

    self.groups.sort(key=lambda e: e.name)
    for g in self.groups:
      g.prepare()

  def toXml(self, filename):
    doc = dom.Document()

    dt = dom.DocumentType("model")
    dt.systemId = "embsysregview.dtd"
    doc.appendChild(dt)

    model = addChild(doc, 'model', chipname=self.chipname)

    import datetime
    descr = '''\n--------------\nThis file was generated by regViewGen v%d (%s) %s''' % (__version__,
        __author__, str(datetime.datetime.now()))
    addChild(model, 'chip_description', self.description + descr)

    for group in self.groups:
      group.toXml(model)

    with open(filename, 'w') as f:
      doc.writexml(f, indent='\t', addindent='\t', newl='\n', encoding='UTF-8')

