# Tâche 3

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
