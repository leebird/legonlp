class Entity(object):
    # template to print the entity
    template = '{0}_{1}_{2}_{3}'

    class EntityZeroInterval(Exception):
        def __str__(self):
            return repr('Zero interval of the text span')

    class EntityNegativeInterval(Exception):
        def __str__(self):
            return repr('Negative interval of the text span')

    class EntityNegativeIndex(Exception):
        def __str__(self):
            return repr('Negative index of the text span')

    def __init__(self, category, start, end, text):
        """A text span that refers to an entity

        :param category: entity category, e.g., gene
        :type category: str
        :param start: entity starting position in text
        :type start: int
        :param end: entity ending position in text
        :type end: int
        :param text: entity text
        :type text: str
        :return: None
        :rtype: None
        """
        self.category = category
        self.start = start
        self.end = end
        self.text = text
        self.property = Property()

        # test if start/end is negative
        if self.start < 0 or self.end < 0:
            raise self.EntityNegativeIndex

        # test if start equals end, which means the entity has 0 length
        if self.start == self.end:
            raise self.EntityZeroInterval

        # test if start is larger than end, which is invalid for indices of text span
        if self.start > self.end:
            raise self.EntityNegativeInterval

    def __str__(self):
        return self.template.format(self.category, self.start, self.end, self.text)

    def __eq__(self, other):
        """
        only compare type, start, end and text
        """
        if isinstance(other, self.__class__):
            return (self.category == other.category and
                    self.start == other.start and
                    self.end == other.end and
                    self.text == other.text)
        else:
            return False


class Event(object):
    # template to print the event
    template = '{0}_{1}_{2}_{3}'

    def __init__(self, category, trigger, arguments):
        """
        An event structure with trigger and arguments
        :param category: event type
        :type category: str
        :param trigger: event trigger
        :type trigger: Entity
        :param arguments: event arguments list
        :type arguments: list
        :return: None
        :rtype: None
        """
        self.category = category
        self.trigger = trigger
        self.arguments = arguments
        self.property = Property()

    def __str__(self):
        return self.template.format(self.category, self.trigger, self.arguments)

    def __eq__(self, other):

        if isinstance(other, self.__class__):
            return (self.category == other.category and
                    self.trigger == other.trigger and
                    set(self.arguments) == set(other.arguments))
        else:
            return False


class Node(object):
    def __init__(self, value):
        self.value = value
        self.category = None
        self.get_category()

    def get_category(self):
        if isinstance(self.value, Entity):
            self.category = 'Entity'
        elif isinstance(self.value, Event):
            self.category = 'Event'
        else:
            raise TypeError('Argument is not an entity or event')

    def is_leaf(self):
        return isinstance(self.value, Entity)


class Property(object):
    def __init__(self):
        """
        property manager for entity/event/relation
        """
        self.vault = {}

    def add(self, key, value):

        if key in self.vault:
            if value not in self.vault[key]:
                self.vault[key].append(value)
        else:
            self.vault[key] = [value]

    def get(self, key):
        try:
            return self.vault[key]
        except:
            return None

    def delete(self, key):
        if key in self.vault:
            del self.vault[key]


class Annotation(object):
    template = '{0} entities, {1} events'

    def __init__(self):
        """
        annotation storing text, entities and events
        :return: None
        :rtype: None
        """
        self.text = ''
        self.entities = []
        self.events = []

    def add_entity(self, category, start, end, text):
        """
        add a new entity
        :param category: entity category, e.g., Gene
        :type category: str
        :param start: entity start position
        :type start: int
        :param end: entity end position
        :type end: int
        :param text: the associated text
        :type text: str
        :return: the created entity
        :rtype: Entity
        """
        entity = Entity(category, start, end, text)
        self.entities.append(entity)
        return entity

    def get_entity_category(self, category, complement=False):
        """
        get a list of entities of the same category
        :param category: entity category
        :type category: str
        :param complement: set True to get all other categories but the input one
        :type complement: bool
        :return: a list of entities of the input category
        :rtype: list
        """
        if complement:
            return [t for t in self.entities if t.category != category]
        else:
            return [t for t in self.entities if t.category == category]

    def add_event(self, category, trigger, arguments):
        """
        add a new event
        :param category: event category, e.g., Regulation
        :type category: str
        :param trigger: the event trigger
        :type trigger: Entity
        :param arguments: a list of event arguments
        :type arguments: list
        :return: the created event
        :rtype: Event
        """
        event = Event(category, trigger, arguments)
        self.events.append(event)
        return event

    def get_event_category(self, category, complement=False):
        """
        get a list of events of the same category
        :param category: event category
        :type category: str
        :param complement: set True to get all other categories but the input one
        :type complement: bool
        :return: a list of events of the input category
        :rtype: list
        """
        if complement:
            return [e for e in self.events if e.category != category]
        else:
            return [e for e in self.events if e.category == category]

    def has_entity(self, entity):
        if entity in self.entities:
            return True
        return False

    def has_event(self, event):
        if event in self.events:
            return True
        return False

    def remove_included(self):
        """
        remove overlapping entities
        if two entities are overlapping with each other,
        remove the inner (included) one
        :return: None
        :rtype: None
        """
        entities = self.entities
        indices = []
        for i, e1 in enumerate(entities):
            for j, e2 in enumerate(entities):
                if e1 == e2:
                    continue
                if e1.start < e2.end and e1.end > e2.start:
                    if e1.start > e2.start:
                        indices.append(i)
                    else:
                        indices.append(j)
        self.entities = [e for i, e in enumerate(self.entities) if i in indices]

    def remove_overlap(self, keep, remove):
        """
        remove overlapping entities of some category
        :param keep: the entity category to keep. If it is None,
        then compare the to-be-removed category to all other categories.
        :type keep: str | None
        :param remove: the entity category to remove
        :type remove: str
        :return: None
        :rtype: None
        """
        if keep is not None:
            entities_keep = self.get_entity_category(keep)
        else:
            entities_keep = self.get_entity_category(remove, complement=True)

        entities_removed = self.get_entity_category(remove)

        for k in entities_keep:
            for r in entities_removed:
                if k.start < r.end and k.end > r.start:
                    self.entities.remove(r)
