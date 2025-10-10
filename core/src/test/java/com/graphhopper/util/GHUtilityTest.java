/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIndexedContainer;
import com.graphhopper.coll.GHIntLongHashMap;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.SpeedWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */
public class GHUtilityTest {

    @Test
    public void testEdgeStuff() {
        assertEquals(2, GHUtility.createEdgeKey(1, false));
        assertEquals(3, GHUtility.createEdgeKey(1, true));
    }

    @Test
    public void testZeroValue() {
        GHIntLongHashMap map1 = new GHIntLongHashMap();
        assertFalse(map1.containsKey(0));
        // assertFalse(map1.containsValue(0));
        map1.put(0, 3);
        map1.put(1, 0);
        map1.put(2, 1);

        // assertTrue(map1.containsValue(0));
        assertEquals(3, map1.get(0));
        assertEquals(0, map1.get(1));
        assertEquals(1, map1.get(2));

        // instead of assertEquals(-1, map1.get(3)); with hppc we have to check before:
        assertTrue(map1.containsKey(0));

        // trove4j behaviour was to return -1 if non existing:
//        TIntLongHashMap map2 = new TIntLongHashMap(100, 0.7f, -1, -1);
//        assertFalse(map2.containsKey(0));
//        assertFalse(map2.containsValue(0));
//        map2.add(0, 3);
//        map2.add(1, 0);
//        map2.add(2, 1);
//        assertTrue(map2.containsKey(0));
//        assertTrue(map2.containsValue(0));
//        assertEquals(3, map2.get(0));
//        assertEquals(0, map2.get(1));
//        assertEquals(1, map2.get(2));
//        assertEquals(-1, map2.get(3));
    }

    @Test
    public void testComparePaths() {
        // Create graph and encoding
        BooleanEncodedValue accessEnc = new SimpleBooleanEncodedValue("access", true);
        DecimalEncodedValue speedEnc = new DecimalEncodedValueImpl("speed", 5, 5, false);
        EncodingManager em = EncodingManager.start().add(accessEnc).add(speedEnc).build();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        
        // Setup nodes
        graph.getNodeAccess().setNode(0, 0.0, 0.0);
        graph.getNodeAccess().setNode(1, 0.1, 0.1);
        graph.getNodeAccess().setNode(2, 0.2, 0.2);
        graph.getNodeAccess().setNode(3, 0.3, 0.3);
        graph.getNodeAccess().setNode(4, 0.4, 0.4);
        
        // Create edges with speeds and access
        EdgeIteratorState edge0 = graph.edge(0, 1).setDistance(1000);
        GHUtility.setSpeed(50, 50, accessEnc, speedEnc, edge0);
        
        EdgeIteratorState edge1 = graph.edge(1, 2).setDistance(1000);
        GHUtility.setSpeed(50, 50, accessEnc, speedEnc, edge1);
        
        EdgeIteratorState edge2 = graph.edge(2, 3).setDistance(1000);
        GHUtility.setSpeed(50, 50, accessEnc, speedEnc, edge2);
        
        // Create an alternative edge for testing different paths
        EdgeIteratorState edge3 = graph.edge(1, 3).setDistance(1500);
        GHUtility.setSpeed(50, 50, accessEnc, speedEnc, edge3);
        
        Weighting weighting = new SpeedWeighting(speedEnc);
        
        // 1: Identical paths
        Path path1 = new Path(graph);
        path1.setFromNode(0);
        path1.setWeight(100.0);
        path1.setDistance(3000.0);
        path1.setTime(72000); // 72 seconds
        path1.addEdge(0);
        path1.addEdge(1);
        path1.addEdge(2);
        path1.setFound(true);
        
        Path path2 = new Path(graph);
        path2.setFromNode(0);
        path2.setWeight(100.0);
        path2.setDistance(3000.0);
        path2.setTime(72000);
        path2.addEdge(0);
        path2.addEdge(1);
        path2.addEdge(2);
        path2.setFound(true);
        
        List<String> violations = GHUtility.comparePaths(path1, path2, 0, 3, 12345L);
        assertEquals(0, violations.size(), "Identical paths should have no violations");
        
        // 2: Different distance
        Path path4 = new Path(graph);
        path4.setFromNode(0);
        path4.setWeight(100.0);
        path4.setDistance(3200.0); // Different distance > 1.e-1
        path4.setTime(72000);
        path4.addEdge(0);
        path4.addEdge(1);
        path4.addEdge(2);
        path4.setFound(true);
        
        violations = GHUtility.comparePaths(path1, path4, 0, 3, 12345L);
        assertEquals(1, violations.size(), "Different distance should create 1 violation");
        assertTrue(violations.get(0).contains("wrong distance"), "Violation should mention distance");
                
        // 3: Different nodes
        Path path6 = new Path(graph);
        path6.setFromNode(0);
        path6.setWeight(100.0);
        path6.setDistance(2500.0);  // Different total distance
        path6.setTime(72000);
        path6.addEdge(0);  // Edge 0: 0->1
        path6.addEdge(3);  // Edge 3: 1->3 (skips node 2)
        path6.setFound(true);
        
        violations = GHUtility.comparePaths(path1, path6, 0, 3, 12345L);
        assertTrue(violations.size() >= 1, "Different nodes should create at least 1 violation");
        boolean hasNodeViolation = violations.stream().anyMatch(v -> v.contains("wrong nodes"));
        assertTrue(hasNodeViolation, "Should have a node violation");
        
    }

    @Test
    public void testGetCommonNode() {
        BaseGraph graph = new BaseGraph.Builder(1).create();
        
        // Create nodes
        for (int i = 0; i < 15; i++) {
            graph.getNodeAccess().setNode(i, i * 0.01, i * 0.01);
        }
        
        // Create edges for testing
        graph.edge(0, 1).setDistance(10); 
        graph.edge(1, 2).setDistance(10); 
        graph.edge(2, 3).setDistance(10); 
        graph.edge(3, 4).setDistance(10); 
        
        // 1: Edges share node at base-adj
        assertEquals(1, GHUtility.getCommonNode(graph, 0, 1), 
            "Edges 0(0->1) and 1(1->2) should share node 1");
        
        // 2: Edges share node at adj-adj
        assertEquals(2, GHUtility.getCommonNode(graph, 1, 2),
            "Edges 1(1->2) and 2(2->3) should share node 2");
        
        //3: Edges form a circle (parallel edges with same nodes)
        graph.edge(6, 7).setDistance(10); // edge 4
        graph.edge(7, 6).setDistance(10); // edge 5 - reverse of edge 4
        assertThrows(IllegalArgumentException.class, () -> 
            GHUtility.getCommonNode(graph, 4, 5),
            "Circular edges should throw IllegalArgumentException");
        
        // 4: Edges connected at base-base
        graph.edge(12, 13).setDistance(10); // edge 6
        graph.edge(12, 14).setDistance(10); // edge 7
        assertEquals(12, GHUtility.getCommonNode(graph, 6, 7),
            "Edges should share node 12 at base-base");
        
        // 5: Edges connected at adj-base  
        // Edge 2 = 2->3, edge 3 = 3->4,  share node 3
        assertEquals(3, GHUtility.getCommonNode(graph, 2, 3),
            "Edges 2(2->3) and 3(3->4) should share node 3 at adj-base");

    }

    @Test
    public void testGetAdjNode() {
        BooleanEncodedValue accessEnc = new SimpleBooleanEncodedValue("access", true);
        com.graphhopper.routing.ev.EncodedValue.InitializerConfig evConf = new com.graphhopper.routing.ev.EncodedValue.InitializerConfig();
        accessEnc.init(evConf);
        BaseGraph graph = new BaseGraph.Builder(evConf.getRequiredBytes()).create();
        
        // Create nodes
        for (int i = 0; i < 10; i++) {
            graph.getNodeAccess().setNode(i, i * 0.01, i * 0.01);
        }
        
        graph.edge(0, 1).setDistance(10).set(accessEnc, true, true); // edge 0: 0->1
        graph.edge(1, 2).setDistance(10).set(accessEnc, true, true); // edge 1: 1->2
        graph.edge(2, 3).setDistance(10).set(accessEnc, true, true); // edge 2: 2->3
        graph.edge(3, 4).setDistance(10).set(accessEnc, true, true); // edge 3: 3->4
        graph.edge(4, 5).setDistance(10).set(accessEnc, true, true); // edge 4: 4->5
        
        // 1: Edge 0 (0->1), adjNode=1, returns 1
        assertEquals(1, GHUtility.getAdjNode(graph, 0, 1),
            "Edge 0 with adjNode=1 should return node 1");
        
        // 2: Edge 0 (0->1), adjNode=0, returns 0
        assertEquals(0, GHUtility.getAdjNode(graph, 0, 0),
            "Edge 0 with adjNode=0 should return node 0");
        
        // 3: Invalid edge (negative), return adjNode parameter
        assertEquals(5, GHUtility.getAdjNode(graph, -1, 5),
            "Invalid edge -1 should return adjNode parameter 5");
        
        // 4: Invalid edge (NO_EDGE constant), return adjNode parameter
        assertEquals(10, GHUtility.getAdjNode(graph, EdgeIterator.NO_EDGE, 10),
            "NO_EDGE should return adjNode parameter 10");
    }

    @Test
    public void testGetAdjNodeWithRandomInvalidEdges() {
        Faker faker = new Faker();
        BaseGraph graph = new BaseGraph.Builder(1).create();
        
        // Create a simple graph with just 3 nodes
        graph.getNodeAccess().setNode(0, 0.0, 0.0);
        graph.getNodeAccess().setNode(1, 0.1, 0.1);
        graph.getNodeAccess().setNode(2, 0.2, 0.2);
        
        // Test with random invalid edge IDs
        for (int i = 0; i < 5; i++) {
            int invalidEdgeId = faker.number().numberBetween(-1000, -1);
            int randomNodeId = faker.number().numberBetween(0, 3);
            
            assertEquals(randomNodeId, GHUtility.getAdjNode(graph, invalidEdgeId, randomNodeId),
                "Invalid edge " + invalidEdgeId + " should return adjNode parameter " + randomNodeId);
        }
    }
}
