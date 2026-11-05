# PE-014 — Seleção local de categorias e níveis etários

**Issue:** [#15](https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/15)  
**Prioridade:** P0 · **MVP** · **Dependências:** PE-012 e PE-013

## Ecrãs e interação

O utilizador pode escolher em ambos os modos:
- **Nível 3–5 anos** (inicial por omissão) ou **6–8 anos**;
- Uma das seis categorias existentes: animais, cores, números, alimentos,
  objetos e expressões. Todas as seis categorias surgem sempre, com
  **quantidade de palavras e perguntas após filtragem**.
- A mesma seleção local é partilhada entre a descoberta e a preparação do Quiz.
  Clicar na mesma categoria volta a desmarcá-la; escolher outra categoria
  ou outro nível atualiza imediatamente os conteúdos.
- Na descoberta, as palavras visíveis são apenas as da categoria e nível
  escolhidos; se nenhuma existir, apresenta-se um estado vazio explícito.
- No Quiz, quando não há perguntas, aparece um estado vazio e o botão de
  pré-visualização de resultados fica desativado. Sem categoria selecionada,
  também não se pode avançar.
- As áreas de toque têm no mínimo 56 dp, a página é deslocável para texto a
  200%, todos os controlos têm rótulos PT-PT e um estado acessível
  selecionado/não selecionado.

## Regra de faixa etária

`LearningLevel` representa **uma escolha pedagógica local**, não a idade
real da criança. O host só envia um identificador opaco do perfil e **não
fornece idade**, nem esta implementação a pede ou infere.

Um item só pode ser mostrado num nível se o seu `AgeRange` incluir
**toda a faixa** escolhida:

```kotlin
range.min <= level.minAge && range.max >= level.maxAge
```

Esta regra conservadora evita mostrar a uma criança com 3 anos conteúdos que
apenas são adequados a partir dos 4 ou 5. **Em concreto**, as palavras de
`objects` (4–8) e `phrases` (5–8) não entram em **3–5**; ambas entram em
**6–8**. Uma pergunta só entra se a resposta correta e todas as opções
forem elegíveis para a faixa escolhida **e** pertencerem à mesma categoria.

### Contagens reais no catálogo PE-013

| Categoria | 3–5 palavras | 3–5 perguntas | 6–8 palavras | 6–8 perguntas |
|---|---:|---:|---:|---:|
| Animais | 12 | 8 | 12 | 8 |
| Cores | 12 | 8 | 12 | 8 |
| Números | 12 | 8 | 12 | 8 |
| Alimentos | 12 | 8 | 12 | 8 |
| Objetos | 0 | 0 | 12 | 8 |
| Expressões | 0 | 0 | 12 | 8 |
| **Total** | **48** | **32** | **72** | **48** |

O esquema e os IDs do asset `content/quiz-v1.json` permanecem inalterados.

## Arquitetura e segurança

- `SelectLearningContentUseCase` é Kotlin puro e opera sobre a porta
  `VocabularyContentRepository`. Devolve categorias, palavras elegíveis
  e IDs de perguntas filtrados, **sem expor `correctId` à seleção UI**.
- `LearningSelectionViewModel` mantém a escolha em `StateFlow`; erros
  no asset colocam o estado em modo seguro, sem perguntas nem resultados.
- `PlayEnglishNavHost` partilha essa escolha com os ecrãs Descobre/Quiz e
  chama `QuizViewModel.selectQuestionSet(ids)` para limitar o **estado real**
  do Quiz às perguntas elegíveis. Mudar a seleção limpa respostas/score
  anteriores, evitando que respostas de outra categoria contaminem o jogo.
- O estado não contém ID de perfil, idade real, dados parentais, autenticação
  nem outros dados privados. Nenhuma requisição à rede.
- A `PlayEnglishPluginActivity` conserva validação de assinatura,
  verificação do host, cancelamento e protocolo existentes sem alterações.
- **PE-015** implementará os cartões de perguntas com opções A/B/C.
  **PE-020** ligará as respostas reais à submissão da sessão; este ecrã
  permanece em modo de pré-visualização e não fabrica pontuações.

## Testes

```bash
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

**JVM:**
- `LearningSelectionUseCaseTest`: elegibilidade 3–5/6–8,
  exclusão por distrator incompatível, contagens, seleção local, erros.
- `QuizViewModelTest`: question IDs realmente filtrados, reset de
  respostas, falha segura em IDs desconhecidos/repetidos.

**Android (instrumentados):**
- `LearningSelectionInstrumentedTest`: verifica contagens do catálogo
  empacotado, com os dois níveis e categorias vazias.
- `PlayEnglishNavigationTest`: filtros em ambos os modos, bloqueio
  sem categoria/sem perguntas, mudança de nível, regressos ao início.

O pipeline CI executa testes JVM e compila os instrumentados, mas só um
emulador/dispositivo os executa. A **Release automática assinada** continua
a ser acionada apenas após alterações à `main`, não por este PR.
