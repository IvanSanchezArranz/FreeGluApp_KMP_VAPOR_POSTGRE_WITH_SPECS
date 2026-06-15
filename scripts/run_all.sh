#!/bin/bash

# 1. DETECCIÓN AUTOMÁTICA DE RUTAS: No importa desde dónde ejecutes el script,
# esto calculará la raíz del proyecto (un nivel por encima de la carpeta scripts/)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"
cd "$PROJECT_ROOT"

# Asegurarnos de que el agente autónomo tiene permisos de ejecución
chmod +x scripts/agente_autonomo.sh

echo "🚀 INICIANDO CONSTRUCCIÓN COMPLETA DEL PROYECTO (11 SPECS)..."
echo "================================================================"

# Ahora que estamos en la RAÍZ, las rutas siempre empiezan por scripts/docs/...
SPECS=(
    "scripts/docs/specs/00_database_and_etl.spec.md"
    "scripts/docs/specs/01_backend_api_vapor.spec.md"
    "scripts/docs/specs/02_kmp_core_setup.spec.md"
    "scripts/docs/specs/03_kmp_data_domain.spec.md"
    "scripts/docs/specs/04_kmp_ui_compose.spec.md"
    "scripts/docs/specs/05_kmp_local_storage_favorites.spec.md"
    "scripts/docs/specs/06_kmp_search_and_filters.spec.md"
    "scripts/docs/specs/07_kmp_web_wasm_adaptation.spec.md"
    "scripts/docs/specs/08_kmp_design_system.spec.md"
    "scripts/docs/specs/09_testing_strategy.spec.md"
    "scripts/docs/specs/10_ci_cd_deployment.spec.md"
)

# Iteramos sobre cada documento de especificación
for spec in "${SPECS[@]}"; do
    echo ""
    echo "================================================================"
    echo "📂 PROCESANDO: $spec"
    echo "================================================================"

    # Validación de seguridad: Comprobar si el archivo físico existe
    if [ ! -f "$spec" ]; then
        echo "⚠️  ATENCIÓN: El archivo $spec no existe."
        echo "Por favor, crea el archivo con la plantilla antes de continuar."
        echo "Saltando al siguiente Spec..."
        continue
    fi

    # Lógica Condicional: Separar Backend de KMP
    if [[ "$spec" == *"00_database"* ]]; then
        echo "🗄️ Detectado Spec 00. Asegurando Base de Datos Local y ETL..."

        if command -v brew &> /dev/null; then
            echo "🐘 Asegurando que PostgreSQL de Homebrew esté activo..."
            brew services start postgresql
        fi

        if [ -f ".db_importada_con_exito" ]; then
            echo "⏭️ La base de datos ya fue poblada anteriormente. Saltando importación."
        else
            echo "🐍 Ejecutando importación del CSV..."
            # Entramos a scripts porque import_csv.py busca "../data/foods.csv"
            cd scripts
            python3 import_csv.py

            PYTHON_STATUS=$?
            cd .. # Volvemos a la raíz del proyecto

            if [ $PYTHON_STATUS -eq 0 ]; then
                touch .db_importada_con_exito
                echo "✅ Base de datos poblada. Candado creado."
            else
                echo "❌ Error: El script de Python falló al importar el CSV."
                exit 1
            fi
        fi

    elif [[ "$spec" == *"01_backend"* ]]; then
        echo "💧 Detectado Spec 01. Auditando Vapor..."

        # Usamos -p para enviar la orden y --approval-mode plan para asegurar que solo lee y reporta
        gemini --approval-mode plan -p "Actúa como un Auditor QA. Abre y lee la especificación del archivo '$spec'. Revisa que el backend Vapor en Swift cumpla con este Spec y devuélveme un reporte con tus observaciones en la consola sin modificar ningún archivo."

        echo "💡 Nota: Recuerda tener tu servidor de Vapor corriendo desde Xcode para que KMP pueda conectarse."

    else
        echo "⚙️  Detectado Spec KMP. Llamando al Agente Autónomo con autocorrección..."

        # Llamamos a tu agente autónomo apuntando a su carpeta
        ./scripts/agente_autonomo.sh "$spec"

        # Verificamos si el agente autónomo falló por completo y se rindió
        if [ $? -ne 0 ]; then
            echo "🛑 ERROR CRÍTICO: El agente no pudo completar $spec tras sus intentos máximos."
            echo "Deteniendo la ejecución en cadena para evitar romper la arquitectura."
            exit 1
        fi

        echo "✅ Spec $spec implementado y compilado con éxito."

        # Solo hacemos commit si Git detecta que hay archivos modificados
        if [[ `git status --porcelain` ]]; then
            git add .
            git commit -m "feat(auto): implementado $spec por Gemini Agent"
            echo "📦 Cambios guardados en Git."
        else
            echo "🤷‍♂️ No hubo cambios en el código para este Spec."
        fi
    fi
done

echo ""
echo "================================================================"
echo "🏁 ¡PROYECTO 100% TERMINADO, AUDITADO Y COMPILADO!"
echo "================================================================"