# PE-011 — Arquitetura MVVM e responsabilidades

A aplicação `VCCAprenderPlayEnglish` usa camadas independentes, com um
ponto de entrada para o host VCCAprender e outro para o launcher Debug.

## Diagrama de dependências

```text
MainActivity / PlayEnglishPluginActivity
    |
    +--> ui (Compose, Navigation, QuizViewModel, QuizUiState)
    |         |
    |         +--> domain/usecase --> domain/model
    |                    |
    |                    +--> domain/repository (port)
    |                               ^
    |                               |
    |                          data/AssetsQuizContentRepository
    |                               |
    |                         assets/content/quiz-v1.json
    |
    +--> plugin (verificação de caller/certificado, SDK v1,
                 GameRequest, SavedState, RESULT_OK/CANCELED)
                 |
                 +--> domain/game (QuizProgress, Score)
    |
    +--> audio/PronunciationOutput (interface; Android TTS futura)
```

## Responsabilidades

| Package | Objetivo | Dependências permitidas |
|---|---|---|
| `domain/model` | Entidades imutáveis `QuizChoice`, `QuizQuestion`, `QuizState`, `QuizScore` e `QuizPhase` | Kotlin standard library |
| `domain/repository` | `QuizContentRepository` (interface síncrona local) | `domain/model` |
| `domain/usecase` | `LoadQuizUseCase`, `AnswerQuestionUseCase`, `CompleteQuizUseCase`, `CancelQuizUseCase` | `domain/*` |
| `domain/game` | `QuizProgress` e snapshot de respostas da PE-006/007 | Kotlin standard library |
| `data` | `AssetsQuizContentRepository`, leitura e validação de JSON empacotado | Android Assets + `domain`; **sem rede** |
| `ui/quiz` | `QuizViewModel`, `QuizUiState`, fábrica de dependências | AndroidX ViewModel, Kotlin Flow, `domain`, `data` na fábrica |
| `ui/screens` | Ecrãs Compose sem referências a `GameProtocol`, `GameRequest`, certificados ou IDs de perfil | `ui/quiz` e componentes |
| `audio` | `PronunciationOutput` como contrato para futura voz `en-GB` | Kotlin standard library |
| `plugin` | Ponto de integração com o host, segurança de assinatura, sessão externa, serialização pelo SDK | `domain/game`, Android e SDK oficial |

O SDK v1 **não** pode entrar em `domain` nem em `ui/screens`. A
`QuizViewModel` delega toda a lógica do quiz nos use cases puros e expõe
`StateFlow<QuizUiState>` somente de leitura. Cada ação de resposta cria um
**novo** `QuizState`, sem alterar o anterior.

### Decisões de arquitetura

- **Sem Hilt / Koin no MVP:** `QuizViewModelFactory` é o composition root e
  injeta o repositório Android e os use cases. Facilita testes com fakes.
- **Sem acesso à Internet:** `AssetsQuizContentRepository` lê exclusivamente
  o ficheiro `assets/content/quiz-v1.json`; a interface do repositório
  permite substituir o armazenamento sem alterar ViewModels/use cases.
- **Estados seguros:** perguntas com três opções distintas; respostas
  duplicadas ou fora da sessão são ignoradas; pontuação apenas depois de
  completar todas as perguntas; uma sessão cancelada não pode pontuar.
- **Sem conteúdo artificial:** a versão inicial do asset tem uma lista vazia.
  Nesse caso, o estado é `NOT_STARTED` e os ecrãs continuam no modo
  pré-visualização. A PE-012 irá adicionar o catálogo completo e o modelo de
  vocabulário/idade/categoria.
- **Compatibilidade com PE-006/007:** `QuizProgress` mudou para
  `domain/game`, com `typealias` temporários em `plugin`, preservando
  consumidores, testes e o protocolo de resultados/cancelamento.
- **Ligação futura ao jogo:** a PE-015/020 usará eventos reais de perguntas
  e respostas da UI para atualizar o ViewModel e o `PluginSessionStateHolder`.
  Esta PE-011 não chama `onQuizCompleted` artificialmente e não altera a
  lógica de segurança de `PlayEnglishPluginActivity`.

## Testar

```bash
./gradlew :game-plugin-sdk:testDebugUnitTest :app:testDebugUnitTest \
    :app:compileDebugAndroidTestKotlin :app:assembleDebug
```

`QuizDomainUseCasesTest` e `QuizViewModelTest` são testes JVM (sem
emulador). `OfflineCatalogInstrumentedTest` exige dispositivo ou emulador
e apenas é compilado no GitHub Actions. Os testes PE-006/007 existentes
continuam no pipeline.
