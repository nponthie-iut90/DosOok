#!/bin/bash

# Vérifie si un argument est passé
if [ $# -eq 0 ]; then
  echo "Usage: $0 <fichier.txt>"
  exit 1
fi

# Chemin vers le fichier à lire
input_file="$1"

# Vérifie si le fichier existe
if [ ! -f "$input_file" ]; then
  echo "Le fichier $input_file n'existe pas."
  exit 1
fi

# Lance DosRead avec le fichier en argument
java -cp ./out DosSend < "$input_file"
