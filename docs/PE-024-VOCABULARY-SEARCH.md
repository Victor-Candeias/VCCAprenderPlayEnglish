# PE-024 — Biblioteca por categoria, pesquisa e opção aleatória

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/25

## Implementação
- Filtro por nível etário e categoria continua fornecido por `LearningSelectionViewModel` (PE-014/023). A pesquisa opera somente sobre a lista já filtrada.
- `SearchVocabularyUseCase.execute(words, query)` procura nos campos `ptPT` e `enGB`, removendo diacríticos Unicode (`Normalizer.Form.NFD`) e ignorando caixa, espaços periféricos e espaços múltiplos.
- A pesquisa não altera a lista do catálogo nem dados persistentes; sem correspondências apresenta mensagem PT-PT e não permite reproduzir áudio.
- Os botões `Anterior` e `Seguinte` percorrem exclusivamente os resultados da pesquisa sem wrap-around.
- `Palavra aleatória` escolhe uma palavra diferente quando há mais de uma, sem modificar a lista.
- `Ouvir` e `Repetir` passam somente `audioText` inglês ao motor en-GB. `SpeechTurnGate` mantém exclusão enquanto a leitura decorre.
- Pesquisa e índice mantidos durante rotação, reiniciados ao selecionar outro grupo de palavras.
- Não há score nem callbacks da sessão VCCAprender no modo de pronúncia.

## Testes
`SearchVocabularyUseCaseTest`: acentos PT, case-insensitive EN, lista vazia, catálogo imutável e escolha aleatória sem repetição imediata.
Validação CI: `./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug`.
Os testes Android instrumentados e testes com voz real continuam a necessitar de emulador/dispositivo.
