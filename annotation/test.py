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
        self.assertRaises(Entity.EntityZeroInterval, Entity, 'Gene', 0, 0, 'BAD')

    def test_negative_index(self):
        self.assertRaises(Entity.EntityNegativeIndex, Entity, 'Gene', -1, 0, 'BAD')
        self.assertRaises(Entity.EntityNegativeIndex, Entity, 'Gene', 0, -1, 'BAD')
        self.assertRaises(Entity.EntityNegativeIndex, Entity, 'Gene', -1, -1, 'BAD')

    def test_negative_interval(self):
        self.assertRaises(Entity.EntityNegativeInterval, Entity, 'Gene', 10, 5, 'BAD')

class TestEvent(unittest.TestCase):
    def setUp(self):
        self.entity = Entity('Gene', 0, 3, 'BAD')
        self.entity = Entity('Gene', 0, 3, 'BAD')
    pass

class TestProperty(unittest.TestCase):
    pass

class TestArgument(unittest.TestCase):
    pass

class TestAnnotation(unittest.TestCase):
    pass

if __name__ == '__main__':
    unittest.main()
