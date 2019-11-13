package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
	"time"
)

type Experiment struct {
	FileName string
	TopK     int
}

func run(exp Experiment) map[string]bool {
	matrix := map[string][]string{} //# {node: ['0', '1', '0'.....]}
	vectors := []string{}           //# ['1', '1'....'1']
	// t0 = time.time()
	fmt.Println("start loading the file")
	file, err := os.Open(exp.FileName)
	if err != nil {
		panic(err)
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	first := true
	t0 := time.Now().UnixNano()
	buf := make([]byte, 0, 64*1024)
	scanner.Buffer(buf, 1024*1024)
	for scanner.Scan() {
		if first {
			first = false
			continue
		}

		r := strings.TrimSpace(scanner.Text())
		if len(r) == 0 {
			continue
		}

		ls := strings.Split(r, " ")

		matrix[ls[0]] = ls[1:]
		if len(vectors) == 0 {
			for range ls[1:] {
				vectors = append(vectors, "1")
			}
		}
	}

	if err := scanner.Err(); err != nil {
		panic(err)
	}

	fmt.Printf("finish loading matrix %v, takes %v\n", exp.FileName, ((time.Now().UnixNano() - t0) / 1000000))

	topK := exp.TopK
	ret := map[string]bool{}
	for i := 0; i < topK; i++ {
		t0 = time.Now().UnixNano()
		max_key := ""
		max_overlap := 0

		for k, _ := range matrix {
			if _, ok := ret[k]; ok {
				continue
			}

			op := overlap(vectors, matrix[k])
			if op > max_overlap {
				max_key = k
				max_overlap = op
			}
		}

		ret[max_key] = true

		if len(max_key) > 0 {
			vectors = remove1(vectors, matrix[max_key])
		}
		fmt.Printf("finish find the %vth, taks %vms\n", i, ((time.Now().UnixNano() - t0) / 1000000))
	}

	return ret

	// with open(exp['input'], 'r') as f:
	//     first = True
	//     for r in f:
	//         if first: # skip first line
	//             first = False
	//             continue

	//         r = r.rstrip()
	//         if not r:
	//             continue

	//         ls = r.split()
	//         matrix[ls[0]] = ls[1:]
	//         if not vectors:
	//             vectors = ['1']*len(ls[1:])
	// print('finish loading...takes {}s'.format(time.time()-t0))
	// #print('matrix size: {}'.format(sys.getsizeof(matrix)))

	// topK = exp['topK']
	// ret = []
	// print('start find top k')
	// for i in range(topK):
	//     t0 = time.time()
	//     # print('start find the {}th'.format(i))
	//     max_key = ''
	//     max_overlap = 0
	//     for k in matrix:
	//         if k in ret:
	//             continue
	//         op = overlap(vectors, matrix[k])
	//         if op > max_overlap:
	//             max_key = k
	//             max_overlap = op

	//     ret.append(max_key)

	//     if max_key:
	//         vectors = remove1(vectors, matrix[max_key])
	//     print('finish find the {}th, takes {}s'.format(i, time.time()-t0))

	// return ret
}

func remove1(a, b []string) []string {
	if len(a) != len(b) {
		panic("remove1 length not equal")
	}
	for i, _ := range a {
		if b[i] == "1" {
			a[i] = "0"
		}
	}
	return a
}

func overlap(a, b []string) int {
	if len(a) != len(b) {
		panic("overlap length not equal")
	}

	cnt := 0
	for i, _ := range a {
		if a[i] == "1" && a[i] == b[i] {
			cnt += 1
		}
	}
	return cnt
}

// {'input': 'result-100', 'topK': 10},
// {'input': 'result-100', 'topK': 100},

// {'input': 'result-1000', 'topK': 10},
// {'input': 'result-1000', 'topK': 100},
// {'input': 'result-1000', 'topK': 500},
// {'input': 'result-1000', 'topK': 1000},

// {'input': 'result-10000', 'topK': 10},
// {'input': 'result-10000', 'topK': 100},
// {'input': 'result-10000', 'topK': 500},
// {'input': 'result-10000', 'topK': 1000},

func main() {
	experiments := []Experiment{
		{
			FileName: "result-100",
			TopK:     10,
		},
		{
			FileName: "result-100",
			TopK:     100,
		},
		{
			FileName: "result-1000",
			TopK:     10,
		},
		{
			FileName: "result-1000",
			TopK:     100,
		},
		{
			FileName: "result-1000",
			TopK:     500,
		},
		{
			FileName: "result-1000",
			TopK:     1000,
		},
		{
			FileName: "result-10000",
			TopK:     10,
		},
		{
			FileName: "result-10000",
			TopK:     100,
		},
		{
			FileName: "result-10000",
			TopK:     500,
		},
		{
			FileName: "result-10000",
			TopK:     1000,
		},
	}

	for _, e := range experiments {
		t0 := time.Now().UnixNano()
		ret := run(e)
		delta := time.Now().UnixNano() - t0
		f, err := os.Create(fmt.Sprintf("./topK/topK-%v-%v", e.FileName, e.TopK))
		if err != nil {
			panic(err)
		}
		defer f.Close()
		for i := range ret {
			f.WriteString(fmt.Sprintf("%v ", i))
		}
		f.WriteString(fmt.Sprintf("\n%v", delta))
	}

}
