'''
Created on 22.04.2012

@author: johnny
'''

import common
from common import *
import re

class BitField:
  def __init__(self, name, bitname, value, comment):
    self.name = name
    self.bitname = bitname
    self.value = value
    self.comment = comment

    startNonZero = -1
    endNonZero = 0
    for i in xrange(0, 32):
      if ((value >> i) & 1) == 1:
        if startNonZero == -1:
          startNonZero = i
        endNonZero = i
    self.offset = startNonZero
    self.length = endNonZero - startNonZero + 1

reIgnoreName = re.compile('_\d+$')

class BitFields:
  tokens = (
          'DEFINE',
          'NAME',
          'NUMBER',
          'DOC_COMMENT',
          'IGNORED_COMMENT'
          ) + types
  literals = '()'

  t_DEFINE = "\#define"
  def t_NAME(self, t):
    r'[A-Za-z_][A-Za-z0-9_]*'
    if t.value in types:
      t.type = t.value
    return t

  t_NUMBER = common.l_number
  def t_DOC_COMMENT(self, t):
    r'/\*!<(.|\n)*?\*/'
    t.value = t.value[4:-2].strip()
    return t

  def t_IGNORED_COMMENT(self, t):
    r'/\*(.|\n)*?\*/'
    return None

  t_ignore = " \t"
  t_newline = common.l_newline
  t_error = common.l_error

  def p_all(self, p):
    '''all : define all
           | '''

  def p_define(self, p):
    'define : DEFINE NAME expr comment'
    name = p[2]

    if reIgnoreName.search(name):
      print 'ignore ' + name
      return

    pos = name.find('_')
    if pos == -1:
      print 'ignore ' + name
      return
    pos = name.find('_', pos + 1)
    if pos == -1:
      print 'ignore ' + name
      return

    bitname = name[pos + 1:]
    bf = BitField(name, bitname, p[3], p[4])
    self.bitfields[name] = bf

    regn = name[:pos]
    if regn in self.regbitfields:
      lst = self.regbitfields[regn]
    else:
      lst = []
      self.regbitfields[regn] = lst
    lst.append(bf)

  def p_define_ignore(self, p):
    'define : DEFINE NAME NAME'


  def p_expression(self, p):
    '''expr : '(' '(' type ')' NUMBER ')' '''
    p[0] = p[5]

  @TOKEN('type : ' + '\n| '.join(types))
  def p_type(self, p):
    pass

  def p_comment(self, p):
    ''' comment : DOC_COMMENT
                | '''
    if len(p) > 1:
      p[0] = p[1]
    else:
      p[0] = ''

  def __init__(self, text):
    self.bitfields = {}
    self.regbitfields = {}

    lexer = lex.lex(object=self)
    parser = yacc.yacc(module=self)
    parser.parse(text, lexer=lexer)

  def __str__(self):
    return str(self.bitfields)

  def __getitem__(self, ind):
    return self.bitfields[ind]
