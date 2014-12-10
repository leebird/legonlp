# -*- coding: utf-8 -*-
import os
import codecs
import json
from annotation import Entity,Event,Relation,Annotation

class BionlpWriter(object):
    def __init__(self):
        self.entityFormat = u'{0}\t{1} {2} {3}\t{4}\n'
        self.eventFormat = u'{0}\t{1}:{2} {3}\t{4}\n'    
        self.relFormat = u'{0}\t{1} Arg1:{2} Arg2:{3}\t{4}\n'

    def entity_line(self,t):
        return self.entityFormat.format(t.id,t.type,t.start,t.end,t.text)

    def event_line(self,e):
        args = [a[0]+':'+a[1].id for a in e.args]
        args = ' '.join(args).strip()
        prop = json.dumps(e.prop.prop)
        if len(args) > 0:
            return self.eventFormat.format(e.id,e.type,e.trigger.id,args,prop)
        else:
            return None

    def relation_line(self,r):
        prop = json.dumps(r.prop.prop)
        return self.relFormat.format(r.id,r.type,r.arg1.id,r.arg2.id,prop)

class AnnWriter(BionlpWriter):
    def __init__(self):
        super(AnnWriter,self).__init__()

    def write(self,filepath,annotation):
        f = codecs.open(filepath,'w+','utf-8')

        for k,t in annotation.get_entities().iteritems():
            line = self.entity_line(t)
            f.write(line)

        for k,e in annotation.get_events().iteritems():
            line = self.event_line(e)
            f.write(line)

        for k,r in annotation.get_relations().iteritems():
            line = self.relation_line(r)
            f.write(line)

        f.close()

class A1A2Writer(BionlpWriter):
    def __init__(self):
        super(A1A2Writer,self).__init__()

    def write(self,a1path,a1file,a2path,a2file,annotation):
        triggerId = []
        
        entities = annotation['T']
        events = annotation['E']

        filepath = os.path.join(a2path,a2file)
        f = codecs.open(filepath,'w+','utf-8')
        
        for k,e in events.iteritems():
            if e.triggerId not in triggerId:
                triggerId.append(e.triggerId)
                trigger = entities[e.triggerId]
                line = self.entity_line(trigger)
                f.write(line)

            line = self.event_line(e)
            if line is not None:
                f.write(line)
            
        f.close()

        filepath = os.path.join(a1path,a1file)
        f = codecs.open(filepath,'w+','utf-8')

        for k,t in entities.iteritems():
            if t.id not in triggerId:
                line = self.entity_line(t)
                f.write(line)
        f.close()

class HtmlWriter:
    def __init(self):
        pass
