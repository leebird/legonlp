# -*- coding: utf-8 -*-
import sys
import re

print sys.stdout.isatty()

# ascii string
print '你', ['你'] , len('你')

# unicode string converted by utf-8
print u'你', [u'你'], len(u'你')

# str encoded with utf-8, it's still a bytes string 
# and len() will count its length using ascii
print len(u'你'.encode('utf-8')) 

# split with ascii encoding
print '123你好123'.split('你') 

# split with a byte
print '123你好123'.split('\xa5') 

# split in unicode
print u'123你好123'.split(u'你') 

# split a unicode with a str, UniCodeDecodeError
# with literal u, 你 is beting converted to unicode by ascii,
# the default python 2 encoding, which causes an error,
# because 你 is treated as 3 spearate bytes in ascii
# this is true for member methods of unicode, because
# member methods of unicode will try to convert its
# argument to unicde using the default encoding, ascii
try:
    print u'123你好123'.split('你') 
except UnicodeDecodeError:
    print 'error'

try:
    u'你'.find('你')
except UnicodeDecodeError:
    print 'error'

# split with unicode string
print u'123你好123'.split(u'你') 

# search by the bytes
m = re.search(r'你','你')
print m.group(), len(m.group())

# search by unicode
m = re.search(ur'你',u'你')
print m.group(), len(m.group())

# search in normal mode
m = re.search(r'\w',u'你')
print m

# search in unicode mode
m = re.search(r'\w',u'你',re.UNICODE)
print m.group(),len(m.group())

# search byte in 你
m = re.search('\xa0','你')
print m.group(),m.start(), len(m.group())

# search bytes in unicode, which returns None
m = re.search(r'你',u'你')
print m

# if filename has unicode char, it will be read as bytes string
import os 
for f in os.listdir('./'):
    #print f,type(f),len(f)
    pass

'''
summary
1. All string/bytes are equal in python 2.
2. Using literal u requires the file encoding to be claimed at
   the beginning of the file.
2. When I/O unicode contents, encoding must be specify, 
   or default encoding of the I/O will be used. If there 
   is no default encoding, ascii will be used.
3. When calling member methods of unicode, unicode arguments 
   should be used otherwise it may cause UnicodeDecodeError 
   when decoding the arguments by ascii. 
4. Other methods won't convert its argument automatically. It
   just uses them as is and may potentially cause error.
'''
