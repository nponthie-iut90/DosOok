# DosOok

DosOok est une application permettant d'échanger des messages via du son via **DosSend** et **DosRead**.  
DosSend convertit le contenu d'un fichier *.txt* en son dans un fichier *.wav*.  
DosRead lit un fichier *.wav* créer par DosSend puis en déchiffre le message.

## Sommaire

1. [Installation](#installation)
2. [Utilisation](#utilisation)
3. [Auteurs](#auteurs)
4. [Satut du projet](#statut-du-projet)

## Installation

1. Récupérer le projet
```bash
git clone https://github.com/nponthie-iut90/DosOok
cd DosOok
```

2. Lancer l'application
    - DosSend
      ```bash
      /bin/bash DosSend.sh <message.txt>
      # exemple /bin/bash DosSend.sh data/message.txt
      ```
    - DosRead
      ```bash
      /bin/bash DosRead.sh <message.wav>
      # exemple /bin/bash DosRead.sh data/DosOok_message.wav
      ```

## Utilisation

Créer un fichier .txt et insérez-y votre message puis exécuter DosSend comme mentionné ci-haut.
Déchiffrer votre message avec DosRead comme mentionné ci-haut.

## Auteurs

- [Nathan PONTHIEU](https://github.com/nponthie-iut90)
- [Romain ARNOUX](https://github.com/rarnoux4-iut90)

## Statut du projet

Le projet est terminé, il a lieu de novembre 2023 à janvier 2024.
