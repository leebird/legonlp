import symbols
import re


class Node(object):
    def __init__(self, node_id=None):
        self.id = node_id
        self.label = ''
        self.parents = []
        self.siblings = []
        self.children = []

    def __str__(self):
        return str((self.label, self.parents, self.siblings, self.children))

    def __repr__(self):
        return self.__str__()


class TregexReader(object):
    def __init__(self):
        pass

    @staticmethod
    def symbol_regex(symbols_list):
        # create a regular expression for all the operators, longest is matched first
        symbols_list = sorted(symbols_list, key=lambda a: len(a), reverse=True)
        symbols_list = [re.escape(symbol) for symbol in symbols_list]
        # match regular expression for node label
        symbols_list.insert(0, r'\/.+?\/')
        # match arbitary text
        symbols_list.append('.')
        return re.compile('|'.join(symbols_list))

    @staticmethod
    def read(tregex):
        symbols_regex = TregexReader.symbol_regex(symbols.symbols)
        matches = symbols_regex.finditer(tregex)

        head = Node()
        stack = [head]
        flag = None
        for match in matches:
            # print(match.group(), head, stack)
            text = match.group()
            if text == ' ':
                continue

            # if it's open parathesis, push head
            # into stack and change new head
            elif text == '(':
                # head = Node()
                stack.append(head)
                if flag == -1:
                    head = head.children[-1]
                elif flag == 0:
                    head = head.siblings[-1]
                elif flag == 1:
                    head = head.parents[-1]
                flag = None
            # if it's close parathesis, pop head from stack
            elif text == ')':
                head = stack.pop()
                flag = None

            # if it's a child constraint, create a new child for the head
            elif text in symbols.children:
                flag = -1
                node = Node()
                head.children.append(node)
            # if it's a sibling constraint, create a new sibling for the head
            elif text in symbols.siblings:
                flag = 0
                node = Node()
                head.siblings.append(node)
            # if it's a parent constraint, create a new parent for the head
            elif text in symbols.parents:
                flag = 1
                node = Node()
                head.parents.append(node)
            # default: append to the corresponding node's label
            else:
                if flag is None:
                    head.label += text
                elif flag == -1:
                    head.children[-1].label += text
                elif flag == 0:
                    head.siblings[-1].label += text
                elif flag == 1:
                    head.parents[-1].label += text

        # print(head)
        return stack[0]

    @staticmethod
    def build_hierarchy(node, hierarchy, index):
        if index in hierarchy:
            hierarchy[index].append(node.label)
        else:
            hierarchy[index] = [node.label]
        for parent in node.parents:
            hierarchy = TregexReader.build_hierarchy(parent, hierarchy, index - 1)
        for child in node.children:
            hierarchy = TregexReader.build_hierarchy(child, hierarchy, index + 1)
        for sibling in node.siblings:
            hierarchy = TregexReader.build_hierarchy(sibling, hierarchy, index)
        return hierarchy


    @staticmethod
    def print_hierarchy(node, indent=0):
        def _print_hierarchy(sub_node, sub_indent=0):
            for parent in sub_node.parents:
                print(' ' * sub_indent + 'parent: ' + parent.label)
                _print_hierarchy(parent, sub_indent + 2)
            for child in sub_node.children:
                print(' ' * sub_indent + 'child: ' + child.label)
                _print_hierarchy(child, sub_indent + 2)
            for sibling in sub_node.siblings:
                print(' ' * sub_indent + 'sibling: ' + sibling.label)
                _print_hierarchy(sibling, sub_indent + 2)

        print('root: ' + node.label)
        _print_hierarchy(node, indent + 2)


tregex = '__=p < (NP|VP <- VBG|NN=tr $+ (PP <, (IN <, /^of$/ $+ /^N.*$/) $+ (PP <, (IN <, /^by$/ $+ /^N.*$/=arg))))'
# tregex = 'NP=p < (/PRP\$/=arg $+ (__=tr))'
tregex = '__=p < (NP=arg < (/^N.*$/ <- NN|NNS|NNP|NNPS=tr $+ (PP < (IN <<, /^(of|for)$/ $+ /^N.*$/))) !<< SBAR)'
tregex = '__=p < (NP|NNP=tr $+ (VP <<, /^(is|are|was|were)$/ < (__ < (S < (VP <, TO <2 (VP <, (VBD|VBZ|VBP|VBG|VB|AUX <, /be|is|are|was|were|been/) <2 NP=arg)))) !<< SBAR))'
if __name__ == '__main__':
    reader = TregexReader()
    node = reader.read(tregex)
    # h = reader.build_hierarchy(node, {}, 0)
    reader.print_hierarchy(node)