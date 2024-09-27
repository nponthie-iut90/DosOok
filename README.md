# DosOok

DosOok est une application permettant d'échanger des messages via du son.  
DosSend convertit le contenu d'un fichier .txt en son dans un fichier .wav.  
Dos Read lit un fichier .wav créer par DosSend puis en déchiffre le message.

## Architecture

- le répertoire **src** contient les sources
- le répertoire **out** contient les sources compilées
- le répertoire **data** contient des fichiers d'exemple pour test

## Utilisation

*** Attention ! Lanceurs compatibles uniquement avec Linux ! Java doit être installé sur votre pc.***
Pour utiliser l'application, vous devez utiliser les lanceurs .sh avec un fichier comme argument.  
Par exemple:
- DosSend : `/bin/bash DosSend.sh data/message.txt` *avec le message à convertir à l'intérieur du fichier
- DosRead : `/bin/bash DosRead.sh data/DosOok_message.wav`

## Auteurs

Développement:
- Nathan PONTHIEU
- Romain ARNOUX

## Statut du projet

Le projet est terminé, il a lieu de fin novembre 2023 à début janvier 2024.
