# Architecture et Fonctionnement de l'Algorithme d'Expédition

Ce document décrit en détail le fonctionnement interne du moteur d'optimisation des expéditions (Shipping Optimizer), les paradigmes mathématiques exploités, ainsi que l'analyse de sa complexité algorithmique.

## Vue d'Ensemble
L'algorithme tente d'optimiser l'allocation d'une série de `Produits` (issus de différentes `Commandes`) à travers un ensemble fini de `Conteneurs` ayant des contraintes strictes en matière de poids maximal et de volume maximal. Il propose deux stratégies principales pour résoudre ce problème :
1. **Remplissage Maximum (Max Fill) :** Conçu pour regrouper les commandes et maximiser le ratio d'utilisation de l'espace (emballer le plus grand nombre de produits possibles).
2. **Valeur Maximale (Max Value) :** Conçu pour générer le chiffre d'affaires (revenue) absolu le plus élevé possible par cm³/kg.

---

## Les Étapes de l'Algorithme

L'optimisation globale repose sur une approche heuristique gloutonne ("Greedy Approach") combinée à une recherche locale adaptative, découpée en 6 phases majeures :

### Étape 0 : Prétraitement et Tri Stratégique
La première étape sépare les produits prioritaires (commandes de `priorité >= 4`) des produits normaux.
- En **Max Fill** : L'algorithme garde les produits d'une même commande *groupés*. Les commandes sont triées par densité (le nombre d'articles divisé par le ratio combiné de taille [poids + volume]). Cela évite la fragmentation des commandes et favorise les petits articles compacts.
- En **Max Value** : L'algorithme *brise complètement le groupement par commande*. Les produits sont évalués individuellement comme des atomes isolés selon une "Densité de Prix" (`Prix / Espace Combiné`). Les objets dégageant la plus haute rentabilité au centimètre carré sont catapultés au début de la file d'attente.

### Étape 1 : Placement des Produits Prioritaires
L'algorithme parcourt les produits prioritaires et les associe de manière agressive au premier conteneur disposant d'assez d'espace (poids et volume). S'ils ne rentrent dans aucun conteneur, ils sont mis en liste d'attente. 

### Étapes 2 & 3 : Remplissage Glouton (Greedy Fitting)
C'est le cœur du système. Chaque produit "normal" est testé virtuellement dans *tous* les conteneurs capables de l'accueillir.
Pour chaque combinaison `(Produit, Conteneur)`, une fonction de "Scoring" est calculée sur base de 3 facteurs :
1. **Contribution au remplissage (Weight/Fill Score) :** Quel pourcentage de l'espace restant l'objet comble-t-il ?
2. **Densité du prix (Price Density Score) :** Quelle est la valeur de l'objet par rapport au ratio de densité mondial du 90e centile ?
3. **Pénalité de perte (Waste Score) :** L'objet créé-t-il un "vide" mathématiquement inexploitable dans le conteneur ?

Le produit est injecté dans le conteneur ayant généré le "Score" absolu le plus élevé.

### Étape 4 : Évaluation Strict des Seuils (Threshold Evaluation)
L'algorithme filtre tous les conteneurs mathématiquement simulés. Si le taux de remplissage final d'un conteneur est inférieur au `fillThreshold` stipulé (Ex: 80% requis mais seulement 60% atteint), le conteneur obtient dynamiquement le statut `POSTPONED` (Reporté). Ni le conteneur, ni le chargement ne sont détruits, ce qui permet à l'utilisateur de les forcer manuellement depuis l'interface via une alerte.

### Étape 5 : Recherche Locale et Compression (Local Search / 2-Opt)
Pour raffiner le résultat du modèle glouton, l'algorithme lance un lissage post-processing avec un plafond de 50 itérations ("Local Search") :
- **Insertion :** Il scanne la liste des produits rejetés (`waiting`) pour voir si quelques écarts millimétriques dans les conteneurs validés peuvent être comblés.
- **2-Opt Swap :** Il échange paires par paires des produits distincts entre différents conteneurs `SHIPPED` pour voir si un échange (swap) permet brutalement d'augmenter le taux de remplissage net, resserrant la "compression" générale des marchandises.

---

## Le Problème Classique du 0-1 Knapsack (Sac à Dos)

### L'Écueil Mathématique
L'application doit résoudre une variante multidimensionnelle du célèbre **Problème du sac à dos (Knapsack Problem)**. Plus précisément, il s'agit d'un "0-1 Knapsack" : un produit est soit mis dans le conteneur (1), soit il ne l'est pas (0). On ne peut pas "scinder/casser" une télévision en deux pour combler un vide comme on le ferait avec un liquide (Fractional Knapsack).

Dans un algorithme *Greedy* visant le `Max Value`, le tri priorise instinctivement les objets luxueux et massifs pour récupérer de la valeur pure. **Le Bug du 0-1 :** Un équipement dense et ultra-cher pourrait remplir 95% d'un gros conteneur. Les 5% d'espace restants deviennent un "Gap Mort". En revanche, en mode `Max Fill`, l'algorithme remplit 100% de ce même camion avec des milliers de toutes petites pièces bon marché. Paradoxalement, la somme de ces milliers de pièces finit parfois par dépasser largement la valeur unique de l'objet luxueux !

### L'Architecture d'Adaptation (La Solution)
Pour pallier ce goulet d'étranglement NP-difficile : l'algorithme applique une course compétitive (Racing Pattern). 
Lorsque vous appuyez sur le bouton **MAX VALUE**, le ViewModel lance simultanément et secrètement l'algorithme **Max Value** ET l'algorithme **Max Fill**. Les deux approches sont exécutées et compilées mathématiquement en background. L'API les compare, garantissant de renvoyer toujours à l'utilisateur celle des deux instances qui a généré le `totalRevenue` le plus élevé physiquement possible.

---

## Analyse de la Complexité Algorithmique

L'algorithme effectue des opérations lourdes en mémoire compensées par l'échelle d'Android :
Soient :
- $N$ le nombre total de produits en file d'attente (pending).
- $C$ le nombre total de conteneurs disponibles.
- $P$ la limite approximative du nombre de produits emballables dans un conteneur.

1. **Tri (Step 0) :** L'évaluation de la liste et le triage via l'algorithme standard interne de Kotlin implique une complexité en temps de **$O(N \log N)$**.
2. **Affectation Gloutonne (Step 1-3) :** Chaque produit iteratif calcule une condition de collision "Fits" et un modèle parabolique "Score" pour chaque conteneur.
Temps estimé : **$O(N \times C)$**.
3. **Passe d'Évaluation (Step 4) :** Parcourt simplement les conteneurs. Temps de complexité ultra mineur : **$O(C)$**.
4. **Recherche Locale (Step 5) :** C'est le bloc potentiellement le plus exponentiel. 
   - L'insertion en scan coûte $O(\text{Attente} \times C)$.
   - Le *2-Opt Swap* superpose deux boucles pour les conteneurs et deux boucles sur les produits à l'intérieur de ces conteneurs. Dans le pire des cas limités par $50$ itérations max : complexité bornée proportionnelle à $O(C^2 \times P^2)$.

### Synthèse Globale
- **Complexité Temporelle (Time Complexity) :** **$O(N \log N + N \times C + C^2 \times P^2)$**. À une échelle commerciale de 10-20 conteneurs et de 500 objets commandés, les constantes garantissent des temps d'exécution transparents pour l'utilisateur allant de 5 à 40 millisecondes maximum, sans aucun lags de la thread UI principale grâce aux Coroutines (`Dispatchers.IO`).
- **Complexité Spatiale (Space Complexity) :** L'algorithme a une empreinte mémoire de **$O(N + C)$**. Les mutations de liste s'effectuent sans deep copies invasives via des ré-allocations de pointeurs mémoires `MutableList`, ce qui évite toutes saturations du Garbage Collector de l'Application Android.
