package com.graphhopper.util;

import com.graphhopper.ResponsePath;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PathMergerTest {

    @Mock
    private Graph graph;

    @Mock
    private Weighting weighting;

    @Mock
    private Translation translation;

    private PathMerger pathMerger;

    @BeforeEach
    void setUp() {

        when(graph.wrapWeighting(weighting)).thenReturn(weighting);


        pathMerger = new PathMerger(graph, weighting)
                .setEnableInstructions(false)
                .setSimplifyResponse(false);
    }

    @Test
    void mergesTwoPaths() {

        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);

        List<Path> paths = Arrays.asList(path1, path2);

        // Les deux chemins sont trouvés
        when(path1.isFound()).thenReturn(true);
        when(path2.isFound()).thenReturn(true);

        // Temps simulés
        when(path1.getTime()).thenReturn(1000L);
        when(path2.getTime()).thenReturn(2000L);

        // Distances simulées
        when(path1.getDistance()).thenReturn(10.0);
        when(path2.getDistance()).thenReturn(20.0);

        // Poids simulés
        when(path1.getWeight()).thenReturn(1.5);
        when(path2.getWeight()).thenReturn(2.5);

        // Descriptions simulées
        when(path1.getDescription()).thenReturn(Collections.singletonList("p1"));
        when(path2.getDescription()).thenReturn(Collections.singletonList("p2"));

        // Path1
        PointList p1 = new PointList();
        p1.add(45.0, -73.0);   // A
        p1.add(45.1, -73.1);   // B

        // Path2
        PointList p2 = new PointList();
        p2.add(45.1, -73.1);   // B
        p2.add(45.2, -73.2);   // C

        when(path1.calcPoints()).thenReturn(p1);
        when(path2.calcPoints()).thenReturn(p2);

        PointList waypoints = new PointList();

        ResponsePath response = pathMerger.doWork(waypoints, paths, null, translation);

        assertEquals(3000L, response.getTime(), "Le temps doit être la somme des temps des paths");
        assertEquals(30.0, response.getDistance(), 1e-6, "La distance doit être la somme des distances des paths");
        assertEquals(4.0, response.getRouteWeight(), 1e-6, "Le poids doit être la somme des poids des paths");

        // Aucun chemin manquant = pas d'erreur
        assertTrue(response.getErrors().isEmpty(), "Aucune erreur ne doit être présente");

        // Points fusionnés: A, B, C car le B du path1 est retiré
        PointList mergedPoints = response.getPoints();
        assertEquals(3, mergedPoints.size(), "Les points fusionnés doivent contenir 3 points");

        verify(path1).calcPoints();
        verify(path2).calcPoints();
    }
}
