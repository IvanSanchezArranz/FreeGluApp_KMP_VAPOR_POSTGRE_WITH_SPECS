import pandas as pd
from sqlalchemy import create_engine

DATABASE_URL = "postgresql://admin:admin@localhost:5432/glutenfree"

engine = create_engine(DATABASE_URL)

USEFUL_COLUMNS = [
    "code",
    "product_name",
    "brands",
    "categories_en",
    "ingredients_text",
    "image_url",
    "countries",
    "labels_tags"
]

chunks = pd.read_csv(
    "../data/foods.csv",
    sep="\t",
    chunksize=5000,
    encoding="utf-8",
    low_memory=False,
    on_bad_lines="skip",
    usecols=USEFUL_COLUMNS
)

for i, chunk in enumerate(chunks):

    print(f"Procesando chunk {i}")

    chunk = chunk.rename(columns={
        "product_name": "name",
        "brands": "brand",
        "categories_en": "categories",
        "ingredients_text": "ingredients",
    })

    chunk["gluten_free"] = chunk["labels_tags"].fillna("").str.contains(
        "gluten-free",
        case=False
    )

    chunk = chunk[chunk["gluten_free"] == True]

    chunk = chunk.dropna(subset=["name"])

    final_chunk = chunk[
        [
            "code",
            "name",
            "brand",
            "categories",
            "ingredients",
            "image_url",
            "countries",
            "gluten_free"
        ]
    ]

    print(final_chunk.head())

    final_chunk.to_sql(
        "foods",
        engine,
        if_exists="append",
        index=False
    )

    print(f"Chunk {i} importado")