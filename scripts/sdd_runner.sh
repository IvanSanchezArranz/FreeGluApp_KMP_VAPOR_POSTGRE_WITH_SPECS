#!/bin/bash
SPEC=$1
TARGET=$2

echo "🤖 Aplicando $SPEC en $TARGET..."
gemini "Aplica el spec a este archivo. Devuelve solo código limpio." \
  --files $SPEC,$TARGET \
  > $TARGET

echo "🧪 Validando con Gradle..."
./gradlew build

if [ $? -eq 0 ]; then
    echo "✅ Éxito."
else
    echo "❌ Fallo en compilación. Inicia corrección manual o piping."
fi