class Entity(object):
    # template to print the entity
    template = '{0}_{1}_{2}_{3}'

    class EntityIndexError(Exception):

        ZERO_INTERVAL = 0
        NEGATIVE_INTERVAL = 1
        NEGATIVE_INDEX = 2
        INEQUAL_LENGTH = 3

        # exception messages
        MESSAGES = {
            ZERO_INTERVAL: 'Zero interval of the text span',
            NEGATIVE_INTERVAL: 'Negative interval of the text span',
            NEGATIVE_INDEX: 'Negative index of the text span',
            INEQUAL_LENGTH: 'Interval length and text length are not equal'
        }

        def __init__(self, value):
            self.value = value

        def __str__(self):
            if self.value in self.MESSAGES:
                return repr(self.MESSAGES[self.value])
            else:
                return repr('Unknown error')

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
            raise self.EntityIndexError(self.EntityIndexError.NEGATIVE_INDEX)

        # test if start equals end, which means the entity has 0 length
        if self.start == self.end:
            raise self.EntityIndexError(self.EntityIndexError.ZERO_INTERVAL)

        # test if start is larger than end, which is invalid for indices of text span
        if self.start > self.end:
            raise self.EntityIndexError(self.EntityIndexError.NEGATIVE_INTERVAL)

        if self.end - self.start != len(self.text):
            raise self.EntityIndexError(self.EntityIndexError.INEQUAL_LENGTH)

    def __str__(self):
        return self.template.format(self.category, self.start, self.end, self.text)

    def __repr__(self):
        return str(self)

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
    template = '{category} ({trigger})'

    def __init__(self, category, trigger=None, arguments=None):
        """
        An event structure with trigger and arguments
        :param category: event type
        :type category: str
        :param trigger: event trigger
        :type trigger: Entity
        :param arguments: a list of node objects
        :type arguments: list
        :return: None
        :rtype: None
        """
        self.category = category
        self.trigger = trigger
        if arguments is None:
            self.arguments = []
        else:
            self.arguments = arguments
        self.property = Property()

    def __str__(self):
        return self.template.format(category=self.category, trigger=self.trigger)

    def __repr__(self):
        return str(self)

    def __eq__(self, other):
        if isinstance(other, self.__class__):
            return (self.category == other.category and
                    self.trigger == other.trigger and
                    set(self.arguments) == set(other.arguments))
        else:
            return False

    def add_argument(self, category, argument):
        """
        add new argument
        :param argument: an argument is an entity or event
        :type argument: Entity | Event
        :param category: semantic category, e.g., agent
        :type category: str
        :return: None
        :rtype: None
        """
        self.arguments.append(Node(category, argument))


class Node(object):
    def __init__(self, category, value):
        """
        the argument must be an entity or another event.
        :param category: the semantic category of the argument, e.g., agent
        :type category: str
        :param value: the actual entity or event
        :type value: Entity | Event
        :return: None
        :rtype: None
        """
        self.value = value
        self.category = category

        if (not self.is_leaf()) and (not self.is_tree()):
            raise TypeError('Value must be an entity or event: ' + str(value))

    def is_leaf(self):
        return isinstance(self.value, Entity)

    def is_tree(self):
        return isinstance(self.value, Event)

    def indent_print(self, indent=0):
        if self.is_leaf():
            return ' ' * indent + self.category + ': ' + str(self.value)
        else:
            return ' ' * indent + self.category + ': ' + str(self.value) + '\n' + \
                   '\n'.join([n.indent_print(indent + 2) for n in self.value.arguments])


class Property(object):
    def __init__(self):
        """
        property manager for entity/event/relation
        """
        self.vault = {}

    def add(self, key, value):
        self.vault[key] = value

    def get(self, key):
        try:
            return self.vault[key]
        except KeyError:
            return None

    def has(self, key, value):
        if key in self.vault and self.vault[key] == value:
            return True
        else:
            return False

    def delete(self, key):
        if key in self.vault:
            del self.vault[key]

    def update(self, vault):
        """
        update the property vault
        :param vault: properties to be added to vault
        :type vault: dict
        :return: None
        :rtype: None
        """
        self.vault.update(vault)


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
        self.special = []

    def __str__(self):
        return self.template.format(len(self.entities), len(self.events))

    def make_argument(self, category, value):
        return Node(category, value)

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

    def add_entities(self, entities):
        """
        add a list of new entities
        :param entities: a list of new entities
        :type entities: list
        :return: None
        :rtype: None
        """
        # TODO: how to deal with duplicate/overlap
        self.entities += entities

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

    def get_entity_with_property(self, key, value):
        return [t for t in self.entities if t.property.has(key, value)]

    def add_event(self, category, trigger=None, arguments=None):
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

    def get_event_without_trigger(self):
        return [event for event in self.events if event.trigger is None]

    def get_event_with_trigger(self):
        return [event for event in self.events if event.trigger is not None]

    def has_entity(self, entity):
        if entity in self.entities:
            return True
        return False

    def has_event(self, event):
        if event in self.events:
            return True
        return False

    def remove_entity(self, entity):
        self.entities.remove(entity)
        
    def remove_event(self, event):
        self.events.remove(event)

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
                '''
                e1: 0-5, e2: 4-6
                e1: 4-6, e2: 0-5
                e1: 0-5, e2: 1-3
                '''
                if e1.start < e2.end and e1.end > e2.start:
                    if e1.start > e2.start and e1.end < e2.end:
                        indices.append(i)
                    elif e1.start < e2.start and e1.end > e2.end:
                        indices.append(j)
        self.entities = [e for i, e in enumerate(self.entities) if i not in indices]

    def remove_overlap(self, keep=None, remove=None):
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

        if keep is None and remove is None:
            # removed any overlapped entities
            # TODO: this branch's result is not clear
            entities_keep = self.entities[:]
            entities_removed = self.entities[:]
        elif keep is None:
            # remove entities of removing type overlapped with any other kinds of entities
            entities_keep = self.get_entity_category(remove, complement=True)
            entities_removed = self.get_entity_category(remove)
        elif remove is None:
            # remove entities of any other kinds overlapped with the keeping type of entities
            entities_keep = self.get_entity_category(keep)
            entities_removed = self.get_entity_category(keep, complement=True)
        else:
            entities_keep = self.get_entity_category(keep)
            entities_removed = self.get_entity_category(remove)

        for k in entities_keep:
            for r in entities_removed:
                if k == r:
                    continue
                if k.start < r.end and k.end > r.start:
                    self.entities.remove(r)
