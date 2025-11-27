# Tests de charge avec Locust

Tests de montée en charge pour l'application Covoitme, intégrés dans la pipeline Jenkins.

## Utilisation

### Lancer les tests localement

```bash
cd load-tests
make test
```

### Personnaliser les paramètres

```bash
make test USERS=50 SPAWN_RATE=10 RUN_TIME=2m HOST=http://localhost:8080
```

## Intégration Jenkins

Les tests sont automatiquement exécutés dans la pipeline après le déploiement en preprod et avant le déploiement en production.

Configuration dans `Jenkinsfile` :
- 20 utilisateurs simultanés
- Montée en charge : 5 users/seconde
- Durée : 1 minute
- Rapport HTML archivé comme artifact

## Fichiers

- **`locustfile.py`** - Scénarios de test
- **`requirements.txt`** - Dépendances Python (Locust)
- **`Makefile`** - Commandes pour lancer les tests

## Rapports

Après chaque exécution, un rapport HTML est généré : `report.html`
