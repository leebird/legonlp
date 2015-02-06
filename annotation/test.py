import sys

from annotation.annotate import *
from annotation.readers import *
from annotation.writers import *
from annotation.evaluation import *

import unittest


class TestEntity(unittest.TestCase):
    def setUp(self):
        self.entity = Entity('Gene', 0, 3, 'BAD')

    def test_category(self):
        self.assertEqual('Gene', self.entity.category)

    def test_start(self):
        self.assertEqual(0, self.entity.start)

    def test_end(self):
        self.assertEqual(3, self.entity.end)

    def test_text(self):
        self.assertEqual('BAD', self.entity.text)

    def test_text_length(self):
        self.assertTrue(len(self.entity.text) == self.entity.end - self.entity.start)

    def test_same_start_end(self):
        self.assertRaises(Entity.EntityIndexError, Entity, 'Gene', 0, 0, 'BAD')

    def test_negative_index(self):
        self.assertRaises(Entity.EntityIndexError, Entity, 'Gene', -1, 0, 'BAD')
        self.assertRaises(Entity.EntityIndexError, Entity, 'Gene', 0, -1, 'BAD')
        self.assertRaises(Entity.EntityIndexError, Entity, 'Gene', -1, -1, 'BAD')

    def test_negative_interval(self):
        self.assertRaises(Entity.EntityIndexError, Entity, 'Gene', 10, 5, 'BAD')

    def test_wrong_length(self):
        self.assertRaises(Entity.EntityIndexError, Entity, 'Gene', 10, 12, 'BAD')


class TestEvent(unittest.TestCase):
    def setUp(self):
        self.trigger = Entity('Trigger', 4, 10, 'target')
        self.arguments = [Node('Agent', Entity('Gene', 0, 3, 'BAD')),
                          Node('Theme', Entity('Gene', 11, 14, 'BAD'))]

    def test_simple_event(self):
        Event('Target', self.trigger, self.arguments)

    def test_nested_event(self):
        event = Event('Target', self.trigger, self.arguments)
        trigger = Entity('Trigger', 20, 28, 'regulate')
        Event('Regulation', trigger, [Node('Theme', event)])


class TestProperty(unittest.TestCase):
    def setUp(self):
        self.property = Property()

    def test_property_add(self):
        self.property.add('id', 'P1234')

    def test_property_get(self):
        self.property.add('id', 'P1234')
        self.assertEqual(self.property.get('id'), 'P1234')

    def test_property_del(self):
        self.property.add('id', 'P1234')
        self.property.delete('id')
        self.assertIsNone(self.property.get('id'))


class TestNode(unittest.TestCase):
    def test_node_entity(self):
        Node('Theme', Entity('Gene', 0, 3, 'BAD'))

    def test_node_event(self):
        trigger = Entity('Trigger', 4, 10, 'target')
        arguments = [Node('Agent', Entity('Gene', 0, 3, 'BAD')),
                     Node('Theme', Entity('Gene', 11, 14, 'BAD'))]
        Node('Theme', Event('Target', trigger, arguments)).indent_print()


    def test_node_nested_event(self):
        trigger = Entity('Trigger', 4, 10, 'target')
        arguments = [Node('Agent', Entity('Gene', 0, 3, 'BAD')),
                     Node('Theme', Entity('Gene', 11, 14, 'BAD'))]

        arguments = [Node('Theme', Event('Target', trigger, arguments))]
        trigger = Entity('Trigger', 20, 28, 'regulate')

        Node('Root', Event('Regulation', trigger, arguments))

    def test_node_invalid_value(self):
        self.assertRaises(TypeError, Node, 123)


class TestAnnotation(unittest.TestCase):
    def setUp(self):
        self.annotation = Annotation()

    def test_add_entity(self):
        self.annotation.add_entity('Gene', 0, 3, 'BAD')

    def test_add_event(self):
        trigger = Entity('Trigger', 4, 10, 'target')
        arguments = [Node('Agent', Entity('Gene', 0, 3, 'BAD')),
                     Node('Theme', Entity('Gene', 11, 14, 'BAD'))]
        self.annotation.add_event('Target', trigger, arguments)

    def test_get_entity_category(self):
        entity = self.annotation.add_entity('Gene', 0, 3, 'BAD')
        self.annotation.add_entity('Protein', 0, 3, 'BAD')
        self.annotation.add_entity('Disease', 0, 6, 'cancer')
        self.assertEqual(self.annotation.get_entity_category('Gene'), [entity])

    def test_get_entity_category_complement(self):
        self.annotation.add_entity('Gene', 0, 3, 'BAD')
        entity = self.annotation.add_entity('Protein', 0, 3, 'BAD')
        self.assertEqual(self.annotation.get_entity_category('Gene', True), [entity])

    def test_get_event_category(self):
        trigger = Entity('Trigger', 4, 10, 'target')
        arguments = [Node('Agent', Entity('Gene', 0, 3, 'BAD')),
                     Node('Theme', Entity('Gene', 11, 14, 'BAD'))]
        event = self.annotation.add_event('Target', trigger, arguments)
        self.assertEqual(self.annotation.get_event_category('Target'), [event])

    def test_get_event_category_complement(self):
        trigger = Entity('Trigger', 4, 10, 'target')
        arguments = [Node('Agent', Entity('Gene', 0, 3, 'BAD')),
                     Node('Theme', Entity('Gene', 11, 14, 'BAD'))]
        self.annotation.add_event('Target', trigger, arguments)

        trigger = Entity('Trigger', 4, 12, 'regulate')
        arguments = [Node('Agent', Entity('Gene', 0, 3, 'BAD')),
                     Node('Theme', Entity('Gene', 11, 14, 'BAD'))]
        event = self.annotation.add_event('Regulation', trigger, arguments)
        self.assertEqual(self.annotation.get_event_category('Target', True), [event])

    def test_remove_included(self):
        entity = self.annotation.add_entity('Gene', 0, 5, 'hBAD1')
        self.annotation.add_entity('Protein', 1, 4, 'BAD')
        self.annotation.add_entity('Disease', 0, 6, 'cancer')
        self.annotation.remove_included()
        self.assertEqual(self.annotation.get_entity_category('Gene'), [entity])
        self.assertEqual(self.annotation.get_entity_category('Protein'), [])

    def test_remove_overlap(self):
        entity = self.annotation.add_entity('Gene', 0, 5, 'hBAD1')
        self.annotation.add_entity('Protein', 1, 4, 'BAD')
        self.annotation.add_entity('Disease', 0, 6, 'cancer')
        self.annotation.remove_overlap('Gene', 'Protein')
        self.assertEqual(self.annotation.get_entity_category('Gene'), [entity])
        self.assertEqual(self.annotation.get_entity_category('Protein'), [])

        self.annotation.remove_overlap('Gene')
        self.assertEqual(self.annotation.get_entity_category('Gene'), [entity])
        self.assertEqual(self.annotation.get_entity_category('Disease'), [])


class TestReader(unittest.TestCase):
    def test_annreader(self):
        reader = AnnReader()
        annotation = reader.parse_file('examples/17438130.ann')
        print(Node('Root', annotation.events[0]).indent_print())

    def test_entity_handler(self):
        def handler(entity, fields):
            if len(fields) == 0:
                return
            gene_id = fields[0]
            entity.property.add('gid', gene_id)

        reader = AnnReader(handler)
        annotation = reader.parse_file('examples/17438130.ann')
        print(annotation.get_entity_with_property('gid', '12345'))

    def test_annwriter(self):
        reader = AnnReader()
        annotation = reader.parse_file('examples/17438130.ann')

        writer = AnnWriter()
        writer.write('output/17438130.ann', annotation)


class TestEvaluation(unittest.TestCase):

    def setUp(self):
        reader = AnnReader()
        self.user_annotation = reader.parse_file('examples/17438130.ann')
        self.gold_annotation = reader.parse_file('examples/17438130.ann')
        
    def test_evaluation(self):
        Evaluation.evaluate({'17438130':self.user_annotation}, 
                            {'17438130':self.gold_annotation}, 
                            entity_category=['Gene'])


if __name__ == '__main__':
    unittest.main()
