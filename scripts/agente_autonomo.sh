#!/bin/bash

SPEC=$1
MAX_INTENTOS=3
INTENTO=1

echo "🤖 Agente iniciando: Generando código para $SPEC..."

# Cambiamos --files por -p (headless) y añadimos -y (YOLO mode) para que modifique archivos solo
gemini -y -p "Actúa como un desarrollador de software. Abre y lee la especificación del archivo '$SPEC'. Tu tarea es implementar de forma autónoma los cambios descritos en ese documento, creando las carpetas y los archivos necesarios (.kt, .toml, .kts) directamente en el proyecto. Aplica los cambios directamente en el sistema de archivos."

while [ $INTENTO -le $MAX_INTENTOS ]; do
    echo "⚙️ Intento $INTENTO: Compilando el proyecto para buscar errores..."

    # Cambiamos temporalmente al directorio de KMP para poder usar gradlew
    cd FreeGluKMP
    ./gradlew :shared:build > ../build_output.log 2>&1
    GRADLE_STATUS=$?
    cd .. # Volvemos a la raíz para mantener las rutas del agente

    if [ $GRADLE_STATUS -eq 0 ]; then
        echo "✅ ¡Compilación exitosa! Código perfecto."
        break
    else
        echo "❌ Error detectado. El agente está analizando y corrigiendo el fallo..."

        # Pasamos el log de error por entrada estándar (stdin), usando -p y -y para auto-corregir
        cat build_output.log | gemini -y -p "La compilación de Kotlin Multiplatform ha fallado con este error. Analiza la entrada de datos, localiza en qué archivo de código te has equivocado y genera el código corregido aplicándolo directamente sobre el archivo correspondiente del sistema de archivos."

        INTENTO=$((INTENTO+1))
    fi
done

if [ $INTENTO -gt $MAX_INTENTOS ]; then
    echo "⚠️ El agente no pudo resolver el error tras $MAX_INTENTOS intentos. Requiere ayuda humana."
    exit 1
fi