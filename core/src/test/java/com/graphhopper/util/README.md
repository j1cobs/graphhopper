# Tâche 3

## Workflow Github Actions

### Choix de conception et d'implémentation
Nous avons choisi de modifier le fichier build.yml directement étant donné que celui-ci était déjà déclenché à chaque push.  Nous avons ensuite choisi de ne presque rien toucher de l'implémentation déjà présente afin de ne pas perdre de richesse dans le CI.  Cependant, nous avons ajouté la version 21 de java étant donné que toutes les dépendances fonctionnaient avec cette version et ne fonctionnaient pas toujours avec les versions 24 ou 25.  Cette décision fût prise suite à plusieurs heures de déboggage à essayer de trouver où sont les problèmes de dépendances.  Pour la suite, nous avons ajouté un total de 5 pas d'exécution faisant chacun une action relativement précise sur java 21.

1. Le premier pas nous permet d'exécuter l'analyse de mutation grâce à Pitest afin d'obtenir un score pour les nouveaux changements apportés lors du push.  Nous avons également décidé d'exécuter cette analyse seulement sur certaines composantes de la classe "core" de Graphhopper que nous avions utilisé pour la tâche 2 étant donné l'ampleur du projet et le fait que nous avons majoritairement travaillé sur cette classe.  L'exécution de l'analyse de mutation pour toutes les classes de ce projet prend un temps énorme et rend le CI beacoup moins efficace.  

2. La deuxième étape consiste à extraire le score de mutation pour la classe core.  Pour ce faire, nous utilisons l'opération grep sur le fichier core/target/pit-reports/index.html, dont nous avons vérifié l'existence auparavant, en but de bien cibler la case du tableau contenant le score de mutation.  Cette valeur est recueillie et mise en cache Git afin de la réutiliser dans les pas d'exécution suivants.

3. Le troisième pas d'exécution est l'étape de comparaison des scores de mutations.  D'abord, nous recueillons le score de mutation actuel dans la cache ainsi que le score de mutation du push précédent situé dans le fichier .github/mutation_coverage.txt.  Si ce fichier n'existe pas, on attribue la valeur 0 au score de mutation du push précédent afin de prévenir une erreur lors de la première exécution du workflow.  La dernière partie consiste à comparer les deux valeurs et de vérifier si le score actuel est plus petit que le score précédent, s'il l'est, le programme build.yml termine avec une valeur de 1 afin de signifier une erreur.

4. Le quatrième pas d'exécution permet de changer le score précédent dans le fichier .github/mutation_coverage.txt pour le score actuel.  Cette étape est seulement exécuté lorsque l'étape précedente a réussie son exécution, une condition cruciale afin de ne pas changer le score précédent pour un score plus bas.  On veut garder le score le plus haut.  Lorsque les conditions sont satisfaites, on efface le score précédent et le remplace par le score actuel puisqu'on n'a pas besoin de garder un historique des scores de mutations.

5. La dernière étape consiste à pousser les changements sur Github d'une façon relativement formattée afin de pouvoir suivre les changements de manière claire.  Cette étape est elle aussi bloquée par la condition de succès des étapes antérieures.  Lorsque le fichier n'a pas modifié, cette étape écrit un message disant qu'aucun changement ne doit être poussé.

### Validation de ce changement

Nous avons utilisé plusieurs techniques pour s'assurer que nos changements soient fonctionnels.  D'abord, lors de la modification du fichier yml, nous avons testé les commandes qui étaient possibles d'être appelées individuellement directement dans le terminal.  Les commandes comme l'exécution de l'analyse de mutation, le grep nous permettant de recueillir le score de mutation dans le fichier html ou encore la comparaison des scores.  Ensuite, nous avons créé une branche "test" sur laquelle nous avons fait plusieurs push afin de s'assurer que le script s'exécutait au complet sans erreur.  C'est d'ailleurs lors de cette étape que nous nous sommes rendu compte que la version 24 de java nous créait des problèmes de dépendances.  Finalement, nous avons pu tester le comportement de notre changement en commentant l'entièreté du fichier ShortestPathTreeTest.java qui réduit le score de mutation.  Voici le log de l'étape "Compare with previous score":

```
Previous mutation score: 27%
Current mutation score: 12%
Mutation score decreased
Error: Process completed with exit code 1.
```

Ainsi, ce test nous assure que notre ajout au Github Actions permet de détecter une réduction du score de mutation.  Nous avons également pu tester le cas lors duquel le score reste stable et nous avons pu remarquer que le workflow termine sans problèmes.  Le dernier cas testé est celui lorsque le score de mutation augmente que nous avons exécuté en modifiant directement le score de mutation dans le fichier .github/mutation_coverage.txt par une valeur inférieur à la valeur actuelle.

```
Previous mutation score: 25%
Current mutation score: 27%
Mutation score maintained or improved
```

## PathMerger avec Mockito

### Choix de la classe testée
PathMerger est responsable de fusionner plusieurs Path en un seul. Elle dépend de plusieurs autres classes, ce qui en fait un bon choix pour les tests Mockito. Le test ajouté vérifie que PathMerger additionne correctement temps et distance, concatène les points GPS et traite correctement plusieurs chemins trouvés.

### Choix des classes simulées
- Graph et Weighting : on ne veut pas construire un vrai graphe, on simule donc ces dépendances pour pouvoir instancier PathMerger.

- Path : on contrôle complètement les valeurs de temps, distance, poids, description des chemins à fusionner.

- Translation : pour créer l'InstructionList.

### Choix des valeurs simulées :
- Temps : 1000 ms et 2000 ms

- Distance : 10.0 m et 20.0 m

- Poids : 1.5 et 2.5

- Points : path1 = (A,B), path2 = (B,C) pour vérifier que PathMerger supprime bien le point B dupliqué entre les deux segments.

Ce sont tous des valeurs simple qui facilite la compréhension et le calcul mental.

## TranslationMap avec Mockito

### Choix de la classe testée:
TranslationMap est responsable de fournir une traduction dépendamment du Locale. Elle est bien pour les Mock puiqu'elle dépend de Translation qui est facilement simulable. Les tests ajoutés s'assure que si le Locale est français, cela retourne la traduction en français et que si la traduction n'existe pas, le retour est automatiquement en anglais.

### Choix des classes simulées:
- Translation (français)
- Translation (anglais)

### Choix des valeurs simulées :
- Locale.ENGLISH
- Locale.FRENCH
