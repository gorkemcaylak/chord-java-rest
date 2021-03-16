import requests
import pickle
import random

QUERY_COUNT = 4
CHORD_SIZE = 128

with open("live_nodes.txt", "rb") as fp:
  present_nodes = pickle.load(fp)

for i in range(QUERY_COUNT):
  entry_node = random.choice(present_nodes)
  find_suc_node = random.randint(0,CHORD_SIZE-1)
  query = "http://localhost:%d/chord/find-successor/%d" % (entry_node+8080, find_suc_node)
  response = requests.get(query)
  print(response.json)
