'''
Created on 22.04.2012

@author: johnny
'''

from ply import lex, yacc
from ply.lex import TOKEN

types = ('uint32_t', 'uint16_t', 'uint8_t')
keywords = ('typedef', 'struct')
io = ('__IO', '__I', '__O')

def l_number(self, t):
    r'([1-9][0-9]*)|(0x[0-9A-Fa-f]+)'
    if len(t.value) > 2 and t.value[1] == "x":
      t.value = int(t.value, 16)
    else:
      t.value = int(t.value)
    return t

def l_newline(self, t):
    r'\n+'
    t.lexer.lineno += t.value.count("\n")

def l_error(self, t):
    print("Illegal character '%s'" % t.value[0])
    t.lexer.skip(1)
