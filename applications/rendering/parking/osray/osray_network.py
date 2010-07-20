# -*- coding: utf-8 -*-
# by kay - basic functions


import networkx as nx

class osrayNetwork():

    G = None

    def __init__(self):
        self.G = nx.Graph()

    def add_highway(self,highway_way,layer):
        lastpoint = None
        for point in highway_way:
            self.G.add_node(point)
            #print "adding node:",point
            if(lastpoint!=None):
                self.G.add_edge(lastpoint,point,layer=layer)
                #print "adding edge:",(lastpoint,point,layer)
            lastpoint=point
        #print "G=",self.G.number_of_nodes(),self.G.number_of_edges()

    def calculate_height_on_nodes(self):
        for n in self.G.nodes():
            neighbors = self.G.neighbors(n)
            sum_height = 0.0
            for m in neighbors:
                edge = self.G[n][m]
                #print "Edge(n,m)=",n,m,edge
                sum_height += edge['layer']*5.0
            self.G.node[n]['height'] = sum_height/len(neighbors)
            print "setting node ",n," with height ",sum_height/len(neighbors)

    def get_height(self,n):
        return self.G.node[n]['height']

           