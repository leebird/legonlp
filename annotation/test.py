from annotation import *

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

    def test_print(self):
        print(self.entity)

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
        self.assertRaises(Entity.EntityLengthError, Entity, 'Gene', 10, 12, 'BAD')

class TestEvent(unittest.TestCase):
    def setUp(self):
        self.trigger = Entity('Trigger', 4, 10, 'target')
        self.arguments = [Node(Entity('Gene', 0, 3, 'BAD')), Node(Entity('Gene', 11, 14, 'BAD'))]

    def test_simple_event(self):
        Event('Target', self.trigger, self.arguments)

    def test_nested_event(self):
        event = Event('Target', self.trigger, self.arguments)
        trigger = Entity('Trigger', 20, 28, 'regulate')
        Event('Regulation', trigger, [Node(event)])

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
        Node(Entity('Gene', 0, 3, 'BAD'))

    def test_node_event(self):
        trigger = Entity('Trigger', 4, 10, 'target')
        arguments = [Node(Entity('Gene', 0, 3, 'BAD')), Node(Entity('Gene', 11, 14, 'BAD'))]
        Node(Event('Target', trigger, arguments))

    def test_node_invalid_value(self):
        self.assertRaises(TypeError, Node, 123)


class TestAnnotation(unittest.TestCase):
    def setUp(self):
        self.annotation = Annotation()

    def test_add_entity(self):
        self.annotation.add_entity('Gene', 0, 3, 'BAD')

    def test_add_event(self):
        trigger = Entity('Trigger', 4, 10, 'target')
        arguments = [Node(Entity('Gene', 0, 3, 'BAD')), Node(Entity('Gene', 11, 14, 'BAD'))]
        self.annotation.add_event('Target', trigger, arguments)

if __name__ == '__main__':
    unittest.main()
