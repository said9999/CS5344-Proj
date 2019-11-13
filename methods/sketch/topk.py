import time
import sys

def main():
    experiments = [
        {'input': 'result-100', 'topK': 10},
        {'input': 'result-100', 'topK': 100},
        
        {'input': 'result-1000', 'topK': 10},
        {'input': 'result-1000', 'topK': 100},
        {'input': 'result-1000', 'topK': 500},
        {'input': 'result-1000', 'topK': 1000},
        
        {'input': 'result-10000', 'topK': 10},
        {'input': 'result-10000', 'topK': 100},
        {'input': 'result-10000', 'topK': 500},
        {'input': 'result-10000', 'topK': 1000},
    ]

    for e in experiments:
        t0 = time.time()
        ret = run(e)
        delta = time.time() - t0
        with open('./topK/topK-{}-{}'.format(e['input'], e['topK']), 'w') as f:
            f.write(' '.join(ret)+'\n')
            f.write(str(delta))

def run(exp):
    matrix = {} # {node: ['0', '1', '0'.....]}
    vectors = [] # ['1', '1'....'1']
    t0 = time.time()
    print('start loading the file')
    with open(exp['input'], 'r') as f:
        first = True
        for r in f:
            if first: # skip first line
                first = False
                continue
            
            r = r.rstrip()
            if not r:
                continue
            
            ls = r.split()
            matrix[ls[0]] = ls[1:]
            if not vectors:
                vectors = ['1']*len(ls[1:])
    print('finish loading...takes {}s'.format(time.time()-t0))
    #print('matrix size: {}'.format(sys.getsizeof(matrix)))

    topK = exp['topK']
    ret = []
    print('start find top k')
    for i in range(topK):
        t0 = time.time()
        # print('start find the {}th'.format(i))
        max_key = ''
        max_overlap = 0
        for k in matrix:
            if k in ret:
                continue
            op = overlap(vectors, matrix[k])
            if op > max_overlap:
                max_key = k
                max_overlap = op
        
        ret.append(max_key)

        if max_key:
            vectors = remove1(vectors, matrix[max_key])
        print('finish find the {}th, takes {}s'.format(i, time.time()-t0))

    return ret

def remove1(a, b):
    if len(a) != len(b):
        raise Exception('remove1 length not equal') 
    
    for i in range(len(a)):
        if b[i] == '1':
            a[i] == '0'
    
    return a

def overlap(a, b):
    if len(a) != len(b):
        raise Exception('overlap length not equal')
    
    cnt = 0
    for i in range(len(a)):
        if a[i] == '1' and a[i] == b[i]:
            cnt += 1
    
    return cnt

if __name__ == '__main__':
    main()
