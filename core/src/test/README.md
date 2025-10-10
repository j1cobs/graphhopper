
## Documentation des tests

### Test 1: testTimeLimitRespected

#### Classe testée
ShortestPathTree

#### Intention
Vérifier que la limite de temps coupe au bon moment.

#### Motivation des données
Petit graphe 0 → 1 → 2, deux arêtes de 1000 m à 50 km/h.
À 50 km/h ≈ 13,89 m/s, parcourir 1000 m prend ≈ 72 000 ms. En fixant timeLimit = 20 000ms, on est bien en-dessous du temps requis pour atteindre le node 1.

#### Oracle
Après search(0, …), la liste des labels ne contient que le node source 0.
Assertion clé : labels.get(0).node == 0 et labels.size() >= 1.


### Test 2: testDistanceLimitRespected

#### Classe testée
ShortestPathTree

#### Intention
Vérifier que la limite de distance borne bien la recherche.

#### Motivation des données
Même chaîne 0 → 1 → 2, arêtes 1000 m à 50 km/h.
On fixe distanceLimit=1500 m : ce qui est suffisant pour atteindre le node 1, mais qui est insuffisant pour atteindre le node 2.

#### Oracle
Après search(0, …), on a exactement 2 labels : node 0 puis 1 ; le node 2 n’est pas atteint.
Assertions : labels.size()==2, labels[0].node==0, labels[1].node==1.

### Test 3: testWeightLimitPrefersFastEdge

#### Classe testée
ShortestPathTree

#### Intention
Vérifier que la limite de poids favorise l’arête la plus rapide lorsqu’il existe deux arêtes parallèles.

#### Motivation des données
Deux arêtes parallèles 0 ↔ 1 de 1000 m chacune :
 — une rapide à 100 km/h → poids ≈ 1000 / 27,78 ≈ 36
 — une lente à 50 km/h → poids ≈ 1000 / 13,89 ≈ 72
On impose weightLimit=50, entre 36 et 72 : la rapide doit passer, mais pas la lente.

#### Oracle
Le label retenu pour le node 1 a un weight ≤ 50, prouvant qu'il a choisi l’arête rapide.
Assertion : labelFor1.weight <= 50.0.


### Test 4: testIsochroneEdges

#### Classe testée
ShortestPathTree

#### Intention
Valider le calcul des arêtes d’isochrone via getIsochroneEdges(). On doit obtenir exactement un.

#### Motivation des données
Chaîne 0 → 1 → 2, 1000 m par arête à 50 km/h.
Avec un seuil z = 1500 m :
- après la première arête, on est à 1000 m
- après la deuxième arête, on passe à 2000 m
Alors une seule arête franchit le seuil.

#### Oracle
getIsochroneEdges(1500) retourne 1 seule arête (taille = 1).
Assertion : edges.size() == 1.

### Test 5: testComparePaths

#### Classe testée
GHUtility

#### Intention
Valider le comportement de la fonction de comparaison de chemin.  On veut s'assurer que la fonction retourne des alertes lorsque les chemins donnés ont des différences significatives comme des différences dans la distance ou les noeuds traversés. 

#### Motivation des données
Le graphe est relativement simple et comporte 5 noeuds et 4 arêtes afin de couvrir différents chemins possibles:
- Un chemin linéaire simple (0→1→2→3)
- Un chemin alternatif sautant par dessus un noeud (0→1→3)
- Des variations dans les distances des chemins afin de s'assurer que les alertes sont bien créées.
Ces chemins nous permettent de nous assurer que la fonction couvre les chemins identiques, légèrement différents et fondamentalement différents (distance).

#### Oracle
L'oracle est en partie le retour de la fonction GHUtility.comparePaths() puisque celle-ci retourne une liste de messages facile à lire pour un humain.
Chaque test vérifie les critères suivants:
- La liste de message est vide (Aucune alerte)
- La liste de message contient des alertes spécifiques selon le scénario

### Test 6: testGetCommonNode

#### Classe testée
GHUtility

#### Intention
S'assurer que la fonction identifie correctement un noeud partagé entre deux arêtes ou crée une exception si les arêtes ne sont pas reliés ou sont invalides.

#### Motivation des données
Le graphe est simple est linéaire grâce à des arêtes consécutives (0→1, 1→2, 2→3, 3→4).  Le test s'assure de vérifier que les tests de base fonctionnent comme anticipé ainsi que quelques scénarios spéciaux comme des arêtes parallèles inverses ou des arêtes ayant une base partagée.

#### Oracle
Il est possible d'identifier si le test fonctionne grâce aux critères suivants:
- Si les arêtes partagent exactement un noeud, on retourne l'identifiant du noeud.
- Si les arêtes créent une paire d'arêtes circulaires, on crée une exception (IllegalArgumentException)

### Test 7: testGetAdjNode

#### Classe testée
GHUtility

#### Intention
Étant donné l'identifiant d'une arête ainsi qu'un référence à un noeud, le test s'assure que la fonction GetAdjNode() retourne correctement le noeud identifié comme adjacent ou l'argument donné comme noeud adjacent par défaut.

#### Motivation des données
Le graphe est encore une fois un graphe linéaire simple (0→1→2→3→4→5) qui nous permet de couvrir:
- Les directions d'arêtes valides
- Les références inversées
- Les identifiants d'arêtes invalides (négative ou NO_EDGE)

#### Oracle
Comportement attendu:
- Pour les arêtes valides, on retourne l'extrémité opposée du noeud donné
- Pour les identifiants d’arêtes invalides, on retourne le noeud d’entrée (aucun noeud adjacent trouvé).

## Pitest
### Classe ShortestPathTree
La couverture de mutation est déjà assez élevé avant l'ajout de test, soit à 82% (36/44).  Après l'ajout des nouveaux tests, la couverture de mutation est restée la même à 82% (36/44) donc les nouveaux tests ne détectent pas de nouveaux mutants.

### Classe GHUtility
La couverture de mutation est très basse avant l'ajout de test, soit à 6% (15/263).  Après l'ajout des nouveaux tests, la couverture de mutation a largement augmentée pour atteindre 18% (48/263).  Les mutants trouvés sont les suivants:

### Mutants
#### getAdjNode():  
- 3 mutants trouvés dans cette fonction
- 1 mutant: Negated conditional sur la condition isValid() et puisque nous avons testé des arêtes invalides, ce mutant est éliminé
- 2 mutants: Remplacement du retour de la méthode par 0 et puisque nous avons testé des valeurs de retours n'étant pas 0, les mutants sont éliminés.

#### getCommonNode():  
- 6 mutants trouvés dans cette fonction
- 4 mutants: Negated conditional sur les conditions e1.getBaseNode() == e1.getAdjNode(), e2.getBaseNode() == e2.getAdjNode() et e1.getBaseNode() == e2.getBaseNode() || e1.getBaseNode() == e2.getAdjNode().  Notre test élimine ces mutants étant donné que nous avons testé plusieurs cas dont des arêtes circulaires, des arêtes ayant un noeud à la base en commun, des arêtes ayant un noeud adjacent en commun ou des arêtes ayant des noeud de base et adjacent.
- 2 mutants: Remplacement du retour de la méthode par 0 et puisque nous avons testé des valeurs de retours n'étant pas 0, les mutants sont éliminés.

#### comparePaths():  
- 4 mutants trouvés dans cette fonction
- 3 mutants: Negated conditional sur les conditions !refNodes.equals(pathNodes), path.getGraph() != refPath.getGraph() et !pathsEqualExceptOneEdge(path.getGraph(), refNodes, pathNodes).  Notre test élimine ces mutants étant donné que nous avons testé le cas où les noeuds des deux chemins sont les mêmes et également lorsqu'ils sont différents.  Nous avons testé des scénarios où les graphes étaient les mêmes.  
- 1 mutant: Remplacement du retour de la méthode par une liste vide et puisque nous testons la taille et le contenu de la liste, notre test élimine ce mutant.    


Les autres mutants éliminés sont probablement créés par des utilisations de fonction autre dans les fonctions pour lesquelles nous avons ajoutés des tests.

## Java Faker
Le test ajouté afin d'intégrer java-faker est testGetAdjNodeWithRandomInvalidEdges() pour la classe GHUtility.  Ce test nous permet de tester différentes valeurs arêtes invalides afind de s'assurer que peu importe la valeur de l'identifiant de l'arête utilisée, elle sera invalide étant donné qu'on prend une valeur entre -1000 et -1.  De plus, nous testons ce comportement à 5 reprises pour n'importe quel des 3 noeuds créés tirés au hasard grâce à faker encore une fois.  Les fakers utilisés nous permettent donc de tirer un noeud au hasard et de créer un identifiant d'arête invalide de façon aléatoire.

## Tableau des différentes métriques observées avant et après l'ajout de tests

### Tableau des valeurs de couverture pour la classe ShortestPathTree
|   | Avant ajout de tests | Après ajout de tests |
| - | :--------------------: | :--------------------: |
| **Couverture de ligne (fraction)** | 73 / 80 | 78 / 80 |
| **Couverture de ligne (%)** | 91 | 98 |
| **Couverture de mutation (fraction)** | 36 / 44 | 36 / 44 |
| **Couverture de mutation (%)** | 82 | 82 |

### Tableau des valeurs de couverture pour la classe GHUtility
|   | Avant ajout de tests | Après ajout de tests |
| - | :--------------------: | :--------------------: |
| **Couverture de ligne (fraction)** | 25 / 355 | 92 / 355 |
| **Couverture de ligne (%)** | 7 | 26 |
| **Couverture de mutation (fraction)** | 17 / 263 | 48 / 263 |
| **Couverture de mutation (%)** | 6 | 18 |