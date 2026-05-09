# 🚀 Stratix

**Stratix** est une plateforme intelligente qui centralise la gestion des ressources humaines, matérielles et des produits. Elle structure les données de l'entreprise pour offrir une meilleure visibilité et soutenir l'amélioration de la performance globale.

---

## 📌 Table des matières
- [Aperçu](#-aperçu)
- [Fonctionnalités](#-fonctionnalités)
- [Technologies utilisées](#-technologies-utilisées)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Structure du projet](#-structure-du-projet)
- [Contribuer](#-contribuer)

---

## 🔍 Aperçu

Stratix est une application de bureau **JavaFX** conçue pour rationaliser les opérations commerciales en fournissant une interface unifiée pour :

* **Ressources Humaines** : Gestion des employés et des responsables.  
* **Ressources Matérielles** : Suivi des équipements et gestion d'inventaire.  
* **Produits** : Gestion du cycle de vie des produits et contrôle des stocks.  

L'application exploite les technologies Java modernes et s'intègre avec diverses API pour fournir une synchronisation des données en temps réel, des rapports analytiques et des outils de communication.

---

## ✨ Fonctionnalités

- 📊 **Tableau de bord centralisé** : Aperçu en temps réel de toutes les ressources.  
- 👥 **Gestion RH** : Dossiers des employés, suivi des présences et traitement de la paie.  
- 📦 **Gestion d'inventaire** : Niveaux de stock, suivi du matériel et alertes automatisées.  
- 🛒 **Gestion des produits** : Catalogue complet, tarification et suivi du cycle de vie.  
- 📄 **Rapports** : Génération de rapports PDF (OpenPDF) et exports Excel (Apache POI).  
- ✉️ **Communication** : Notifications via Gmail/SendGrid et intégration WebSocket (Pusher).  
- 🤖 **Intelligence Artificielle** : Assistant intelligent intégré via l'API Groq.  
- ☁️ **Logistique** : Données météo en temps réel (OpenWeatherMap) pour la planification.  

---

## 🛠 Technologies utilisées

### Technologies principales
- **Java 17** — Langage de programmation principal  
- **JavaFX 17.0.10** — Framework GUI desktop  
- **Maven** — Gestion des dépendances et outil de build  
- **MySQL** — Base de données relationnelle  

### Bibliothèques clés
- **JSON** : GSON & Jackson  
- **Réseau** : OkHttp, Java-WebSocket, Pusher  
- **Documents** : OpenPDF, Apache POI  
- **Tests** : JUnit Jupiter  

---

## 📋 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Java JDK 17** ou supérieur  
- **Maven 3.6+**  
- **MySQL Server 8.0+**  
- **Git**  

---

## ⚙️ Installation

### 1️⃣ Cloner le dépôt

git clone https://github.com/islemalibii/stratix.git
cd stratix

### 2️⃣ Installer les dépendances

mvn clean install

### 3️⃣ Lancer l'application

mvn clean install

## 🔧 Configuration
 Créez un fichier .env ou configurez vos variables d'environnement comme suit :
 
```properties
DB_URL=jdbc:mysql://localhost:3306/stratix
GROQ_API_KEY=your_groq_api_key
OPENWEATHER_API_KEY=your_openweather_api_key
SENDGRID_API_KEY=your_sendgrid_api_key
```

## 📁 Structure du projet
```text
src/
└── main/
├── java/
│   ├── controllers/    # Contrôleurs JavaFX (Logique UI)
│   ├── models/         # Entités et POJOs
│   ├── services/       # Logique métier et accès DB
│   ├── utils/          # Classes utilitaires (Base de données, Helpers)
│   └── org/example/    # Point d'entrée de l'application
└── resources/          # Fichiers FXML, CSS, images
└── test/                   # Tests unitaires JUnit
```
## 🤝 Contribuer

Les contributions sont les bienvenues.

#### Fork le projet

#### Créer une branche : git checkout -b feature/ma-fonctionnalite

#### Commit vos modifications : git commit -m "Ajout d'une fonctionnalité"

#### Push : git push origin feature/ma-fonctionnalite

#### Ouvrir une Pull Request
