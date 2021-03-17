import os
import subprocess
import random
import time
import pickle
import requests

CHORD_SIZE = 128
TOTAL_NODES = 30
QUERY_COUNT = 4
PORT = 8080

jar_target = 'target/chord-java-REST-1.0-jar-with-dependencies.jar'

nodes_list = random.sample(range(CHORD_SIZE), TOTAL_NODES)
present_nodes = []
child_processes = []

proc = subprocess.Popen(['java', '-jar', jar_target, '-p', str(PORT), '-m'])
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
    opt = "-p %d -i %d --jh localhost --jp %d --ji %d"   \
          % (8080+node, node, 8080+entry_node, entry_node)	
    print(opt)
    
    print("joining node: ", node, " to node: ", entry_node)
    http_cmd = "http://localhost:%d/%d/create" % (PORT, node)
    print(http_cmd)
    response = requests.get(http_cmd)
    print("create response: ", response)
    http_cmd = "http://localhost:%d/%d/join/%d" % (PORT, node, entry_node)
    print(http_cmd)
    response = requests.get(http_cmd)
    print("join response: ", response)
    
  present_nodes.append(node)

for i in range(QUERY_COUNT):
  entry_node = random.choice(present_nodes)
  find_suc_node = random.randint(0,CHORD_SIZE-1)
  query = "http://localhost:%d/%d/find-successor/%d" % (PORT, entry_node, find_suc_node)
  print("entry node: ", entry_node, "target node: ", find_suc_node)
  response = requests.get(query)
  resp = response.json()
  print(resp)

# with open("live_nodes.txt", "wb") as fp:
#   pickle.dump(present_nodes, fp)

proc.wait() 
