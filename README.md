# Android Order Management

Cette application Android, developpee avec Kotlin et Jetpack Compose, permet aux entreprises de gerer leurs commandes, leurs employes, leurs conteneurs et leurs plans d'expedition. L'application prend en charge trois types d'utilisateurs : les administrateurs, les entreprises et les employes

---

## Lancement du projet
### Prerequis

- Android Studio (version Hedgehog ou ulterieure recommandee)
- JDK 17 ou superieur
- Un emulateur Android ou un appareil physique sous Android 8.0 (API 26) ou superieur

### Etapes
1. Cloner le depot :
   ```
   git clone https://github.com/Ayoubzaanouni/android-order-management.git
   ```
2. Ouvrir le projet dans Android Studio :
   - Aller dans **File > Open** et selectionner le dossier du projet clone.
3. Laisser Gradle synchroniser automatiquement. Si cela ne demarre pas, cliquer sur **File > Sync Project with Gradle Files**.
4. Lancer l'application :
   - Selectionner l'appareil cible (emulateur ou appareil physique) dans la liste deroulante.
   - Cliquer sur le bouton **Run** (triangle vert) ou appuyer sur `Shift + F10`.
---

## Note importante : Validation des comptes entreprise
Lorsqu'une nouvelle entreprise cree un compte via l'ecran d'inscription, ce compte est cree avec le statut **PENDING** (en attente) et ne peut pas etre utilise tant qu'il n'a pas ete approuve par un administrateur.

Pour valider un compte entreprise, il faut se connecter en tant qu'administrateur avec les identifiants suivants :

- **Identifiant :** `admin`
- **Mot de passe :** `admin123`
Une fois connecte, rendez-vous dans le tableau de bord administrateur pour consulter et approuver ou rejeter les demandes d'inscription en attente.
