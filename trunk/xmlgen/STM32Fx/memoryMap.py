'''
Created on 22.04.2012

@author: johnny
'''

import common
from common import *

class MemoryMap:
  tokens = (
          'DEFINE',
          'NAME',
          'NUMBER',
          'COMMENT'
          ) + types
  literals = '+()'

  t_DEFINE = "\#define"
  def t_NAME(self, t):
    r'[A-Za-z_][A-Za-z0-9_]*'
    if t.value in types:
      t.type = t.value
    return t

  t_NUMBER = common.l_number
  def t_COMMENT(self, t):
    r'/\*.*?\*/'
    t.value = t.value[2:-2]
    return None

  t_ignore = " \t"
  t_newline = common.l_newline
  t_error = common.l_error

  precedence = (
    ('left', '+'),
    )

  def p_map(self, p):
    '''map : define map
           | '''

  def p_define(self, p):
    'define : DEFINE NAME expr'
    self.macros[p[2]] = p[3]

  def p_expression_parent(self, p):
    '''expr : '(' expr ')' '''
    p[0] = p[2]
  def p_expression_cast(self, p):
    '''expr : '(' type ')' expr '''
    p[0] = p[4]

  def p_expression_num(self, p):
    '''expr : NUMBER '''
    p[0] = p[1]

  def p_expression_oper(self, p):
    '''expr : expr '+' expr '''
    p[0] = p[1] + p[3]

  def p_expr_name(self, p):
    '''expr : NAME'''
    p[0] = self.macros[p[1]]

  @TOKEN('type : ' + '\n| '.join(types))
  def p_type(self, p):
    pass


  def __init__(self, text):
    self.macros = {}

    lexer = lex.lex(object=self)
    parser = yacc.yacc(module=self)
    parser.parse(text, lexer=lexer)

  def __str__(self):
    return str(self.macros)

  def __getitem__(self, ind):
    return self.macros[ind]
