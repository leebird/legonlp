symbols = {
    '(': 'left parenthesis',
    ')': 'right parenthesis',
    '=': 'node label',
    '<<': 'dominates',
    '>>': 'is dominated by',
    '<': 'immediately dominates',
    '>': 'is immediately dominated by',
    '$': 'is a sister of  (and not equal to)',
    '..': 'precedes',
    '.': 'immediately precedes',
    ',,': 'follows',
    ',': 'immediately follows',
    '<<,': 'is a leftmost descendant of',
    '<<-': 'is a rightmost descendant of',
    '>>,': 'is a leftmost descendant of',
    '>>-': 'is a rightmost descendant of',
    '<,': 'is the first child of',
    '>,': 'is the first child of',
    '<-': 'is the last child of',
    '>-': 'is the last child of',
    '<`': 'is the last child of',
    '>`': 'is the last child of',
    '<i': 'is the ith child of  (i > 0)',
    '>i': 'is the ith child of  (i > 0)',
    '<-i': 'is the ith-to-last child of  (i > 0)',
    '>-i': 'is the ith-to-last child of  (i > 0)',
    '<:': 'is the only child of',
    '>:': 'is the only child of',
    '<<:': 'dominates  via an unbroken chain (length > 0) of unary local trees.',
    '>>:': 'is dominated by  via an unbroken chain (length > 0) of unary local trees.',
    '$++': 'is a left sister of  (same as $.. for context-free trees)',
    '$--': 'is a right sister of  (same as $,, for context-free trees)',
    '$+': 'is the immediate left sister of  (same as $. for context-free trees)',
    '$-': 'is the immediate right sister of  (same as $, for context-free trees)',
    '$..': 'is a sister of  and precedes',
    '$,,': 'is a sister of  and follows',
    '$.': 'is a sister of  and immediately precedes',
    '$,': 'is a sister of  and immediately follows',
    '<<#': 'is a head of phrase',
    '>>#': 'is a head of phrase',
    '<#': 'is the immediate head of phrase',
    '>#': 'is the immediate head of phrase',
    '==': 'and  are the same node',
    '<=': 'and  are the same node or  is the parent of',
    ':': '[this is a pattern-segmenting operator that places no constraints on the relationship between  and ]',
}

parents = ['>>', '>', ',,', ',', '>,', '>>,', '>>-', '>-', '>`', '>i', '>-i', '>:', '>>:', '>>#', '>#']

siblings = ['$', '$++', '$--', '$+', '$-', '$..', '$,,', '$.', '$,']

children = ['<<', '<', '..', '.', '<<,', '<<-', '<,', '<-', '<`', '<i', '<-i', '<:', '<<:', '<<#', '<#']
# TODO: '==', '<=',