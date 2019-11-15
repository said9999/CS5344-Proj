#!/usr/bin/env python
# coding: utf-8

# In[1]:


import time
import collections

import pandas as pd
import numpy as np
import multiprocessing as mp
import networkx as nx
import ndlib.models.ModelConfig as mc
import ndlib.models.epidemics as ep
import concurrent.futures
from tqdm import tqdm


# In[2]:


def IC(G_df, S, iter_num=3):
    '''
    Input: G_df: dataframe of edges ['followee','follower', 'prob']
           S: seedset
           iter_num: Number of total IC steps
           mc_num: Number of Monte-Carlo simulations
    Output: list of activated nodes
    '''
    g = nx.DiGraph()
    G = G_df[['followee','follower']].values
    weights = G_df['prob'].values
    g.add_edges_from(G, weights=weights)
    
    # Model configuration
    model = ep.IndependentCascadesModel(g)
    config = mc.Configuration()
    config.add_model_initial_configuration('Infected', S)
    
    # Edge configuration
    for e in g.edges:
        config.add_edge_configuration('threshold', e, 1)

    model.set_initial_status(config)

    iteration = model.iteration_bunch(iter_num)
    return iteration
    
    activated = []
    for i in range(iter_num):
        activated += [k for k, v in iteration[i]['status'].items() if v == 1]

    return list(set(activated))


# In[3]:


def batch_score(i, uniq_nodes, G, merged, accum_score, activated, d):
    
    THRESHOLD = 10e-6
    BATCH_SIZE = 1000
    
    selected_nodes = uniq_nodes[i * BATCH_SIZE : (i + 1) * BATCH_SIZE]
    batch = merged.loc[merged.followee.isin(selected_nodes), :]
    
    pre = batch.copy()
    for i in range(d - 1):
        res = batch.merge(G, how='left', left_on='follower', right_on='followee').dropna().drop_duplicates()
        res = res[['followee_x', 'follower_y', 'prob_x', 'prob_y']]
        res['prob'] = res['prob_x'] * res['prob_y']
        res = res.loc[res.prob > THRESHOLD, ['followee_x', 'follower_y', 'prob']]
        res.rename({'followee_x': 'followee', 'follower_y': 'follower'}, axis=1, inplace=True)

        # remove activated nodes in previous round
        res = pd.concat([res, pre])
        pre = res.drop_duplicates(subset=['followee', 'follower'])
        res.drop_duplicates(subset=['followee', 'follower'], keep=False, inplace=True)

        # Get marginal gain on score
        curr_score = res.loc[~res.follower.isin(activated), ['followee', 'prob']].groupby('followee').sum()
        accum_score = accum_score.add(curr_score, fill_value=0)

        # update the path dataframe
        batch = res

    return accum_score.idxmax().values[0], accum_score.max().values[0] 


# In[4]:


def assgin_score(G, uniq_nodes, d=3, seeds=None):
    '''
    Input: G: dataframe of edges ['followee','follower', 'prob']
           uniq_nodes: list of nodes to loop
           d: max depth of of the path
           seeds: current seed set
           activated: set of nodes has been activated
    Output: list of scores of nodes
    '''
    
    BATCH_SIZE = 1000
    THRESHOLD = 10e-6
    
    G.rename({G.columns.values[2]: 'prob'}, axis=1, inplace=True)
    
    # Filter out activated nodes
    if seeds:
        activated = IC(G, seeds, d)
    else:
        activated = []
    
    # Run in batches to compute scores
#     uniq_node = G['followee'].unique()   
    num_batch = len(uniq_nodes) // 1000 + 1
    
    merged = G.copy()
    merged = merged.loc[~merged.follower.isin(activated), :]
    accum_score = merged[['followee', 'prob']].groupby('followee').sum()
    
    max_id, max_score = None, 0

#####################################
#     multi processing              #
#####################################
#     pool = mp.Pool(30)
#     #def batch_score(i, uniq_node, G, merged, accum_score, activated, d)
#     multi_res = [pool.apply_async(batch_score, 
#                                   (i, uniq_nodes, G, merged, accum_score.copy(), activated, d)) 
#                  for i in range(num_batch)]
#     marg_gain = [res.get() for res in multi_res]
#     Q = sorted(marg_gain, key = lambda x: x[1],reverse=True)

#####################################
#         single processing         #
#####################################    

    for i in range(num_batch):
        selected_nodes = uniq_nodes[i * BATCH_SIZE : (i + 1) * BATCH_SIZE]
        batch = merged.loc[merged.followee.isin(selected_nodes), :]
        
        pre = batch.copy()
        for i in range(d - 1):
            res = batch.merge(G, how='left', left_on='follower', right_on='followee').dropna().drop_duplicates()
            res = res[['followee_x', 'follower_y', 'prob_x', 'prob_y']]
            res['prob'] = res['prob_x'] * res['prob_y']
            res = res.loc[res.prob > THRESHOLD, ['followee_x', 'follower_y', 'prob']]
            res.rename({'followee_x': 'followee', 'follower_y': 'follower'}, axis=1, inplace=True)
            
            # remove activated nodes in previous round
            res = pd.concat([res, pre])
            pre = res.drop_duplicates(subset=['followee', 'follower'])
            res.drop_duplicates(subset=['followee', 'follower'], keep=False, inplace=True)
            
            # Get marginal gain on score
            curr_score = res.loc[~res.follower.isin(activated), ['followee', 'prob']].groupby('followee').sum()
            accum_score = accum_score.add(curr_score, fill_value=0)
            
            # update the path dataframe
            batch = res
        
        if accum_score.max().values[0] > max_score:
            max_score = accum_score.max().values[0]
            max_id = accum_score.idxmax().values[0]
            
#     return Q[0][0]
    return max_id


# In[5]:


def write_res(k, result):
    filename = 'ASIM_seedset_{}.txt'.format(k)
    with open(filename, 'w') as f:
        f.write(str(result))


# In[ ]:


if __name__ == '__main__':
#     twitter = pd.read_csv('twitter_graph_prob_range.csv', index_col=None, header=0)
    twitter = pd.read_csv('twitter_graph_prob.csv', index_col=None, header=0)
    
    result = []
    k = 1000
    
    start_time = time.time()
    print('Start time: ', start_time)
    
    for _ in tqdm(range(k)):
        temp = twitter['followee'].unique()
        uniq_nodes = temp[~np.isin(temp, result)]
        new_seed = assgin_score(twitter, uniq_nodes, 2, result)
        result.append(new_seed)
    
    write_res(k, result)
    
    print('Duration: ', time.time() - start_time)


# In[ ]:




