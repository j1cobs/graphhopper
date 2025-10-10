
## Documentation des tests

#### Test 1 testTimeLimitRespected

**Classe testée**
ShortestPathTree

**Intention**
Vérifier que la limite de temps coupe au bon moment.

**Motivation des données**
Petit graphe 0 → 1 → 2, deux arêtes de 1000 m à 50 km/h.
À 50 km/h ≈ 13,89 m/s, parcourir 1000 m prend ≈ 72 000 ms. En fixant timeLimit = 20 000ms, on est bien en-dessous du temps requis pour atteindre le node 1.

**Oracle**
Après search(0, …), la liste des labels ne contient que le node source 0.
Assertion clé : labels.get(0).node == 0 et labels.size() >= 1.


#### Test 2 testDistanceLimitRespected

**Classe testée**
ShortestPathTree

**Intention**
Vérifier que la limite de distance borne bien la recherche.

**Motivation des données**
Même chaîne 0 → 1 → 2, arêtes 1000 m à 50 km/h.
On fixe distanceLimit=1500 m : ce qui est suffisant pour atteindre le node 1, mais qui est insuffisant pour atteindre le node 2.

**Oracle**
Après search(0, …), on a exactement 2 labels : node 0 puis 1 ; le node 2 n’est pas atteint.
Assertions : labels.size()==2, labels[0].node==0, labels[1].node==1.

#### Test 3 testWeightLimitPrefersFastEdge

**Classe testée**
ShortestPathTree

**Intention**
Vérifier que la limite de poids favorise l’arête la plus rapide lorsqu’il existe deux arêtes parallèles.

**Motivation des données**
Deux arêtes parallèles 0 ↔ 1 de 1000 m chacune :
 — une rapide à 100 km/h → poids ≈ 1000 / 27,78 ≈ 36
 — une lente à 50 km/h → poids ≈ 1000 / 13,89 ≈ 72
On impose weightLimit=50, entre 36 et 72 : la rapide doit passer, mais pas la lente.

**Oracle**
Le label retenu pour le node 1 a un weight ≤ 50, prouvant qu'il a choisi l’arête rapide.
Assertion : labelFor1.weight <= 50.0.


#### Test 4 testIsochroneEdges

**Classe testée**
ShortestPathTree

**Intention**
Valider le calcul des arêtes d’isochrone via getIsochroneEdges(). On doit obtenir exactement un.

**Motivation des données**
Chaîne 0 → 1 → 2, 1000 m par arête à 50 km/h.
Avec un seuil z = 1500 m :
- après la première arête, on est à 1000 m
- après la deuxième arête, on passe à 2000 m
Alors une seule arête franchit le seuil.

**Oracle**
getIsochroneEdges(1500) retourne 1 seule arête (taille = 1).
Assertion : edges.size() == 1.


## Pitest

Après ajout des nouveaux tests, la couverture de lignes est montée de 91% à 97%, mais la couverture de mutation est restée stable à 82%. Cela indique que les nouveaux tests exécutent davantage de code, mais ne détectent pas de nouveaux mutants.

## Java Faker
