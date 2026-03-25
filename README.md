# 🏜️ DesertAkal

**DesertAkal** est une application full-stack moderne utilisant une architecture monolithique distribuée (séparation stricte entre le Frontend et le Backend). Le projet encapsule tous ses services dans un environnement de développement et de production conteneurisé.

## 🏗️ Architecture et Technologies

L'écosystème du projet repose sur les technologies suivantes :

- **Frontend** : [Angular](https://angular.io/) (TypeScript) servi par **Nginx**.
- **Backend** : [Spring Boot / Java](https://spring.io/projects/spring-boot) (Maven) exposant une API REST.
- **Base de Données** : **PostgreSQL**.
- **Stockage de Fichiers** : **MinIO** (Alternative S3 Open Source) pour héberger les médias et pièces jointes.
- **Infrastructure** : **Docker** & **Docker Compose** pour orchestrer l'ensemble des services.

## 📂 Structure du Répertoire

```text
DesertAkal/
├── backend/          # API REST Spring Boot & configuration Maven
├── frontend/         # Application Angular SPA & configuration Nginx
├── minio_data/       # Point de montage du volume de stockage MinIO
├── uml/              # Ressources de conception (Diagrammes de classes, Use cases)
├── compose.yaml      # Configuration d'orchestration Docker
└── .env              # Variables d'environnement de l'infrastructure
```

## 🚀 Démarrage Rapide (Getting Started)

### Prérequis
Assurez-vous d'avoir installé sur votre machine :
- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- *(Optionnel pour le dev local)*: Node.js, Angular CLI, JDK 17+ et Maven.

### Variables d'Environnement
Avant de lancer le projet, assurez-vous de créer ou paramétrer un fichier `.env` à la racine (en vous basant sur `.env.exemple`) contenant les identifiants pour PostgreSQL et MinIO.

### Lancement avec Docker Compose

Pour construire les images et lancer tous les services (Base de données, Stockage, Backend, et Frontend), exécutez la commande suivante à la racine :

```bash
docker-compose up -d --build
```
*(Le flag `-d` lance l'environnement en arrière-plan, `--build` force la recompilation des images).*

Pour arrêter les services proprement :
```bash
docker-compose down
```

## 🌐 Ports et Points d'Accès

Une fois les conteneurs démarrés, voici les points d'accès aux différents services :

| Service | Point d'accès (Port host) | Description |
| :--- | :--- | :--- |
| **Frontend Angular** | `http://localhost:80` (et `443` HTTPS) | Interface Utilisateur (via proxy Nginx). |
| **Backend API** | `http://localhost:8080` | L'API Spring Boot principale. `5005` est ouvert pour le débogage distant. |
| **Base PostgreSQL** | `localhost:5432` | Accessible via PGAdmin, DBeaver ou Datagrip. |
| **API MinIO** | `http://localhost:9000` | Port technique S3. |
| **Console UI MinIO** | `http://localhost:9001` | Interface graphique de gestion des buckets et des fichiers. |

## 📦 Environnement de Développement Local

Si vous souhaitez développer sans dépendre des images Docker packagées de l'application :
1. Lancez uniquement les bases de données et dépendances via Docker : `docker-compose up -d desertakal-postgres desertakal-minio`
2. Lancez le **Backend** via Maven : `cd backend && ./mvnw spring-boot:run`
3. Lancez le **Frontend** via Angular CLI : `cd frontend && npm install && ng serve`

---
*Généré pour le Projet Fil Rouge DeserAkal.*
