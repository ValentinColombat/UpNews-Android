# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Le projet

UpNews : une news positive par jour. Lecture + audio, gamification (XP, niveaux, streaks, compagnons animaux), abonnement premium. App Android native, publiée sur le Play Store.

Le code est un **port terminé de l'app iOS** — les commentaires `// MARK: -` et les singletons `.shared` viennent de là. Ne pas les « corriger » : c'est la convention du projet.

## Prérequis avant de compiler

Deux fichiers de secrets sont exclus de git. **Sans eux, rien ne compile** :

```bash
cp SupabaseSecrets.example.kt app/src/main/java/com/valentincolombat/upnews/data/remote/SupabaseSecrets.kt
cp GoogleSecrets.example.kt  app/src/main/java/com/valentincolombat/upnews/service/GoogleSecrets.kt
```

`keystore.properties` (signature release) est également hors git. S'il est absent, le build release retombe sur la clé debug — l'`.aab` produit est alors **inutilisable pour le Play Store**, sans erreur visible.

## Commandes

Il n'y a pas de `java` dans le PATH sur ce poste. Toute commande Gradle doit être préfixée :

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

| But | Commande |
|---|---|
| Vérification rapide (~10 s) | `./gradlew :app:compileReleaseKotlin` |
| Bundle Play Store (~3-13 min, R8) | `./gradlew :app:bundleRelease` |
| APK debug | `./gradlew :app:assembleDebug` |
| Tests unitaires | `./gradlew :app:testDebugUnitTest` |
| Un seul test | `./gradlew :app:testDebugUnitTest --tests "*NomDuTest*"` |
| Lint | `./gradlew :app:lintDebug` |

Le bundle sort dans `app/build/outputs/bundle/release/app-release.aab` — dossier effacé par tout `clean`.

**`compileReleaseKotlin` avant `bundleRelease`** : R8 prend plusieurs minutes, autant attraper les erreurs de compilation en 10 secondes.

## Architecture

### Le routeur d'écran, pas un NavController

`AppStateService` (singleton) expose un `StateFlow<AppScreen>` qui pilote **tout le premier niveau de navigation**. `AppContent.kt` est un simple `when` sur cet état :

```
LOADING → ONBOARDING → AUTH → COMPANION_SELECTION → CATEGORY_SELECTION → MAIN
                                                                      ↘ ERROR
```

Pour changer le flux de démarrage (auth, onboarding, écran d'erreur), c'est **`AppStateService` qu'il faut modifier**, pas une navigation Compose. La navigation par onglets à l'intérieur de `MAIN` est gérée séparément dans `ui/navigation/MainTabView.kt`.

`AppStateService.refreshIfActive()` est rappelé à chaque retour en foreground : il revalide la session Supabase **et** l'abonnement Play.

### Pas d'injection de dépendances

Les repositories et services sont des singletons exposés par `.shared` (`UserRepository.shared`, `ArticleRepository.shared`, `AppStateService.shared`…). `BillingManager` et `SupabaseClientProvider` demandent en plus un `init(application)` explicite, fait dans `UpNewsApplication.onCreate()`.

Conséquence : **l'ordre d'initialisation dans `UpNewsApplication` compte**, et les ViewModels lisent ces singletons directement plutôt que de les recevoir en paramètre. Ne pas introduire Hilt/Koin sans en discuter — ce serait un refactor transverse.

### Couches

- `data/repository/` — accès Supabase + état applicatif exposé en `StateFlow`. C'est ici que vit la vérité (niveau, XP, streak, tier d'abonnement).
- `data/billing/BillingManager.kt` — Play Billing 9, achat / restauration / vérification d'expiration.
- `service/` — audio (Media3 `MediaSessionService`), notifications, `BootReceiver`, état applicatif.
- `ui/<feature>/` — un ViewModel + ses Composables par feature.

### Vérification des achats

L'app ne valide **jamais** un achat localement. `BillingManager` appelle deux Edge Functions Supabase :

- `verify-android-purchase` (token + productId) → passage en premium
- `downgrade-android-purchase` → retour en gratuit

**Ces fonctions vivent dans le projet Supabase, pas dans ce repo.** Si un achat aboutit côté Play mais que le premium ne s'active pas, le bug est côté serveur — l'app le signale d'ailleurs via `activationFailed`.

## Règles métier à connaître

Elles sont dispersées et faciles à casser sans le savoir :

- **Audio gratuit coupé à 15 s** (`ArticleDetailViewModel.freeLimit`). On appelle `stop()` et non `pause()`, volontairement : ça passe le player en `STATE_IDLE`, ce qui fait retirer la notification lock screen par `MediaSessionService`.
- **XP attribué à 95 % de lecture/écoute**, pas à 100 %.
- **Compagnons** : les niveaux 1 à 5 sont accessibles à tous, au-delà c'est premium (`UserRepository.isCompanionUnlocked`).
- **Membres OG** (`isOGMember`) : premium à vie, hors Play Billing. Ne jamais les rétrograder — `checkSubscriptionValidity()` les exclut explicitement du downgrade.

## Tests

Il n'y a **aucun test réel** dans ce projet : `ExampleUnitTest` et `ExampleInstrumentedTest` sont les gabarits générés par Android Studio. La validation se fait aujourd'hui en manuel sur appareil.

Toute nouvelle logique métier doit être testée (TDD, cf. règles globales). Ne pas invoquer l'absence de tests existants pour s'en dispenser.

## Pièges connus (Things That Will Bite You)

- **Le repo n'est pas la source de vérité des versions.** Des `versionCode` ont été bumpés depuis Android Studio sans être commités (git était à 9 quand la prod était à 11). Avant toute release : `git fetch && git log HEAD..origin/main`, puis lire le `versionCode` max dans Play Console → Versions → Explorateur d'App Bundles. Play refuse tout code déjà déposé, **y compris sur un canal archivé ou supprimé**.

- **`main` local peut être en retard sur la prod.** Créer une branche sans `git fetch` préalable a déjà produit un bundle amputé d'un correctif de conformité Play Store. Toujours brancher depuis `origin/main`, jamais depuis le `main` local supposé à jour.

- **Prouver le contenu de l'`.aab`, pas celui des sources.** Le versionCode réel se lit avec `unzip -p app-release.aab base/manifest/AndroidManifest.xml`. Un build réussi ne prouve pas qu'on a construit ce qu'on croit.

- **`| tail` masque l'échec de Gradle.** `./gradlew ... | tail -15` renvoie le code de sortie de `tail` (0), et coupe les lignes `e:`. Filtrer sur `grep -E "^e: |FAILED|BUILD"`, et vérifier que l'artefact existe vraiment avant de conclure.

- **Le billing ne se teste pas en USB.** Un build installé par `adb` ne peut pas acheter. Il faut passer par le canal Test interne du Play Store, et s'ajouter dans Play Console → Paramètres → **Tests de licence** pour ne pas être réellement débité.

- **Play Billing : la reconnexion automatique est un opt-in.** `enableAutoServiceReconnection()` doit rester sur le `BillingClient.Builder`. Sans lui, `onBillingServiceDisconnected` ne fait rien et le client reste mort jusqu'au redémarrage de l'app.

- **`ProductDetailsResult.productDetailsList` reste nullable** en Billing 9.1.0, malgré ce que laisse croire la signature Java décompilée. Garder le `?.`.

## Environnement

- Kotlin 2.2.10, AGP 9.1.0, Compose (BOM 2024.09.00), `minSdk 26` / `targetSdk 36`
- Jetpack Compose uniquement, zéro XML de layout
- Thème forcé en clair : `UpNewsTheme(darkTheme = false)` dans `MainActivity`
- Commits en anglais, Conventional Commits, jamais de push direct sur `main` (cf. règles globales)
