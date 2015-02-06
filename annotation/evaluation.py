class Evaluation(object):
    def __init__(self):
        pass

    @classmethod
    def calculate(cls, user_set, golden_set, verbose=False):

        golden_num = len(golden_set)
        tp = len(golden_set & user_set)
        fp = len(user_set - golden_set)
        fn = len(golden_set - user_set)

        precision = tp * 1.0 / (tp + fp) if tp + fp > 0 else 0
        recall = tp * 1.0 / (tp + fn) if tp + fn > 0 else 0
        fscore = 2 * precision * recall / (precision + recall) if precision + recall > 0 else 0

        print('all relations:', golden_num)
        print("%-10s %-10s %10s" % ('precision', 'recall', 'f1-score'))
        print("%-10.2f %-10.2f %10.2f" % (precision, recall, fscore))

        if verbose:
            print()
            print('TP:')
            for t in sorted(user_set & golden_set, key=lambda a: a[0]):
                print(t)

            print()
            print('FP:')
            for t in sorted(user_set - golden_set, key=lambda a: a[0]):
                print(t)

            print()
            print('FN:')
            for t in sorted(golden_set - user_set, key=lambda a: a[0]):
                print(t)

            print()
            print('FN:')
            print('\n'.join(set([str(t[0]) for t in golden_set - user_set])))

    @classmethod
    def get_entity_category_set(cls, docid, annotation, category):
        entity_set = set()
        entities = annotation.get_entity_category(category)
        for entity in entities:
            entity_set.add((docid, str(entity)))
        return entity_set

    @classmethod
    def get_event_category_set(cls, docid, annotation, category):
        event_set = set()
        events = annotation.get_event_category(category)
        for event in events:
            event_set.add((docid, str(event)))
        return event_set

    @classmethod
    def evaluate(cls, user_annotation, gold_annotation, entity_category=None, event_category=None):

        user_set, gold_set = set(), set()

        if entity_category is not None and len(entity_category) > 0:
            for category in entity_category:
                for docid, annotation in user_annotation.items():
                    user_set = user_set.union(cls.get_entity_category_set(docid, annotation, category))
                for docid, annotation in gold_annotation.items():
                    gold_set = gold_set.union(cls.get_entity_category_set(docid, annotation, category))

        if event_category is not None and len(event_category) > 0:
            for category in event_category:
                for docid, annotation in user_annotation.items():
                    user_set.union(cls.get_event_category_set(docid, annotation, category))
                for docid, annotation in gold_annotation.items():
                    gold_set.union(cls.get_event_category_set(docid, annotation, category))
                    
        cls.calculate(user_set, gold_set)



