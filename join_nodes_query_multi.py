import os
import subprocess
import random
import time
import pickle
import requests
import statistics

PORT = 8080

jar_target = 'target/chord-java-REST-1.0-jar-with-dependencies.jar'

chord_size_list = [128, 256, 512]

file = open("results.txt", "a")

for chord_size in chord_size_list:
  for j in range(1,5):
    total_nodes = int(chord_size * j / 10) #was 4
    print("chord size: ", chord_size, " total nodes: ", total_nodes)
    nodes_list = random.sample(range(chord_size), total_nodes)
    present_nodes = []
    
    proc = subprocess.Popen(['java', '-jar', jar_target, '-p', str(PORT), '-m', '-l', str(chord_size)])
    time.sleep(2)
    
    for i,node in enumerate(nodes_list):
    
      if i == 0:
        print("initial node: ", node)
        http_cmd = "http://localhost:%d/%d/create" % (PORT, node)
        print(http_cmd)
        response = requests.get(http_cmd)
        print(response)
    
      else:
        entry_node = random.choice(present_nodes)
#        print("joining node: ", node, " to node: ", entry_node)
        http_cmd = "http://localhost:%d/%d/create" % (PORT, node)
#        print(http_cmd)
        response = requests.get(http_cmd)
#        print("create response: ", response)
        http_cmd = "http://localhost:%d/%d/join/%d" % (PORT, node, entry_node)
#        print(http_cmd)
        response = requests.get(http_cmd)
#        print("join response: ", response)
        
      present_nodes.append(node)
    
    hops = []
    
    for i in range(chord_size):
      entry_node = random.choice(present_nodes)
      #find_suc_node = random.randint(0,chord_size-1)
      query = "http://localhost:%d/%d/find-successor/%d" % (PORT, entry_node, i)
      print("entry node: ", entry_node, "target node: ", i)
      response = requests.get(query)
      resp = response.json()
      path_count = resp['pathCount']
#      print("path count: ", path_count)
      hops.append(path_count)
    
    std_dev_hops = statistics.pstdev(hops) 
    mean_hops = statistics.mean(hops)
    median_hops = statistics.median(hops)
    
    result = "Chord size: %d Node count: %d ---- Mean: %f Median: %f StdDev: %f\n"  % (chord_size, total_nodes, mean_hops, median_hops, std_dev_hops)
    print(result)
    file.write(result)    
    #proc.wait()
    proc.kill()
