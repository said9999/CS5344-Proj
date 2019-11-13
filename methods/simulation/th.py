import networkx as nx
import ndlib.models.ModelConfig as mc
import ndlib.models.epidemics as ep
import time
import random
import os
import re

node_scores = {}
def load_scores():
    for p in os.listdir('./nodes_score'):
        if not p.startswith('part'):
            continue
        
        with open('./nodes_score/'+p, 'r') as f:
            for row in f:
                row = row.rstrip()
                if not row:
                    continue
                print(row)
                ret = re.findall(r".+'(\d+)'.+([\d\.]+).+", row)
                print(ret)
                node_scores[str(ret[0][0])] = float(ret[0][1])

load_scores()
g = nx.DiGraph()

with open('twitter_graph.txt', 'r') as f:
    for row in f:
        row = row.rstrip()
        if not row:
            continue
        
        ls = row.split()
        source = ls[0]
        to = ls[1]
        score = float(ls[2])
        g.add_edge(source, to, weight=score)

# Model selection
model = ep.ThresholdModel(g)
config = mc.Configuration()
infected_nodes = ['70775228', '17845620', '512355630', '14191945', '211955846', '121858259', '54981205', '26143624', '20050047', '20801235']
config.add_model_initial_configuration("Infected", infected_nodes)

# Setting the edge parameters
threshold = 0.5
for i in g.nodes():
    if i not in node_scores:
        threshold = random.uniform(0, 1)
    else:
        threshold = node_scores[i]
    config.add_node_configuration("threshold", i, threshold)

model.set_initial_status(config)

print("start iteration")
t0 = time.time()
# Simulation execution
iterations = model.iteration_bunch(100)

print(iterations)

print('takes {}s'.format(time.time()-t0))
