import os
import subprocess
import random
import time
import pickle

CHORD_SIZE = 65536
TOTAL_NODES = 1000
QUERY_COUNT = 4

jar_target = 'target/chord-java-REST-1.0-jar-with-dependencies.jar'

nodes_list = random.sample(range(CHORD_SIZE), TOTAL_NODES)
present_nodes = []
child_processes = []

for i,node in enumerate(nodes_list):

  if i == 0:
    # -p 8080 -i 0
    opt = " -p %d -i %d" % (8080+node, node)
    print(opt)
    
# This is blocking:
# subprocess.run(['java', '-jar', jar_target, '-p', str(node+8080), '-i', str(node)])
 
    proc = subprocess.Popen(['java', '-jar', jar_target, '-p', str(node+8080), '-i', str(node)])
    child_processes.append(proc)
    time.sleep(2) # to make sure first node created? 
#    res = subprocess.run(['java', '-jar', jar_target, '-p', str(node+8080), '-i', str(node)])
#    print(proc.returncode)
#    if proc.returncode != 0:
#      print("Failed joining ",node) 
#      break	
  

  else:
    entry_node = random.choice(present_nodes)
    opt = "-p %d -i %d --jh localhost --jp %d --ji %d"   \
          % (8080+node, node, 8080+entry_node, entry_node)	
    print(opt)

    proc = subprocess.Popen(['java', '-jar', jar_target, '-p', str(node+8080), '-i', str(node),        \
                    '--jh', 'localhost', '--jp', str(entry_node+8080), '--ji', str(entry_node)])
    child_processes.append(proc)

  present_nodes.append(node)

with open("live_nodes.txt", "wb") as fp:
  pickle.dump(present_nodes, fp)

for cp in child_processes:
  cp.wait() 
