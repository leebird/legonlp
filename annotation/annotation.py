# -*- coding: utf-8 -*-

class Base(object):
    def __str__(self):
        return unicode(self).encode('utf-8')

    def __repr__(self):
        return self.__str__()

    def __eq__(self, other):
        if isinstance(other, self.__class__):
            return self.__dict__ == other.__dict__
        else:
            return False

    def __ne__(self, other):
        return not self.__eq__(other)    

class Entity(Base):

    linestart = u'T'

    def __init__(self,tid,typing,start,end,text):
        self.id = tid
        self.type = typing
        self.start = start
        self.end = end
        self.text = text
        self.prop = Property()
        self.tmpl = u'{0}_{1}_{2}_{3}_{4}'

    def __unicode__(self):
        return self.tmpl.format(self.id,self.type,self.start,self.end,self.text)

    '''
    only compare type, start, end and text
    '''
    def __eq__(self, other):
        if isinstance(other, self.__class__):
            return (self.type == other.type and 
                    self.start == other.start and 
                    self.end == other.end and 
                    self.text == other.text)
        else:
            return False

class Event(Base):

    linestart = u'E'

    def __init__(self,tid,typing,trigger,args):
        self.id = tid
        self.type = typing
        self.trigger = trigger
        self.args = args
        self.prop = Property()
        self.tmpl = u'{0}_{1}_{2}_{3}'
        
    def __unicode__(self):
        return self.tmpl.format(self.id,self.type,self.trigger,self.args)

    '''
    only compare type, trigger and args
    '''
    def __eq__(self, other):
        if isinstance(other, self.__class__):
            return (self.type == other.type and 
                    self.trigger == other.trigger and 
                    set(self.args) == set(other.args))
        else:
            return False

    def add_prop(self,key,value):
        self.prop.add_prop(key,value)

class Relation(Base):

    linestart = u'R'

    def __init__(self,rid,typing,arg1,arg2):
        self.id = rid
        self.type = typing
        self.arg1 = arg1
        self.arg2 = arg2
        self.prop = Property()
        self.tmpl = u'{0}_{1}_{2}_{3}'

    def __unicode__(self):
        return self.tmpl.format(self.id,self.type,self.arg1,self.arg2)

    '''
    only compare type, arg1 and arg2
    '''
    def __eq__(self, other):
        if isinstance(other, self.__class__):
            return (self.type == other.type and 
                    self.arg1 == other.arg1 and 
                    self.arg2 == other.arg2)
        else:
            return False

    def add_prop(self,key,value):
        self.prop.add_prop(key,value)

class Property(Base):
    '''
    property manager for entity/event/relation
    '''
    def __init__(self):
        self.prop = {}

    def add_prop(self,key,value):
        if self.prop.has_key(key):
            if value not in self.prop[key]:
                self.prop[key].append(value)
        else:
            self.prop[key] = [value]

    def get_prop(self, key):
        try:
            return self.prop[key]
        except:
            return

    def delete_prop(self,key,value):
        if self.prop.has_key(key):
            if value in self.prop[key]:
                self.prop[key].remove(value)

class Annotation(Base):
    def __init__(self):
        self.text = None
        self.entities = {}
        self.events = {}
        self.relations = {}        
        self.tid = 0
        self.eid = 0
        self.rid = 0
        self.tidtmpl = u'T{}'
        self.eidtmpl = u'E{}'
        self.ridtmpl = u'R{}'
        self.tmpl = u'Annotation: {} entities, {} events, {} relations'

    def __unicode__(self):
        return self.tmpl.format(len(self.entities),len(self.events),len(self.relations))

    def get_entities(self):
        return self.entities

    def get_events(self):
        return self.events

    def get_relations(self):
        return self.relations

    def get_entity(self,tid):
        if self.entities.has_key(tid):
            return self.entities[tid]

    def get_event(self,eid):
        if self.events.has_key(eid):
            return self.events[eid]

    def get_relation(self,rid):
        if self.relations.has_key(rid):
            return self.relations[rid]

    def get_entity_type(self,typing):
        return [t for t in self.entities.values() if t.type == typing]

    def add_entity(self,typing,start,end,text):
        entity = self.has_entity_prop(typing,start,end,text)
        if entity is not None:
            return entity

        self.tid += 1
        tid = self.tidtmpl.format(self.tid)
        entity = Entity(tid,typing,start,end,text)
        self.entities[tid] = entity
        return entity

    def add_entities(self,annotation):
        for t in annotation.get_entities().values():
            self.add_entity(t.type, t.start, t.end, t.text)
        
    def add_event(self,typing,trigger,args):
        event = self.has_event_prop(typing, trigger, args)
        if event is not None:
            return event

        self.eid += 1
        eid = self.eidtmpl.format(self.eid)
        event = Event(eid,typing,trigger,args)
        self.events[eid] = event
        return event

    def add_relation(self,typing,arg1,arg2):
        relation = self.has_relation_prop(typing,arg1,arg2)
        if relation is not None:
            return relation

        self.rid += 1
        rid = self.ridtmpl.format(self.rid)
        relation = Relation(rid,typing,arg1,arg2)
        self.relations[rid] = relation
        return relation

    def add_exist_entity(self,tid,typing,start,end,text):
        order = self.get_number(tid)
        if self.tid < order:
            self.tid = order
        entity = Entity(tid,typing,start,end,text)
        self.entities[tid] = entity
        return entity

    def add_exist_event(self,eid,typing,trigger,args):
        order = self.get_number(eid)
        if self.eid < order:
            self.eid = order
        event = Event(eid,typing,trigger,args)
        self.events[eid] = event
        return event

    def add_exist_relation(self,rid,typing,arg1,arg2):
        order = self.get_number(rid)
        if self.rid < order:
            self.rid = order
        relation = Relation(rid,typing,arg1,arg2)
        self.relations[rid] = relation
        return relation

    def has_entity(self,entity):
        if entity in self.entities.values():
            return True
        return False

    def has_entity_prop(self,typing,start,end,text):
        for entity in self.entities.values():
            if (typing == entity.type and
                start == entity.start and
                end == entity.end and
                text == entity.text):
                return entity            
        return None

    def has_event(self,event):
        if event in self.events.values():
            return True
        return False

    def has_event_prop(self,typing,trigger,args):
        for event in self.events.values():
            if (typing == event.type and
                trigger == event.trigger and
                sorted(args) == sorted(event.args)):
                return event
            
        return None

    def has_relation(self,relation):
        if relation in self.relations.value():
            return True
        return False

    def has_relation_prop(self,typing,arg1,arg2):
        for relation in self.relations.values():
            if (typing == relation.type and 
                arg1 == relation.arg1 and 
                arg2 == relation.arg2):
                return relation

        return None

    def del_entity(self,entity):
        if self.entities.has_key(entity.id):
            del self.entities[entity.id]
    
    def get_number(self,xid):
        return int(xid[1:])

    def remove_included(self):
        entities = self.get_entities().values()
        for e1 in entities:
            for e2 in entities:
                if e1 == e2:
                    continue
                if e1.start < e2.end and e1.end > e2.start:
                    if e1.start > e2.start:
                        self.del_entity(e1)
                    else:
                        self.del_entity(e2)

    def remove_overlap(self, keep, remove):
        toKeep = self.get_entity_type(keep)
        toRemove = self.get_entity_type(remove)
        for k in toKeep:
            for r in toRemove:
                if k.start < r.end and k.end > r.start:
                    self.del_entity(r)
