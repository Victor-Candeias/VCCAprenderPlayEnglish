# PE-012 — Catálogo offline de vocabulário e perguntas

## Modelo (PT-PT → EN-GB)

- `WordEntry(id, ptPT, enGB, category, ageRange, imageRef, audioText)`
  representa uma palavra ou expressão. Os IDs são ASCII, minúsculos e
  estáveis (ex.: `animals.cat`) — nunca são obtidos de traduções.
- `AgeRange(min,max)` aceita crianças dos **3 aos 8 anos**.
- `WordCategory` utiliza as categorias `animals`, `colors`, `numbers`,
  `food`, `objects` e `phrases`.
- `QuizQuestionSpec(id, prompt, optionIds, correctId)` é a representação
  **persistida no JSON**. Cada pergunta tem exatamente **três IDs diferentes**
  e um identificador de resposta correta presente nas opções.
- `QuizQuestion` (PE-011) continua a ser a representação **resolvida para a
  interface** com três `QuizChoice(id,label)`. Expõe também `optionIds` e
  `correctId` por compatibilidade. `AssetsQuizContentRepository` transforma
  `QuizQuestionSpec` em `QuizQuestion` usando `WordEntry.enGB` como texto
  de cada opção.

## Esquema do asset

O ficheiro `app/src/main/assets/content/quiz-v1.json` é parte do APK.
Exemplo reduzido:

```json
{
  "schemaVersion": 1,
  "locales": {"source": "pt-PT", "target": "en-GB"},
  "words": [
    {
      "id": "animals.cat",
      "ptPT": "Gato",
      "enGB": "Cat",
      "category": "animals",
      "ageRange": {"min": 3, "max": 8},
      "imageRef": null,
      "audioText": "Cat"
    }
  ],
  "questions": [
    {
      "id": "quiz.animals.cat",
      "prompt": "Como se diz «Gato» em inglês?",
      "optionIds": ["animals.dog", "animals.cat", "animals.bird"],
      "correctId": "animals.cat"
    }
  ]
}
```

O catálogo inicial contém **18 palavras/expressões** e **6 perguntas**
reais sobre animais, cores, números, comida, objetos e saudações. A expansão
editorial e imagens pertence à **PE-013**. Os valores `imageRef` ficam
`null` até existirem ficheiros de imagens locais. `audioText` é texto
para futura pronúncia TTS, não um ficheiro de som.

## Verificações e segurança

`OfflineVocabularyCatalog` verifica:
- IDs únicos de palavras e perguntas, com até 80 caracteres e sem espaços;
- traduções PT/EN e texto de áudio não vazios;
- categorias válidas, idade entre 3–8 anos e intervalo de idade comum entre
  as opções de cada pergunta;
- exatamente três opções distintas, todas referentes a palavras existentes,
  e uma única resposta correta presente nas opções;
- máximo de 10 000 palavras e 10 000 perguntas;
- imagens limitadas a caminhos dentro do APK, sem URLs nem `../`; se uma
  imagem for referenciada, o repository exige que exista no bundle.

O parser exige `schemaVersion=1`, os locales `pt-PT` e `en-GB` e tipos
corretos nos campos JSON. O carregamento falha de forma segura no ViewModel se
o asset estiver inválido; não tenta efetuar pedidos HTTP nem consulta o host.

**A existência de perguntas válidas no catálogo não faz com que a UI de
pré-visualização envie pontuações.** O motor de Quiz interativo e a ligação
com os eventos reais do plugin permanecem em PE-015/PE-020.

## Testes

Os testes Python (stdlib) verificam o asset empacotado e adulterações do
catálogo; os testes JVM verificam as invariantes dos modelos sem Android;
os testes instrumentados verificam a leitura por `AssetManager` e a
resolução dos rótulos:

```bash
python3 scripts/validate_offline_catalog.py
python3 -m unittest discover -s scripts -p "test_offline_catalog.py" -v
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

O GitHub Actions executa os testes Python/JVM e compila os instrumentados,
mas não executa Android instrumentado sem emulador.
