<p align="center">
  <img src="https://img.shields.io/github/languages/top/Ollopic/covoitme" alt="Language" />
  <img src="https://img.shields.io/github/stars/Ollopic/covoitme" alt="Stars" />
  <img src="https://img.shields.io/github/contributors/Ollopic/covoitme" alt="Contributors" />
</p>

# Covoitme

L'application de covoiturage entre particuliers

## Auteurs

Maréchal Antoine
Lemont Gaétan

[![Contributors](https://contrib.rocks/image?repo=Ollopic/covoitme)](https://github.com/Ollopic/covoitme/graphs/contributors)

## Technologies utilisées

- **Backend** : Java EE, Servlets, JSP
- **Frontend** : HTML5, CSS3, JavaScript
- **Base de données** : PostgreSQL
- **Serveur d'application** : Apache Tomcat
- **Conteneurisation** : Docker
- **Tests E2E** : Playwright, JUnit 5

## Prérequis

- [Docker](https://docs.docker.com/get-docker/): Téléchargez et installez Docker en suivant les instructions de votre OS. Pour vérifier que Docker a été installé avec succès, exécutez `docker --version`.

## Lancer en production

1. Utilisez le fichier [compose de Docker](./compose.yml) pour lancer l'application :
   ```bash
   docker compose up -d
   ```

> **Note** : Le docker-compose utilise une image précompilée disponible sur Docker Hub. Cette approche évite d'avoir à compiler l'application en local, ce qui permet un déploiement plus rapide et simplifié.

L'application sera accessible en local via l'adresse [http://localhost:8080](http://localhost:8080)

## Build Docker Image

Pour construire l'image Docker à la fois pour les architectures `linux/amd64` et `linux/arm64`, utilisez dans un premier temps la commande suivante afin de créer un builder multi-architecture :

```bash
docker buildx create --name multi-arch --platform "linux/arm64,linux/amd64,linux/arm/v7" --driver "docker-container"
```

Ensuite, il suffit de construire l'image Docker en utilisant le builder créé précédemment. Assurez-vous d'être dans le répertoire contenant le fichier `Dockerfile` :

```bash
docker buildx build --platform linux/amd64,linux/arm64 -f Dockerfile . -t ollopic/covoitme:latest --push
```

> **Note** : La commande permet de push directement l'image créé sur le Docker Hub. Si vous souhaitez uniquement construire l'image sans la pousser, vous pouvez omettre l'option `--push`.

## Lancer localement

Afin de lancer le projet pour le développement local, nous avons un docker-compose contenant une base de données PostgreSQL, et nous avons notre serveur web Tomcat d'installé sur notre machine. Pour lancer l'application, il faut dans un premier temps lancer la base de données :

```bash
docker compose -f compose-dev.yml up -d
```

Puis ensuite à chaque modification nous recompilons l'app et nous la déployons sur le serveur Tomcat, cela à l'aide d'un script que nous avons en local et contenant la commande suivante :

```bash
mvn clean package && sudo rm -rf /opt/tomcat/webapps/ROOT/ && sudo cp -r target/Covoitme-1.0-SNAPSHOT/* /var/lib/tomcat10/webapps/ROOT/
```

## Lancer les tests

L'application utilise Playwright pour les tests End-to-End (E2E).

### Prérequis

1. **Installer les navigateurs Playwright** (première fois uniquement) :

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install-deps"
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

2. **Démarrer l'application avec Docker** :

```bash
docker compose -f compose.yml up -d
```

3. **Initialiser la base de données** (première fois uniquement) :

```bash
docker exec -i covoitme-db psql -U covoitme -d covoitme < src/main/webapp/WEB-INF/sql/init.sql
```

### Exécuter les tests

**Tous les tests :**
```bash
mvn test
```

**Un test spécifique :**
```bash
mvn test -Dtest=AuthenticationTest#shouldLoginSuccessfully
```

**Avec logs détaillés :**
```bash
mvn clean test -X -e
```