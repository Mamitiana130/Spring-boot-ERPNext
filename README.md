# 🌟 ERP Integration App - Spring Boot & ERPNext

Une application web développée avec **Spring Boot** et **Thymeleaf**, intégrée aux API REST de **Frappe/ERPNext**, pour interagir avec un système ERP et gérer des opérations RH (HRMS).

## 🚀 Fonctionnalités principales

- 🔐 Authentification via les services ERPNext
- 🔄 Communication REST sécurisée avec l’ERP
- 🧑‍💼 Modules de gestion RH (employés, salaires, structures, etc.)
- 📤 Import / export CSV de données
- 🌐 Interface web dynamique avec **Thymeleaf**

## 🧩 Modules RH Implémentés

| Fonctionnalité              | Description |
|----------------------------|-------------|
| 👨‍💼 **Gestion des employés** | Ajout, consultation, modification et suppression des données des employés. |
| 💰 **Structure salariale** | Création et gestion des structures de salaire (éléments fixes et variables). |
| 📊 **Calcul de salaires**  | Calcul automatisé des salaires à partir des structures définies. |
| 📥 **Import CSV**          | Importation en masse des employés, salaires, ou éléments de salaire depuis des fichiers CSV. |
| 📤 **Export CSV**          | Export des données RH (employés, salaires, structures) en format CSV. |
| 🔐 **Authentification**    | Connexion sécurisée, gestion de session utilisateur via API ERPNext. |
| 🔄 **Réinitialisation**    | Réinitialisation des salaires mensuels ou des bases de calcul. |

## 🛠️ Tech Stack

| Backend | Frontend | Intégration API | IDE | Langage |
|--------|----------|-----------------|-----|---------|
| ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) | ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white) | ![ERPNext](https://img.shields.io/badge/ERPNext_API-1C3D5A?style=for-the-badge&logo=frappe&logoColor=white) | ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000?style=for-the-badge&logo=intellij-idea&logoColor=white) | ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) |

## 🖼️ Aperçus

Voici quelques aperçus de l'application :

### Liste Employés + Recherche multi-critères :
<img width="1919" height="942" alt="Capture d'écran 2025-07-08 140320" src="https://github.com/user-attachments/assets/904c0086-b6f7-4148-9923-0fc04948f44b" />

### Details Employés + Export salaire :
<img width="1919" height="941" alt="Capture d'écran 2025-07-08 140350" src="https://github.com/user-attachments/assets/827bd80d-783a-400c-be7e-34cbae47ef95" />

### Formulaire de creation de bulletin de salaire :
<img width="1919" height="947" alt="Capture d'écran 2025-07-08 140302" src="https://github.com/user-attachments/assets/624f85b4-c3e4-4062-b85e-4f60abe8d158" />

### Visualisation des salaires avec filtre année :
<img width="1919" height="946" alt="Capture d'écran 2025-07-08 140228" src="https://github.com/user-attachments/assets/e325742b-9a76-4222-947b-1303ce5496ed" />

### Graphique des salaires avec filtre année :
<img width="1919" height="945" alt="Capture d'écran 2025-07-08 140246" src="https://github.com/user-attachments/assets/2693b6c8-be94-41ce-b139-2936c12e3a15" />


## ⚙️ Prérequis

- ERPNExt + HRMS (installer)
- Java 17+
- Maven
- IntelliJ IDEA (ou autre IDE Java)
- Instance ERPNext avec API REST activée

## 📦 Installation

1. **Cloner le dépôt**
```bash
git clone https://github.com/Mamitiana130/Spring-boot-ERPNext.git
cd Spring-boot-ERPNext
